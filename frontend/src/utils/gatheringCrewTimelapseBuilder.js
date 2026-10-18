// TripGather — 모임(Gathering) 크루 미션·스냅샷 합동 동선 타임랩스 빌더 (#30)

const LOCATION_ANCHORS = [
  { keyword: '제주', lat: 33.38, lng: 126.55 },
  { keyword: '서귀포', lat: 33.2497, lng: 126.563 },
  { keyword: '서울', lat: 37.5665, lng: 126.978 },
  { keyword: '홍대', lat: 37.5563, lng: 126.9236 },
  { keyword: '강남', lat: 37.4979, lng: 127.0276 },
  { keyword: '부산', lat: 35.1796, lng: 129.0756 },
  { keyword: '해운대', lat: 35.1587, lng: 129.1604 },
  { keyword: '강릉', lat: 37.7519, lng: 128.8761 },
  { keyword: '속초', lat: 38.207, lng: 128.5918 },
  { keyword: '경주', lat: 35.8562, lng: 129.2247 },
  { keyword: '여수', lat: 34.7604, lng: 127.6622 },
  { keyword: '전주', lat: 35.8242, lng: 127.148 },
  { keyword: '오사카', lat: 34.6937, lng: 135.5023 },
  { keyword: '도쿄', lat: 35.6762, lng: 139.6503 },
  { keyword: '후쿠오카', lat: 33.5902, lng: 130.4017 },
  { keyword: '방콕', lat: 13.7563, lng: 100.5018 },
  { keyword: '다낭', lat: 16.0544, lng: 108.2022 }
];

/**
 * 모임 객체의 위경도가 있으면 그대로 반환하고, 없으면 location 텍스트로 대표 좌표를 추론합니다.
 */
export function resolveGatheringAnchorCoords(gathering = {}) {
  if (gathering && gathering.lat != null && gathering.lng != null) {
    return { lat: Number(gathering.lat), lng: Number(gathering.lng) };
  }
  const locText = String(gathering?.location || gathering?.title || '');
  for (const item of LOCATION_ANCHORS) {
    if (locText.includes(item.keyword)) {
      return { lat: item.lat, lng: item.lng };
    }
  }
  return { lat: 33.489, lng: 126.498 }; // 기본 제주 앵커
}

/**
 * 모임의 연결된 여정 경유지, 갤러리 포스트 사진, 미션 인증 사진들을 크루 합동 동선 스팟 배열로 조합합니다. (Immutable)
 */
