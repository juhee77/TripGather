# TripGather (가칭: 일정 공유 & 동네 모임 어플) 🗺️ 🤝

<div align="center">
  <img src="./docs/assets/logo/logo.png" width="200" alt="TripGather Logo" />
  <p><strong>나의 여권에 찍히는 스탬프, 우리 동네에서 시작하는 새로운 여행</strong></p>
</div>

## 🌟 프로젝트 개요
나만의 여행 일정이나 루틴을 기록 및 공유하고, 관심사가 맞는 사람들을 가볍게 모을 수 있는 **프리미엄 소셜 여정 플랫폼**입니다.
단순한 모임을 넘어 '라운지(Lounge)'에서의 발견, '비행 계획(Flight Plan)' 수립, 그리고 '내 탑승권(Boarding Pass)' 기반의 참여를 통해 일상을 여행처럼 즐길 수 있는 프리미엄한 경험을 제공합니다.

---

## 📸 주요 기능 (Key Features)

### 1. 라운지 (Lounge): 새로운 여정의 발견 🔍
- **[탐색 피드]** 내 주변에서 열리는 다양한 관심사 기반 여정을 '보딩 패스' 디자인의 카드 형태로 확인합니다.
- **[실시간 핫플레이스]** 인기 있는 여행지와 급상승 중인 모집 공고를 한눈에 파악합니다.

### 2. 비행 계획 (Flight Plan): 프리미엄 여정 설계 ✈️
- **[Aviation-inspired Planner]** 각 경유지별 **ARR(Arrival/Landing)** 및 **DEP(Departure/Take-off)** 시간을 설정하여 실제 비행 스케줄과 같은 정밀한 계획이 가능합니다.
- **[Aviation Dual Slider]** 듀얼 핸들 슬라이더를 통해 방문 시간대를 직관적으로 조절하며, 체류 시간(STAY)을 자동으로 계산합니다.
- **[나이트 플라이트 에디터]** 세련된 다크 테마의 에디터로 여행 경로(Flight Path)와 미션을 정교하게 설계합니다.

### 3. 크루와 챌린지 (Crew & Challenge) 🎫
- **[크루 시스템]** 단순 참여자가 아닌 함께 모험을 떠나는 '크루'로서의 소속감을 부여하며, 승인된 크루만 전용 콘텐츠에 접근할 수 있습니다.
- **[챌린지 & 복사 기능]** 관심 있는 비행 계획을 내 여정으로 **'발권(Clone)'**하여 커스텀할 수 있으며, 챌린지 완료 후 여권(Passport)에 디지털 스탬프를 수집합니다.

### 4. 무전과 실시간 채팅 (Radio & Live Chat) 💬
- **[실시간 무전 (STOMP)]** 저지연 WebSocket 기반의 채팅 채널로, 여행 중의 긴밀한 연결성을 보장합니다.
- **[갤러리 (Gallery)]** 여정의 순간들을 기록하고 공유하는 공간으로, 크루 전용 또는 선택적 공개가 가능합니다.

### 5. 여행 인사이트 (Travel Insight) 🤖
- **[AI 대시보드]** 사용자의 활동 데이터를 분석하여 여행 성향과 챌린지 달성률을 시각화하는 스마트 위젯을 제공합니다.

---

## 🏗️ 시스템 아키텍처 (Architecture)

### **[Backend] Layered Architecture & Clean Tech**
- **Spring Boot 3.3**: 견고한 백엔드 프레임워크 기반.
- **QueryDSL**: 복잡한 동적 쿼리를 타입 안정성 있게 처리.
- **Spring Security & JWT**: OAuth2(카카오, 네이버) 연동 및 토큰 기반 보안 강화.
- **WebSocket (STOMP)**: `connectionRef` 및 안정화 로직이 적용된 리얼타임 통신.
- **MinIO**: 이미지 및 파일 업로드를 위한 S3 호환 고성능 스토리지.

### **[Frontend] ViewModel Pattern & Glassmorphism**
- **React 19 & Vite**: 빠른 개발 속도와 최신 리액트 기능 활용.
- **Custom Hook ViewModel**: UI 레이어와 비즈니스 로직(API 통신 등)을 완벽히 분리.
- **Premium Design System**: Vanilla CSS를 활용한 글래스모피즘 및 'Night Flight' 특화 UI.

---

## 🛠️ 기술 스택 (Tech Stack)

### **Backend**
- `Java 17`, `Spring Boot 3.3.4`
- `Spring Data JPA`, `QueryDSL`, `PostgreSQL`, `H2`
- `Spring Security`, `OAuth2 (Kakao/Naver)`, `JWT`
- `WebSocket`, `STOMP`, `SockJS`
- `MinIO` (S3 Compatible Storage), `Lombok`, `Flyway`

### **Frontend**
- `JavaScript (ES6+)`, `React 19`
- `Vite`, `React Router 7`
- `Vanilla CSS` (Custom Design System), `Lucide React`
- `StompJS`, `Axios`, `SockJS`

---

## 💡 트러블슈팅 및 기술적 도전

### **1. 비행기 컨셉의 듀얼 타임 범위 슬라이더 구현**
- **도전**: HTML5 기본 `range input`은 단일 핸들만 지원하여, 비행 스케줄(범위)을 직관적으로 표현하기 어려움.
- **해결**: 두 개의 슬라이더를 레이어링하고 `pointer-events`를 활용해 겹치는 영역을 제어하는 **Custom Dual-Thumb Slider**를 구현했습니다. 이를 통해 ARR/DEP 기반의 유연한 시간 관리(Aviation Style)를 성공적으로 도입했습니다.

### **2. WebSocket 연결의 신뢰성 및 지속성 확보**
- **도전**: React의 리렌더링 주기에 따라 웹소켓 연결이 빈번하게 끊어지고 다시 맺어지는 '연결 플래핑(Flapping)' 현상 발생.
- **해결**: `useChatViewModel` 내부에 `connectionRef`를 도입하여 현재 연결 상태를 추적하고, 동일한 세션에서는 불필요한 재연결을 방지하도록 로직을 고도화했습니다.

---

## 🚀 실행 가이드

### **Environment Setup (Mise)**
본 프로젝트는 도구 관리자 `mise`를 통해 개발 환경을 통합 관리합니다.
```bash
mise install
mise run dev  # 프론트엔드와 백엔드 개발 서버 동시 실행
```

### **Manual Build**
```bash
# Backend
cd backend && ./gradlew bootRun

# Frontend
cd frontend && npm install && npm run dev
```

---

<p align="center">
  <b>TripGather</b>: 나만의 여정을 기록하고 우리 동네의 새로운 친구를 만나보세요.
</p>
