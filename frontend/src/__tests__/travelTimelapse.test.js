import { describe, it, expect } from 'vitest';
import {
  parseExifDateTime,
  dmsToDecimal,
  calcDistanceKm,
  projectMercator,
  unprojectMercator0,
  sortSpots,
  estimateUnplacedLocations,
  matchUnplacedToRoutePoint,
  autoMatchPhotosToRoutePoints,
  formatTimeDiff
} from '../utils/photoExifParser';
import {
  planTravelGif,
  GIF_LAYOUTS
} from '../utils/boardingPassGifRenderer';

describe('photoExifParser 유틸리티 단위 테스트 (#28)', () => {
  it('EXIF 날짜 문자열(YYYY:MM:DD HH:MM:SS)을 Date 객체로 정확히 파싱한다', () => {
    const dt = parseExifDateTime('2026:10:10 14:30:15');
    expect(dt).toBeInstanceOf(Date);
    expect(dt.getFullYear()).toBe(2026);
    expect(dt.getMonth()).toBe(9); // 0-indexed October
    expect(dt.getDate()).toBe(10);
    expect(dt.getHours()).toBe(14);
    expect(dt.getMinutes()).toBe(30);

    expect(parseExifDateTime('')).toBeNull();
    expect(parseExifDateTime('invalid')).toBeNull();
    expect(parseExifDateTime('1970:01:01 00:00:00')).toBeNull();
  });

  it('DMS(도·분·초) 배열과 방위(N/S/E/W)를 십진수 위경도로 변환한다', () => {
    const lat = dmsToDecimal([33, 30, 36], 'N');
    expect(lat).toBeCloseTo(33.51, 4);

    const southLat = dmsToDecimal([33, 30, 36], 'S');
    expect(southLat).toBeCloseTo(-33.51, 4);

    const westLng = dmsToDecimal([126, 30, 0], 'W');
    expect(westLng).toBeCloseTo(-126.5, 4);
  });

  it('두 위경도 좌표 사이의 Haversine 거리(km)와 메르카토르 투영 역변환을 계산한다', () => {
    // 제주공항 ~ 성산일출봉 약 42km
    const dist = calcDistanceKm(33.5065, 126.4930, 33.4580, 126.9426);
    expect(dist).toBeGreaterThan(40);
    expect(dist).toBeLessThan(45);

    const p = projectMercator(33.5065, 126.4930, 0);
    const back = unprojectMercator0(p.x, p.y);
    expect(back.lat).toBeCloseTo(33.5065, 4);
    expect(back.lng).toBeCloseTo(126.4930, 4);
  });

  it('sortSpots는 원본 배열을 변이시키지 않고(Immutability) 지정된 모드로 정렬한다', () => {
    const spots = [
      { uid: 's1', name: '다혜 해변', lat: 33.5, lng: 126.5, timeVal: 3000 },
      { uid: 's2', name: '가파도', lat: 33.2, lng: 126.2, timeVal: 1000 },
      { uid: 's3', name: '나주곰탕', lat: 33.51, lng: 126.51, timeVal: 2000 }
    ];
    const snapshot = JSON.stringify(spots);

    const asc = sortSpots(spots, 'time-asc');
    expect(asc.map(s => s.uid)).toEqual(['s2', 's3', 's1']);
    expect(JSON.stringify(spots)).toBe(snapshot);

    const desc = sortSpots(spots, 'time-desc');
    expect(desc.map(s => s.uid)).toEqual(['s1', 's3', 's2']);

    const byName = sortSpots(spots, 'name');
    expect(byName.map(s => s.name)).toEqual(['가파도', '나주곰탕', '다혜 해변']);

    const tsp = sortSpots(spots, 'tsp');
    // s1(33.5, 126.5)에서 가장 가까운 곳은 s3(33.51, 126.51), 그 다음 s2(33.2, 126.2)
    expect(tsp.map(s => s.uid)).toEqual(['s1', 's3', 's2']);
  });

  it('위치 미지정 사진을 앞뒤 사진 촬영 시각 비율로 선형 보간해 추정한다', () => {
    const placed = [
      { uid: 'p1', lat: 33.0, lng: 126.0, loc: 'gps', timeVal: 1000, timeFromFile: false },
      { uid: 'p2', lat: 34.0, lng: 127.0, loc: 'gps', timeVal: 3000, timeFromFile: false }
    ];
    const unplaced = [
      { uid: 'u1', name: '중간사진', lat: null, lng: null, loc: 'none', timeVal: 2000, timeFromFile: false },
      { uid: 'u2', name: '파일시간사진', lat: null, lng: null, loc: 'none', timeVal: 2500, timeFromFile: true }
    ];

    const res = estimateUnplacedLocations(placed, unplaced, ['u1', 'u2']);
    expect(res.estimatedCount).toBe(1);
    expect(res.skippedNoTimeCount).toBe(1);
    expect(res.placedSpots).toHaveLength(3);
    expect(res.unplacedSpots).toHaveLength(1);

    const moved = res.placedSpots.find(s => s.uid === 'u1');
    expect(moved.loc).toBe('estimated');
    expect(moved.lat).toBeGreaterThan(33.4);
    expect(moved.lat).toBeLessThan(33.6);
    expect(moved.lng).toBeCloseTo(126.5, 2);
  });

  it('TripGather 고유 기능: 미지정 사진을 내 여정의 RoutePoint 경유지에 원클릭 매칭한다', () => {
    const placed = [];
    const unplaced = [
      { uid: 'u1', name: 'IMG_001', lat: null, lng: null, loc: 'none', timeVal: 1000 }
    ];
    const routePoint = {
      id: 42,
      dayNumber: 2,
      sequenceOrder: 3,
      label: '성산일출봉',
      lat: 33.4580,
      lng: 126.9426,
      startTime: '09:00'
    };

    const res = matchUnplacedToRoutePoint(placed, unplaced, ['u1'], routePoint);
    expect(res.placedSpots).toHaveLength(1);
    expect(res.unplacedSpots).toHaveLength(0);
    expect(res.placedSpots[0].lat).toBe(33.4580);
    expect(res.placedSpots[0].lng).toBe(126.9426);
    expect(res.placedSpots[0].loc).toBe('route');
    expect(res.placedSpots[0].routePointId).toBe(42);
    expect(res.placedSpots[0].dayNumber).toBe(2);
    expect(res.placedSpots[0].name).toBe('성산일출봉');
  });

  it('GPS가 있는 사진이 기존 RoutePoint 반경 내에 있으면 자동으로 경유지 정보를 연결한다', () => {
    const spots = [
      { uid: 's1', name: 'IMG_1023', lat: 33.4582, lng: 126.9425, loc: 'gps', timeVal: 1000 }
    ];
    const routePoints = [
      { id: 10, dayNumber: 1, label: '성산일출봉 정상', lat: 33.4580, lng: 126.9426 }
    ];

    const matched = autoMatchPhotosToRoutePoints(spots, routePoints, 0.5);
    expect(matched[0].routePointId).toBe(10);
    expect(matched[0].dayNumber).toBe(1);
    expect(matched[0].name).toBe('성산일출봉 정상');
  });

  it('시간 차이를 한국어 문자열로 포맷한다', () => {
    expect(formatTimeDiff(30 * 60 * 1000)).toBe('30분 후');
    expect(formatTimeDiff(-90 * 60 * 1000)).toBe('1시간 30분 전');
  });
});

