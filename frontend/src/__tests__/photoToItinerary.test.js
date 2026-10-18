import { describe, it, expect } from 'vitest';
import {
  toHalfHourSlot,
  inferRegionFromCoords,
  clusterNearbySpots,
  buildItineraryDraftFromSpots
} from '../utils/photoToItineraryBuilder';

describe('photoToItineraryBuilder 유틸리티 단위 테스트 (#29)', () => {
  it('촬영 시각(Date)을 ItineraryEditorPage 슬라이더 규격(HH:00 또는 HH:30)의 도착·출발 시각으로 변환한다', () => {
    const dt1 = new Date(2026, 9, 10, 9, 12, 0); // 09:12 -> 09:00 ~ 10:00
    expect(toHalfHourSlot(dt1)).toEqual({ startTime: '09:00', endTime: '10:00' });

    const dt2 = new Date(2026, 9, 10, 14, 38, 0); // 14:38 -> 14:30 ~ 15:30
    expect(toHalfHourSlot(dt2)).toEqual({ startTime: '14:30', endTime: '15:30' });

    const dtLate = new Date(2026, 9, 10, 23, 25, 0); // 23:25 -> 23:30 ~ 23:30 (상한 캡)
    expect(toHalfHourSlot(dtLate)).toEqual({ startTime: '23:30', endTime: '23:30' });
  });

  it('위경도 좌표로부터 주요 국내외 여행 권역(region)을 추정한다', () => {
    expect(inferRegionFromCoords(33.489, 126.498)).toBe('제주도');
    expect(inferRegionFromCoords(37.5665, 126.978)).toBe('서울');
    expect(inferRegionFromCoords(35.1587, 129.1604)).toBe('부산');
    expect(inferRegionFromCoords(34.6937, 135.5023)).toBe('오사카');
  });

  it('반경 및 시간 임계값 이내에 연속 촬영된 인접 사진들을 하나의 대표 경유지로 클러스터링한다 (Immutability 보장)', () => {
    const t0 = new Date(2026, 9, 10, 10, 0, 0).getTime();
    const spots = [
      { uid: 's1', name: '성산일출봉 입구', lat: 33.4580, lng: 126.9426, timeVal: t0, date: new Date(t0) },
      { uid: 's2', name: '성산일출봉 정상', lat: 33.4583, lng: 126.9428, timeVal: t0 + 15 * 60000, date: new Date(t0 + 15 * 60000) },
      { uid: 's3', name: '섭지코지', lat: 33.4242, lng: 126.9311, timeVal: t0 + 120 * 60000, date: new Date(t0 + 120 * 60000) }
    ];
    const snapshot = JSON.stringify(spots);

    const clustered = clusterNearbySpots(spots, 0.2, 45);
    expect(JSON.stringify(spots)).toBe(snapshot);
    expect(clustered).toHaveLength(2);
    expect(clustered[0].uid).toBe('s1');
    expect(clustered[0].mergedCount).toBe(2);
    expect(clustered[1].uid).toBe('s3');
  });

  it('다수의 촬영 사진으로부터 일자별(Day 1, Day 2...) RoutePoint 배열과 startDate/endDate/region 초안을 생성한다', () => {
    const spots = [
      {
        uid: 'd1-1',
        name: '제주국제공항',
        lat: 33.5065,
        lng: 126.493,
        date: new Date(2026, 9, 10, 10, 5, 0),
        timeVal: new Date(2026, 9, 10, 10, 5, 0).getTime(),
        remotePhotoUrl: '/uploads/p1.jpg'
      },
      {
        uid: 'd1-2',
        name: '함덕해수욕장',
        lat: 33.5434,
        lng: 126.6697,
        date: new Date(2026, 9, 10, 14, 20, 0),
        timeVal: new Date(2026, 9, 10, 14, 20, 0).getTime(),
        remotePhotoUrl: '/uploads/p2.jpg'
      },
      {
        uid: 'd2-1',
        name: '서귀포 올레시장',
        lat: 33.2497,
        lng: 126.563,
        date: new Date(2026, 9, 11, 11, 40, 0),
        timeVal: new Date(2026, 9, 11, 11, 40, 0).getTime(),
        remotePhotoUrl: '/uploads/p3.jpg'
      }
    ];

    const draft = buildItineraryDraftFromSpots(spots, { clusterNearby: false });

    expect(draft.startDate).toBe('2026-10-10');
    expect(draft.endDate).toBe('2026-10-11');
    expect(draft.region).toBe('제주도');
    expect(draft.suggestedTitle).toContain('제주도');
    expect(draft.routePoints).toHaveLength(3);

    expect(draft.routePoints[0]).toMatchObject({
      dayNumber: 1,
      dayLabel: 'Day 1',
      sequenceOrder: 1,
      label: '제주국제공항',
      startTime: '10:00',
      endTime: '11:00',
      photoUrl: '/uploads/p1.jpg'
    });

    expect(draft.routePoints[1]).toMatchObject({
      dayNumber: 1,
      dayLabel: 'Day 1',
      sequenceOrder: 2,
      label: '함덕해수욕장',
      startTime: '14:30',
      endTime: '15:30',
      photoUrl: '/uploads/p2.jpg'
    });

    expect(draft.routePoints[2]).toMatchObject({
      dayNumber: 2,
      dayLabel: 'Day 2',
      sequenceOrder: 1,
      label: '서귀포 올레시장',
      startTime: '11:30',
      endTime: '12:30',
      photoUrl: '/uploads/p3.jpg'
    });
  });
});
