# TripGather (일정 공유 & 동행 소셜 플랫폼) 🗺️ 🤝

<div align="center">
  <img src="./docs/assets/logo/logo.png" width="140" alt="TripGather Logo" />
  <br />
  <p><strong>"나의 여권에 찍히는 스탬프, 우리 동네에서 시작하는 새로운 여행"</strong></p>
  <p>
    <img src="https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen?logo=springboot" alt="Spring Boot" />
    <img src="https://img.shields.io/badge/React-19.0-blue?logo=react" alt="React" />
    <img src="https://img.shields.io/badge/Vite-7.3-646CFF?logo=vite" alt="Vite" />
    <img src="https://img.shields.io/badge/Tests-275%20PASS-success?logo=junit5" alt="Tests" />
    <img src="https://img.shields.io/badge/Coverage-94%25-brightgreen?logo=jacoco" alt="Coverage" />
  </p>
</div>

---

## 🌟 프로젝트 개요

TripGather는 나만의 여행 일정이나 코스를 공유하고 관심사가 맞는 동행을 가볍게 모을 수 있는 **프리미엄 소셜 여정 플랫폼**입니다.
단순한 모임을 넘어 '라운지(Lounge)'에서의 여정 탐색, '비행 계획(Flight Plan)' 수립, 그리고 '스탬프 여권(Passport)' 기반의 혜택 및 미션 달성을 통해 일상을 여행처럼 즐길 수 있는 특별한 경험을 제공합니다.

---

## 📸 실제 서비스 UI 갤러리 (Live Visual Showcase)

<div align="center">
  <h3>🗺️ 1. 메인 여정 랜딩 & 탐색 피드 (Lounge Discovery)</h3>
  <img src="./docs/images/home_page.png" width="850" alt="TripGather Home Discovery UI" />
  <p><i>프리미엄 글래스모피즘 디자인과 실시간 인기 여행 코스 및 보딩패스 카드를 제공합니다.</i></p>
</div>

<br />

<div align="center">
  <h3>🤝 2. 관심사 기반 동행 모집 (Gathering Matching)</h3>
  <img src="./docs/images/gathering_list.jpg" width="850" alt="Gathering Matching UI" />
  <p><i>목적지별 모임 태그, 참여 정원 현황 및 호스트 프로필을 직관적으로 탐색합니다.</i></p>
</div>

<br />

<div align="center">
  <h3>📅 3. 스마트 일정 플래너 & 준비물 체크리스트 (Itinerary & Checklist)</h3>
  <img src="./docs/images/itinerary_checklist.jpg" width="850" alt="Itinerary & Checklist UI" />
  <p><i>일자별 이동 타임라인과 실시간 준비물 달성률(%)을 시각적으로 관리합니다.</i></p>
</div>

<br />

<div align="center">
  <h3>📔 4. 여권 스탬프 & 마이페이지 대시보드 (Passport & Profile)</h3>
  <img src="./docs/images/mypage_stamps.jpg" width="850" alt="Passport & Stamps UI" />
  <p><i>여행 달성 레벨 배지, 여권 스타일 디지털 스탬프 수집 및 리워드 포인트 이력을 제공합니다.</i></p>
</div>

---

## 📸 주요 기능 (Key Features)

### 1. 라운지 (Lounge): 새로운 여정의 발견 🔍
- **[탐색 피드]** 내 주변에서 열리는 다양한 관심사 기반 여정을 '보딩 패스' 디자인의 카드 형태로 확인합니다.
- **[실시간 핫플레이스]** 인기 있는 여행지와 급상승 중인 모집 공고를 한눈에 파악합니다.

### 2. 비행 계획 (Flight Plan): 프리미엄 여정 설계 ✈️
- **[항공권 테마 플래너]** 여행 일정을 실제 항공권 디자인으로 구성하여 직관적이고 몰입감 있는 플래닝 기능을 제공합니다.
- **[준비물 체크리스트]** 여행 카테고리별 준비물을 관리하고 달성률(%)을 실시간 계산합니다.

