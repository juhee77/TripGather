# TripGather: JWT 인증 및 데일리 노트 구현 결과

오늘 작업을 통해 TripGather 프로젝트에 사용자 인증 시스템의 기초를 마련하고, 프로젝트 분석 데일리 노트를 작성했습니다.

## 1. AI 데일리 노트
- **위치:** [2026-03-16-project-analysis.md](file:///Users/juhee/IdeaProjects/TripGather/ai/daily/2026-03-16-project-analysis.md)
- **내용:** 현재 프로젝트 아키텍처 분석, 인증 시스템 설계 결정(JWT, HS256), 향후 SNS 연동을 위한 확장성 고려 사항 등을 기록했습니다.

## 2. 백엔드 인증 시스템 (Spring Security + JWT)

### 주요 구현 사항
- **JWT 보안 계층:** JJWT 0.12.6을 사용하여 `JwtTokenProvider`와 `JwtAuthenticationFilter`를 구현했습니다.
- **Security 설정:** `SecurityConfig`를 통해 무상태(stateless) 인증 및 경로별 접근 권한을 설정했습니다.
- **사용자 정보 확충:** `User` 엔티티에 이메일, 비밀번호(BCrypt 해시), 소셜 연동용 `provider` 필드를 추가했습니다.
- **데이터 초기화:** 기존 샘플 유저에게 로그인 가능한 계정 정보(암호화된 비번)를 할당했습니다.

### 테스트 결과
- `AuthServiceTest`: 회원가입 중복 체크 및 로그인 비즈니스 로직 검증 완료.
- `JwtTokenProviderTest`: 토큰 생성 및 파싱 무결성 검증 완료.
- **결과:** `./gradlew test` 통과 (BUILD SUCCESSFUL)

## 3. 프론트엔드 인증 UI/UX

### 주요 구현 사항
- **AuthContext:** 전역에서 로그인 상태 및 토큰을 관리하며, 모든 요청에 JWT 헤더를 자동 포함하는 `authFetch` 헬퍼를 도입했습니다.
- **LoginPage:** 세련된 디자인의 로그인/회원가입 카드 UI를 구현했습니다. (Kakao/Google 버튼은 자리 마련)
- **라우팅 보안:** `PrivateRoute`와 `AuthProvider`를 통해 인가되지 않은 사용자가 홈, 지도, 마이페이지 등에 접근할 시 자동으로 로그인 페이지로 리다이렉트되도록 구성했습니다.

## 4. Git 작업 완료
- 모든 변경 사항을 스테이징, 커밋하고 원격 저장소(`main` 브랜치)에 푸시했습니다.

> [!NOTE]
> **테스트용 계정 정보:**
> - 이메일: `jihyun@test.com` / `alex@test.com`
> - 비밀번호: `pass1234`
