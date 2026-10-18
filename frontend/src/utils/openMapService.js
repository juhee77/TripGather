/**
 * 오픈소스 지도(Leaflet + OpenStreetMap / Carto / Esri + Nominatim) 공통 유틸리티
 * - 유료 API 키(Kakao/Google 등) 없이도 모든 여행 동선 지도, 모임 탐색 지도, 장소 검색이 즉시 동작하도록 지원합니다.
 */
import { calcDistanceKm } from './photoExifParser';

export const OPEN_MAP_TILES = {
  voyager: {
    key: 'voyager',
    label: '🗺️ 일반 상세지도',
    url: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
    attribution: '&copy; OpenStreetMap contributors'
  },
  satellite: {
    key: 'satellite',
    label: '🛰️ 위성 지도',
    url: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
    attribution: 'Tiles &copy; Esri'
  },
  osm: {
    key: 'osm',
    label: '⛰️ 지형·투어맵',
    url: 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}',
    attribution: 'Tiles &copy; Esri'
  },
  dark: {
    key: 'dark',
    label: '🌙 다크 보딩패스',
    url: 'https://server.arcgisonline.com/ArcGIS/rest/services/Canvas/World_Dark_Gray_Base/MapServer/tile/{z}/{y}/{x}',
    attribution: 'Tiles &copy; Esri'
  }
};

export const DAY_COLORS = [
  '#FF5C00', // Day 1: Primary Orange
  '#0EA5E9', // Day 2: Sky Blue
  '#10B981', // Day 3: Emerald
  '#8B5CF6', // Day 4: Violet
  '#EC4899', // Day 5: Pink
  '#F59E0B'  // Day 6+: Amber
];

export function getDayColor(dayNumber = 1) {
  const idx = Math.max(0, (Number(dayNumber) || 1) - 1) % DAY_COLORS.length;
  return DAY_COLORS[idx];
}

