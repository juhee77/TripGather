# TripGather (가칭: 일정 공유 & 동네 모임 어플) 🗺️ 🤝

## 프로젝트 개요
나만의 여행 일정이나 루틴을 지도와 함께 기록 및 공유하고, "이번 주말 부산 갈 사람~", "오늘 저녁 한강 러닝 뛸 사람~" 처럼 관심사가 맞는 사람들을 가볍게 모을 수 있는 **소셜 모임 플랫폼**입니다.

주변 사람들과 소통할 수 있는 **직관적이고 친근한 UI**를 바탕으로, 복잡한 절차 없이 카테고리별로 일정을 구경하고 모임에 참여할 수 있는 활용성을 뛰어난 제공합니다.

## 기술 스택
### Backend
- **Language / Framework**: Java 17+, Spring Boot
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


