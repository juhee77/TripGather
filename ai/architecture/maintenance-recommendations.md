# TripGather — 유지보수 권장 사항

**문서 버전:** 1.0  
**작성일:** 2025-03-14  
**대상:** 개발자 및 유지보수 담당자.  
**참고 문서:** current-assessment.md (As-is 상태).

이 권장 사항은 **단기**(빠른 효과, 낮은 리스크)와 **중기**(구조적 개선)로 구분되어 있습니다. 모두 **변경 비용 절감**과 **운영 장애 감소**를 목표로 합니다.

---

## 1. 단기 (가능한 빨리)

### 1.1 API 베이스 URL 외부화 (프론트엔드)

**문제:** `http://localhost:8080`이 여러 컴포넌트에 하드코딩되어 있음. 스테이징·프로덕션 빌드 시 잘못된 백엔드를 바라보게 됨.

**조치:**

- `frontend/`에 `.env`와 `.env.example` 추가:
  - `VITE_API_BASE_URL=http://localhost:8080`
- Vite에서는 `VITE_` 접두사가 붙은 변수만 클라이언트에 노출됨. 코드에서 `import.meta.env.VITE_API_BASE_URL` 사용.
- 소규모 **API 클라이언트 모듈** 도입(예: `frontend/src/api/client.js` 또는 `config.js`):
  - `const API_BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';`
  - `API_BASE` 또는 `apiUrl('/api/gatherings')` 같은 헬퍼 export.
- 모든 `fetch('http://localhost:8080/api/...')`를 `fetch(apiUrl('/api/...'))`(또는 동일한 방식)로 교체.

**수정 대상 파일:** `fetch`를 호출하는 모든 컴포넌트 (Home.jsx, ItineraryTab.jsx, GatheringDetailModal.jsx, CreateGatheringModal.jsx, CreateItineraryModal.jsx).

---

### 1.2 CORS 및 허용 출처 일원화 (백엔드)

**문제:** 컨트롤러마다 `@CrossOrigin(origins = "http://localhost:5173")`가 있음. 프로덕션 프론트 출처가 설정되어 있지 않고, 중복으로 인해 변경 시 실수하기 쉬움.

**조치:**

- **WebMvcConfigurer**(또는 `WebMvcConfig`) 하나에서 설정으로 허용 출처를 지정:
  - `application.yml`: `app.cors.allowed-origins: http://localhost:5173` (로컬) 및 prod 값(예: `https://your-app.com`).
  - 설정 클래스에서 `@Value("${app.cors.allowed-origins}")` 또는 리스트로 주입 후 `addCorsMappings`에 등록.
- 모든 컨트롤러에서 `@CrossOrigin` 제거.

**결과:** 출처 변경이 한 곳에서만 이루어지고, 여러 출처나 환경별 값 추가가 쉬워짐.

---

### 1.3 전역 예외 처리 (백엔드)

**문제:** 서비스에서 `IllegalArgumentException` / `IllegalStateException`을 던지면 API가 500을 반환하고 응답 본문이 일관되지 않음. 클라이언트가 “찾을 수 없음”과 “서버 에러”를 구분할 수 없음.

**조치:**

- **@ControllerAdvice** 도입(예: `GlobalExceptionHandler`):
  - `IllegalArgumentException` → 400 Bad Request(또는 “찾을 수 없음” 의미면 404).
  - `IllegalStateException` → 409 Conflict 또는 400(용도에 따라).
  - “엔티티 없음”(예: 커스텀 `UserNotFoundException`) → 404.
  - 작은 JSON 본문 반환, 예: `{ "message": "...", "code": "..." }`.
- 선택: Bean Validation의 `MethodArgumentNotValidException`에 `@ExceptionHandler`를 두어 검증 실패 시 400과 필드별 에러 반환.

**결과:** 예측 가능한 상태 코드와 에러 페이로드; 프론트 메시지 표시와 운영 로깅이 수월해짐.

---

### 1.4 기본 입력 검증 (백엔드)

**문제:** 요청 본문이 검증되지 않음. 빈 문자열, 과도한 길이, 잘못된 타입이 서비스까지 전달되어 불명확한 에러나 DB 문제를 일으킬 수 있음.

**조치:**

- **Bean Validation** 추가(예: `spring-boot-starter-validation` 미포함 시 추가) 후 컨트롤러에서 `@Valid @RequestBody` 사용.
- 요청 본문으로 쓰는 엔티티 또는 DTO에: 필수 문자열은 `@NotBlank`, `@Size`, `@Min`/`@Max` 등 적절히 적용. DB 제약만 의존하지 말 것.

**결과:** 잘못된 요청은 400과 검증 메시지로 조기 실패; DB에 잘못된 데이터가 쌓이는 것을 줄임.

---

## 2. 중기 (구조적 개선)

### 2.1 User와 host/author 연결

**문제:** Gathering.host, Itinerary.author, Comment.author가 단순 문자열임. “내 모임”, “내 일정”이 문자열 매칭이나 localStorage로만 구현되어 있어, 권한 부여와 사용자 기준 조회가 제대로 되지 않음.

