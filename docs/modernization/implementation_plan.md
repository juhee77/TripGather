# TripGather 구조 재설계 구현 계획

챌린지(Mission) 기능을 제거하고, 여행 일정 복제/수정/공유 시스템을 도입하며, 모임-여행 연동 기능을 추가합니다.

## Proposed Changes

### Phase 1: 챌린지(Mission) 제거

#### [DELETE] Backend Mission 관련 파일들

| 파일 | 역할 |
|---|---|
| [UserMission.java](file:///Users/bagjuhui/IdeaProjects/TripGather/backend/src/main/java/com/example/demo/domain/UserMission.java) | 미션 엔티티 |
| [UserMissionStep.java](file:///Users/bagjuhui/IdeaProjects/TripGather/backend/src/main/java/com/example/demo/domain/UserMissionStep.java) | 미션 스텝 엔티티 |
| [MissionStatus.java](file:///Users/bagjuhui/IdeaProjects/TripGather/backend/src/main/java/com/example/demo/domain/MissionStatus.java) | 미션 상태 Enum |
| [UserMissionRepository.java](file:///Users/bagjuhui/IdeaProjects/TripGather/backend/src/main/java/com/example/demo/repository/UserMissionRepository.java) | 미션 리포지토리 |
| [UserMissionStepRepository.java](file:///Users/bagjuhui/IdeaProjects/TripGather/backend/src/main/java/com/example/demo/repository/UserMissionStepRepository.java) | 미션 스텝 리포지토리 |
| [UserMissionController.java](file:///Users/bagjuhui/IdeaProjects/TripGather/backend/src/main/java/com/example/demo/controller/UserMissionController.java) | 미션 API 컨트롤러 |
| [UserMissionServiceImpl.java](file:///Users/bagjuhui/IdeaProjects/TripGather/backend/src/main/java/com/example/demo/service/UserMissionServiceImpl.java) | 미션 서비스 |
| [UserMissionUseCase.java](file:///Users/bagjuhui/IdeaProjects/TripGather/backend/src/main/java/com/example/demo/usecase/UserMissionUseCase.java) | 미션 유즈케이스 |
| [UserMissionResponse.java](file:///Users/bagjuhui/IdeaProjects/TripGather/backend/src/main/java/com/example/demo/dto/UserMissionResponse.java) | 미션 응답 DTO |
| [UserMissionStepResponse.java](file:///Users/bagjuhui/IdeaProjects/TripGather/backend/src/main/java/com/example/demo/dto/UserMissionStepResponse.java) | 스텝 응답 DTO |

#### [DELETE] Frontend Mission 관련 파일들

| 파일 | 역할 |
|---|---|
| [MissionTab.jsx](file:///Users/bagjuhui/IdeaProjects/TripGather/frontend/src/components/MissionTab.jsx) | 챌린지 탭 UI |
| [useMissionsViewModel.js](file:///Users/bagjuhui/IdeaProjects/TripGather/frontend/src/viewmodels/useMissionsViewModel.js) | 미션 뷰모델 |
| [UserMissionRepository.js](file:///Users/bagjuhui/IdeaProjects/TripGather/frontend/src/repositories/UserMissionRepository.js) | 미션 API 호출 |

#### [MODIFY] Mission 참조 제거

- [Home.jsx](file:///Users/bagjuhui/IdeaProjects/TripGather/frontend/src/pages/Home.jsx) — `useMissionsViewModel` import 제거, "챌린지" 탭 제거
- [ItineraryTab.jsx](file:///Users/bagjuhui/IdeaProjects/TripGather/frontend/src/components/ItineraryTab.jsx) — "챌린지에 넣기" 모달 제거, `useMissionsViewModel` 의존성 제거
- [DataInitializer.java](file:///Users/bagjuhui/IdeaProjects/TripGather/backend/src/main/java/com/example/demo/DataInitializer.java) — 미션 관련 시드 데이터 제거
- [PointService.java](file:///Users/bagjuhui/IdeaProjects/TripGather/backend/src/main/java/com/example/demo/service/PointService.java) — 미션 완수 포인트 로직 검토

---

### Phase 2: Itinerary 복제/공유 시스템

#### [MODIFY] [Itinerary.java](file:///Users/bagjuhui/IdeaProjects/TripGather/backend/src/main/java/com/example/demo/domain/Itinerary.java)

새 필드 추가:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "owner_id")
private User owner;            // 소유자 (복제 시 복제한 사용자)

private Long originalId;       // 원본 Itinerary ID (null이면 직접 생성)

@Builder.Default
private boolean isPublic = true; // 피드 공개 여부
```

#### [NEW] MyTripController.java
- `GET /api/my-trips` — 내 여행 일정 목록 (소유자 기준)
- `POST /api/my-trips/clone/{itineraryId}` — 다른 사람 일정 복제 (Itinerary + RoutePoints 깊은 복사)
- `DELETE /api/my-trips/{id}` — 내 여행에서 제거 (물리 삭제)
- `PATCH /api/my-trips/{id}/publish` — 피드에 공개/비공개 전환

#### [MODIFY] [ItineraryResponse.java](file:///Users/bagjuhui/IdeaProjects/TripGather/backend/src/main/java/com/example/demo/dto/ItineraryResponse.java)
- `isPublic`, `originalId`, `ownerName`, `ownerEmail` 필드 추가

#### [MODIFY] [ItineraryServiceImpl.java](file:///Users/bagjuhui/IdeaProjects/TripGather/backend/src/main/java/com/example/demo/service/ItineraryServiceImpl.java)
- `cloneItinerary(Long originalId, User owner)` 메서드 추가

#### [MODIFY] 기존 일정 목록 API
- `GET /api/itineraries` 는 `isPublic = true`인 것만 반환 (여행 피드용)

---

### Phase 3: Gathering ↔ Itinerary 연동

#### [MODIFY] [Gathering.java](file:///Users/bagjuhui/IdeaProjects/TripGather/backend/src/main/java/com/example/demo/domain/Gathering.java)

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "itinerary_id")
private Itinerary linkedItinerary; // 연결된 여행 일정 (선택)
```

#### [MODIFY] [GatheringResponse.java](file:///Users/bagjuhui/IdeaProjects/TripGather/backend/src/main/java/com/example/demo/dto/GatheringResponse.java)
- `linkedItineraryId`, `linkedItineraryTitle` 필드 추가

#### [MODIFY] [CreateGatheringPage.jsx](file:///Users/bagjuhui/IdeaProjects/TripGather/frontend/src/pages/CreateGatheringPage.jsx)
- "여행 일정 연결하기" 선택 UI 추가 (내 일정 목록에서 선택)

---

### Phase 4: 프론트엔드 탭 구조 전환

#### [MODIFY] [Home.jsx](file:///Users/bagjuhui/IdeaProjects/TripGather/frontend/src/pages/Home.jsx)

탭 배열 변경:
```
['라운지', '비행 계획', '챌린지', '내 여정', '내 여권']
→ ['라운지', '여행 피드', '내 여행', '내 여권']
```

- **라운지**: 기존 유지
- **여행 피드**: 기존 "비행 계획" 리네이밍 + `isPublic=true` 필터 + "내 여행에 복제" 버튼
- **내 여행**: 새로 구현 — 소유 일정 목록, 생성/수정/삭제/공유 + 정렬
- **내 여권**: 기존 유지

#### [NEW] MyTripTab.jsx
- 내 여행 일정 목록 표시
- "새 여행 추가" FAB
- 각 카드에 편집/삭제/공개 토글 기능

#### [MODIFY] [ItineraryTab.jsx](file:///Users/bagjuhui/IdeaProjects/TripGather/frontend/src/components/ItineraryTab.jsx)
- "여행 피드"로 리네이밍
- BOARD 모달을 "내 여행에 복제" 단일 액션으로 변경
- 챌린지 관련 코드 완전 제거

#### [MODIFY] [ItineraryDetailPage.jsx](file:///Users/bagjuhui/IdeaProjects/TripGather/frontend/src/pages/ItineraryDetailPage.jsx)
- "내 여행에 복제" 버튼 추가
- 공개/비공개 토글 (소유자일 때)

#### [DELETE] 기존 JourneyEntry 시스템 (복제 방식으로 대체)
- [JourneyEntry.java](file:///Users/bagjuhui/IdeaProjects/TripGather/backend/src/main/java/com/example/demo/domain/JourneyEntry.java)
- [JourneyRepository.js](file:///Users/bagjuhui/IdeaProjects/TripGather/frontend/src/repositories/JourneyRepository.js)
- 관련 백엔드 Controller/Service/Repository

---

## User Review Required

> [!IMPORTANT]
> 이 작업은 대규모 리팩터링입니다. 아래 사항을 확인해 주세요.

1. **DB 테이블 변경**: `user_mission`, `user_mission_step` 테이블 삭제, `itinerary` 테이블에 `owner_id`, `original_id`, `is_public` 컬럼 추가, `gathering` 테이블에 `itinerary_id` 컬럼 추가
2. **기존 데이터**: H2 인메모리 DB이므로 데이터 손실 없음 (DataInitializer로 재생성)
3. **Mission 완전 삭제**: 스탬프/포인트 시스템 중 미션 완수 보상 로직도 함께 제거됨

## Verification Plan

### Automated Tests
- `./gradlew test` — 컴파일 에러 없이 통과 확인
- Mission 관련 테스트 파일도 삭제

### Manual Verification
- 프론트엔드 4탭 정상 렌더링
- 여행 피드에서 "내 여행에 복제" → 내 여행 탭에 복제본 표시
- 복제본 수정 → 원본 영향 없음
- "피드에 공유" 토글 → 여행 피드에 노출/비노출
- 모임 생성 시 여행 일정 연결 가능
