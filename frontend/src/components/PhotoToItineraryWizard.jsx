import React, { useState, useMemo } from 'react';
import { authFetch, apiUrl } from '../api/client';
import {
  extractExifFromFile,
  loadPhotoFile,
  captureCurrentGeolocation,
  estimateUnplacedLocations,
  createDemoSpotGraphic
} from '../utils/photoExifParser';
import {
  buildItineraryDraftFromSpots,
  enrichSpotsWithReverseGeocode
} from '../utils/photoToItineraryBuilder';
import { renderTravelTimelapse } from '../utils/boardingPassGifRenderer';
import './PhotoToItineraryWizard.css';

const SAMPLE_TWO_DAY_SPOTS = [
  { name: '제주국제공항', lat: 33.5065, lng: 126.493, dayOffset: 0, hour: 10, min: 0, emoji: '🛫', c1: '#FF5C00', c2: '#0F172A' },
  { name: '함덕 서우봉 해변', lat: 33.5434, lng: 126.6697, dayOffset: 0, hour: 13, min: 30, emoji: '🏖️', c1: '#0EA5E9', c2: '#0F172A' },
  { name: '성산일출봉', lat: 33.458, lng: 126.9426, dayOffset: 0, hour: 17, min: 0, emoji: '🌋', c1: '#F97316', c2: '#1E293B' },
  { name: '서귀포 매일올레시장', lat: 33.2497, lng: 126.563, dayOffset: 1, hour: 11, min: 30, emoji: '🍊', c1: '#FB923C', c2: '#0F172A' },
  { name: '카멜리아힐', lat: 33.2896, lng: 126.3701, dayOffset: 1, hour: 15, min: 0, emoji: '🌺', c1: '#EC4899', c2: '#0F172A' },
  { name: '애월 한담해안산책로', lat: 33.4628, lng: 126.3106, dayOffset: 1, hour: 18, min: 0, emoji: '🌅', c1: '#F43F5E', c2: '#0F172A' }
];

function dataUrlToBlob(dataUrl) {
  const parts = dataUrl.split(',');
  const mime = (parts[0].match(/:(.*?);/) || [null, 'image/jpeg'])[1];
  const bin = atob(parts[1]);
  const arr = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) arr[i] = bin.charCodeAt(i);
  return new Blob([arr], { type: mime });
}

