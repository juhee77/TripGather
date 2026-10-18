import React, { useState, useEffect, useRef, useMemo } from 'react';
import { authFetch, apiUrl } from '../api/client';
import {
  calcDistanceKm,
  sortSpots,
  estimateUnplacedLocations,
  matchUnplacedToRoutePoint,
  autoMatchPhotosToRoutePoints,
  spotWhen,
  extractExifFromFile,
  loadPhotoFile,
  loadRemotePhoto,
  captureCurrentGeolocation,
  createDemoSpotGraphic
} from '../utils/photoExifParser';
import {
  planTravelGif,
  renderTravelTimelapse,
  exportBoardingMapPng,
  saveToGallery,
  canRecordMp4
} from '../utils/boardingPassGifRenderer';
import './TravelTimelapseStudio.css';

const LEAFLET_CSS = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
const LEAFLET_JS = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';

function ensureLeafletLoaded() {
  if (typeof window === 'undefined') return Promise.resolve(null);
  if (window.L) return Promise.resolve(window.L);
  return new Promise((resolve) => {
    if (!document.querySelector(`link[href="${LEAFLET_CSS}"]`)) {
      const link = document.createElement('link');
      link.rel = 'stylesheet';
      link.href = LEAFLET_CSS;
      document.head.appendChild(link);
    }
    const existing = document.querySelector(`script[src="${LEAFLET_JS}"]`);
    if (existing) {
      existing.addEventListener('load', () => resolve(window.L));
      return;
    }
    const script = document.createElement('script');
    script.src = LEAFLET_JS;
    script.async = true;
    script.onload = () => resolve(window.L);
    script.onerror = () => resolve(null);
    document.head.appendChild(script);
  });
}

function dataUrlToBlob(dataUrl) {
  const parts = dataUrl.split(',');
  const mime = (parts[0].match(/:(.*?);/) || [null, 'image/jpeg'])[1];
  const bin = atob(parts[1]);
  const arr = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) arr[i] = bin.charCodeAt(i);
  return new Blob([arr], { type: mime });
}

const DEMO_SPOTS_INFO = [
  { name: '제주국제공항', lat: 33.5065, lng: 126.493, emoji: '🛫', c1: '#FF5C00', c2: '#1E293B', hrs: 9 },
  { name: '함덕 서우봉 해변', lat: 33.5434, lng: 126.6697, emoji: '🏖️', c1: '#0EA5E9', c2: '#0F172A', hrs: 11 },
  { name: '성산일출봉', lat: 33.458, lng: 126.9426, emoji: '🌋', c1: '#F97316', c2: '#1E293B', hrs: 14 },
  { name: '서귀포 매일올레시장', lat: 33.2497, lng: 126.563, emoji: '🍊', c1: '#FB923C', c2: '#0F172A', hrs: 17 },
  { name: '애월 한담해안산책로', lat: 33.4628, lng: 126.3106, emoji: '🌅', c1: '#F43F5E', c2: '#0F172A', hrs: 19 }
];

