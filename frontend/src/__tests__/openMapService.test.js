import { describe, it, expect } from 'vitest';
import {
  OPEN_MAP_TILES,
  normalizeRoutePointsForMap,
  calculateTotalRouteKm,
  resolveFallbackCoordinates,
  getRecommendedSpotsForRegion
} from '../utils/openMapService';

describe('openMapService (오픈소스 여행 동선 지도 유틸)', () => {
  it('오픈소스 타일 프리셋(voyager, osm, satellite, dark)을 제공한다', () => {
    expect(OPEN_MAP_TILES.voyager).toBeDefined();
    expect(OPEN_MAP_TILES.osm).toBeDefined();
    expect(OPEN_MAP_TILES.satellite).toBeDefined();
    expect(OPEN_MAP_TILES.dark).toBeDefined();
  });

  it('경유지 배열을 일자(dayNumber) 및 순서(sequenceOrder) 기준으로 정규화하고 총 이동거리를 계산한다', () => {
    const rawPoints = [
      { id: 2, dayNumber: 1, sequenceOrder: 2, label: '성산일출봉', lat: 33.458, lng: 126.9426 },
      { id: 1, dayNumber: 1, sequenceOrder: 1, label: '제주국제공항', lat: 33.5065, lng: 126.493 },
      { id: 3, dayNumber: 2, sequenceOrder: 1, label: '서귀포 올레시장', lat: 33.2497, lng: 126.563 }
    ];

    const normalized = normalizeRoutePointsForMap(rawPoints);
    expect(normalized).toHaveLength(3);
    expect(normalized[0].label).toBe('제주국제공항');
    expect(normalized[0].globalOrder).toBe(1);
    expect(normalized[1].label).toBe('성산일출봉');
    expect(normalized[1].globalOrder).toBe(2);
    expect(normalized[2].dayNumber).toBe(2);
    expect(normalized[2].globalOrder).toBe(3);

    const totalKm = calculateTotalRouteKm(normalized);
    expect(totalKm).toBeGreaterThan(70);
  });

  it('좌표가 없는 장소명이나 지역명에 대해 내장 한국 주요 랜드마크 좌표를 반환한다', () => {
    const jeju = resolveFallbackCoordinates('제주국제공항');
    expect(jeju.lat).toBeCloseTo(33.5065, 2);
    expect(jeju.lng).toBeCloseTo(126.493, 2);

    const gangnam = resolveFallbackCoordinates('강남구 모임');
    expect(gangnam.lat).toBeCloseTo(37.4979, 2);
  });

  it('여행 지역명에 맞는 오픈소스 추천 명소 목록을 반환한다', () => {
    const jejuSpots = getRecommendedSpotsForRegion('제주도');
    expect(jejuSpots.length).toBeGreaterThanOrEqual(4);
    expect(jejuSpots[0]).toHaveProperty('name');
    expect(jejuSpots[0]).toHaveProperty('lat');
    expect(jejuSpots[0]).toHaveProperty('lng');
  });
});