const PhotoToItineraryWizard = ({ onApplyDraft }) => {
  const [spots, setSpots] = useState([]);
  const [unplacedCount, setUnplacedCount] = useState(0);
  const [clusterNearby, setClusterNearby] = useState(true);
  const [autoGenerateGif, setAutoGenerateGif] = useState(false);
  const [busyText, setBusyText] = useState('');
  const [isApplying, setIsApplying] = useState(false);

  const draft = useMemo(
    () => buildItineraryDraftFromSpots(spots, { clusterNearby }),
    [spots, clusterNearby]
  );

  const groupedPreviewDays = useMemo(() => {
    const map = new Map();
    draft.routePoints.forEach((rp) => {
      const d = rp.dayNumber || 1;
      if (!map.has(d)) map.set(d, []);
      map.get(d).push(rp);
    });
    return Array.from(map.entries()).map(([dayNumber, points]) => ({ dayNumber, points }));
  }, [draft.routePoints]);

  // 사진 여러 장 선택 또는 카메라 촬영 처리
  const handleFilesSelected = async (fileList, isCamera = false) => {
    const files = Array.from(fileList || []).filter((f) => f && f.type.startsWith('image/'));
    if (files.length === 0) return;

    setBusyText(`사진 ${files.length}장의 EXIF 촬영 시각·GPS 위치를 분석하는 중...`);
    try {
      let camGeo = null;
      if (isCamera) {
        camGeo = await captureCurrentGeolocation(4500);
      }

      const placed = [];
      const unplaced = [];
      for (let i = 0; i < files.length; i++) {
        const file = files[i];
        const [exif, media] = await Promise.all([
          extractExifFromFile(file),
          loadPhotoFile(file)
        ]);
        const dt = exif.date || (file.lastModified ? new Date(file.lastModified) : new Date());
        const lat = exif.lat ?? camGeo?.lat ?? null;
        const lng = exif.lng ?? camGeo?.lng ?? null;
        const cleanName = (file.name || `여행사진_${i + 1}`).replace(/\.[^.]+$/, '');

        const item = {
          uid: `pti-${Date.now()}-${i}`,
          name: cleanName,
          lat,
          lng,
          date: dt,
          timeVal: dt.getTime(),
          timeFromFile: !exif.date,
          img: media.img,
          dataUrl: media.dataUrl,
          thumbUrl: media.thumbUrl
        };

        if (lat != null && lng != null) {
          placed.push(item);
        } else {
          unplaced.push(item);
        }
      }

      // GPS가 없는 사진은 앞뒤 GPS 사진의 시간 비율로 자동 보간 시도
      const combinedPlaced = [...spots, ...placed];
      if (unplaced.length > 0 && combinedPlaced.length > 0) {
        const est = estimateUnplacedLocations(combinedPlaced, unplaced);
        setSpots(est.placedSpots);
        setUnplacedCount(est.unplacedSpots.length);
      } else {
        setSpots(combinedPlaced);
        setUnplacedCount(unplaced.length);
      }
    } finally {
      setBusyText('');
    }
  };

  // 1박 2일 제주 포토 코스 샘플 불러오기
  const handleLoadSampleTwoDays = () => {
    const sample = SAMPLE_TWO_DAY_SPOTS.map((info, i) => {
      const graphic = createDemoSpotGraphic(info, i);
      const img = new Image();
      img.src = graphic.dataUrl;
      const dt = new Date(2026, 9, 18 + info.dayOffset, info.hour, info.min, 0);
      return {
        uid: `sample-2d-${i}`,
        name: info.name,
        placeLabel: info.name,
        regionName: '제주도',
        lat: info.lat,
        lng: info.lng,
        date: dt,
        timeVal: dt.getTime(),
        timeFromFile: false,
        img,
        dataUrl: graphic.dataUrl,
        thumbUrl: graphic.thumbUrl
      };
    });
    setSpots(sample);
    setUnplacedCount(0);
  };

  // 좌표 기반 역지오코딩으로 실제 지명 채우기
  const handleReverseGeocode = async () => {
    if (spots.length === 0) return;
    setBusyText('지도 좌표로부터 실제 장소명·지역명을 조회하는 중...');
    try {
      const enriched = await enrichSpotsWithReverseGeocode(spots);
      setSpots(enriched);
    } finally {
      setBusyText('');
    }
  };

  // 서버 파일 업로드 헬퍼 (실패 시 로컬 dataUrl 유지)
  const uploadBlobSafe = async (blob, filename, fallbackUrl) => {
    try {
      const fd = new FormData();
      fd.append('file', blob, filename);
      const res = await authFetch('/api/files/upload', {
        method: 'POST',
        body: fd
      });
      if (res.ok) {
        const data = await res.json();
        return data.url ? apiUrl(data.url) : fallbackUrl;
      }
    } catch {
      // fallback
    }
    return fallbackUrl;
  };

  // 여정 폼(ItineraryEditorPage)에 자동 생성된 일자별 코스와 사진·타임랩스 적용
  const handleApplyToForm = async () => {
    if (draft.routePoints.length === 0) return;
    setIsApplying(true);
    setBusyText('경유지 사진을 업로드하고 일자별 코스를 여정 폼에 채우는 중...');
    try {
      // 각 스팟 사진 업로드
      const uploadedSpots = [];
      for (let i = 0; i < spots.length; i++) {
        const s = spots[i];
        let remotePhotoUrl = s.remotePhotoUrl || null;
        if (!remotePhotoUrl && s.dataUrl && s.dataUrl.startsWith('data:')) {
          const blob = dataUrlToBlob(s.dataUrl);
          remotePhotoUrl = await uploadBlobSafe(
            blob,
            `itinerary-spot-${i + 1}-${Date.now()}.jpg`,
            s.thumbUrl || s.dataUrl
          );
        }
        uploadedSpots.push({ ...s, remotePhotoUrl });
      }

      let timelapseUrl = null;
      if (autoGenerateGif && uploadedSpots.length >= 2) {
        setBusyText('보딩패스 동선 타임랩스(GIF)를 렌더링하는 중...');
        const gifRes = await renderTravelTimelapse(
          uploadedSpots,
          { title: draft.suggestedTitle },
          { format: 'square', output: 'gif', quality: 'light', camera: 'follow', zoom: 'street' }
        );
        timelapseUrl = await uploadBlobSafe(
          gifRes.blob,
          `itinerary-timelapse-${Date.now()}.gif`,
          null
        );
      }

      const finalDraft = buildItineraryDraftFromSpots(uploadedSpots, { clusterNearby });
      if (onApplyDraft) {
        onApplyDraft({
          ...finalDraft,
          timelapseUrl,
          coverPhotoUrl: finalDraft.routePoints[0]?.photoUrl || null
        });
      }
    } finally {
      setIsApplying(false);
      setBusyText('');
    }
  };

  return (
    <section className="pti-wizard" aria-label="여행 사진으로 여정 자동 생성">
      <div className="pti-header">
        <div>
          <span className="pti-badge">✨ PHOTO-TO-ITINERARY AUTO BUILDER</span>
          <h3 className="pti-title">📸 여행 사진 업로드로 일자별 코스 1초 자동 완성</h3>
          <p className="pti-sub">
            다녀온 여행 사진들을 올리면 촬영 날짜·시각과 GPS 좌표를 분석해 Day 1, Day 2 경유지와 도착·출발 스케줄을 자동으로 채워줍니다.
          </p>
        </div>

        <div className="pti-actions">
          <label className="pti-btn pti-btn-orange">
            🖼️ 여행 사진 여러 장 올리기
            <input
              type="file"
              accept="image/*"
              multiple
              className="pti-hidden-input"
              onChange={(e) => {
                handleFilesSelected(e.target.files, false);
                e.target.value = '';
              }}
            />
          </label>

          <label className="pti-btn">
            📷 카메라 촬영 추가
            <input
              type="file"
              accept="image/*"
              capture="environment"
              className="pti-hidden-input"
              onChange={(e) => {
                handleFilesSelected(e.target.files, true);
                e.target.value = '';
              }}
            />
          </label>

          <button type="button" className="pti-btn" onClick={handleLoadSampleTwoDays}>
            ✈️ 1박 2일 제주 샘플 체험
          </button>
        </div>
      </div>

      {busyText && <div className="pti-status">⏳ {busyText}</div>}

      {spots.length > 0 && (
        <div className="pti-body">
          <div className="pti-options-row">
            <div style={{ display: 'flex', gap: '14px', flexWrap: 'wrap' }}>
              <label className="pti-check-label">
                <input
                  type="checkbox"
                  checked={clusterNearby}
                  onChange={(e) => setClusterNearby(e.target.checked)}
                />
                📍 같은 장소 연속 촬영 사진 하나로 묶기 (반경 150m · 45분 이내)
              </label>

              <label className="pti-check-label">
                <input
                  type="checkbox"
                  checked={autoGenerateGif}
                  onChange={(e) => setAutoGenerateGif(e.target.checked)}
                />
                🎬 보딩패스 동선 GIF 타임랩스도 함께 생성해 여정에 첨부
              </label>
            </div>

            <div style={{ display: 'flex', gap: '8px' }}>
              <button
                type="button"
                className="pti-btn"
                style={{ color: 'var(--text-primary)', borderColor: 'rgba(15,23,42,0.18)' }}
                onClick={handleReverseGeocode}
              >
                🗺️ 좌표로 실제 지명 자동 찾기
              </button>
              <button
                type="button"
                className="pti-btn"
                style={{ color: 'var(--text-secondary)', borderColor: 'rgba(15,23,42,0.15)' }}
                onClick={() => {
                  setSpots([]);
                  setUnplacedCount(0);
                }}
              >
                초기화
              </button>
            </div>
          </div>

          <div className="pti-summary-card">
            <div>
              <div style={{ fontWeight: 900, fontSize: '0.95rem', color: '#0F172A' }}>
                🎫 추천 제목: {draft.suggestedTitle}
              </div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '2px' }}>
                📍 권역: <strong>{draft.region}</strong> · 📅 기간:{' '}
                <strong>
                  {draft.startDate} ~ {draft.endDate}
                </strong>{' '}
                · 총 <strong>{draft.routePoints.length}개</strong> 보딩 게이트
                {unplacedCount > 0 && ` (GPS 없는 사진 ${unplacedCount}장 제외됨)`}
              </div>
            </div>

            <button
              type="button"
              className="pti-btn pti-btn-orange"
              onClick={handleApplyToForm}
              disabled={isApplying}
            >
              ✨ {isApplying ? '여정 폼에 반영 중...' : '이 코스로 아래 여정 폼 자동 채우기'}
            </button>
          </div>

          <div className="pti-day-list">
            {groupedPreviewDays.map((day) => (
              <div key={day.dayNumber} className="pti-day-group">
                <div className="pti-day-header">
                  <span className="pti-day-pill">DAY {day.dayNumber}</span>
                  <span style={{ fontSize: '0.8rem', fontWeight: 800 }}>
                    경유지 {day.points.length}곳 자동 추출
                  </span>
                </div>
                <div className="pti-stop-grid">
                  {day.points.map((pt, idx) => (
                    <div key={`${day.dayNumber}-${idx}`} className="pti-stop-item">
                      {pt.photoUrl && (
                        <img src={pt.photoUrl} alt={pt.label} className="pti-stop-thumb" />
                      )}
                      <div className="pti-stop-text">
                        <div className="pti-stop-name">
                          #{pt.sequenceOrder} {pt.label}
                        </div>
                        <div className="pti-stop-time">
                          ✈️ {pt.startTime} ~ {pt.endTime}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </section>
  );
};

export default PhotoToItineraryWizard;