// 내장 주요 도시·명소 좌표 사전 (오프라인 및 즉시 검색 폴백용)
export const KOREA_LANDMARKS = [
  { name: '제주국제공항', category: '공항/교통', region: '제주도', lat: 33.5065, lng: 126.493, desc: '제주 여행의 설레는 첫 체크인 게이트' },
  { name: '함덕 서우봉 해변', category: '해변/자연', region: '제주도', lat: 33.5434, lng: 126.6697, desc: '에메랄드빛 바다와 서우봉 산책로' },
  { name: '성산일출봉', category: '자연/명소', region: '제주도', lat: 33.458, lng: 126.9426, desc: '유네스코 세계자연유산 일출 명소' },
  { name: '섭지코지', category: '자연/산책', region: '제주도', lat: 33.4242, lng: 126.9311, desc: '푸른 바다와 기암괴석이 어우러진 해안 절경' },
  { name: '서귀포 매일올레시장', category: '맛집/시장', region: '제주도', lat: 33.2497, lng: 126.563, desc: '흑돼지 강정·귤하르방 등 먹거리 천국' },
  { name: '카멜리아힐', category: '정원/포토스팟', region: '제주도', lat: 33.2896, lng: 126.3701, desc: '사계절 꽃과 동백나무가 가득한 수목원' },
  { name: '애월 한담해안산책로', category: '카페/노을', region: '제주도', lat: 33.4628, lng: 126.3106, desc: '오션뷰 카페거리와 황금빛 일몰 코스' },
  { name: '협재 해수욕장', category: '해변/자연', region: '제주도', lat: 33.3943, lng: 126.2397, desc: '비양도가 한눈에 보이는 투명한 바다' },
  { name: '우도 산호해변', category: '섬/자연', region: '제주도', lat: 33.5043, lng: 126.9438, desc: '땅콩 아이스크림과 해안 자전거 일주' },
  { name: '해운대 해수욕장', category: '해변/명소', region: '부산', lat: 35.1587, lng: 129.1604, desc: '부산 대표 해변과 해리단길 투어' },
  { name: '광안리 해변 & 광안대교', category: '야경/해변', region: '부산', lat: 35.1532, lng: 129.1186, desc: '광안대교 야경과 오션뷰 다이닝' },
  { name: '감천문화마을', category: '포토스팟', region: '부산', lat: 35.0975, lng: 129.0106, desc: '어린왕자 포토존과 파스텔톤 골목길' },
  { name: '해동용궁사', category: '명소/사찰', region: '부산', lat: 35.1884, lng: 129.2233, desc: '바다 절벽 위에 자리한 아름다운 수상 사찰' },
  { name: '흰여울문화마을', category: '산책/카페', region: '부산', lat: 35.0782, lng: 129.0452, desc: '영도 바다를 따라 걷는 감성 해안 골목' },
  { name: '강남역 스퀘어', category: '모임/핫플', region: '서울 강남구', lat: 37.4979, lng: 127.0276, desc: '교통이 편리한 강남 중심 모임 집결지' },
  { name: '성수동 연무장길 카페거리', category: '카페/팝업', region: '서울 성동구', lat: 37.5446, lng: 127.056, desc: '트렌디한 팝업스토어와 로스터리 카페' },
  { name: '서울숲 공원', category: '공원/피크닉', region: '서울 성동구', lat: 37.5444, lng: 127.0374, desc: '도심 속 푸른 산책로와 피크닉 명소' },
  { name: '홍대입구 경의선숲길', category: '모임/산책', region: '서울 마포구', lat: 37.5575, lng: 126.9245, desc: '연트럴파크 피크닉과 개성 넘치는 맛집' },
  { name: '여의도 한강공원', category: '피크닉/야경', region: '서울 영등포구', lat: 37.5284, lng: 126.9328, desc: '한강 노을 피크닉과 야시장 코스' },
  { name: '반포 한강공원 달빛무지개분수', category: '야경/피크닉', region: '서울 서초구', lat: 37.5104, lng: 126.996, desc: '무지개분수 야경과 한강 산책' },
  { name: '경복궁 & 서촌 한옥거리', category: '역사/산책', region: '서울 종로구', lat: 37.5796, lng: 126.977, desc: '고궁 산책과 한옥 골목 갤러리 투어' },
  { name: '남산서울타워', category: '전망/야경', region: '서울 용산구', lat: 37.5512, lng: 126.9882, desc: '서울 파노라마 전경과 남산 둘레길' },
  { name: '잠실 롯데월드타워 & 석촌호수', category: '명소/산책', region: '서울 송파구', lat: 37.5126, lng: 127.1025, desc: '석촌호수 산책과 전망대 투어' },
  { name: '경주 황리단길 & 첨성대', category: '역사/핫플', region: '경주', lat: 35.8347, lng: 129.219, desc: '한옥 카페거리와 신라 유적 야경 산책' },
  { name: '경주 동궁과 월지', category: '야경/역사', region: '경주', lat: 35.8348, lng: 129.2266, desc: '연못에 비치는 환상적인 신라 왕궁 야경' },
  { name: '강릉 경포해변 & 안목해변 카페거리', category: '해변/카페', region: '강릉', lat: 37.7726, lng: 128.9472, desc: '동해 바다를 바라보며 즐기는 핸드드립 커피' },
  { name: '속초 관광수산시장 & 영금정', category: '시장/해변', region: '속초', lat: 38.2045, lng: 128.5902, desc: '속초 중앙시장 먹거리와 동해 일출 명소' },
  { name: '전주 한옥마을', category: '한옥/미식', region: '전주', lat: 35.8147, lng: 127.1526, desc: '전통 한옥 골목과 전주 미식 투어' },
  { name: '여수 이순신광장 & 낭만포차거리', category: '야경/미식', region: '여수', lat: 34.7397, lng: 127.7357, desc: '여수 밤바다 야경과 해산물 포차 거리' }
];

let leafletLoadPromise = null;

/**
 * Leaflet(L) 글로벌 객체가 준비되도록 보장합니다.
 */
