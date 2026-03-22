# TripGather (가칭: 일정 공유 & 동네 모임 어플) 🗺️ 🤝

[👉 프로젝트 실행 가이드 (GUIDE.md)](file:///Users/juhee/IdeaProjects/TripGather/GUIDE.md)

## 프로젝트 개요
나만의 여행 일정이나 루틴을 지도와 함께 기록 및 공유하고, "이번 주말 부산 갈 사람~", "오늘 저녁 한강 러닝 뛸 사람~" 처럼 관심사가 맞는 사람들을 가볍게 모을 수 있는 **소셜 모임 플랫폼**입니다.

주변 사람들과 소통할 수 있는 **직관적이고 친근한 UI**를 바탕으로, 복잡한 절차 없이 카테고리별로 일정을 구경하고 모임에 참여할 수 있는 활용성을 뛰어난 제공합니다.

## 기술 스택
### Backend
- **Language / Framework**: Java 17+, Spring Boot
- **API 문서**: Springdoc OpenAPI (Swagger UI) — 서버 실행 후 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) 에서 API 확인·테스트
- **Database**: 
  - **Local/Test**: H2 Database (개발 및 빠른 로컬 테스트)
  - **Production/Main**: PostgreSQL (실제 데이터 및 위치 기반 데이터의 안정적 저장)
- **ORM**: Spring Data JPA (추후 Map 적용을 위해 PostGIS 등 공간 DB 확장 고려)
- **Build Tool**: Gradle (또는 Maven)

### Frontend
- **Framework**: React (Vite 기반, 빠르고 부드러운 SPA 경험 제공)
- **Styling**: Vanilla CSS 및 따뜻한 파스텔톤/오렌지 포인트 컬러의 UI. 둥근 모서리와 여백을 활용한 깔끔한 모바일 뷰.
- **Map Library**: Kakao Map API 또는 Naver Map API (여행 경로, 장소 핀 찍기)

## 주요 기능 및 탭 구조 (확장성 고려)
앱 하단에 주요 탭을 구성하여 직관적인 네비게이션을 제공합니다.

1. **🗺️ 발견 (일정 및 장소 공유)**
   - 지도 기반으로 장소(위치 정보)를 선택해 나만의 루트/루틴 만들기
   - 시간대별 일정 작성 (예: 09:00 N서울타워, 12:00 맛집) 
   - 일정을 피드에 공개하여 다른 사람들에게 코스 추천

2. **🙋‍♂️ 모임 모집 (갈사람~)**
   - "@@ 갈 사람 생수구함" 과 같은 가벼운 모임 모집글 작성
   - **확장형 카테고리**: [여행], [맛집탐방], [스포츠/운동], [취미/클래스], [동네친목] 등
   - 게시글 내 댓글, 참여 인원 표시, 참여하기(채팅) 기능

3. **📍 지도 (내 주변 탐색)**
   - 내 위치를 기반으로 주변에서 열리는 모임이나 핫한 일정 코스를 지도에 핀셋 형태로 노출

4. **👤 마이페이지**
   - 내가 찜한 코스, 내가 참여/주최한 모임 목록 관리
   - 신뢰도를 위한 간단한 프로필 및 유저 매너 점수/뱃지 관리

## 🚀 유저 시나리오 및 워크플로우 (User Workflow)

**1. 영감 얻기 및 나만의 일정 계획 (Discovery & Itinerary)**
- 사용자는 앱에 접속하여 **'발견'** 탭에서 다른 사람들이 공유한 다양한 여행 일정과 관심사 피드를 구경합니다.
- 마음에 드는 장소를 참고하여, **'일정' 탭(My Flights)**에서 자신만의 여행 루트를 생성하고 지도에 핀셋(Route Points)을 추가해 상세 계획을 세웁니다.

**2. 동네/여행지 모임 주최 및 탐색 (Gather & Map)**
- 일정을 세웠지만 혼자 가기 아쉬운 경우 혹은 즉흥적인 만남이 필요한 경우, 우측 하단의 `+` 버튼을 눌러 **모임 모집글**을 작성합니다. ("내일 오후 2시 에펠탑 같이 가실 분!")
- 다른 사용자들은 **'지도(Map)'** 탭을 통해 자신의 현재 위치 또는 특정 지역 주변에 열려있는 모임 핀들을 시각적으로 확인하고 탐색합니다.

**3. 소통, 참여 신청 및 호스트 승인 (Interaction & Approval)**
- 마음에 드는 모임 핀을 클릭해 상세 모달 창을 엽니다. 궁금한 점이 있다면 실시간 **질문/댓글**을 남겨 소통한 후, `참여 신청하기` 버튼을 누릅니다.
- 모임 방장(Host)은 **'내 모임'** 탭에서 자신의 모임을 관리하며, 신청자 목록의 프로필을 확인하고 `승인(Check)` 또는 `거절(X)`을 결정합니다.

**4. 모임 확정 및 오프라인 만남 (Meetup)**
- 참여가 승인된 멤버들은 **'내 모임'** 탭에서 해당 일정이 활성화되어 한눈에 일정을 파악할 수 있게 됩니다.
- 참여자들끼리 (추후 구현될 채팅방 등에서) 자세한 약속을 잡고 성공적인 오프라인 모임을 가집니다.

## 프로젝트 구조 설계 (Backend / Frontend 분리)

```text
TripGather/
├── backend/                  # Spring Boot 기반의 API 서버
│   ├── src/main/java/        # 비즈니스 로직, 모델 (User, Itinerary, Gathering 등)
│   ├── src/main/resources/   # application.yml (H2 / PostgreSQL 프로필 분리)
│   └── build.gradle
│
└── frontend/                 # 모바일 퍼스트 React 앱
    ├── src/
    │   ├── components/       # 공통 UI (BottomNav, FeedCard, MapView 등)
    │   ├── pages/            # 주요 탭 화면 (Home, Map, Gather, MyPage 등)
    │   └── utils/            # 지도 API 연동 헬퍼 및 통신 코드
    └── package.json
```