export function buildCrewTimelapseSpots({
  gathering = {},
  posts = [],
  missions = [],
  currentUserName = '크루',
  includePosts = true,
  includeMissions = true,
  includeItineraryStops = true
} = {}) {
  const anchor = resolveGatheringAnchorCoords(gathering);
  const routePoints = (gathering?.linkedItinerary?.routePoints || []).filter(
    (rp) => rp && rp.lat != null && rp.lng != null
  );

  // 각 항목에 배정할 좌표 생성기 (연결된 여정 경유지가 있으면 순환 배정 + 소폭 오프셋, 없으면 앵커 주변 원형 경로)
  const assignCoord = (seqIdx) => {
    if (routePoints.length > 0) {
      const rp = routePoints[seqIdx % routePoints.length];
      const cycle = Math.floor(seqIdx / routePoints.length);
      const offset = cycle * 0.0022;
      return {
        lat: Number(rp.lat) + offset,
        lng: Number(rp.lng) + offset,
        dayNumber: rp.dayNumber ?? 1,
        routePointId: rp.id ?? null,
        routeLabel: rp.label || null
      };
    }
    const angle = (seqIdx * 65 * Math.PI) / 180;
    const radius = 0.018 + (seqIdx % 4) * 0.008;
    return {
      lat: anchor.lat + Math.sin(angle) * radius,
      lng: anchor.lng + Math.cos(angle) * radius,
      dayNumber: 1,
      routePointId: null,
      routeLabel: null
    };
  };

  const results = [];
  let coordCursor = 0;

  // 1) 연결된 여정의 경유지 중 사진이 있는 지점 또는 기본 경유지
  if (includeItineraryStops && routePoints.length > 0) {
    routePoints.forEach((rp, idx) => {
      if (rp.photoUrl) {
        const dt = rp.takenAt ? new Date(rp.takenAt) : new Date(Date.now() + idx * 1800000);
        results.push({
          uid: `crew-rp-${rp.id || idx}`,
          name: `🎫 ${rp.label || `경유지 #${idx + 1}`}`,
          crewMember: gathering?.host?.name || gathering?.host || '호스트',
          sourceType: 'itinerary',
          lat: Number(rp.lat),
          lng: Number(rp.lng),
          dayNumber: rp.dayNumber ?? 1,
          routePointId: rp.id ?? null,
          date: dt,
          timeVal: dt.getTime(),
          photoUrl: rp.photoUrl,
          dataUrl: rp.photoUrl,
          thumbUrl: rp.photoUrl,
          loc: 'route'
        });
      }
    });
  }

  // 2) 모임 갤러리 스냅샷 포스트 (imageUrl이 있는 게시글)
  if (includePosts && Array.isArray(posts)) {
    posts
      .filter((p) => p && p.imageUrl)
      .forEach((post, idx) => {
        const c = assignCoord(coordCursor++);
        const author = post.authorName || post.author || '크루';
        const dt = post.createdAt ? new Date(post.createdAt) : new Date(Date.now() + idx * 1200000);
        const shortContent = post.content ? String(post.content).trim().slice(0, 18) : '현장 스냅샷';
        results.push({
          uid: `crew-post-${post.id || idx}`,
          name: `📸 ${author} · ${shortContent}`,
          crewMember: author,
          sourceType: 'post',
          lat: post.lat != null ? Number(post.lat) : c.lat,
          lng: post.lng != null ? Number(post.lng) : c.lng,
          dayNumber: c.dayNumber,
          routePointId: c.routePointId,
          date: dt,
          timeVal: dt.getTime(),
          photoUrl: post.imageUrl,
          dataUrl: post.imageUrl,
          thumbUrl: post.imageUrl,
          loc: 'crew-post'
        });
      });
  }

  // 3) 모임 미션 인증 사진 (내 인증 사진 + 호스트 심사 목록의 크루 인증 사진)
  if (includeMissions && Array.isArray(missions)) {
    missions.forEach((mission, mIdx) => {
      if (!mission) return;
      if (mission.myPhotoUrl) {
        const c = assignCoord(coordCursor++);
        const dt = new Date(Date.now() + (mIdx + 1) * 900000);
        results.push({
          uid: `crew-mission-me-${mission.id || mIdx}`,
          name: `🎯 ${currentUserName} · ${mission.title}`,
          crewMember: currentUserName,
          sourceType: 'mission',
          missionTitle: mission.title,
          lat: c.lat,
          lng: c.lng,
          dayNumber: c.dayNumber,
          routePointId: c.routePointId,
          date: dt,
          timeVal: dt.getTime(),
          photoUrl: mission.myPhotoUrl,
          dataUrl: mission.myPhotoUrl,
          thumbUrl: mission.myPhotoUrl,
          loc: 'crew-mission'
        });
      }

      if (Array.isArray(mission.pendingCompletions)) {
        mission.pendingCompletions
          .filter((comp) => comp && comp.photoUrl)
          .forEach((comp, cIdx) => {
            const c = assignCoord(coordCursor++);
            const memberName = comp.userName || comp.userEmail || '크루';
            const dt = new Date(Date.now() + (mIdx + cIdx + 2) * 900000);
            results.push({
              uid: `crew-mission-comp-${comp.id || cIdx}`,
              name: `🎯 ${memberName} · ${mission.title}`,
              crewMember: memberName,
              sourceType: 'mission',
              missionTitle: mission.title,
              lat: c.lat,
              lng: c.lng,
              dayNumber: c.dayNumber,
              routePointId: c.routePointId,
              date: dt,
              timeVal: dt.getTime(),
              photoUrl: comp.photoUrl,
              dataUrl: comp.photoUrl,
              thumbUrl: comp.photoUrl,
              loc: 'crew-mission'
            });
          });
      }
    });
  }

  return [...results].sort((a, b) => (a.timeVal || 0) - (b.timeVal || 0));
}

/**
 * 스팟 목록에서 참여 크루원 이름 목록(중복 제거)을 추출합니다. (Immutable)
 */
export function extractCrewMemberNames(spots) {
  if (!Array.isArray(spots)) return [];
  return Array.from(new Set(spots.map((s) => s.crewMember).filter(Boolean)));
}

/**
 * 특정 크루원(또는 'ALL')의 스팟만 필터링하여 새 배열로 반환합니다. (Immutable)
 */
export function filterCrewSpotsByMember(spots, memberName = 'ALL') {
  if (!Array.isArray(spots)) return [];
  if (!memberName || memberName === 'ALL') return spots.map((s) => ({ ...s }));
  return spots.filter((s) => s.crewMember === memberName).map((s) => ({ ...s }));
}