**조치:**

- Gathering, Itinerary, Comment에 선택적 **User FK** 추가(예: `hostUserId`, `authorUserId`)하고, 마이그레이션 동안 `host`/`author`는 비정규화된 표시 이름으로 유지.
- 백엔드: 모임/일정/댓글 생성·수정 시 “현재 유저” 기준으로 FK 설정하고, User.name으로 표시 이름 동기화.
- “내 모임” / “내 일정”을 API로 제공(예: `GET /api/gatherings?hostId=current` 또는 `GET /api/users/me/gatherings`)하고, 프론트에서는 문자열 필터 대신 이 API 사용.

**결과:** “누가 무엇을 했는지”에 대한 단일 소스; 권한 검사와 프로필 링크 대비 가능.

---

### 2.2 프론트엔드 API 계층과 에러 처리

**문제:** 컴포넌트마다 자체 `fetch` 호출; 공통 에러 처리, 로딩 상태, 인증 헤더 주입이 없음.

**조치:**

- 소규모 **api** 계층 구성(예: `frontend/src/api/`):
  - `client.js`: 베이스 URL, 기본 헤더, `fetch`를 실행하고 비-2xx 처리(예: status + body를 담아 throw 또는 Result 타입 반환)하는 래퍼.
  - 선택: 리소스별 모듈(`gatherings.js`, `itineraries.js`, `users.js`)에 `getGatherings()`, `createGathering(body)` 등 함수 정의.
- 페이지·컴포넌트에서 이 계층만 사용. 전역 상태 라이브러리를 도입하지 않는다면 로딩/에러 상태용 훅(예: `useApi`) 추가 검토.

**결과:** 인증 헤더, 재시도, 로깅을 한 곳에서 관리; 에러 처리와 fetch 로직 중복 감소.

---

### 2.3 API 응답 DTO (필요해질 때)

**문제:** 컨트롤러가 JPA 엔티티를 반환함. 내부 필드(id, 타임스탬프, 연관)가 노출되고 API가 영속성에 묶임. MVP에는 허용 가능하나, 클라이언트별 다른 형태나 버저닝이 필요해지면 부담.

**조치 (불편해질 때까지 미룸):**

- 주요 리소스에 **응답 DTO** 도입(예: GatheringResponse, UserResponse), 서비스 또는 소규모 매퍼에서 entity → DTO 변환.
- 생성/수정용 요청 DTO는 이미 검증에 쓰고 있다면 유지; 그렇지 않으면 엔티티와 1:1이 아닌 필드를 받아야 할 때 도입.

**결과:** 안정적·문서화된 API 표면; 영속 구조를 바꿔도 클라이언트를 깨지 않고 변경 가능.

---

### 2.4 DB 마이그레이션

**문제:** `ddl-auto: update`는 개발에는 편하나 프로덕션에서는 위험함(제어되지 않는 스키마 변경, 롤백 불가).

**조치:**

- 첫 프로덕션 배포 또는 신경 쓰는 스키마 변경 전에 **Flyway** 또는 **Liquibase** 도입.
- 현재 스키마 기준으로 초기 마이그레이션 생성; prod에서는 `ddl-auto: validate`(또는 `none`)로 두어 마이그레이션 스크립트만 적용.

**결과:** 재현 가능·되돌리기 가능한 스키마 진화; prod에서의 예기치 않은 변경 감소.

---

### 2.5 비밀값과 프로덕션 설정

**문제:** DB 비밀번호 등이 `application.yml`에 있음; prod 비밀번호는 placeholder.

**조치:**

- 민감한 값은 **환경 변수** 사용(예: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_PASSWORD`), runbook 또는 README에 문서화(실제 비밀값은 커밋하지 않음).
- 선택: 프로필별 `application-prod.yml`에서 placeholder(예: `${DB_PASSWORD}`)를 참조하고, 실행 시 값을 주입.

**결과:** 저장소에 비밀값 없음; 배포가 더 안전하고 유연해짐.

---

## 3. 권장 적용 순서

1. **1.1** API 베이스 URL + 프론트 API 모듈  
2. **1.2** CORS 일원화  
3. **1.3** 전역 예외 처리  
4. **1.4** 주요 엔드포인트에 Bean Validation  
5. **2.1** User ↔ host/author 연결 및 “내” API  
6. **2.2** 프론트 API 계층  
7. **2.5** 프로덕션 전 비밀값 env화  
8. **2.4** prod 스키마 고정 시점에 마이그레이션  
9. **2.3** API 안정화 또는 다중 클라이언트 필요 시 DTO  

---

## 4. 문서 유지

- 새 계층·서비스 추가 또는 토폴로지 변경 시 **current-assessment.md**를 갱신.
- 아키텍처 결정(예: “Flyway 사용”)을 할 때는 `ai/architecture/adr/` 아래에 짧은 **ADR** 추가(예: `0001-use-flyway-for-migrations.md`), 맥락·결정·결과를 기록.
- 주요 기능 완료 시점이나 프로덕션 출시 전에 이 목록을 다시 검토.
