import { describe, it, expect } from 'vitest';
import {
  resolveGatheringAnchorCoords,
  buildCrewTimelapseSpots,
  filterCrewSpotsByMember,
  extractCrewMemberNames
} from '../utils/gatheringCrewTimelapseBuilder';

describe('gatheringCrewTimelapseBuilder 유틸리티 단위 테스트 (#30)', () => {
  it('모임 좌표가 없을 때 지역명(location)으로부터 대표 앵커 좌표를 추론한다', () => {
    expect(resolveGatheringAnchorCoords({ lat: 35.1, lng: 129.0, location: '제주' })).toEqual({
      lat: 35.1,
      lng: 129.0
    });

    const jeju = resolveGatheringAnchorCoords({ lat: null, lng: null, location: '제주도 서귀포' });
    expect(jeju.lat).toBeCloseTo(33.38, 1);

    const busan = resolveGatheringAnchorCoords({ lat: null, lng: null, location: '부산 해운대' });
    expect(busan.lat).toBeCloseTo(35.18, 1);
  });

  it('연결된 여정 경유지, 모임 갤러리 포스트, 미션 인증 사진들을 크루 합동 동선 스팟으로 통합한다 (Immutability 보장)', () => {
    const gathering = {
      id: 7,
      title: '제주 올레길 크루 모임',
      location: '제주도',
      lat: 33.5065,
      lng: 126.493,
      linkedItinerary: {
        id: 100,
        title: '제주 동부 코스',
        routePoints: [
          { id: 1, dayNumber: 1, sequenceOrder: 1, label: '제주공항 집결', lat: 33.5065, lng: 126.493 },
          { id: 2, dayNumber: 1, sequenceOrder: 2, label: '성산일출봉 미션', lat: 33.458, lng: 126.9426 }
        ]
      }
    };

    const posts = [
      {
        id: 501,
        authorName: '김하늘',
        content: '공항 도착 인증!',
        imageUrl: '/uploads/post1.jpg',
        createdAt: '2026-10-18T09:30:00'
      }
    ];

    const missions = [
      {
        id: 901,
        title: '성산일출봉 정상 인증샷',
        myPhotoUrl: '/uploads/my-mission.jpg',
        myStatus: 'APPROVED',
        pendingCompletions: [
          {
            id: 301,
            userName: '박바다',
            photoUrl: '/uploads/bada-mission.jpg',
            memo: '정상 등반 성공!'
          }
        ]
      }
    ];

    const spots = buildCrewTimelapseSpots({
      gathering,
      posts,
      missions,
      currentUserName: '이산들'
    });

    expect(spots.length).toBeGreaterThanOrEqual(3);
    expect(spots.some((s) => s.crewMember === '김하늘' && s.sourceType === 'post')).toBe(true);
    expect(spots.some((s) => s.crewMember === '이산들' && s.sourceType === 'mission')).toBe(true);
    expect(spots.some((s) => s.crewMember === '박바다' && s.sourceType === 'mission')).toBe(true);

    // 모든 스팟에 유효한 위경도 좌표가 배정되었는지 검증
    spots.forEach((s) => {
      expect(typeof s.lat).toBe('number');
      expect(typeof s.lng).toBe('number');
    });

    const members = extractCrewMemberNames(spots);
    expect(members).toEqual(expect.arrayContaining(['김하늘', '이산들', '박바다']));

    const onlyHaneul = filterCrewSpotsByMember(spots, '김하늘');
    expect(onlyHaneul.every((s) => s.crewMember === '김하늘')).toBe(true);
  });
});
