# TripGather — 현재 아키텍처 평가

**문서 버전:** 1.0  
**작성일:** 2025-03-14  
**관점:** 시니어 소프트웨어 아키텍트, 유지보수성 중심.

---

## 1. 요약

TripGather는 **백엔드/프론트엔드가 명확히 분리된 모노레포**입니다: Spring Boot(Java 17) REST API와 React(Vite) SPA. 백엔드는 단순한 **3계층 구조**(controller → service → repository)를 따르고, 프론트엔드는 페이지/컴포넌트 분리와 **모바일 퍼스트**로 구성되어 있습니다. MVP 수준에서는 구조가 이해하기 쉽고 적절합니다. **유지보수성 측면의 주요 리스크**는 다음과 같습니다: **API 베이스 URL과 CORS 허용 출처 하드코딩**, **도메인 엔티티를 API에서 그대로 노출**(DTO 없음), **전역 예외 처리·검증 계층 부재**, **Gathering/Itinerary의 host/author가 여전히 String**이며 User 참조로 연결되지 않음. 이 부분을 정리하면 기능과 팀이 커질수록 변경 비용을 줄일 수 있습니다.

---

## 2. As-Is 개요

### 2.1 상위 토폴로지

```
┌─────────────────────────────────────────────────────────────┐
│  Frontend (React 19, Vite 7)                                 │
│  - SPA, 모바일 퍼스트 (max-width 480px)                      │
│  - 페이지: Home(/gather), MapPage, ChatPage, MyPage          │
│  - 백엔드로 직접 fetch(); 공유 API 클라이언트 모듈 없음      │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTP (fetch)
                            │ Base URL: localhost:8080 하드코딩
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  Backend (Spring Boot 3.3.4, Java 17)                       │
│  - REST /api/*                                               │
│  - CORS: http://localhost:5173 만 (컨트롤러에 하드코딩)     │
└───────────────────────────┬─────────────────────────────────┘
                            │ JPA
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  DB: H2 (로컬) / PostgreSQL (prod 프로필)                   │
│  - ddl-auto: update (아직 마이그레이션 도구 미사용)         │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 백엔드 구조

| 계층       | 위치                         | 역할 |
|-----------|------------------------------|------|
| Controller | `controller/*Controller.java` | REST 엔드포인트; 엔티티를 그대로 반환. |
| Service    | `service/*Service.java`        | 트랜잭션 경계; 오케스트레이션. |
| Repository | `repository/*Repository.java` | JPA 접근. |
| Domain     | `domain/*.java`               | JPA 엔티티 (User, Gathering, Itinerary, RoutePoint, Comment). |

**강점:**

- 계층 분리가 명확함: 컨트롤러는 서비스에 위임, 서비스는 리포지토리 사용.
- 단일 패키지 `com.example.demo`로 소규모 코드베이스에서 탐색이 쉬움.
- DB·JPA에 프로필 기반 설정(`local` / `prod`) 적용.
- User 엔티티와 `/api/users/me` 존재; 인증 없이 “현재 유저”로 id=1 기본 사용.

**공백:**

- **엔티티를 API 계약으로 사용**: 컨트롤러가 JPA 엔티티를 반환. API가 영속성에 묶임(예: `Comment.gathering` 순환, `createdAt`/`updatedAt` 노출). MVP에는 괜찮으나 API가 바뀌거나 클라이언트별로 다른 형태가 필요해지면 부담.
- **전역 예외 처리 없음**: 서비스의 `IllegalArgumentException` / `IllegalStateException`이 500으로 전파; 일관된 에러 본문이나 404/400 매핑 없음.
- **명시적 검증 없음**: 요청 본문에 `@Valid` + Bean Validation 미적용; 잘못된 데이터가 서비스까지 도달 가능.
- **컨트롤러마다 CORS**: `@CrossOrigin(origins = "http://localhost:5173")` 반복; prod 출처 미설정.
- **Gathering/Itinerary의 host/author**: 여전히 `String`; `User` 엔티티와 연결되지 않음. “내 모임” 일관 처리와 이후 권한 검사에 걸림.

### 2.3 프론트엔드 구조

| 영역       | 위치              | 역할 |
|-----------|-------------------|------|
| Entry     | `main.jsx`, `App.jsx` | 라우터, 레이아웃, 전역 스타일. |
| Pages     | `pages/*.jsx`      | 라우트 단위 화면 (Home, MapPage, ChatPage, MyPage). |
| Components | `components/*.jsx` | 재사용 UI (BottomNav, FeedCard, 모달, ItineraryTab). |
| Styles    | `index.css`        | CSS 변수(디자인 토큰), 전역·레이아웃. |

**강점:**

- `index.css`에 디자인 토큰(예: `--primary`, `--surface`); 테마 일관성.
- 모바일 퍼스트 레이아웃과 하단 네비; 페이지/컴포넌트 구분 명확.
- Lucide 아이콘, 카카오맵 SDK 연동.

**공백:**

- **API 베이스 URL**: 여러 컴포넌트(Home, ItineraryTab, GatheringDetailModal, CreateGatheringModal, CreateItineraryModal)에 `http://localhost:8080` 하드코딩. 스테이징/프로덕션 등 환경 변경 시 일괄 치환 필요.
- **공유 API 계층 없음**: 컴포넌트마다 자체 `fetch()` 구성; 중앙 에러 처리, 인증 헤더, 요청/응답 타입 없음.
- **로컬 상태만 사용**: “현재 유저” 등 전역 클라이언트 상태 없음; 마이페이지와 “내 모임”은 localStorage나 호스트 이름 문자열 매칭에 의존.

### 2.4 설정과 보안

- **백엔드**: `application.yml`에 DB URL·자격 증명 보관; prod 비밀번호는 placeholder(`password`). 비밀값 외부화(환경 변수·vault) 없음.
- **프론트엔드**: API URL용 `.env` 미사용; `vite.config.js`에 클라이언트용 env 접두사 미정의.
- **인증**: 없음. “현재 유저”는 `UserService.getCurrentUser()`에서 id=1 고정.

### 2.5 데이터와 도메인

- **User**: id, name, bio, profileImageUrl, points, 타임스탬프 보유. 아직 Gathering/Itinerary/Comment에서 FK로 사용되지 않음.
- **Gathering**: host(String), location, lat/lng, category, dates, currentJoining, maxJoining, comments. 댓글 수는 `@PostLoad`로 계산.
- **Itinerary**: author(String), routePoints(OneToMany). RoutePoint는 sequenceOrder, label, lat/lng.
- **Comment**: gathering, author(String), content, createdAt.

관계는 일관됨(예: Comment → Gathering). 부족한 부분: User ↔ Gathering/Itinerary/Comment 연결, 필요 시 소프트 삭제·감사 필드.

---

## 3. 리스크 요약

| 리스크 | 영향 | 가능성 |
|--------|------|--------|
| 환경별 URL/출처 하드코딩 | 배포 실패 또는 보안 취약 | 높음 |
| 전역 예외/검증 부재 | API 계약 불명확, 디버깅 어려운 에러 | 중간 |
| 엔티티를 API로 노출 | API 진화 취약, 필드 과다 노출 | 중간(기능 증가 시) |
| host/author가 String | “내 데이터” 불일치, 추후 인증 연동 어려움 | 중간 |
| 마이그레이션 도구 없음(ddl-auto만) | prod 스키마 변경 위험 | 첫 prod 변경 전까지는 낮음 |

---

## 4. 결론

프로젝트는 **MVP에 맞게 구조화**되어 있고 탐색이 쉽습니다. 제품과 팀이 성장해도 **유지보수성**을 유지하려면, (1) 설정 외부화(API 베이스 URL, CORS, 비밀값), (2) 양쪽 모두에서 얇고 일관된 API 계층과 에러 처리, (3) User와 host/author 연결 및 검증 도입에 우선 초점을 두는 것이 좋습니다. 구체적·우선순위화된 권장 사항은 **maintenance-recommendations.md**에 있습니다.
