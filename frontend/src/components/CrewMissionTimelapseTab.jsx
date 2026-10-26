import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { authFetch, apiUrl } from '../api/client';
import { getMissions } from '../api/missions';
import {
  calcDistanceKm,
  extractExifFromFile,
  loadPhotoFile,
  loadRemotePhoto,
  captureCurrentGeolocation,
  createDemoSpotGraphic
} from '../utils/photoExifParser';
import {
  resolveGatheringAnchorCoords,
  buildCrewTimelapseSpots,
  extractCrewMemberNames,
  filterCrewSpotsByMember
} from '../utils/gatheringCrewTimelapseBuilder';
import {
  renderTravelTimelapse,
  exportBoardingMapPng,
  saveToGallery
} from '../utils/boardingPassGifRenderer';
import './CrewMissionTimelapseTab.css';

const CREW_SAMPLE_DATA = [
  { member: '김하늘(호스트)', title: '집결지 체크인 완료!', emoji: '🛫', c1: '#FF5C00', c2: '#0F172A', type: 'post' },
  { member: '박바다', title: '미션 #1 로컬 맛집 인증', emoji: '🍲', c1: '#0EA5E9', c2: '#0F172A', type: 'mission' },
  { member: '이산들', title: '해안도로 단체 스냅샷', emoji: '📸', c1: '#10B981', c2: '#0F172A', type: 'post' },
  { member: '최별빛', title: '미션 #2 일몰 전망대 도착', emoji: '🌅', c1: '#F43F5E', c2: '#0F172A', type: 'mission' }
];