### 3. 크루와 챌린지 (Crew & Challenge) 🎫
- **[크루 시스템]** 단순 참여자가 아닌 함께 모험을 떠나는 '크루'로서의 소속감을 부여하며, 승인된 크루만 전용 콘텐츠에 접근할 수 있습니다.
- **[디지털 여권 & 스탬프]** 여정 중 주어지는 챌린지를 완료하고, 나만의 여권(Passport)에 디지털 스탬프를 수집합니다.

### 4. 무전과 갤러리 (Radio & Gallery) 💬
- **[크루 무전 (Radio)]** WebSocket STOMP 기반 승인 크루 전용 실시간 대화망을 지원합니다.
- **[여행 갤러리]** 여정의 순간들을 기록하고 공유하는 공간을 제공합니다.

---

## 🏗️ 사용자 경험 (UX) & 화면 흐름도 (Screen Flow)

```mermaid
graph TD
    A["🔑 로그인 (Login)<br>글래스모피즘 인증"] --> B["🔍 라운지 (Lounge Feed)<br>동네 모임 & 보딩패스 탐색"]
    B -->|여정 상세 확인| C["✈️ 여정 정보 (Itinerary Detail)<br>경로(Flight Path) & 세부 일정 탐색"]
    C -->|참여 신청 & 승인| D["🛂 나의 터미널 (Host Terminal)<br>신청 승인/거절 및 참여 관리"]
    D -->|실시간 소통 & 조정| E["💬 무전기 채팅 (Radio Room)<br>실시간 STOMP 크루 무전망"]
    E -->|미션 달성 & 스탬프| F["📔 디지털 여권 (Passport)<br>디지털 스탬프 날인 & 포인트 적립"]
    
    style A fill:#1a1c2e,stroke:#4b5563,stroke-width:2px,color:#fff
    style B fill:#1a1c2e,stroke:#3b82f6,stroke-width:2px,color:#fff
    style C fill:#1a1c2e,stroke:#8b5cf6,stroke-width:2px,color:#fff
    style D fill:#1a1c2e,stroke:#10b981,stroke-width:2px,color:#fff
    style E fill:#1a1c2e,stroke:#f59e0b,stroke-width:2px,color:#fff
    style F fill:#1a1c2e,stroke:#ec4899,stroke-width:2px,color:#fff
```

---

## 🛠️ 핵심 기술 스택 (Tech Stack)

### **Backend**
- `Java 17`, `Spring Boot 3.3.4`
- `Spring Data JPA`, `QueryDSL`, `PostgreSQL`, `H2`
- `Spring Security`, `OAuth2 (Kakao/Naver)`, `JWT`
- `WebSocket`, `STOMP`
- `JUnit 5`, `Mockito`, `JaCoCo` (347개 테스트 전수 통과)

### **Frontend**
- `JavaScript (ES6+)`, `React 19`
- `Vite`, `React Router 7`
- `Vanilla CSS` (Glassmorphism 디자인 시스템)
- `StompJS`, `Axios`

---

## 🛡️ 품질 보증 및 검증 지표 (Quality & Reliability)

- **[단위 & 통합 테스트 275개 100% PASS]**: 백엔드 서비스/컨트롤러 전반 275개 테스트 케이스 100% 무결점 검증 완료.
- **[JaCoCo 라인 커버리지 94%]**: 서비스 레이어 라인 커버리지 94%(브랜치 72%). DTO/도메인/설정/리포지토리/컨트롤러를 제외한 비즈니스 로직 클래스 기준이며, 클래스별 최소 80% 규칙을 `check` 태스크에 연결해 `./gradlew build` 및 CI 에서 실제로 강제합니다.
- **[보안 및 예외 검증 연동]**: null 방어, 권한 검증 및 입력 유효성 검사 적용 완료.

---

## 🚀 빠른 로컬 실행 가이드

### **1. Backend 실행**
```bash
cd backend
./gradlew bootRun -Dstorage.type=local
```
*(H2 인메모리 DB 및 로컬 디스크 파일 업로드가 자동으로 활성화됩니다.)*

### **2. Frontend 실행**
```bash
cd frontend
npm install
npm run dev
```

---

<p align="center">
  <b>TripGather</b>: 나만의 여정을 기록하고 우리 동네의 새로운 동행을 만나보세요. ✈️
</p>