export function ensureLeafletLoaded() {
  if (typeof window === 'undefined') return Promise.resolve(null);
  if (window.L) return Promise.resolve(window.L);
  if (leafletLoadPromise) return leafletLoadPromise;

  leafletLoadPromise = new Promise((resolve, reject) => {
    if (!document.getElementById('leaflet-css-cdn')) {
      const link = document.createElement('link');
      link.id = 'leaflet-css-cdn';
      link.rel = 'stylesheet';
      link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
      document.head.appendChild(link);
    }
    const script = document.createElement('script');
    script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';
    script.async = true;
    script.onload = () => resolve(window.L);
    script.onerror = (err) => {
      leafletLoadPromise = null;
      reject(err);
    };
    document.head.appendChild(script);
  });

  return leafletLoadPromise;
}

/**
 * 텍스트(장소명/지역명)로부터 기본 좌표를 추론합니다.
 */
export function resolveFallbackCoordinates(text = '') {
  const clean = String(text || '').trim();
  if (!clean) return { lat: 37.5665, lng: 126.978, matched: false };

  const found = KOREA_LANDMARKS.find(
    (item) =>
      clean.includes(item.name) ||
      item.name.includes(clean) ||
      (clean.length >= 2 && item.region.includes(clean.slice(0, 2)))
  );
  if (found) {
    return { lat: found.lat, lng: found.lng, matched: true, label: found.name };
  }

  const regionKeywords = [
    { kw: '제주', lat: 33.489, lng: 126.498 },
    { kw: '서귀포', lat: 33.2497, lng: 126.563 },
    { kw: '부산', lat: 35.1587, lng: 129.1604 },
    { kw: '해운대', lat: 35.1587, lng: 129.1604 },
    { kw: '강남', lat: 37.4979, lng: 127.0276 },
    { kw: '서초', lat: 37.4919, lng: 127.0076 },
    { kw: '송파', lat: 37.5126, lng: 127.1025 },
    { kw: '마포', lat: 37.5575, lng: 126.9245 },
    { kw: '홍대', lat: 37.5575, lng: 126.9245 },
    { kw: '용산', lat: 37.534, lng: 126.994 },
    { kw: '성동', lat: 37.5446, lng: 127.056 },
    { kw: '성수', lat: 37.5446, lng: 127.056 },
    { kw: '종로', lat: 37.5796, lng: 126.977 },
    { kw: '한강', lat: 37.5284, lng: 126.9328 },
    { kw: '경주', lat: 35.8347, lng: 129.219 },
    { kw: '강릉', lat: 37.7726, lng: 128.9472 },
    { kw: '속초', lat: 38.2045, lng: 128.5902 },
    { kw: '전주', lat: 35.8147, lng: 127.1526 },
    { kw: '여수', lat: 34.7397, lng: 127.7357 },
    { kw: '도쿄', lat: 35.6762, lng: 139.6503 },
    { kw: '오사카', lat: 34.6937, lng: 135.5023 },
    { kw: '방콕', lat: 13.7563, lng: 100.5018 },
    { kw: '파리', lat: 48.8566, lng: 2.3522 }
  ];

  for (const r of regionKeywords) {
    if (clean.includes(r.kw)) {
      return { lat: r.lat, lng: r.lng, matched: true, label: clean };
    }
  }

  return { lat: 37.5665, lng: 126.978, matched: false };
}

/**
 * 여정 routePoints 배열을 일자·순서 순으로 정렬하고 지도에 표시 가능한 형태로 정규화합니다.
 */