const CrewMissionTimelapseTab = ({ gathering, currentUser, onGoToGallery }) => {
  const [rawSpots, setRawSpots] = useState([]);
  const [selectedMember, setSelectedMember] = useState('ALL');
  const [includePosts, setIncludePosts] = useState(true);
  const [includeMissions, setIncludeMissions] = useState(true);
  const [gifFormat, setGifFormat] = useState('square');
  const [gifQuality, setGifQuality] = useState('normal');
  const [loadingData, setLoadingData] = useState(false);
  const [rendering, setRendering] = useState(false);
  const [progressPct, setProgressPct] = useState(0);
  const [generatedMedia, setGeneratedMedia] = useState(null);
  const [postingToFeed, setPostingToFeed] = useState(false);
  const [toast, setToast] = useState('');

  const notify = (msg) => {
    setToast(msg);
    setTimeout(() => {
      setToast((prev) => (prev === msg ? '' : prev));
    }, 4000);
  };

  // 모임 갤러리 포스트 + 미션 인증 내역 로드 및 캔버스 이미지 프리로드
  const loadCrewData = useCallback(async () => {
    if (!gathering?.id) return;
    setLoadingData(true);
    try {
      const [postsRes, missionsList] = await Promise.all([
        authFetch(`/api/gatherings/${gathering.id}/posts`).catch(() => null),
        getMissions(gathering.id).catch(() => [])
      ]);
      const posts = postsRes && postsRes.ok ? await postsRes.json() : [];
      const built = buildCrewTimelapseSpots({
        gathering,
        posts,
        missions: missionsList || [],
        currentUserName: currentUser?.name || '크루',
        includePosts: true,
        includeMissions: true,
        includeItineraryStops: true
      });

      const hydrated = [];
      for (const item of built) {
        if (item.photoUrl) {
          const media = await loadRemotePhoto(item.photoUrl);
          hydrated.push({
            ...item,
            img: media.img,
            dataUrl: item.photoUrl,
            thumbUrl: media.thumbUrl
          });
        } else {
          hydrated.push(item);
        }
      }
      setRawSpots(hydrated);
    } finally {
      setLoadingData(false);
    }
  }, [gathering, currentUser?.name]);

  useEffect(() => {
    loadCrewData();
  }, [loadCrewData]);

  // 소스 타입 및 크루원 필터링 적용 (Immutable)
  const filteredSpots = useMemo(() => {
    const byType = rawSpots.filter((s) => {
      if (s.sourceType === 'post' && !includePosts) return false;
      if (s.sourceType === 'mission' && !includeMissions) return false;
      return true;
    });
    return filterCrewSpotsByMember(byType, selectedMember);
  }, [rawSpots, includePosts, includeMissions, selectedMember]);

  const crewMembers = useMemo(() => extractCrewMemberNames(rawSpots), [rawSpots]);

  const totalKm = useMemo(() => {
    let d = 0;
    for (let i = 1; i < filteredSpots.length; i++) {
      d += calcDistanceKm(
        filteredSpots[i - 1].lat,
        filteredSpots[i - 1].lng,
        filteredSpots[i].lat,
        filteredSpots[i].lng
      );
    }
    return d;
  }, [filteredSpots]);

  // 크루 합동 샘플 데이터 체험
  const handleLoadCrewSample = () => {
    const anchor = resolveGatheringAnchorCoords(gathering);
    const sample = CREW_SAMPLE_DATA.map((info, idx) => {
      const graphic = createDemoSpotGraphic(
        {
          name: `${info.member} · ${info.title}`,
          emoji: info.emoji,
          c1: info.c1,
          c2: info.c2
        },
        idx
      );
      const img = new Image();
      img.src = graphic.dataUrl;
      const angle = (idx * 80 * Math.PI) / 180;
      const dt = new Date(Date.now() + idx * 1800000);
      return {
        uid: `crew-sample-${idx}-${Date.now()}`,
        name: `${info.emoji} ${info.member} · ${info.title}`,
        crewMember: info.member,
        sourceType: info.type,
        lat: anchor.lat + Math.sin(angle) * 0.025,
        lng: anchor.lng + Math.cos(angle) * 0.025,
        dayNumber: 1,
        date: dt,
        timeVal: dt.getTime(),
        img,
        dataUrl: graphic.dataUrl,
        thumbUrl: graphic.thumbUrl
      };
    });
    setRawSpots(sample);
    setSelectedMember('ALL');
    notify('✈️ 4인의 크루 합동 미션·스냅샷 샘플 동선을 불러왔습니다.');
  };

  // 크루 현장 사진 즉석 추가
  const handleAddCrewPhotos = async (fileList, isCamera = false) => {
    const files = Array.from(fileList || []).filter((f) => f && f.type.startsWith('image/'));
    if (files.length === 0) return;

    const anchor = resolveGatheringAnchorCoords(gathering);
    let camGeo = null;
    if (isCamera) {
      camGeo = await captureCurrentGeolocation(4500);
    }

    const added = [];
    for (let i = 0; i < files.length; i++) {
      const file = files[i];
      const [exif, media] = await Promise.all([
        extractExifFromFile(file),
        loadPhotoFile(file)
      ]);
      const dt = exif.date || new Date();
      const idxOffset = rawSpots.length + i;
      const angle = (idxOffset * 70 * Math.PI) / 180;
      const lat = exif.lat ?? camGeo?.lat ?? anchor.lat + Math.sin(angle) * 0.02;
      const lng = exif.lng ?? camGeo?.lng ?? anchor.lng + Math.cos(angle) * 0.02;
      const memberName = currentUser?.name || '크루';
      const cleanTitle = (file.name || '현장인증').replace(/\.[^.]+$/, '');

      added.push({
        uid: `crew-upload-${Date.now()}-${i}`,
        name: `📸 ${memberName} · ${cleanTitle}`,
        crewMember: memberName,
        sourceType: 'post',
        lat,
        lng,
        dayNumber: 1,
        date: dt,
        timeVal: dt.getTime(),
        img: media.img,
        dataUrl: media.dataUrl,
        thumbUrl: media.thumbUrl
      });
    }

    setRawSpots((prev) => [...prev, ...added]);
    notify(`📸 크루 현장 사진 ${added.length}장이 합동 동선에 추가되었습니다.`);
  };

  // 합동 보딩패스 GIF 타임랩스 렌더링
  const handleRenderCrewGif = async () => {
    if (filteredSpots.length < 2) {
      notify('⚠️ 최소 2장 이상의 크루 미션·갤러리 사진이 필요합니다.');
      return;
    }
    setRendering(true);
    setProgressPct(0);
    try {
      const res = await renderTravelTimelapse(
        filteredSpots,
        {
          title: `${gathering?.title || '크루 모임'} · 합동 보딩패스`
        },
        {
          format: gifFormat,
          output: 'gif',
          quality: gifQuality,
          camera: 'follow',
          zoom: 'street',
          styleKey: 'satellite',
          transportKey: 'plane'
        },
        (ratio) => setProgressPct(Math.round(ratio * 100))
      );
      if (generatedMedia?.url) URL.revokeObjectURL(generatedMedia.url);
      setGeneratedMedia({
        url: URL.createObjectURL(res.blob),
        blob: res.blob,
        ext: res.ext
      });
      notify('🎬 크루 합동 보딩패스 타임랩스(GIF)가 완성되었습니다!');
    } catch (err) {
      console.error(err);
      notify('⚠️ 타임랩스 생성 중 오류가 발생했습니다.');
    } finally {
      setRendering(false);
    }
  };

  const handleDownloadCrewPoster = async () => {
    if (filteredSpots.length === 0) return;
    setRendering(true);
    try {
      const blob = await exportBoardingMapPng(
        filteredSpots,
        { title: `${gathering?.title || '크루 모임'} · 합동 동선` },
        'satellite',
        'plane'
      );
      if (blob) {
        if (generatedMedia?.url) URL.revokeObjectURL(generatedMedia.url);
        const url = URL.createObjectURL(blob);
        setGeneratedMedia({
          url,
          blob,
          ext: 'png',
          width: 540,
          height: 540,
          frames: 1
        });
        await saveToGallery(blob, `tripgather-crew-map-${Date.now()}.png`);
        notify('🖼️ 크루 합동 보딩패스 포스터(PNG)를 생성 및 저장했습니다.');
      }
    } finally {
      setRendering(false);
    }
  };

  // 생성된 합동 타임랩스를 모임 갤러리 피드(/api/gatherings/{id}/posts)에 즉시 공유
  const handleShareToGatheringFeed = async () => {
    if (!generatedMedia?.blob || !gathering?.id) return;
    setPostingToFeed(true);
    try {
      const fd = new FormData();
      fd.append('file', generatedMedia.blob, `crew-timelapse-${gathering.id}-${Date.now()}.gif`);
      const upRes = await authFetch('/api/files/upload', {
        method: 'POST',
        body: fd
      });
      if (!upRes.ok) throw new Error('파일 업로드 실패');
      const { url } = await upRes.json();
      const fullImageUrl = apiUrl(url);

      const postRes = await authFetch(`/api/gatherings/${gathering.id}/posts`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          content: `✈️ [${gathering.title}] 크루 ${crewMembers.length}인의 미션·인증 합동 보딩패스 타임랩스 (${filteredSpots.length} GATES · ${totalKm.toFixed(1)}km)`,
          imageUrl: fullImageUrl,
          isPublic: true
        })
      });
      if (!postRes.ok) throw new Error('게시글 등록 실패');
      notify('📮 모임 갤러리 피드에 크루 합동 타임랩스를 공유했습니다!');
      if (onGoToGallery) onGoToGallery();
    } catch (err) {
      console.error(err);
      notify('⚠️ 갤러리 공유 중 오류가 발생했습니다.');
    } finally {
      setPostingToFeed(false);
    }
  };

  return (
    <div className="crew-tl">
      <div className="crew-tl-hero">
        <div className="crew-tl-head" style={{ marginBottom: 0 }}>
          <div>
            <span className="crew-tl-kicker">🤝 CREW JOINT BOARDING PASS TIMELAPSE</span>
            <h3 className="crew-tl-title">{gathering?.title} · 크루 합동 동선 스튜디오</h3>
            <p className="crew-tl-desc">
              크루원들이 미션 보드와 갤러리에 올린 인증샷들을 시간순으로 엮어 하나의 합동 보딩패스 타임랩스 GIF로 제작합니다.
            </p>
          </div>
          <button type="button" className="crew-tl-btn" onClick={handleLoadCrewSample}>
            ✨ 4인 크루 미션 샘플 체험
          </button>
        </div>

        <div className="crew-tl-metrics">
          <div className="crew-tl-metric">
            <strong>{crewMembers.length}명</strong>
            <span>참여 크루원</span>
          </div>
          <div className="crew-tl-metric">
            <strong>{filteredSpots.length} GATES</strong>
            <span>합동 인증 지점</span>
          </div>
          <div className="crew-tl-metric">
            <strong>{totalKm.toFixed(1)} km</strong>
            <span>크루 합동 이동거리</span>
          </div>
        </div>
      </div>

      {toast && <div className="crew-tl-toast">{toast}</div>}

      {/* 크루 현장 사진 추가 & 필터 */}
      <div className="crew-tl-card">
        <div className="crew-tl-head">
          <h4>👥 크루원 필터 & 현장 인증샷 추가</h4>
          <div className="crew-tl-chips">
            <label className="crew-tl-btn is-primary">
              🖼️ 현장 사진 추가
              <input
                type="file"
                accept="image/*"
                multiple
                className="crew-tl-hidden"
                onChange={(e) => {
                  handleAddCrewPhotos(e.target.files, false);
                  e.target.value = '';
                }}
              />
            </label>
            <label className="crew-tl-btn">
              📷 카메라 즉석 촬영
              <input
                type="file"
                accept="image/*"
                capture="environment"
                className="crew-tl-hidden"
                onChange={(e) => {
                  handleAddCrewPhotos(e.target.files, true);
                  e.target.value = '';
                }}
              />
            </label>
          </div>
        </div>

        <div className="crew-tl-chips" style={{ marginBottom: '10px' }}>
          <button
            type="button"
            className={`crew-tl-chip ${selectedMember === 'ALL' ? 'is-active' : ''}`}
            onClick={() => setSelectedMember('ALL')}
          >
            전체 크루 합동 ({rawSpots.length})
          </button>
          {crewMembers.map((member) => (
            <button
              key={member}
              type="button"
              className={`crew-tl-chip ${selectedMember === member ? 'is-active' : ''}`}
              onClick={() => setSelectedMember(member)}
            >
              🧑‍✈️ {member}
            </button>
          ))}
        </div>

        <div className="crew-tl-chips">
          <button
            type="button"
            className={`crew-tl-chip ${includeMissions ? 'is-active' : ''}`}
            onClick={() => setIncludeMissions((v) => !v)}
          >
            🎯 미션 인증샷 포함
          </button>
          <button
            type="button"
            className={`crew-tl-chip ${includePosts ? 'is-active' : ''}`}
            onClick={() => setIncludePosts((v) => !v)}
          >
            📸 갤러리 스냅샷 포함
          </button>
        </div>
      </div>

      {/* 합동 인증 지점 타임라인 */}
      <div className="crew-tl-card">
        <div className="crew-tl-head">
          <h4>🧭 합동 보딩 게이트 타임라인 ({filteredSpots.length}곳)</h4>
          <button type="button" className="crew-tl-btn" onClick={loadCrewData} disabled={loadingData}>
            🔄 {loadingData ? '불러오는 중...' : '모임 미션·갤러리 다시 동기화'}
          </button>
        </div>

        {filteredSpots.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '28px 12px', color: 'var(--text-secondary)', fontSize: '0.86rem' }}>
            아직 등록된 미션 인증 사진이나 갤러리 스냅샷이 없습니다. 상단 <strong>✨ 4인 크루 미션 샘플 체험</strong> 또는 <strong>🖼️ 현장 사진 추가</strong>를 눌러보세요!
          </div>
        ) : (
          <div className="crew-tl-grid">
            {filteredSpots.map((spot, idx) => (
              <div key={spot.uid} className="crew-tl-spot">
                <div className="crew-tl-spot-num">{idx + 1}</div>
                <img
                  src={spot.thumbUrl || spot.dataUrl}
                  alt={spot.name}
                  className="crew-tl-spot-thumb"
                />
                <div className="crew-tl-spot-info">
                  <div className="crew-tl-spot-title">{spot.name}</div>
                  <div className="crew-tl-spot-sub">
                    {spot.sourceType === 'mission'
                      ? '🎯 미션 CLEAR'
                      : spot.sourceType === 'itinerary'
                      ? '🎫 공식 경유지'
                      : '📸 크루 스냅샷'}{' '}
                    · {spot.crewMember}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* 합동 타임랩스 GIF 생성 & 갤러리 피드 공유 */}
      <div className="crew-tl-card">
        <div className="crew-tl-head">
          <h4>🎬 크루 합동 보딩패스 GIF 제작 & 모임 갤러리 공유</h4>
          <div className="crew-tl-chips">
            {[
              { key: 'square', label: '1:1 피드' },
              { key: 'wide', label: '16:9 와이드' },
              { key: 'story', label: '9:16 스토리' }
            ].map((f) => (
              <button
                key={f.key}
                type="button"
                className={`crew-tl-chip ${gifFormat === f.key ? 'is-active' : ''}`}
                onClick={() => setGifFormat(f.key)}
              >
                {f.label}
              </button>
            ))}
            <button
              type="button"
              className={`crew-tl-chip ${gifQuality === 'normal' ? 'is-active' : ''}`}
              onClick={() => setGifQuality(gifQuality === 'normal' ? 'light' : 'normal')}
            >
              {gifQuality === 'normal' ? '화질: 표준' : '화질: 경량'}
            </button>
          </div>
        </div>

        <div className="crew-tl-chips">
          <button
            type="button"
            className="crew-tl-btn is-primary"
            onClick={handleRenderCrewGif}
            disabled={rendering || filteredSpots.length < 2}
          >
            🎬 {rendering ? `합성 중 (${progressPct}%)...` : '크루 합동 보딩패스 GIF 만들기'}
          </button>

          <button
            type="button"
            className="crew-tl-btn"
            onClick={handleDownloadCrewPoster}
            disabled={rendering || filteredSpots.length === 0}
          >
            🖼️ 합동 동선 포스터(PNG) 저장
          </button>
        </div>

        {generatedMedia && (
          <div className="crew-tl-preview">
            <div style={{ fontWeight: 800, fontSize: '0.9rem', marginBottom: '10px' }}>
              ✈️ 완성된 크루 합동 보딩패스 타임랩스
            </div>
            <img
              src={generatedMedia.url}
              alt="크루 합동 타임랩스"
              className="crew-tl-preview-media"
            />
            <div className="crew-tl-chips" style={{ justifyContent: 'center', marginTop: '12px' }}>
              <button
                type="button"
                className="crew-tl-btn is-primary"
                onClick={() =>
                  saveToGallery(
                    generatedMedia.blob,
                    `tripgather-crew-timelapse-${Date.now()}.gif`
                  )
                }
              >
                💾 기기에 GIF 저장 / 공유
              </button>
              <button
                type="button"
                className="crew-tl-btn"
                onClick={handleShareToGatheringFeed}
                disabled={postingToFeed}
              >
                📮 {postingToFeed ? '갤러리 업로드 중...' : '모임 갤러리 피드에 바로 게시하기'}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default CrewMissionTimelapseTab;
