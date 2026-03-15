# TripGather 프로젝트 일일 분석

**작성일:** 2026-03-15

---

## 1. 오늘의 요약

전날 분석(2025-03-14)과 비교하여 대규모 기능 추가가 이루어졌습니다. 주요 변경점은 크게 세 가지입니다.

1. **백엔드: 유저(User) 도메인 실체화** — 기존 mock 방식에서 실제 JPA 엔티티 기반으로 전환
2. **프론트엔드: 마이페이지 기능화 및 UserContext 도입** — 하드코딩 제거, 실제 API 연동
3. **일정 탭 UI 고도화** — Night Flight 다크 테마, 일정 생성 모달, 인스타그램 스타일 루트 상세 뷰

---

## 2. 백엔드 변경사항

### 2.1 [NEW] User 도메인 완성

| 항목 | 내용 |
|------|------|
| 엔티티 | `User.java` — `id`, `name`, `bio`, `profileImageUrl`, `points`, `createdAt`, `updatedAt` |
| 리포지토리 | `UserRepository.java` (JPA, `findByName` 포함) |
| 서비스 | `UserService.java` |
| 컨트롤러 | `UserController.java` (대폭 확장) |

**이전 상태:** `UserController`에 mock 유저 고정 반환만 존재하고, 실제 DB 연동 없음.  
**현재 상태:** `User` JPA 엔티티 + 서비스 + 리포지토리로 완전한 레이어 구조 구성. `DataInitializer`에서 부팅 시 기본 유저(id=1) 생성 보장.

**User 필드:**
```
id (PK), name, bio (TEXT), profileImageUrl, points, createdAt, updatedAt
```

**새 API 엔드포인트:**

| 경로 | 메서드 | 설명 |
|------|--------|------|
| `/api/users/me` | GET | 현재 유저 (기본 id=1) 반환 |
| `/api/users/{id}` | GET | 특정 유저 조회 |
| `/api/users` | GET | 전체 유저 목록 |
| `/api/users` | POST | 유저 생성 |
| `/api/users/{id}` | PATCH | 이름·소개·프로필 이미지 수정 |

### 2.2 [NEW] OpenApiConfig (Swagger)

`OpenApiConfig.java`가 추가되어 `/swagger-ui.html`을 통해 API 문서를 확인할 수 있습니다.

### 2.3 DataInitializer 변경

앱 부팅 시 기본 유저(Jihyun, id=1)가 없으면 자동으로 생성하도록 초기화 로직이 추가되었습니다.

### 2.4 application.yml 변경

CORS, Springdoc 등 추가 설정이 반영되었습니다.

---

## 3. 프론트엔드 변경사항

### 3.1 [NEW] UserContext.jsx — 전역 유저 상태 관리

`src/contexts/UserContext.jsx`가 추가되어, 앱 전역에서 현재 유저를 조회하고 프로필을 수정할 수 있는 `useUser()` 훅을 제공합니다.

- `fetchMe()`: 앱 진입 시 `/api/users/me` 호출하여 유저 정보 로드
- `updateProfile(id, payload)`: `/api/users/{id}` PATCH 호출로 이름·소개 수정
- `App.jsx`에서 `<UserProvider>`로 앱 전체를 감싸고 있음

### 3.2 [NEW] api/client.js

API 베이스 URL를 관리하는 클라이언트 헬퍼가 추가되어, 컴포넌트에서 하드코딩된 URL을 줄이도록 설계되었습니다.

### 3.3 [MAJOR UPDATE] MyPage.jsx — 기능 완성

**이전:** 정적 플레이스홀더(하드코딩된 "지현 ✨") 표시만.  
**현재:** 실제 백엔드 API와 연동된 마이페이지로 완전히 개편.

| 기능 | 설명 |
|------|------|
| 프로필 표시 | `useUser()` 훅으로 API에서 이름·소개·포인트 실시간 로드 |
| 프로필 수정 | ✏️ 버튼 클릭 → 바텀 시트 모달에서 이름·소개 수정 후 저장 |
| 로딩/에러 처리 | 로딩 중 스피너 표시, 실패 시 "다시 시도" 버튼 제공 |
| 포인트 표시 | `user.points` 값을 "N pts" 형태로 프로필 하단 표시 |

### 3.4 [NEW] 일정 탭 고도화 (Night Flight 테마)

이번 사이클에서 2회에 걸쳐 일정 탭이 대폭 업그레이드되었습니다.

| 컴포넌트 | 변경 내용 |
|----------|-----------|
| `ItineraryTab.jsx` | `Home.jsx`에서 분리. Night Flight 다크 테마 적용, API 실시간 연동 |
| `CreateItineraryModal.jsx` | 새 일정 생성 UI (제목·설명 입력 후 `POST /api/itineraries`) |
| `TicketCard.jsx` | `itinerary` 전체 객체를 prop으로 받아 VIEW ROUTE 버튼 연동 |
| `RouteDetailModal.jsx` | 인스타그램 스타일 수직 타임라인 "트래블 로그" 뷰 (신규) |

---

## 4. 현재 아키텍처 상태 (수정된 구조)

```
backend/
├── controller/  Gathering, Comment, Itinerary, User
├── domain/      Gathering, Comment, Itinerary, RoutePoint, User ← 신규
├── repository/  GatheringRepository, ItineraryRepository, UserRepository ← 신규
├── service/     GatheringService, ItineraryService, UserService ← 신규
└── config/      OpenApiConfig ← 신규

frontend/src/
├── api/         client.js ← 신규
├── contexts/    UserContext.jsx ← 신규
├── components/  BottomNav, FeedCard, CreateGatheringModal, GatheringDetailModal
│               TicketCard*, ItineraryTab*, CreateItineraryModal*, RouteDetailModal* (신규/변경)
└── pages/       Home, MapPage, ChatPage, MyPage*
```

---

## 5. 여전히 미구현 / 한계

| 항목 | 현황 |
|------|------|
| 인증 | 로그인/회원가입 없음. `UserService`가 항상 id=1 유저 반환 |
| 지도-모임 연동 | `MapPage` 마커 여전히 하드코딩. DB 기반 핀 미구현 |
| 채팅 | UI 플레이스홀더만. 기능 없음 |
| 일정 RoutePoint UI | 생성 모달에서 경유지 직접 추가하는 흐름 없음 |
| 마이페이지 심화 | 내가 참여한 모임 목록, 내 일정 목록 등 미구현 |
| 이미지 업로드 | 프로필 이미지 수정 API 연결 없음 (profileImageUrl 변경 불가) |

---

## 6. 다음 단계 제안

1. **지도 탭 실 연동**: `MapPage`에서 `GET /api/gatherings` 호출해 위경도 기반 마커 렌더링
2. **내 모임 기반 필터링 개선**: `Home.jsx`의 하드코딩 `'Jihyun (지현)'` 비교를 `UserContext`의 실제 유저로 교체
3. **일정 경유지 추가 UI**: `CreateItineraryModal`에서 `RoutePoint`를 직접 추가하는 UX 구현
4. **프로필 이미지 업로드**: 이미지 선택 → Base64 or URL 저장 → `PATCH /api/users/{id}` 연동
5. **Swagger 문서 활용**: `http://localhost:8080/swagger-ui.html`에서 API 스펙 확인 및 테스트
