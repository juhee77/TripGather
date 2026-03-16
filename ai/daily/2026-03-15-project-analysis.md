# TripGather 프로젝트 분석

**작성일:** 2026-03-15

## 1. 한눈에 보기

TripGather는 여행 일정 공유와 동네 모임 모집을 결합한 모바일 퍼스트 앱입니다. 백엔드는 Spring Boot + JPA, 프론트는 React(Vite)로 구성되어 있으며, 현재 핵심 기능은 모임/일정 CRUD의 일부와 댓글, 지도 기반 모임 표시, 마이페이지 프로필 편집까지 구현되어 있습니다.

## 2. 아키텍처 개요

```
TripGather/
├── backend/                  # Spring Boot API
│   ├── src/main/java/com/example/demo/
│   │   ├── controller/       # REST API
│   │   ├── service/          # 비즈니스 로직
│   │   ├── repository/       # Spring Data JPA
│   │   ├── domain/           # JPA 엔티티
│   │   └── config/           # CORS, OpenAPI
│   └── src/main/resources/application.yml
└── frontend/                 # React (Vite)
    ├── src/pages/            # 탭 화면
    ├── src/components/       # 공용 UI
    ├── src/contexts/         # 전역 상태
    └── src/api/              # API 헬퍼
```

## 3. 백엔드 분석

### 3.1 기술 스택

- Java 17, Spring Boot 3.3.4
- Spring Data JPA, H2(로컬), PostgreSQL(프로덕션)
- Springdoc OpenAPI (Swagger UI)

### 3.2 주요 도메인 모델

- User: 프로필(이름/소개/이미지), 포인트, 생성/수정 시각
- Gathering: 모임 정보(제목, 호스트, 위치, 일정, 인원, 카테고리, 이미지)
- Itinerary: 일정(제목, 작성자, 설명, RoutePoint 목록)
- RoutePoint: 일정 경유지(일자, 순서, 라벨, 좌표)
- Comment: 모임 댓글(작성자, 내용, 생성 시각)

### 3.3 API 엔드포인트 요약

- GET /api/gatherings
- POST /api/gatherings
- POST /api/gatherings/{id}/join
- GET /api/gatherings/{gatheringId}/comments
- POST /api/gatherings/{gatheringId}/comments
- GET /api/itineraries
- POST /api/itineraries
- GET /api/users/me
- GET /api/users/{id}
- GET /api/users
- POST /api/users
- PATCH /api/users/{id}

### 3.4 데이터 시드

`DataInitializer`에서 기본 유저, 기본 모임, 샘플 일정/경유지를 부팅 시 자동 생성합니다. 개발 환경에서 화면이 바로 채워지도록 설계되어 있습니다.

### 3.5 CORS 및 문서화

- 전역 CORS: `http://localhost:5173~5175` 허용
- Swagger UI: `/swagger-ui.html`

## 4. 프론트엔드 분석

### 4.1 화면 구조

- Home: 모임 피드, 탭 전환(발견/내 모임/일정/지도) 및 모임 생성 모달
- MapPage: 카카오 지도 SDK로 모임 핀 표시, 상세 카드에서 참여 가능
- MyPage: 유저 프로필 표시 및 편집(모달)
- ChatPage: 현재는 플레이스홀더

### 4.2 주요 컴포넌트

- CreateGatheringModal: 모임 생성 폼
- GatheringDetailModal: 모임 상세, 댓글 조회/작성, 참여
- ItineraryTab: 일정 카드 목록 + 일정 생성 + 상세 모달
- RouteDetailModal / TicketCard: 일정 상세/카드 UI

### 4.3 데이터 흐름

- 모임: Home/MapPage에서 `GET /api/gatherings`, 생성은 `POST /api/gatherings`, 참여는 `POST /api/gatherings/{id}/join`
- 일정: ItineraryTab에서 `GET /api/itineraries`, 생성은 `POST /api/itineraries`
- 댓글: GatheringDetailModal에서 `GET/POST /api/gatherings/{id}/comments`
- 유저: UserContext에서 `GET /api/users/me`, 프로필 편집은 `PATCH /api/users/{id}`

## 5. 현재 상태에서 보이는 개선 포인트

1. API 베이스 URL 통일
   - `api/client.js`가 있지만 여러 컴포넌트에서 `http://localhost:8080`을 하드코딩 중입니다.

2. 인증/권한 미구현
   - `UserService`는 기본 id=1 유저 반환이며, 실제 로그인/권한 처리가 없습니다.

3. 일정 경유지 추가 UI 미구현
   - CreateItineraryModal에서는 `routePoints`가 항상 빈 배열입니다.

4. 참여 인원 동시성
   - `joinGathering`은 현재 단순 증가로 경쟁 조건에 취약합니다.

5. 댓글 작성자 하드코딩
   - 프론트/백 모두 기본 작성자(지현) 하드코딩이 포함되어 있습니다.

## 6. 실행 방법 (로컬)

백엔드:
- `backend/gradlew bootRun`

프론트엔드:
- `cd frontend`
- `npm install`
- `npm run dev`

## 7. 다음 작업 제안

1. API base URL 일괄 적용 (`apiUrl` 유틸)
2. 인증 스캐폴딩(로그인/세션/토큰) 추가
3. 일정 경유지 입력 UI 추가
4. 모임 참여 시 동시성 제어(낙관적/비관적 락) 또는 제한 검증 강화
5. 댓글/프로필 작성자 데이터를 로그인 유저 기반으로 연결

---

필요하면 이 분석을 기준으로 세부 항목별 개선 작업도 바로 진행할 수 있습니다.
