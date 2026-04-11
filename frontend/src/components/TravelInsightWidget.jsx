import React, { useMemo } from 'react';
import { MissionStatus } from '../constants/enums';
import { Sparkles, Lightbulb, TrendingUp, Compass } from 'lucide-react';

const TravelInsightWidget = ({ user, myMissions }) => {
  const completedMissions = useMemo(() => myMissions?.filter(m => m.status === MissionStatus.COMPLETED) || [], [myMissions]);
  const points = user?.points || 0;

  const insight = useMemo(() => {
    if (completedMissions.length >= 5) {
      return {
        icon: <Sparkles size={20} color="var(--primary-orange)" />,
        title: "베테랑 탐험가 등극!",
        message: `벌써 ${completedMissions.length}개의 스탬프를 모으셨네요. 당신은 이제 완벽한 TripGather 전문가입니다!`
      };
    } else if (points > 100) {
      return {
        icon: <TrendingUp size={20} color="#51cf66" />,
        title: "포인트 부자!",
        message: "활동 포인트가 100점을 넘었습니다! 포인트로 더 즐거운 여행을 계획해 보세요."
      };
    } else if (completedMissions.length > 0) {
      return {
        icon: <Compass size={20} color="#4dabf7" />,
        title: "성장하는 여행자",
        message: "첫 스탬프의 설렘을 잊지 마세요. 다음 여행지도 당신을 기다리고 있습니다!"
      };
    } else {
      return {
        icon: <Lightbulb size={20} color="#ffd43b" />,
        title: "여행의 시작",
        message: "아직 첫 모임에 참여하지 않으셨나요? 지금 근처의 인기 있는 모임을 확인해 보세요!"
      };
    }
  }, [completedMissions, points]);

  return (
    <div className="glass animate-fade" style={{ 
      padding: '16px 20px', 
      borderRadius: '20px', 
      background: 'white',
      border: '1px solid rgba(0,0,0,0.03)',
      boxShadow: '0 8px 30px rgba(0,0,0,0.04)',
      display: 'flex',
      gap: '14px',
      alignItems: 'center'
    }}>
      <div style={{ 
        width: '44px', 
        height: '44px', 
        borderRadius: '12px', 
        background: 'var(--bg-lite)', 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: 'center',
        flexShrink: 0
      }}>
        {insight.icon}
      </div>
      <div>
        <h4 style={{ fontSize: '14px', fontWeight: 800, color: 'var(--text-primary)', marginBottom: '2px' }}>{insight.title}</h4>
        <p style={{ fontSize: '13px', color: 'var(--text-secondary)', lineHeight: 1.4 }}>{insight.message}</p>
      </div>
    </div>
  );
};

export default TravelInsightWidget;
