// TripGather — 여행 사진 EXIF 기반 여정(Itinerary) 코스 자동 생성 빌더 (#29)

import { calcDistanceKm, sortSpots } from './photoExifParser';

const KNOWN_REGIONS = [
  { name: '제주도', lat: 33.38, lng: 126.55, radiusKm: 55 },
  { name: '서울', lat: 37.5665, lng: 126.978, radiusKm: 28 },
  { name: '부산', lat: 35.1796, lng: 129.0756, radiusKm: 32 },
  { name: '강릉·속초', lat: 37.9, lng: 128.82, radiusKm: 42 },
  { name: '경주', lat: 35.8562, lng: 129.2247, radiusKm: 28 },
  { name: '여수·순천', lat: 34.7604, lng: 127.6622, radiusKm: 35 },
  { name: '전주', lat: 35.8242, lng: 127.148, radiusKm: 25 },
  { name: '인천', lat: 37.4563, lng: 126.7052, radiusKm: 25 },
  { name: '대전', lat: 36.3504, lng: 127.3845, radiusKm: 25 },
  { name: '대구', lat: 35.8714, lng: 128.6014, radiusKm: 25 },
  { name: '오사카', lat: 34.6937, lng: 135.5023, radiusKm: 45 },
  { name: '도쿄', lat: 35.6762, lng: 139.6503, radiusKm: 50 },
  { name: '후쿠오카', lat: 33.5902, lng: 130.4017, radiusKm: 40 },
  { name: '방콕', lat: 13.7563, lng: 100.5018, radiusKm: 45 },
  { name: '다낭', lat: 16.0544, lng: 108.2022, radiusKm: 40 },
  { name: '타이베이', lat: 25.033, lng: 121.5654, radiusKm: 40 },
  { name: '파리', lat: 48.8566, lng: 2.3522, radiusKm: 40 }
];

/**
 * 촬영 시각(Date)을 ItineraryEditorPage 슬라이더 규격(00:00~23:30, 30분 단위)의 startTime·endTime으로 변환합니다.
 */
export function toHalfHourSlot(dateInput) {
  const dt = dateInput instanceof Date ? dateInput : new Date(dateInput);
  if (isNaN(dt.getTime())) {
    return { startTime: '12:00', endTime: '13:00' };
  }
  const totalMinutes = dt.getHours() * 60 + dt.getMinutes();
  const startHalfIdx = Math.min(47, Math.max(0, Math.round(totalMinutes / 30)));
  const endHalfIdx = Math.min(47, startHalfIdx + 2); // 기본 +1시간 체류

  const formatSlot = (idx) => {
    const h = Math.floor(idx / 2);
    const m = idx % 2 === 0 ? '00' : '30';
    return `${String(h).padStart(2, '0')}:${m}`;
  };

  return {
    startTime: formatSlot(startHalfIdx),
    endTime: formatSlot(endHalfIdx)
  };
}

/**
 * 날짜 객체를 로컬 YYYY-MM-DD 문자열로 변환합니다.
 */
