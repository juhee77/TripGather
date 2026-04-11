# 📚 TripGather Storybook Guide

이 문서는 TripGather(트립개더) 프로젝트의 프론트엔드 공통 UI 컴포넌트를 테스트하고 디자인을 확인할 수 있는 **Storybook**의 사용 가이드입니다. 

## 🚀 왜 Storybook인가요?

* 전체 앱을 실행시키지 않아도, 격리된 환경에서 컴포넌트 단위로 빠르게 시각적 테스트를 진행할 수 있습니다.
* 새로운 버튼 크기나 상태(Success, Error, Disabled 등)가 생겼을 때, 여러 화면을 뒤질 필요 없이 스토리북 내에서 바로 prop을 변경하며 결과를 관찰할 수 있습니다.
* 백엔드 API가 완성되지 않은 상태에서도 디자인 요구사항을 점검할 수 있어, 프론트엔드 개발 속도와 코드 재사용성이 비약적으로 증가합니다.

## 🛠️ Storybook 실행 방법

Storybook은 항상 `frontend` 폴더 내부에서 실행해야 합니다:

```bash
cd frontend
npm run storybook
```
명령어를 실행하면 자동으로 브라우저가 열리며 `http://localhost:6006`에서 Storybook UI를 확인할 수 있습니다.

## 📁 주요 공통 UI 컴포넌트 설명 (src/components/UI/)

현재 Storybook에 등록된 대표적인 공통 UI 컴포넌트들입니다:

### 1. PrimaryButton
프로젝트의 전반적인 클릭/제출 등 액션을 담당하는 공용 버튼입니다.
* **Variant 속성 지원**: `primary`, `secondary`, `glass`(투명 유리 느낌 패턴), `night`(어두운 투명 패턴), `outline`
* **옵션**: `loading={true}` 적용 시 버튼 내 텍스트가 자동으로 "PROCESSING..."으로 바뀌며 비활성화됩니다.

### 2. TicketContainer
항공권(Boarding Pass)과 영수증에서 영감을 받은 TripGather 특유의 핵심 컴포넌트입니다.
* **구조**: 가운데 점선(perforated divider)을 기준으로 위쪽 부분(`topSection`)과 아래쪽 상세 정보 부분(`bottomSection`)으로 분리하여 조립할 수 있습니다.
* **적용처**: 미션 카드, 그룹 참여 상세정보 등.

### 3. FormInput
유저 입력 창과 아이콘, 라벨을 감싸는 통일성 있는 모던 형태의 인풋 UI입니다.
* **옵션**: `dark={true}` 입력 시, 배경이 어두운 Modal에서 보이기 쉽게 글씨색과 배경 테두리 색을 가독성 있게 최적화해줍니다. (주로 로그인 모달 내에서 활용)

## ✍️ 새로운 스토리 파일 작성법

공통 컴포넌트를 만들거나 수정할 때는 반드시 Storybook 파일(`.stories.jsx`)을 같이 작성/수정하여 문서화를 동기화해야 합니다.

`components/UI/` 폴더 내의 새 컴포넌트 옆에 작성하세요.
예제 포맷 (Ex: `MyComponent.stories.jsx`):

```jsx
import MyComponent from './MyComponent';

export default {
  title: 'UI/MyComponent', // Storybook 좌측 메뉴에 표시될 트리 구조
  component: MyComponent,
};

// 기본 디자인 스토리
export const Default = {
  args: {
    label: '기본 텍스트',
    isActive: false,
  },
};

// 특정 상태의 다른 스토리
export const Active = {
  args: {
    label: '활성화 상태',
    isActive: true,
  },
};
```