describe('boardingPassGifRenderer 타임랩스 플래너 단위 테스트 (#28)', () => {
  it('정사각·가로·세로 비율에 맞춰 프레임 플랜과 예상 용량·재생 시간을 산출한다', () => {
    const spots = [
      { lat: 33.5065, lng: 126.4930, name: '제주공항' },
      { lat: 33.4580, lng: 126.9426, name: '성산일출봉' },
      { lat: 33.2497, lng: 126.5630, name: '서귀포올레시장' }
    ];

    const squarePlan = planTravelGif(spots, 'square', { quality: 'normal', output: 'gif', camera: 'follow', zoom: 'street' });
    expect(squarePlan.outW).toBe(GIF_LAYOUTS.square.GW);
    expect(squarePlan.outH).toBe(GIF_LAYOUTS.square.GH);
    expect(squarePlan.plan.length).toBeGreaterThan(3);
    expect(squarePlan.seconds).toBeGreaterThan(2);
    expect(squarePlan.estBytes).toBeGreaterThan(100000);

    const storyMp4Plan = planTravelGif(spots, 'story', { quality: 'light', output: 'mp4', camera: 'overview', zoom: 'area' });
    expect(storyMp4Plan.video).toBe(true);
    expect(storyMp4Plan.outW).toBe(Math.round(GIF_LAYOUTS.story.GW * 0.75 / 2) * 2);
    expect(storyMp4Plan.follow).toBe(false);

    // 피날레 프레임(isFinale: true)은 사진 팝업 카드가 지도를 가리지 않도록 cardAlpha === 0 이어야 함
    const finaleFrames = squarePlan.plan.filter((f) => f.isFinale);
    expect(finaleFrames.length).toBeGreaterThan(0);
    finaleFrames.forEach((f) => {
      expect(f.cardAlpha).toBe(0);
    });
  });
});
