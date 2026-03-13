# TripGather 프로젝트 분석

**작성일:** 2025-03-14

---

## 1. 프로젝트 개요

**TripGather**는 일정 공유와 동네 모임을 결합한 **소셜 모임 플랫폼**입니다.  
나만의 여행/루틴 일정을 지도와 함께 기록·공유하고, 관심사가 맞는 사람들을 가볍게 모을 수 있는 서비스를 목표로 합니다.

- **레포 구조:** 모노레포, `backend/`(Spring Boot) + `frontend/`(React/Vite) 분리
- **현재 상태:** MVP 수준, 로컬 개발·테스트용 H2, 프로덕션용 PostgreSQL 프로필 준비됨

---

## 2. 기술 스택

| 구분 | 기술 |
|------|------|
| **Backend** | Java 17, Spring Boot 3.3.4, Spring Data JPA, Gradle |
| **DB** | H2 (local/test), PostgreSQL (prod) |
| **Frontend** | React 19, Vite 7, React Router 7, Lucide React |
| **지도** | react-kakao-maps-sdk (카카오맵) |
| **기타** | Lombok |

---

## 3. 백엔드 구조

### 3.1 도메인 엔티티

| 엔티티 | 설명 | 주요 필드 |
|--------|------|------------|
| **Gathering** | 모임 모집글 | title, host, location, lat/lng, category, dates, currentJoining, maxJoining, bgImageUrl, comments |
| **Comment** | 모임 댓글 | gathering, author, content, createdAt |
| **Itinerary** | 일정(여행/루트) | title, author, description, routePoints, createdAt |
| **RoutePoint** | 일정 내 경유지 | itinerary, sequenceOrder, label, lat, lng |

- **User 엔티티 없음.** 현재는 `UserController`의 mock `/api/users/me`로 "Jihyun (지현)" 등 고정 사용자만 사용.

### 3.2 API 엔드포인트

| 경로 | 메서드 | 설명 |
|------|--------|------|
| `/api/gatherings` | GET, POST | 모임 목록 조회, 모임 생성 |
| `/api/gatherings/{id}/join` | POST | 모임 참여 (currentJoining 증가) |
| `/api/gatherings/{gatheringId}/comments` | GET, POST | 댓글 목록 조회, 댓글 작성 |
| `/api/itineraries` | GET, POST | 일정 목록 조회, 일정 생성 |
| `/api/users/me` | GET | 현재 유저 정보 (mock) |

- CORS: `http://localhost:5173` (Vite 개발 서버) 허용

### 3.3 데이터 초기화

- `DataInitializer`: 앱 기동 시 H2 DB가 비어 있으면  
  - 모임 2건 (부산 주말 여행, 한강 러닝)  
  - 일정 2건 (서울 나들이, 제주도 먹방)  
  자동 생성.

---

## 4. 프론트엔드 구조

### 4.1 라우팅 및 탭

| 경로 | 페이지 | 역할 |
|------|--------|------|
| `/` | → `/gather` 리다이렉트 | 진입점 |
| `/gather` | Home | 모임 피드 + 탭(발견/내 모임/일정/지도) |
| `/map` | MapPage | 카카오맵 + 모임 핀 |
| `/chat` | ChatPage | 채팅 (현재 플레이스홀더) |
| `/mypage` | MyPage | 마이페이지 (현재 플레이스홀더) |

- 하단 고정: `BottomNav` (모임, 지도, 채팅, 마이페이지)

### 4.2 Home(모임) 탭 구성

- **발견:** 전체 모임 피드 (FeedCard), API `GET /api/gatherings` 연동
- **내 모임:** 호스트가 "Jihyun (지현)"이거나 `localStorage`의 `myJoinedIds`에 포함된 모임만 표시
- **일정:** `ItineraryTab` — 일정 목록(TicketCard), 생성 모달, 상세(경로) 모달, `GET/POST /api/itineraries` 연동
- **지도:** 안내 문구만 (실제 지도는 하단 [지도] 탭)

- 모임 생성: FAB → `CreateGatheringModal` → `POST /api/gatherings`
- 모임 상세: 카드 클릭 → `GatheringDetailModal` (댓글, 참여하기 → `POST .../join`, 참여 시 `myJoinedIds` 갱신)

### 4.3 지도 페이지 (MapPage)

- 카카오맵 연동 (react-kakao-maps-sdk)
- **마커가 하드코딩** (서울/한강/여의도 3개). API 연동 없음.
- 마커 클릭 시 하단 시트에 제목·설명·참여하기 버튼 표시 (실제 참여 API 연동 없음)

### 4.4 미구현/제한 사항

- **채팅:** UI만 있고 기능 없음
- **마이페이지:** 프로필 표시만, 찜/참여 목록·설정 미구현
- **인증:** 로그인/회원가입 없음, mock 유저만 사용
- **지도-모임 연동:** 지도 마커가 DB/API와 연동되지 않음
- **일정-경로:** Itinerary에 RoutePoint는 있으나, 프론트에서 경로 생성/수정/지도 표시 흐름은 제한적

---

## 5. 디렉터리 구조 요약

```
TripGather/
├── backend/
│   ├── src/main/java/com/example/demo/
│   │   ├── controller/   # Gathering, Comment, Itinerary, User
│   │   ├── domain/       # Gathering, Comment, Itinerary, RoutePoint
│   │   ├── repository/  # JPA Repository
│   │   ├── service/      # GatheringService, ItineraryService
│   │   ├── DataInitializer.java, DemoApplication.java
│   └── src/main/resources/application.yml  # local(H2) / prod(PostgreSQL)
├── frontend/
│   └── src/
│       ├── components/   # BottomNav, FeedCard, CreateGatheringModal, GatheringDetailModal,
│       │                  # TicketCard, ItineraryTab, CreateItineraryModal, RouteDetailModal
│       ├── pages/        # Home, MapPage, ChatPage, MyPage
│       ├── App.jsx, main.jsx, index.css
│       └── assets/
├── ai/
│   ├── daily/            # 일별 분석/기록
│   └── plan/             # 앞으로의 계획
└── README.md
```

---

## 6. 정리

- **구현된 핵심:** 모임 CRUD, 참여하기, 댓글, 일정 CRUD, 지도 UI(하드코딩 마커), 모바일 퍼스트 레이아웃
- **부족한 부분:** 사용자 인증/회원, 채팅, 지도-모임/일정 연동, 마이페이지 기능, 일정 경로(RoutePoint) 풀 플로우
- **다음 단계:** `/ai/plan` 폴더의 계획 문서 참고
