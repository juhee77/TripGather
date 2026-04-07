# TripGather (가칭: 일정 공유 & 동네 모임 어플) 🗺️ 🤝

<div align="center">
  <img src="./docs/assets/logo/logo.png" width="200" alt="TripGather Logo" />
  <p><strong>나의 여권에 찍히는 스탬프, 우리 동네에서 시작하는 새로운 여행</strong></p>
</div>

## 🌟 프로젝트 개요
나만의 여행 일정이나 루틴을 기록 및 공유하고, 관심사가 맞는 사람들을 가볍게 모을 수 있는 **프리미엄 소셜 모임 플랫폼**입니다.
단순한 모임을 넘어 '보딩 패스' 테마의 일정 관리와 '스탬프 북' 형태의 미션 시스템을 통해 일상을 여행처럼 즐길 수 있는 경험을 제공합니다.

---

## 📸 주요 기능 (Key Features)

### 1. 전 세계(동네)의 모임 발견 🔍
- **[피드 탐색]** 내 주변에서 열리는 다양한 관심사 기반 모임을 카드 형태로 확인합니다.
- **[지도 기반 탐색]** 지도 위 핀을 통해 내 위치 주변의 모임을 시각적으로 발견합니다.

### 2. 프리미엄 일정 관리 (Boarding Pass UI) ✈️
- **[항공권 테마 일정]** 여행 일정을 실제 보딩 패스 디자인으로 관리하여 여행의 설렘을 더합니다.
- **[나이트 플라이트 에디터]** 세련된 어두운 테마의 에디터로 여행 경로(Flight Path)를 정교하게 설계합니다.

### 3. 미션과 스탬프 북 (Gamification) 🎫
- **[활동 미션]** 모임 참여 시 주어지는 미션을 완료하고 실시간으로 진행률을 확인합니다.
- **[스탬프 북]** 완료된 미션은 나만의 스탬프 북에 기록되어 소장 가치를 높입니다.

### 4. 실시간 소통 시스템 💬
- **[그룹 및 1:1 대화]** 모임원들과 그룹 채팅은 물론, 관심 있는 에이전트와 1:1 DM을 통해 소통합니다.
- **[실시간 알림]** 새로운 참여 요청이나 메시지를 놓치지 않도록 실시간 푸시를 제공합니다.

### 5. AI 여행 인사이트 (Smart Widget) 🤖
- **[개인화 통계]** 사용자의 활동 데이터를 분석하여 여행 성향과 인사이트를 제공하는 대시보드 위젯을 제공합니다.

---

## 🏗️ 시스템 아키텍처 (Architecture)

### **[Backend] Layered Architecture & Clean Tech**
- **Spring Boot 3.3**: 견고한 백엔드 프레임워크 기반.
- **QueryDSL**: 복잡한 동적 쿼리를 타입 안정성 있게 처리.
- **Spring Security & JWT**: OAuth2(카카오, 네이버) 연동 및 토큰 기반 보안 강화.
- **WebSocket (STOMP)**: 저지연 실시간 채팅 처리.
- **MinIO**: 이미지 및 파일 업로드를 위한 고성능 스토리지.

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
- `MinIO` (S3 Compatible Storage)
- `Lombok`, `Flyway` (Migration)

### **Frontend**
- `JavaScript (ES6+)`, `React 19`
- `Vite`, `React Router 7`
- `Vanilla CSS` (Custom Design System)
- `Lucide React` (Icons)
- `StompJS`, `Axios`

---

## 💡 트러블슈팅 및 기술적 도전

### **1. 다크 테마(Night Flight) 가독성 최적화**
- **문제**: 어두운 배경의 모달 에디터 내에서 입력창 배경과 텍스트가 구분되지 않는 이슈.
- **해결**: `--night-bg` 및 `--night-surface` 토큰을 정의하고, 전용 `input-night` 클래스를 구현하여 고대비 시인성을 확보했습니다.

### **2. 실시간 채팅 데이터 매핑**
- **문제**: 그룹 대화 중 송신자 정보를 효율적으로 표시하고, DM 파트너를 식별하는 로직의 복잡성.
- **해결**: `senderEmail` 기반의 캐싱 및 `useChatViewModel` 내 파트너 필터링 로직을 통해 렌더링 부하를 줄이고 로직의 간결함을 유지했습니다.

---

## 🚀 실행 가이드

### **Backend**
```bash
cd backend
./gradlew bootRun
```

### **Frontend**
```bash
cd frontend
npm install
npm run dev
```

---

<p align="center">
  <b>TripGather</b>: 나만의 여정을 기록하고 우리 동네의 새로운 친구를 만나보세요.
</p>
