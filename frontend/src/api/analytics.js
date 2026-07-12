/**
 * TripGather Growth Analytics Helper
 * 
 * 이 모듈은 제품 출시 후 그로스 마케터들이 Mixpanel, Amplitude, GA4 등을 통해
 * 유저 행동 퍼널(AARRR)을 분석할 수 있도록 돕는 범용 트래킹 헬퍼입니다.
 * 현재는 개발/베타 환경을 위해 Console Mocking으로 구현되어 있으며, 
 * 상용 배포 시 환경 변수 설정에 따라 실제 분석 툴 SDK로 쉽게 스위칭할 수 있습니다.
 */

const IS_PROD = process.env.NODE_ENV === 'production';

// 실제 Mixpanel / GA4 SDK가 로드되어 있는 경우를 대비한 헬퍼
const getGlobalTracker = () => {
  return {
    mixpanel: window.mixpanel || null,
    gtag: window.gtag || null
  };
};

export const analytics = {
  /**
   * 사용자 식별 (로그인/가입 시 호출)
   * @param {string} userId - 사용자 고유 ID (이메일 등)
   * @param {Object} traits - 사용자 속성 (이름, 가입 경로 등)
   */
  identify: (userId, traits = {}) => {
    const { mixpanel, gtag } = getGlobalTracker();
    
    if (IS_PROD) {
      if (mixpanel) {
        mixpanel.identify(userId);
        mixpanel.people.set(traits);
      }
      if (gtag) {
        gtag('config', process.env.REACT_APP_GA_MEASUREMENT_ID, { 'user_id': userId });
      }
    }
    
    console.log(`[Analytics - Identify] User: ${userId}`, traits);
  },

  /**
   * 이벤트 트래킹 (일반 액션 발생 시 호출)
   * @param {string} eventName - 이벤트명 (예: 'itinerary_create', 'lounge_search')
   * @param {Object} properties - 이벤트 상세 속성
   */
  track: (eventName, properties = {}) => {
    const { mixpanel, gtag } = getGlobalTracker();
    const payload = {
      ...properties,
      timestamp: new Date().toISOString(),
      platform: 'web',
      viewport: `${window.innerWidth}x${window.innerHeight}`
    };

    if (IS_PROD) {
      if (mixpanel) {
        mixpanel.track(eventName, payload);
      }
      if (gtag) {
        gtag('event', eventName, payload);
      }
    }

    console.log(`[Analytics - Track] Event: "${eventName}"`, payload);
  },

  /**
   * 페이지 뷰 트래킹 (라우트 변경 시 호출)
   * @param {string} pagePath - 페이지 경로 (예: '/lounge', '/mypage')
   * @param {string} pageTitle - 페이지 제목
   */
  pageView: (pagePath, pageTitle = '') => {
    const { mixpanel, gtag } = getGlobalTracker();

    if (IS_PROD) {
      if (mixpanel) {
        mixpanel.track('page_view', { path: pagePath, title: pageTitle });
      }
      if (gtag) {
        gtag('event', 'page_view', {
          page_path: pagePath,
          page_title: pageTitle
        });
      }
    }

    console.log(`[Analytics - PageView] Path: "${pagePath}" (${pageTitle})`);
  }
};