const TravelTimelapseStudio = ({ itinerary, tripTitle, canEdit = false, onItineraryUpdated }) => {
  const [placedSpots, setPlacedSpots] = useState([]);
  const [unplacedSpots, setUnplacedSpots] = useState([]);
  const [selectedUnplacedUids, setSelectedUnplacedUids] = useState([]);
  const [selectedRoutePointIdx, setSelectedRoutePointIdx] = useState('0');
  const [placeQuery, setPlaceQuery] = useState('');
  const [mapStyle, setMapStyle] = useState('satellite');
  const [transportKey, setTransportKey] = useState('plane');
  const [tourSpeed, setTourSpeed] = useState(1);
  const [isTouring, setIsTouring] = useState(false);
  const [adjustMode, setAdjustMode] = useState(false);
  const [pickOnMapMode, setPickOnMapMode] = useState(false);

  // GIF / MP4 설정
  const [gifFormat, setGifFormat] = useState('square');
  const [gifOutput, setGifOutput] = useState('gif');
  const [gifQuality, setGifQuality] = useState('normal');
  const [gifZoom, setGifZoom] = useState('street');
  const [gifCamera, setGifCamera] = useState('follow');

  const [rendering, setRendering] = useState(false);
  const [renderProgress, setRenderProgress] = useState(0);
  const [renderStatusText, setRenderStatusText] = useState('');
  const [generatedMedia, setGeneratedMedia] = useState(null); // { url, blob, ext, width, height }
  const [savingToServer, setSavingToServer] = useState(false);
  const [toastMsg, setToastMsg] = useState('');

  const mapContainerRef = useRef(null);
  const mapInstanceRef = useRef(null);
  const tileLayerRef = useRef(null);
  const markersLayerRef = useRef(null);
  const routeLineRef = useRef(null);
  const tourAnimRef = useRef(null);
  const uidCounterRef = useRef(1);

  const routePoints = useMemo(() => itinerary?.routePoints || [], [itinerary]);
  const validRoutePoints = useMemo(
    () => routePoints.filter((rp) => rp && rp.lat != null && rp.lng != null),
    [routePoints]
  );

  const showToast = (msg) => {
    setToastMsg(msg);
    setTimeout(() => {
      setToastMsg((prev) => (prev === msg ? '' : prev));
    }, 4000);
  };

  // 여정(Itinerary)에 저장된 경유지 사진·좌표 초기 로드
  useEffect(() => {
    let cancelled = false;
    const initFromItinerary = async () => {
      const ptsWithCoords = (itinerary?.routePoints || []).filter(
        (rp) => rp && rp.lat != null && rp.lng != null
      );
      if (ptsWithCoords.length === 0) return;

      const loaded = [];
      for (let i = 0; i < ptsWithCoords.length; i++) {
        const rp = ptsWithCoords[i];
        const uid = `rp-${rp.id || i}-${uidCounterRef.current++}`;
        const dt = rp.takenAt ? new Date(rp.takenAt) : new Date(Date.now() + i * 3600000);
        if (rp.photoUrl) {
          const media = await loadRemotePhoto(rp.photoUrl);
          loaded.push({
            uid,
            name: rp.label || `경유지 ${i + 1}`,
            lat: Number(rp.lat),
            lng: Number(rp.lng),
            loc: 'route',
            routePointId: rp.id ?? null,
            dayNumber: rp.dayNumber ?? 1,
            sequenceOrder: rp.sequenceOrder ?? i + 1,
            date: dt,
            timeVal: dt.getTime(),
            timeFromFile: false,
            img: media.img,
            dataUrl: rp.photoUrl,
            thumbUrl: media.thumbUrl,
            remotePhotoUrl: rp.photoUrl,
            color: media.color
          });
        } else {
          const demo = createDemoSpotGraphic(
            {
              name: rp.label || `경유지 ${i + 1}`,
              emoji: i === 0 ? '🛫' : i === ptsWithCoords.length - 1 ? '🏁' : '📍',
              c1: '#FF5C00',
              c2: '#0F172A'
            },
            i
          );
          const img = new Image();
          img.src = demo.dataUrl;
          loaded.push({
            uid,
            name: rp.label || `경유지 ${i + 1}`,
            lat: Number(rp.lat),
            lng: Number(rp.lng),
            loc: 'route',
            routePointId: rp.id ?? null,
            dayNumber: rp.dayNumber ?? 1,
            sequenceOrder: rp.sequenceOrder ?? i + 1,
            date: dt,
            timeVal: dt.getTime(),
            timeFromFile: true,
            img,
            dataUrl: demo.dataUrl,
            thumbUrl: demo.thumbUrl,
            color: '#FF5C00',
            isPlaceholderGraphic: true
          });
        }
      }
      if (!cancelled && loaded.length > 0) {
        setPlacedSpots(loaded);
      }
    };
    initFromItinerary();
    return () => {
      cancelled = true;
    };
  }, [itinerary?.id]);

  // Leaflet 지도 초기화 및 타일 스타일 반영
  useEffect(() => {
    let mounted = true;
    ensureLeafletLoaded().then((L) => {
      if (!mounted || !L || !mapContainerRef.current) return;
      if (!mapInstanceRef.current) {
        const map = L.map(mapContainerRef.current, {
          center: [33.38, 126.55],
          zoom: 10,
          zoomControl: true
        });
        mapInstanceRef.current = map;
        markersLayerRef.current = L.layerGroup().addTo(map);
      }
      const map = mapInstanceRef.current;
      if (tileLayerRef.current) {
        map.removeLayer(tileLayerRef.current);
      }
      const tileUrls = {
        satellite:
          'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
        street: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
        topo: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}'
      };
      tileLayerRef.current = L.tileLayer(tileUrls[mapStyle] || tileUrls.satellite, {
        maxZoom: 18
      }).addTo(map);
    });
    return () => {
      mounted = false;
    };
  }, [mapStyle]);

  // 지도 클릭으로 미지정 사진 배치
  useEffect(() => {
    const map = mapInstanceRef.current;
    if (!map) return;
    const handleMapClick = (e) => {
      if (!pickOnMapMode) return;
      const targets =
        selectedUnplacedUids.length > 0
          ? selectedUnplacedUids
          : unplacedSpots[0]
          ? [unplacedSpots[0].uid]
          : [];
      if (targets.length === 0) {
        setPickOnMapMode(false);
        return;
      }
      const targetSet = new Set(targets);
      const moved = [];
      const rem = [];
      unplacedSpots.forEach((s, idx) => {
        if (targetSet.has(s.uid)) {
          const jitter = idx * 0.00018;
          moved.push({
            ...s,
            lat: e.latlng.lat + jitter,
            lng: e.latlng.lng + jitter,
            loc: 'manual'
          });
        } else {
          rem.push({ ...s });
        }
      });
      setPlacedSpots((prev) => autoMatchPhotosToRoutePoints([...prev, ...moved], validRoutePoints));
      setUnplacedSpots(rem);
      setSelectedUnplacedUids([]);
      setPickOnMapMode(false);
      showToast(`📍 ${moved.length}장의 사진 위치를 지도에 배치했습니다.`);
    };
    map.on('click', handleMapClick);
    return () => {
      map.off('click', handleMapClick);
    };
  }, [pickOnMapMode, selectedUnplacedUids, unplacedSpots, validRoutePoints]);

  // placedSpots 변경 시 지도 마커 및 폴리라인 갱신
  useEffect(() => {
    const L = typeof window !== 'undefined' ? window.L : null;
    const map = mapInstanceRef.current;
    const layer = markersLayerRef.current;
    if (!L || !map || !layer) return;

    layer.clearLayers();
    if (routeLineRef.current) {
      map.removeLayer(routeLineRef.current);
      routeLineRef.current = null;
    }
    if (placedSpots.length === 0) return;

    const latlngs = placedSpots.map((s) => [s.lat, s.lng]);
    if (latlngs.length > 1) {
      routeLineRef.current = L.polyline(latlngs, {
        color: '#FF5C00',
        weight: 4,
        opacity: 0.92,
        dashArray: '8, 6'
      }).addTo(map);
    }

    placedSpots.forEach((spot, idx) => {
      const icon = L.divIcon({
        className: '',
        html: `<div class="tl-leaflet-pin"><span class="tl-leaflet-pin-num">${idx + 1}</span><img src="${
          spot.thumbUrl || spot.dataUrl
        }" alt="" /></div>`,
        iconSize: [44, 44],
        iconAnchor: [22, 22]
      });
      const marker = L.marker([spot.lat, spot.lng], {
        icon,
        draggable: adjustMode
      }).addTo(layer);

      marker.bindPopup(
        `<strong>GATE #${idx + 1} · ${spot.name}</strong><br/><span style="font-size:12px;color:#64748B">${
          spotWhen(spot) || ''
        }</span>`
      );

      if (adjustMode) {
        marker.on('dragend', (ev) => {
          const pos = ev.target.getLatLng();
          setPlacedSpots((prev) =>
            prev.map((item) =>
              item.uid === spot.uid
                ? { ...item, lat: pos.lat, lng: pos.lng, loc: 'manual' }
                : { ...item }
            )
          );
        });
      }
    });

    if (!adjustMode) {
      const bounds = L.latLngBounds(latlngs);
      map.fitBounds(bounds, { padding: [42, 42], maxZoom: 15 });
    }
  }, [placedSpots, adjustMode]);

  // 총 이동 거리 계산
  const totalDistanceKm = useMemo(() => {
    let sum = 0;
    for (let i = 1; i < placedSpots.length; i++) {
      sum += calcDistanceKm(
        placedSpots[i - 1].lat,
        placedSpots[i - 1].lng,
        placedSpots[i].lat,
        placedSpots[i].lng
      );
    }
    return sum;
  }, [placedSpots]);

  // GIF/MP4 예상 용량 및 재생시간 계산
  const gifPlanSummary = useMemo(() => {
    if (placedSpots.length < 2) return null;
    return planTravelGif(placedSpots, gifFormat, {
      output: gifOutput,
      quality: gifQuality,
      zoom: gifZoom,
      camera: gifCamera
    });
  }, [placedSpots, gifFormat, gifOutput, gifQuality, gifZoom, gifCamera]);

  // 사진 파일 업로드(갤러리 또는 카메라 촬영) 처리
  const handleFilesSelected = async (fileList, isCameraCapture = false) => {
    const files = Array.from(fileList || []).filter((f) => f && f.type.startsWith('image/'));
    if (files.length === 0) return;

    let fallbackGeo = null;
    if (isCameraCapture) {
      fallbackGeo = await captureCurrentGeolocation(4500);
    }

    const newPlaced = [];
    const newUnplaced = [];

    for (const file of files) {
      try {
        const [exif, media] = await Promise.all([
          extractExifFromFile(file),
          loadPhotoFile(file)
        ]);
        const uid = `photo-${Date.now()}-${uidCounterRef.current++}`;
        const cleanName = (file.name || '여행사진').replace(/\.[^.]+$/, '');
        const hasExifTime = Boolean(exif.date);
        const dt = exif.date || (file.lastModified ? new Date(file.lastModified) : new Date());
        const lat = exif.lat ?? fallbackGeo?.lat ?? null;
        const lng = exif.lng ?? fallbackGeo?.lng ?? null;

        const spotObj = {
          uid,
          name: cleanName,
          lat,
          lng,
          loc: exif.lat != null ? 'gps' : fallbackGeo ? 'camera-gps' : 'none',
          date: dt,
          timeVal: dt.getTime(),
          timeFromFile: !hasExifTime,
          img: media.img,
          dataUrl: media.dataUrl,
          thumbUrl: media.thumbUrl,
          color: media.color
        };

        if (lat != null && lng != null) {
          newPlaced.push(spotObj);
        } else {
          newUnplaced.push(spotObj);
        }
      } catch (err) {
        console.error('사진 처리 실패:', err);
      }
    }

    if (newPlaced.length > 0) {
      setPlacedSpots((prev) => {
        // 기존 플레이스홀더만 있었다면 실제 사진으로 교체
        const base = prev.every((p) => p.isPlaceholderGraphic) ? [] : prev;
        const combined = autoMatchPhotosToRoutePoints(
          [...base, ...newPlaced],
          validRoutePoints
        );
        return sortSpots(combined, 'time-asc');
      });
    }
    if (newUnplaced.length > 0) {
      setUnplacedSpots((prev) => [...prev, ...newUnplaced]);
      setSelectedUnplacedUids(newUnplaced.map((s) => s.uid));
    }

    showToast(
      `📸 GPS 위치 확인 ${newPlaced.length}장 · 위치 미지정 ${newUnplaced.length}장 추가됨`
    );
  };

  // 데모 제주 보딩패스 샘플 로드
  const handleLoadDemoCourse = () => {
    const baseTime = new Date(2026, 9, 18, 9, 0, 0).getTime();
    const demoPlaced = DEMO_SPOTS_INFO.map((info, i) => {
      const graphic = createDemoSpotGraphic(info, i);
      const img = new Image();
      img.src = graphic.dataUrl;
      const dt = new Date(baseTime + i * 2.2 * 3600 * 1000);
      return {
        uid: `demo-${i}-${uidCounterRef.current++}`,
        name: info.name,
        lat: info.lat,
        lng: info.lng,
        loc: 'gps',
        dayNumber: 1,
        date: dt,
        timeVal: dt.getTime(),
        timeFromFile: false,
        img,
        dataUrl: graphic.dataUrl,
        thumbUrl: graphic.thumbUrl,
        color: info.c1
      };
    });
    setPlacedSpots(demoPlaced);
    setUnplacedSpots([]);
    showToast('✈️ 제주 보딩패스 샘플 코스(5개 게이트)를 불러왔습니다.');
  };

  // 미지정 사진을 여정의 특정 RoutePoint에 원클릭 매칭
  const handleMatchToRoutePoint = () => {
    const rp = validRoutePoints[Number(selectedRoutePointIdx)];
    if (!rp) return;
    const targets =
      selectedUnplacedUids.length > 0
        ? selectedUnplacedUids
        : unplacedSpots.map((s) => s.uid);
    if (targets.length === 0) return;

    const res = matchUnplacedToRoutePoint(placedSpots, unplacedSpots, targets, rp);
    setPlacedSpots(res.placedSpots);
    setUnplacedSpots(res.unplacedSpots);
    setSelectedUnplacedUids([]);
    showToast(`📌 '${rp.label}' 경유지에 ${res.matchedCount}장의 사진을 매칭했습니다.`);
  };

  // 촬영 시각 비율로 미지정 사진 자동 보간 추정
  const handleEstimateByTime = () => {
    const targets =
      selectedUnplacedUids.length > 0 ? selectedUnplacedUids : null;
    const res = estimateUnplacedLocations(placedSpots, unplacedSpots, targets);
    if (res.estimatedCount === 0) {
      showToast('⚠️ 기준이 될 GPS 사진과 EXIF 촬영 시각이 필요합니다.');
      return;
    }
    setPlacedSpots(res.placedSpots);
    setUnplacedSpots(res.unplacedSpots);
    setSelectedUnplacedUids([]);
    showToast(`⏱️ 촬영 시각 비율로 ${res.estimatedCount}장의 위치를 추정해 배치했습니다.`);
  };

  // 인접(가장 가까운 시각) 사진 위치 복사
  const handleCopyNeighborSpot = () => {
    if (placedSpots.length === 0 || unplacedSpots.length === 0) return;
    const targetSet =
      selectedUnplacedUids.length > 0
        ? new Set(selectedUnplacedUids)
        : new Set(unplacedSpots.map((s) => s.uid));

    const moved = [];
    const rem = [];
    unplacedSpots.forEach((u, idx) => {
      if (!targetSet.has(u.uid)) {
        rem.push({ ...u });
        return;
      }
      let best = placedSpots[0];
      let bestDiff = Infinity;
      placedSpots.forEach((p) => {
        const diff = Math.abs((p.timeVal || 0) - (u.timeVal || 0));
        if (diff < bestDiff) {
          bestDiff = diff;
          best = p;
        }
      });
      const jitter = (idx + 1) * 0.0002;
      moved.push({
        ...u,
        lat: best.lat + jitter,
        lng: best.lng + jitter,
        loc: 'copied',
        dayNumber: best.dayNumber ?? 1
      });
    });

    setPlacedSpots((prev) => sortSpots([...prev, ...moved], 'time-asc'));
    setUnplacedSpots(rem);
    setSelectedUnplacedUids([]);
    showToast(`📎 인접 지점 좌표로 ${moved.length}장을 배치했습니다.`);
  };

  // 장소명 검색(Nominatim)으로 미지정 사진 배치
  const handleSearchPlaceAndAssign = async () => {
    const q = placeQuery.trim();
    if (!q) return;
    try {
      const res = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&limit=1&q=${encodeURIComponent(q)}`
      );
      const data = await res.json();
      if (!Array.isArray(data) || data.length === 0) {
        showToast('🔍 검색 결과가 없습니다. 다른 지명으로 시도해 주세요.');
        return;
      }
      const { lat, lon, display_name } = data[0];
      const targets =
        selectedUnplacedUids.length > 0
          ? selectedUnplacedUids
          : unplacedSpots[0]
          ? [unplacedSpots[0].uid]
          : [];
      if (targets.length === 0) return;
      const targetSet = new Set(targets);
      const shortLabel = display_name.split(',')[0] || q;

      const moved = [];
      const rem = [];
      unplacedSpots.forEach((s) => {
        if (targetSet.has(s.uid)) {
          moved.push({
            ...s,
            lat: Number(lat),
            lng: Number(lon),
            name: shortLabel,
            loc: 'search'
          });
        } else {
          rem.push({ ...s });
        }
      });
      setPlacedSpots((prev) => [...prev, ...moved]);
      setUnplacedSpots(rem);
      setSelectedUnplacedUids([]);
      setPlaceQuery('');
      showToast(`🔍 '${shortLabel}' 위치에 ${moved.length}장을 배치했습니다.`);
    } catch {
      showToast('⚠️ 장소 검색 중 오류가 발생했습니다.');
    }
  };

  // 지도 위 실시간 동선 투어 재생
  const handleToggleTour = () => {
    const map = mapInstanceRef.current;
    if (!map || placedSpots.length < 2) return;
    if (isTouring) {
      if (tourAnimRef.current) clearTimeout(tourAnimRef.current);
      setIsTouring(false);
      return;
    }
    setIsTouring(true);
    let idx = 0;
    const step = () => {
      if (idx >= placedSpots.length) {
        setIsTouring(false);
        return;
      }
      const s = placedSpots[idx];
      map.flyTo([s.lat, s.lng], 14, { duration: Math.max(0.4, 1.2 / tourSpeed) });
      idx += 1;
      tourAnimRef.current = setTimeout(step, Math.round(1600 / tourSpeed));
    };
    step();
  };

  // 스팟 순서 이동 / 이름 변경 / 삭제 (Immutable)
  const handleMoveSpot = (index, delta) => {
    const target = index + delta;
    if (target < 0 || target >= placedSpots.length) return;
    setPlacedSpots((prev) => {
      const next = prev.map((s) => ({ ...s }));
      const temp = next[index];
      next[index] = next[target];
      next[target] = temp;
      return next;
    });
  };

  const handleRenameSpot = (uid, newName) => {
    setPlacedSpots((prev) =>
      prev.map((s) => (s.uid === uid ? { ...s, name: newName } : { ...s }))
    );
  };

  const handleUnplaceSpot = (uid) => {
    const target = placedSpots.find((s) => s.uid === uid);
    if (!target) return;
    setPlacedSpots((prev) => prev.filter((s) => s.uid !== uid));
    setUnplacedSpots((prev) => [
      ...prev,
      { ...target, lat: null, lng: null, loc: 'none' }
    ]);
  };

  const handleRemoveSpot = (uid) => {
    setPlacedSpots((prev) => prev.filter((s) => s.uid !== uid));
  };

  // 보딩패스 GIF / MP4 생성 실행
  const handleGenerateTimelapse = async () => {
    if (placedSpots.length < 2) {
      showToast('⚠️ 최소 2개 이상의 위치가 지정된 사진이 필요합니다.');
      return;
    }
    setRendering(true);
    setRenderProgress(0);
    setRenderStatusText('보딩패스 지도 타일 및 프레임 합성 중...');
    try {
      const title = tripTitle || itinerary?.title || '내 여행 보딩패스';
      const res = await renderTravelTimelapse(
        placedSpots,
        { title },
        {
          format: gifFormat,
          output: gifOutput,
          quality: gifQuality,
          zoom: gifZoom,
          camera: gifCamera,
          styleKey: mapStyle,
          transportKey
        },
        (ratio, cur, total) => {
          setRenderProgress(Math.round(ratio * 100));
          setRenderStatusText(`프레임 인코딩 중 (${cur}/${total})...`);
        }
      );
      if (generatedMedia?.url) {
        URL.revokeObjectURL(generatedMedia.url);
      }
      const url = URL.createObjectURL(res.blob);
      setGeneratedMedia({
        url,
        blob: res.blob,
        ext: res.ext,
        width: res.width,
        height: res.height
      });
      showToast(`🎬 보딩패스 타임랩스(${res.ext.toUpperCase()}) 생성이 완료되었습니다!`);
    } catch (err) {
      console.error('타임랩스 생성 오류:', err);
      showToast('⚠️ 타임랩스 생성 중 오류가 발생했습니다.');
    } finally {
      setRendering(false);
    }
  };

  // 동선 요약 이미지(PNG) 저장
  const handleDownloadMapPng = async () => {
    if (placedSpots.length < 1) return;
    setRendering(true);
    setRenderStatusText('보딩패스 요약 포스터(PNG) 렌더링 중...');
    try {
      const title = tripTitle || itinerary?.title || '내 여행 보딩패스';
      const blob = await exportBoardingMapPng(placedSpots, { title }, mapStyle, transportKey);
      await saveToGallery(blob, `tripgather-boarding-map-${Date.now()}.png`);
      showToast('🖼️ 보딩패스 동선 포스터(PNG)를 저장했습니다.');
    } finally {
      setRendering(false);
    }
  };

  // 서버 업로드 유틸 (`/api/files/upload`)
  const uploadBlobToServer = async (blob, filename) => {
    const formData = new FormData();
    formData.append('file', blob, filename);
    const res = await authFetch('/api/files/upload', {
      method: 'POST',
      body: formData
    });
    if (!res.ok) throw new Error('파일 업로드 실패');
    const data = await res.json();
    return apiUrl(data.url);
  };

  // 내 여정(Itinerary)에 경유지별 사진(photoUrl, takenAt) 및 타임랩스(timelapseUrl) 영구 저장
  const handleSaveToItinerary = async () => {
    if (!itinerary?.id) return;
    setSavingToServer(true);
    try {
      // 1) 새로 추가된 사진(data:image/...)을 서버에 업로드
      const updatedSpots = [];
      for (let i = 0; i < placedSpots.length; i++) {
        const s = placedSpots[i];
        let remoteUrl = s.remotePhotoUrl || null;
        if (!remoteUrl && s.dataUrl && s.dataUrl.startsWith('data:') && !s.isPlaceholderGraphic) {
          const blob = dataUrlToBlob(s.dataUrl);
          remoteUrl = await uploadBlobToServer(blob, `spot-${i + 1}-${Date.now()}.jpg`);
        }
        updatedSpots.push({ ...s, remotePhotoUrl: remoteUrl });
      }
      setPlacedSpots(updatedSpots);

      // 2) 생성된 타임랩스 GIF/MP4가 있으면 업로드
      let timelapseUrl = itinerary.timelapseUrl || null;
      if (generatedMedia?.blob) {
        timelapseUrl = await uploadBlobToServer(
          generatedMedia.blob,
          `timelapse-${itinerary.id}-${Date.now()}.${generatedMedia.ext}`
        );
      }

      // 3) 기존 routePoints에 매칭된 사진 반영 + 매칭되지 않은 신규 사진 경유지 병합
      const existingPoints = (itinerary.routePoints || []).map((rp) => ({ ...rp }));
      const usedSpotUids = new Set();

      const mergedPoints = existingPoints.map((rp) => {
        const matchedSpot = updatedSpots.find(
          (s) =>
            !usedSpotUids.has(s.uid) &&
            ((rp.id && s.routePointId === rp.id) ||
              (rp.lat != null &&
                rp.lng != null &&
                calcDistanceKm(s.lat, s.lng, Number(rp.lat), Number(rp.lng)) <= 0.35))
        );
        if (matchedSpot) {
          usedSpotUids.add(matchedSpot.uid);
          return {
            ...rp,
            photoUrl: matchedSpot.remotePhotoUrl || rp.photoUrl || null,
            takenAt:
              matchedSpot.date instanceof Date
                ? matchedSpot.date.toISOString().slice(0, 19)
                : rp.takenAt || null
          };
        }
        return rp;
      });

      updatedSpots.forEach((s, idx) => {
        if (!usedSpotUids.has(s.uid) && !s.isPlaceholderGraphic) {
          mergedPoints.push({
            dayNumber: s.dayNumber || 1,
            dayLabel: `Day ${s.dayNumber || 1}`,
            sequenceOrder: mergedPoints.length + idx + 1,
            label: s.name || `촬영 지점 ${idx + 1}`,
            lat: s.lat,
            lng: s.lng,
            photoUrl: s.remotePhotoUrl || null,
            takenAt:
              s.date instanceof Date ? s.date.toISOString().slice(0, 19) : null,
            isCompleted: true
          });
        }
      });

      const payload = {
        ...itinerary,
        timelapseUrl,
        routePoints: mergedPoints
      };

      const patchRes = await authFetch(`/api/itineraries/${itinerary.id}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!patchRes.ok) throw new Error('여정 저장 실패');
      const savedItinerary = await patchRes.json();
      if (onItineraryUpdated) onItineraryUpdated(savedItinerary);
      showToast('☁️ 내 여정에 경유지 사진과 보딩패스 타임랩스가 영구 저장되었습니다!');
    } catch (err) {
      console.error(err);
      showToast('⚠️ 여정 저장 중 오류가 발생했습니다.');
    } finally {
      setSavingToServer(false);
    }
  };

  return (
    <div className="tl-studio">
      {/* 1. 보딩패스 티켓 헤더 배너 */}
      <div className="tl-ticket-banner">
        <div className="tl-banner-top">
          <div>
            <span className="tl-kicker">✈️ BOARDING PASS TIMELAPSE STUDIO</span>
            <h2 className="tl-banner-title">
              {tripTitle || itinerary?.title || '내 여행 동선 타임랩스 스튜디오'}
            </h2>
            <p className="tl-banner-desc">
              여행 사진을 올리거나 카메라로 즉석 촬영하면 EXIF GPS·시각을 분석해 보딩패스 동선 GIF·MP4를 제작합니다.
            </p>
          </div>
          <button
            type="button"
            className="tl-btn"
            onClick={handleLoadDemoCourse}
          >
            ✨ 제주 샘플 코스 체험
          </button>
        </div>

        <div className="tl-stats-row">
          <div className="tl-stat-box">
            <span className="tl-stat-val">{placedSpots.length} GATES</span>
            <span className="tl-stat-lbl">지도 배치 완료</span>
          </div>
          <div className="tl-stat-box">
            <span className="tl-stat-val">{totalDistanceKm.toFixed(1)} km</span>
            <span className="tl-stat-lbl">총 비행·이동 거리</span>
          </div>
          <div className="tl-stat-box">
            <span className="tl-stat-val">{unplacedSpots.length}장</span>
            <span className="tl-stat-lbl">위치 미지정 대기</span>
          </div>
        </div>
      </div>

      {toastMsg && (
        <div className="tl-toast" role="status">
          <span>{toastMsg}</span>
        </div>
      )}

      {/* 2. 사진 업로드 & 즉석 카메라 촬영 섹션 */}
      <div className="tl-card">
        <div className="tl-card-head">
          <h3 className="tl-card-title">📸 여행 사진 추가 & 현장 즉석 촬영</h3>
          <span style={{ fontSize: '0.78rem', color: 'var(--text-secondary)' }}>
            GPS 메타데이터 자동 추출 · 위치 없는 사진도 경유지에 매칭 가능
          </span>
        </div>

        <div className="tl-upload-grid">
          <label className="tl-upload-btn">
            🖼️ 갤러리 사진 여러 장 선택
            <input
              type="file"
              accept="image/*"
              multiple
              className="tl-hidden-input"
              onChange={(e) => {
                handleFilesSelected(e.target.files, false);
                e.target.value = '';
              }}
            />
          </label>

          <label className="tl-upload-btn tl-camera">
            📷 지금 현장 사진 찍기 (GPS 자동기록)
            <input
              type="file"
              accept="image/*"
              capture="environment"
              className="tl-hidden-input"
              onChange={(e) => {
                handleFilesSelected(e.target.files, true);
                e.target.value = '';
              }}
            />
          </label>
        </div>
      </div>

      {/* 3. 위치 미지정 사진 트레이 (카카오톡 등 EXIF 없는 사진 보정) */}
      {unplacedSpots.length > 0 && (
        <div className="tl-card tl-unplaced-card">
          <div className="tl-card-head">
            <h3 className="tl-card-title">
              📍 위치 미지정 사진 트레이
              <span className="tl-badge-warn">{unplacedSpots.length}장 대기 중</span>
            </h3>
            <div className="tl-chip-group">
              <button
                type="button"
                className="tl-btn"
                onClick={() => setSelectedUnplacedUids(unplacedSpots.map((s) => s.uid))}
              >
                전체 선택
              </button>
              <button
                type="button"
                className="tl-btn"
                onClick={() => setSelectedUnplacedUids([])}
              >
                선택 해제
              </button>
            </div>
          </div>

          <div className="tl-unplaced-strip">
            {unplacedSpots.map((spot) => {
              const selected = selectedUnplacedUids.includes(spot.uid);
              return (
                <div
                  key={spot.uid}
                  className={`tl-unplaced-item ${selected ? 'is-selected' : ''}`}
                  onClick={() =>
                    setSelectedUnplacedUids((prev) =>
                      prev.includes(spot.uid)
                        ? prev.filter((id) => id !== spot.uid)
                        : [...prev, spot.uid]
                    )
                  }
                >
                  <img src={spot.thumbUrl || spot.dataUrl} alt={spot.name} />
                  <div className="tl-unplaced-name">{spot.name}</div>
                </div>
              );
            })}
          </div>

          {/* TripGather 전용: 내 여정 경유지(RoutePoint)에 원클릭 매칭 */}
          {validRoutePoints.length > 0 && (
            <div className="tl-route-matcher">
              <strong style={{ fontSize: '0.82rem', color: 'var(--primary-orange)' }}>
                🎫 내 여정 경유지에 원클릭 매칭:
              </strong>
              <select
                className="tl-select"
                value={selectedRoutePointIdx}
                onChange={(e) => setSelectedRoutePointIdx(e.target.value)}
              >
                {validRoutePoints.map((rp, idx) => (
                  <option key={rp.id || idx} value={String(idx)}>
                    Day {rp.dayNumber || 1} · #{rp.sequenceOrder || idx + 1} {rp.label}
                  </option>
                ))}
              </select>
              <button
                type="button"
                className="tl-btn tl-btn-primary"
                onClick={handleMatchToRoutePoint}
              >
                📌 선택한 경유지에 배치
              </button>
            </div>
          )}

          <div className="tl-action-row">
            <button
              type="button"
              className={`tl-btn ${pickOnMapMode ? 'tl-btn-primary' : ''}`}
              onClick={() => setPickOnMapMode((v) => !v)}
            >
              🗺️ {pickOnMapMode ? '지도 클릭 대기 중 (취소)' : '지도에서 직접 찍기'}
            </button>
            <button type="button" className="tl-btn" onClick={handleEstimateByTime}>
              ⏱️ 촬영시간 비율로 자동 추정
            </button>
            <button
              type="button"
              className="tl-btn"
              onClick={handleCopyNeighborSpot}
              disabled={placedSpots.length === 0}
            >
              📎 인접 사진 위치 복사
            </button>
          </div>

          <div className="tl-action-row" style={{ marginTop: '8px' }}>
            <input
              type="text"
              className="tl-input"
              placeholder="장소명 검색으로 위치 지정 (예: 성산일출봉, 도쿄타워)"
              value={placeQuery}
              onChange={(e) => setPlaceQuery(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearchPlaceAndAssign()}
            />
            <button type="button" className="tl-btn" onClick={handleSearchPlaceAndAssign}>
              🔍 장소 검색 배치
            </button>
          </div>
        </div>
      )}

      {/* 4. 인터랙티브 동선 지도 & 애니메이션 투어 */}
      <div className="tl-card">
        <div className="tl-map-toolbar">
          <div className="tl-chip-group">
            {[
              { key: 'satellite', label: '🛰️ 위성' },
              { key: 'street', label: '🗺️ 일반' },
              { key: 'topo', label: '⛰️ 지형' }
            ].map((st) => (
              <button
                key={st.key}
                type="button"
                className={`tl-chip ${mapStyle === st.key ? 'is-active' : ''}`}
                onClick={() => setMapStyle(st.key)}
              >
                {st.label}
              </button>
            ))}
          </div>

          <div className="tl-chip-group">
            <button
              type="button"
              className={`tl-chip ${adjustMode ? 'is-active' : ''}`}
              onClick={() => setAdjustMode((v) => !v)}
            >
              ✋ {adjustMode ? '핀 드래그 보정 완료' : '핀 위치 드래그 보정'}
            </button>
          </div>
        </div>

        <div className="tl-map-box">
          {pickOnMapMode && (
            <div className="tl-pick-banner">
              <span>📍 지도에서 사진을 배치할 위치를 클릭하세요</span>
              <button
                type="button"
                className="tl-btn"
                onClick={() => setPickOnMapMode(false)}
              >
                취소
              </button>
            </div>
          )}
          <div ref={mapContainerRef} className="tl-leaflet-canvas" />
        </div>

        <div className="tl-tour-bar">
          <div className="tl-chip-group">
            {[
              { key: 'plane', label: '✈️ 비행' },
              { key: 'car', label: '🚗 드라이브' },
              { key: 'walk', label: '🥾 도보' }
            ].map((tr) => (
              <button
                key={tr.key}
                type="button"
                className={`tl-chip ${transportKey === tr.key ? 'is-active' : ''}`}
                onClick={() => setTransportKey(tr.key)}
              >
                {tr.label}
              </button>
            ))}
          </div>

          <div className="tl-chip-group">
            {[1, 2, 4].map((spd) => (
              <button
                key={spd}
                type="button"
                className={`tl-chip ${tourSpeed === spd ? 'is-active' : ''}`}
                onClick={() => setTourSpeed(spd)}
              >
                {spd}x
              </button>
            ))}
            <button
              type="button"
              className="tl-btn tl-btn-primary"
              onClick={handleToggleTour}
              disabled={placedSpots.length < 2}
            >
              {isTouring ? '⏹ 투어 정지' : '▶ 경로 투어 재생'}
            </button>
          </div>
        </div>
      </div>

      {/* 5. 보딩 게이트(촬영 지점) 순서 정렬 & 편집 리스트 */}
      {placedSpots.length > 0 && (
        <div className="tl-card">
          <div className="tl-card-head">
            <h3 className="tl-card-title">🧭 보딩 게이트 동선 순서 ({placedSpots.length}곳)</h3>
            <div className="tl-chip-group">
              <button
                type="button"
                className="tl-btn"
                onClick={() => setPlacedSpots((prev) => sortSpots(prev, 'time-asc'))}
              >
                ⏱️ 시간순
              </button>
              <button
                type="button"
                className="tl-btn"
                onClick={() => setPlacedSpots((prev) => sortSpots(prev, 'tsp'))}
              >
                🧭 최단거리(TSP)
              </button>
              <button
                type="button"
                className="tl-btn"
                onClick={() => setPlacedSpots((prev) => sortSpots(prev, 'reverse'))}
              >
                🔄 역순
              </button>
            </div>
          </div>

          <div className="tl-spot-list">
            {placedSpots.map((spot, idx) => (
              <div key={spot.uid} className="tl-spot-row">
                <div className="tl-gate-num">{idx + 1}</div>
                <img
                  src={spot.thumbUrl || spot.dataUrl}
                  alt={spot.name}
                  className="tl-spot-thumb"
                />
                <div className="tl-spot-info">
                  <input
                    type="text"
                    className="tl-spot-title-input"
                    value={spot.name}
                    onChange={(e) => handleRenameSpot(spot.uid, e.target.value)}
                  />
                  <div className="tl-spot-meta">
                    <span className="tl-loc-tag">
                      {spot.loc === 'gps'
                        ? '📍 EXIF GPS'
                        : spot.loc === 'camera-gps'
                        ? '📷 현장 GPS'
                        : spot.loc === 'route'
                        ? `🎫 Day ${spot.dayNumber || 1} 경유지`
                        : spot.loc === 'estimated'
                        ? '⏱️ 시간보간'
                        : '🗺️ 수동지정'}
                    </span>
                    <span>{spotWhen(spot)}</span>
                    {idx > 0 && (
                      <span>
                        +{calcDistanceKm(
                          placedSpots[idx - 1].lat,
                          placedSpots[idx - 1].lng,
                          spot.lat,
                          spot.lng
                        ).toFixed(1)}{' '}
                        km
                      </span>
                    )}
                  </div>
                </div>
                <div className="tl-spot-actions">
                  <button
                    type="button"
                    className="tl-btn"
                    onClick={() => handleMoveSpot(idx, -1)}
                    disabled={idx === 0}
                    title="위로"
                  >
                    ▲
                  </button>
                  <button
                    type="button"
                    className="tl-btn"
                    onClick={() => handleMoveSpot(idx, 1)}
                    disabled={idx === placedSpots.length - 1}
                    title="아래로"
                  >
                    ▼
                  </button>
                  <button
                    type="button"
                    className="tl-btn"
                    onClick={() => handleUnplaceSpot(spot.uid)}
                    title="위치 재지정"
                  >
                    📍
                  </button>
                  <button
                    type="button"
                    className="tl-btn"
                    onClick={() => handleRemoveSpot(spot.uid)}
                    title="제거"
                  >
                    ✕
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* 6. 보딩패스 GIF / MP4 타임랩스 스튜디오 */}
      <div className="tl-card">
        <div className="tl-card-head">
          <h3 className="tl-card-title">🎬 보딩패스 타임랩스(GIF / MP4) 제작 스튜디오</h3>
          {gifPlanSummary && (
            <span style={{ fontSize: '0.78rem', fontWeight: 700, color: 'var(--primary-orange)' }}>
              예상 {gifPlanSummary.seconds.toFixed(1)}초 · {gifPlanSummary.plan.length}프레임 · 약{' '}
              {(gifPlanSummary.estBytes / (1024 * 1024)).toFixed(1)}MB
            </span>
          )}
        </div>

        <div className="tl-gif-options">
          <div className="tl-opt-group">
            <span className="tl-opt-label">📐 화면 비율</span>
            <div className="tl-chip-group">
              {[
                { key: 'square', label: '1:1 피드' },
                { key: 'wide', label: '16:9 와이드' },
                { key: 'story', label: '9:16 릴스·스토리' }
              ].map((f) => (
                <button
                  key={f.key}
                  type="button"
                  className={`tl-chip ${gifFormat === f.key ? 'is-active' : ''}`}
                  onClick={() => setGifFormat(f.key)}
                >
                  {f.label}
                </button>
              ))}
            </div>
          </div>

          <div className="tl-opt-group">
            <span className="tl-opt-label">🎞️ 출력 포맷 & 화질</span>
            <div className="tl-chip-group">
              <button
                type="button"
                className={`tl-chip ${gifOutput === 'gif' ? 'is-active' : ''}`}
                onClick={() => setGifOutput('gif')}
              >
                움짤 GIF
              </button>
              {canRecordMp4() && (
                <button
                  type="button"
                  className={`tl-chip ${gifOutput === 'mp4' ? 'is-active' : ''}`}
                  onClick={() => setGifOutput('mp4')}
                >
                  고화질 MP4
                </button>
              )}
              <button
                type="button"
                className={`tl-chip ${gifQuality === 'normal' ? 'is-active' : ''}`}
                onClick={() => setGifQuality('normal')}
              >
                표준
              </button>
              <button
                type="button"
                className={`tl-chip ${gifQuality === 'light' ? 'is-active' : ''}`}
                onClick={() => setGifQuality('light')}
              >
                경량(빠름)
              </button>
            </div>
          </div>

          <div className="tl-opt-group">
            <span className="tl-opt-label">🎥 카메라 연출</span>
            <div className="tl-chip-group">
              <button
                type="button"
                className={`tl-chip ${gifCamera === 'follow' ? 'is-active' : ''}`}
                onClick={() => setGifCamera('follow')}
              >
                게이트 추적 줌
              </button>
              <button
                type="button"
                className={`tl-chip ${gifCamera === 'overview' ? 'is-active' : ''}`}
                onClick={() => setGifCamera('overview')}
              >
                전체 경로 고정
              </button>
              <button
                type="button"
                className={`tl-chip ${gifZoom === 'street' ? 'is-active' : ''}`}
                onClick={() => setGifZoom(gifZoom === 'street' ? 'area' : 'street')}
              >
                {gifZoom === 'street' ? '🔍 상세 줌' : '🌐 권역 줌'}
              </button>
            </div>
          </div>
        </div>

        {rendering && (
          <div>
            <div className="tl-progress-bar">
              <div className="tl-progress-fill" style={{ width: `${renderProgress}%` }} />
            </div>
            <p style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', margin: 0 }}>
              {renderStatusText} ({renderProgress}%)
            </p>
          </div>
        )}

        <div className="tl-action-row">
          <button
            type="button"
            className="tl-btn tl-btn-primary"
            onClick={handleGenerateTimelapse}
            disabled={rendering || placedSpots.length < 2}
          >
            🎬 보딩패스 {gifOutput.toUpperCase()} 타임랩스 만들기
          </button>

          <button
            type="button"
            className="tl-btn"
            onClick={handleDownloadMapPng}
            disabled={rendering || placedSpots.length < 1}
          >
            🖼️ 동선 요약 포스터(PNG) 저장
          </button>

          {canEdit && itinerary?.id && (
            <button
              type="button"
              className="tl-btn"
              onClick={handleSaveToItinerary}
              disabled={savingToServer || placedSpots.length === 0}
            >
              ☁️ {savingToServer ? '여정에 저장 중...' : '내 여정에 사진·타임랩스 영구 저장'}
            </button>
          )}
        </div>

        {(generatedMedia || itinerary?.timelapseUrl) && (
          <div className="tl-preview-stage">
            <div style={{ marginBottom: '10px', fontWeight: 800, fontSize: '0.9rem' }}>
              ✈️ 완성된 보딩패스 타임랩스 미리보기
            </div>
            {generatedMedia ? (
              generatedMedia.ext === 'gif' ? (
                <img
                  src={generatedMedia.url}
                  alt="보딩패스 타임랩스 GIF"
                  className="tl-preview-media"
                />
              ) : (
                <video
                  src={generatedMedia.url}
                  controls
                  autoPlay
                  loop
                  muted
                  playsInline
                  className="tl-preview-media"
                />
              )
            ) : itinerary?.timelapseUrl?.endsWith('.mp4') ||
              itinerary?.timelapseUrl?.endsWith('.webm') ? (
              <video
                src={itinerary.timelapseUrl}
                controls
                autoPlay
                loop
                muted
                playsInline
                className="tl-preview-media"
              />
            ) : (
              <img
                src={itinerary.timelapseUrl}
                alt="저장된 보딩패스 타임랩스"
                className="tl-preview-media"
              />
            )}

            {generatedMedia && (
              <div className="tl-action-row" style={{ justifyContent: 'center', marginTop: '12px' }}>
                <button
                  type="button"
                  className="tl-btn tl-btn-primary"
                  onClick={() =>
                    saveToGallery(
                      generatedMedia.blob,
                      `tripgather-boarding-pass-${Date.now()}.${generatedMedia.ext}`
                    )
                  }
                >
                  💾 기기에 {generatedMedia.ext.toUpperCase()} 저장 / 공유하기
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default TravelTimelapseStudio;
