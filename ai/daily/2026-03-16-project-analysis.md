# TripGather 프로젝트 AI 데일리 노트

**작성일:** 2026-03-16

---

## 1. 오늘의 핵심 작업

오늘은 TripGather 프로젝트에 **사용자 인증 시스템(JWT)**을 처음으로 도입한 날입니다.  
백엔드에 Spring Security + JJWT를 추가하고, 프론트엔드에 로그인/회원가입 페이지를 구축했습니다.  
SNS 연동(카카오/구글 OAuth2)을 위한 `provider` 필드도 사전 설계하여 미래 확장을 고려했습니다.

---

## 2. 주요 변경 사항

### 2.1 백엔드

| 파일 | 변경 내용 |
|---|---|
| `build.gradle` | Spring Security, JJWT 0.12.6 의존성 추가 |
| `User.java` | email, password, provider, role 필드 추가, UserDetails 구현 |
| `UserRepository.java` | `findByEmail()` 추가 |
| `SecurityConfig.java` | 인증 필터 체인, BCrypt, public 경로 설정 |
| `JwtTokenProvider.java` | accessToken(24h) / refreshToken(7d) 발급·검증 |
| `JwtAuthenticationFilter.java` | Bearer 토큰 파싱 → SecurityContext 주입 |
| `AuthController.java` | POST /api/auth/register, POST /api/auth/login |
| `AuthService.java` | 회원가입·로그인 비즈니스 로직 |
| `DataInitializer.java` | 기본 유저에 이메일/BCrypt 비밀번호 추가 |

### 2.2 프론트엔드

| 파일 | 변경 내용 |
|---|---|
| `AuthContext.jsx` | 전역 로그인 상태, localStorage 토큰 관리 |
| `LoginPage.jsx` | 로그인 / 회원가입 탭 전환 UI |
| `App.jsx` | /login 라우트, PrivateRoute 인증 가드 추가 |
| `api/client.js` | authFetch() - Authorization 헤더 자동 첨부 |
| `UserContext.jsx` | authFetch 기반으로 변경 |

---

## 3. 아키텍처 결정

### JWT vs 세션
- **선택: JWT (stateless)**
- 이유: 나중에 모바일 앱 또는 SNS OAuth 연동 시 세션보다 유연함
- accessToken 만료: **24시간** (개발 편의), 프로덕션엔 15분 권장

### 토큰 저장 위치
- **선택: localStorage** (초기 개발 편의상)
- 주의: XSS 취약. 프로덕션 전환 시 **HttpOnly Cookie** 방식으로 변경 권장

### SNS 연동 대비 설계
- `User.provider` 필드: `"local"` / `"kakao"` / `"google"`
- 나중에 `spring-boot-starter-oauth2-client` 추가 시 별도 OAuth2 로그인 엔드포인트 구현

---

## 4. 테스트 결과

- `AuthServiceTest`: 회원가입 중복 이메일, 로그인 성공/실패 케이스 검증
- `JwtTokenProviderTest`: 토큰 발급·파싱·만료 검증
- `AuthControllerTest`: REST API 엔드포인트 통합 테스트 (MockMvc)
- 전체 테스트 **통과 (PASSED)**

---

## 5. 현재 상태 (개선 포인트)

1. **refreshToken 미구현** - 현재 accessToken만 발급. 만료 시 재로그인 필요.
2. **SNS 로그인 버튼 비활성** - UI에만 자리 마련, 실제 OAuth2 연동 미구현.
3. **권한(ROLE) 분리 미적용** - role 필드는 있지만 ADMIN vs USER 접근 제어 미구현.
4. **프로필 이미지 업로드** - 현재 URL 입력 방식. S3/Cloudinary 연동 예정.

---

## 6. 다음 작업 제안

1. Refresh Token 엔드포인트 구현 (`POST /api/auth/refresh`)
2. SNS OAuth2 로그인 (카카오/구글) 구현
3. 댓글/모임 작성자를 로그인 유저 기반으로 연결 (하드코딩 제거)
4. 프로필 이미지 업로드 기능 추가
5. 프로덕션 배포 시 JWT 저장을 HttpOnly Cookie로 전환

---

*이 노트는 Antigravity AI 에이전트가 자동 생성한 개발 일지입니다.*