export function toLocalDateString(dateInput) {
  const dt = dateInput instanceof Date ? dateInput : new Date(dateInput);
  if (isNaN(dt.getTime())) return '';
  const yyyy = dt.getFullYear();
  const mm = String(dt.getMonth() + 1).padStart(2, '0');
  const dd = String(dt.getDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
}

/**
 * 위경도 좌표로부터 가장 가까운 대표 여행 권역(region)을 추정합니다.
 */
export function inferRegionFromCoords(lat, lng) {
  if (lat == null || lng == null) return '';
  let closest = null;
  let minDist = Infinity;
  for (const reg of KNOWN_REGIONS) {
    const d = calcDistanceKm(Number(lat), Number(lng), reg.lat, reg.lng);
    if (d <= reg.radiusKm && d < minDist) {
      minDist = d;
      closest = reg.name;
    }
  }
  return closest || '';
}

/**
 * 반경(radiusKm) 및 시간(maxMinutes) 이내에 연속 촬영된 인접 사진들을 하나의 대표 경유지로 묶습니다. (Immutable)
 */
export function clusterNearbySpots(spots, radiusKm = 0.15, maxMinutes = 45) {
  if (!Array.isArray(spots) || spots.length === 0) return [];
  const ordered = sortSpots(spots, 'time-asc');
  const clusters = [];

  for (const spot of ordered) {
    const copy = { ...spot, mergedCount: spot.mergedCount || 1 };
    if (clusters.length === 0) {
      clusters.push(copy);
      continue;
    }
    const prev = clusters[clusters.length - 1];
    const dist =
      prev.lat != null && spot.lat != null
        ? calcDistanceKm(prev.lat, prev.lng, spot.lat, spot.lng)
        : Infinity;
    const diffMin = Math.abs((spot.timeVal || 0) - (prev.timeVal || 0)) / 60000;

    if (dist <= radiusKm && diffMin <= maxMinutes) {
      clusters[clusters.length - 1] = {
        ...prev,
        mergedCount: (prev.mergedCount || 1) + 1
      };
    } else {
      clusters.push(copy);
    }
  }

  return clusters;
}

/**
 * 촬영 지점 배열로부터 여정(Itinerary) 폼 초안(startDate, endDate, region, suggestedTitle, routePoints)을 생성합니다. (Immutable)
 */
export function buildItineraryDraftFromSpots(spots, options = {}) {
  const { clusterNearby = false, radiusKm = 0.15, maxMinutes = 45 } = options;
  const validSpots = (spots || []).filter((s) => s && s.lat != null && s.lng != null);
  if (validSpots.length === 0) {
    return {
      startDate: '',
      endDate: '',
      region: '',
      suggestedTitle: '',
       suggestedDescription: '',
      routePoints: []
    };
  }

  const ordered = clusterNearby
    ? clusterNearbySpots(validSpots, radiusKm, maxMinutes)
    : sortSpots(validSpots, 'time-asc');

  // 일자(YYYY-MM-DD)별 그룹화
  const dateKeys = [];
  const dayBuckets = new Map();

  ordered.forEach((spot) => {
    const dt = spot.date instanceof Date ? spot.date : new Date(spot.timeVal || Date.now());
    const dateKey = toLocalDateString(dt) || 'day-1';
    if (!dayBuckets.has(dateKey)) {
      dayBuckets.set(dateKey, []);
      dateKeys.push(dateKey);
    }
    dayBuckets.get(dateKey).push({ ...spot, resolvedDate: dt });
  });

  dateKeys.sort();
  const startDate = dateKeys[0] !== 'day-1' ? dateKeys[0] : '';
  const endDate = dateKeys[dateKeys.length - 1] !== 'day-1' ? dateKeys[dateKeys.length - 1] : '';

  const routePoints = [];
  dateKeys.forEach((dateKey, dIdx) => {
    const dayNumber = dIdx + 1;
    const daySpots = dayBuckets.get(dateKey) || [];
    daySpots.forEach((spot, sIdx) => {
      const { startTime, endTime } = toHalfHourSlot(spot.resolvedDate);
      const cleanLabel =
        spot.placeLabel ||
        (spot.name && !/^(IMG|DSC|PXL|PHOTO|사진)[_-]?\d+$/i.test(spot.name.trim())
          ? spot.name.trim()
          : `Day ${dayNumber} 방문지 #${sIdx + 1}`);

      const memoParts = [];
      if (spot.mergedCount && spot.mergedCount > 1) {
        memoParts.push(`📸 현장 사진 ${spot.mergedCount}장 클러스터링 지점`);
      }

      routePoints.push({
        dayNumber,
        dayLabel: `Day ${dayNumber}`,
        sequenceOrder: sIdx + 1,
        label: cleanLabel,
        lat: Number(spot.lat),
        lng: Number(spot.lng),
        startTime,
        endTime,
        visitTime: startTime,
        photoUrl: spot.remotePhotoUrl || spot.photoUrl || spot.thumbUrl || null,
        takenAt:
          spot.resolvedDate && !isNaN(spot.resolvedDate.getTime())
            ? spot.resolvedDate.toISOString().slice(0, 19)
            : null,
        memo: memoParts.join(' · ')
      });
    });
  });

  const region =
    ordered.map((s) => s.regionName || inferRegionFromCoords(s.lat, s.lng)).find(Boolean) ||
    '나만의 여행지';
  const daySpanText =
    dateKeys.length > 1 ? `${dateKeys.length - 1}박 ${dateKeys.length}일` : '당일치기';
  const suggestedTitle = `${region} ${daySpanText} 포토 보딩패스 코스 (${routePoints.length}곳)`;
  const suggestedDescription = `여행 사진 ${validSpots.length}장의 EXIF 촬영 시각과 GPS 위치로 자동 생성한 ${region} ${daySpanText} 여정입니다.`;

  return {
    startDate,
    endDate,
    region,
    suggestedTitle,
    suggestedDescription,
    routePoints
  };
}

/**
 * OpenStreetMap Nominatim 역지오코딩으로 사진들의 실제 지명(placeLabel)과 권역(regionName)을 보강합니다. (Immutable)
 */
export async function enrichSpotsWithReverseGeocode(spots) {
  if (!Array.isArray(spots) || spots.length === 0) return [];
  const enriched = [];
  for (let i = 0; i < spots.length; i++) {
    const spot = spots[i];
    if (spot.lat == null || spot.lng == null) {
      enriched.push({ ...spot });
      continue;
    }
    const localRegion = inferRegionFromCoords(spot.lat, spot.lng);
    try {
      const res = await fetch(
        `https://nominatim.openstreetmap.org/reverse?format=json&lat=${spot.lat}&lon=${spot.lng}&zoom=16&accept-language=ko`
      );
      if (res.ok) {
        const data = await res.json();
        const addr = data.address || {};
        const placeName =
          data.name ||
          addr.tourism ||
          addr.attraction ||
          addr.amenity ||
          addr.neighbourhood ||
          addr.suburb ||
          addr.Town ||
          addr.village ||
          spot.name;
        const cityRegion =
          localRegion || addr.city || addr.province || addr.state || addr.county || '';
        enriched.push({
          ...spot,
          name: placeName || spot.name,
          placeLabel: placeName || spot.name,
          regionName: cityRegion
        });
        continue;
      }
    } catch {
      // 오프라인 또는 요청 제한 시 로컬 권역 폴백
    }
    enriched.push({
      ...spot,
      regionName: localRegion
    });
  }
  return enriched;
}