export function normalizeRoutePointsForMap(routePoints = []) {
  if (!Array.isArray(routePoints)) return [];

  const sorted = [...routePoints].sort((a, b) => {
    const dayDiff = (Number(a.dayNumber) || 1) - (Number(b.dayNumber) || 1);
    if (dayDiff !== 0) return dayDiff;
    return (Number(a.sequenceOrder) || 0) - (Number(b.sequenceOrder) || 0);
  });

  const result = [];
  for (let i = 0; i < sorted.length; i++) {
    const p = sorted[i];
    let lat = p.lat != null && p.lat !== '' ? Number(p.lat) : null;
    let lng = p.lng != null && p.lng !== '' ? Number(p.lng) : null;

    if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
      const fb = resolveFallbackCoordinates(p.label || '');
      if (fb.matched) {
        lat = fb.lat;
        lng = fb.lng;
      } else {
        continue;
      }
    }

    const prev = result[result.length - 1];
    const segmentKm = prev ? calcDistanceKm(prev.lat, prev.lng, lat, lng) : 0;

    result.push({
      ...p,
      dayNumber: Number(p.dayNumber) || 1,
      sequenceOrder: Number(p.sequenceOrder) || i + 1,
      globalOrder: result.length + 1,
      lat,
      lng,
      segmentKm
    });
  }

  return result;
}

/**
 * 정규화된 경유지 배열의 총 이동거리(km)를 계산합니다.
 */
export function calculateTotalRouteKm(points = []) {
  if (!Array.isArray(points) || points.length < 2) return 0;
  let total = 0;
  for (let i = 1; i < points.length; i++) {
    total += calcDistanceKm(
      Number(points[i - 1].lat),
      Number(points[i - 1].lng),
      Number(points[i].lat),
      Number(points[i].lng)
    );
  }
  return Number(total.toFixed(1));
}

/**
 * 오픈소스 OpenStreetMap Nominatim API + 내장 랜드마크 사전을 결합한 장소 검색
 */
export async function searchOpenStreetMapPlaces(query) {
  const q = String(query || '').trim();
  if (!q) return [];

  const localMatches = KOREA_LANDMARKS.filter(
    (item) =>
      item.name.toLowerCase().includes(q.toLowerCase()) ||
      item.region.toLowerCase().includes(q.toLowerCase()) ||
      item.category.toLowerCase().includes(q.toLowerCase())
  ).map((item, idx) => ({
    id: `local-${idx}-${item.name}`,
    name: item.name,
    address: `${item.region} · ${item.category}`,
    lat: item.lat,
    lng: item.lng,
    source: 'curated'
  }));

  try {
    const url = `https://nominatim.openstreetmap.org/search?format=json&limit=6&accept-language=ko&q=${encodeURIComponent(q)}`;
    const res = await fetch(url, {
      headers: { Accept: 'application/json' }
    });
    if (!res.ok) return localMatches;
    const data = await res.json();
    const remoteMatches = (Array.isArray(data) ? data : []).map((row) => {
      const parts = String(row.display_name || '').split(',').map((s) => s.trim());
      return {
        id: `osm-${row.place_id}`,
        name: row.name || parts[0] || q,
        address: parts.slice(0, 4).join(' '),
        lat: Number(row.lat),
        lng: Number(row.lon),
        source: 'osm'
      };
    });

    const seen = new Set(localMatches.map((m) => m.name));
    const merged = [...localMatches];
    for (const r of remoteMatches) {
      if (!seen.has(r.name) && Number.isFinite(r.lat) && Number.isFinite(r.lng)) {
        seen.add(r.name);
        merged.push(r);
      }
    }
    return merged.slice(0, 8);
  } catch {
    return localMatches;
  }
}

/**
 * 여행 목적지/지역명에 어울리는 추천 명소 목록을 반환합니다.
 */
export function getRecommendedSpotsForRegion(regionText = '') {
  const clean = String(regionText || '').trim();
  if (clean) {
    const matched = KOREA_LANDMARKS.filter(
      (spot) =>
        spot.region.includes(clean) ||
        clean.includes(spot.region) ||
        (clean.includes('제주') && spot.region.includes('제주')) ||
        (clean.includes('부산') && spot.region.includes('부산')) ||
        (clean.includes('서울') && spot.region.includes('서울'))
    );
    if (matched.length >= 3) return matched;
  }
  return KOREA_LANDMARKS.slice(0, 8);
}
