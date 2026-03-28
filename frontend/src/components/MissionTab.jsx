import React, { useState } from 'react';
import RouteDetailModal from './RouteDetailModal';

const MissionTab = ({
  activeMissions,
  onMissionComplete,
  onStepComplete
}) => {
  const [selectedMission, setSelectedMission] = useState(null);

  if (activeMissions.length === 0) {
    return (
      <div style={{ padding: '24px', textAlign: 'center', color: 'var(--text-secondary)' }}>
        <p>현재 참여 중인 미션이 없습니다.</p>
        <p style={{ fontSize: '14px', marginTop: '8px' }}>일정 탭에서 새로운 미션을 시작해보세요!</p>
      </div>
    );
  }

  return (
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '20px' }}>
      {activeMissions?.map((mission) => {
        const completedSteps = mission?.steps?.filter(s => s.isCompleted || s.completed).length || 0;
        const totalSteps = mission?.steps?.length || 0;
        const progress = totalSteps > 0 ? Math.round((completedSteps / totalSteps) * 100) : 0;
        const isCompleted = mission?.status === 'COMPLETED';

        return (
          <div
            key={mission?.id}
            onClick={() => setSelectedMission(mission)}
            style={{
              background: 'var(--surface)',
              borderRadius: '16px',
              overflow: 'hidden',
              cursor: 'pointer',
              border: '1px solid var(--border-color)',
              position: 'relative'
            }}
          >
            {mission?.itineraryBgImageUrl && (
              <div style={{ height: '140px', overflow: 'hidden' }}>
                <img
                  src={mission.itineraryBgImageUrl}
                  alt={mission.itineraryTitle || 'Mission'}
                  style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                />
              </div>
            )}
            
            <div style={{ padding: '16px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                <h3 style={{ margin: 0, fontSize: '18px', color: 'var(--text-primary)' }}>
                  {mission?.itineraryTitle || 'Unknown Mission'}
                </h3>
                {isCompleted && (
                  <span style={{ 
                    background: '#E8F5E9', 
                    color: '#2E7D32', 
                    padding: '4px 8px', 
                    borderRadius: '8px', 
                    fontSize: '12px',
                    fontWeight: 600
                  }}>
                    CLEAR
                  </span>
                )}
              </div>
              
              <div style={{ color: 'var(--text-secondary)', fontSize: '14px', marginBottom: '16px' }}>
                {mission?.itineraryLocation || 'Unknown Location'} • {mission?.itineraryDates || 'No Dates'}
              </div>

              <div style={{ background: 'var(--bg-color)', borderRadius: '8px', height: '8px', overflow: 'hidden' }}>
                <div style={{ 
                  height: '100%', 
                  background: isCompleted ? '#2E7D32' : 'var(--primary)', 
                  width: `${progress}%`,
                  transition: 'width 0.3s ease'
                }} />
              </div>
              <div style={{ textAlign: 'right', fontSize: '12px', color: 'var(--text-secondary)', marginTop: '4px' }}>
                {progress}% ({completedSteps}/{totalSteps})
              </div>
            </div>
          </div>
        );
      })}

      {selectedMission && (
        <RouteDetailModal
          itinerary={selectedMission.itinerary || {}}
          onClose={() => setSelectedMission(null)}
          mission={selectedMission}
          onMissionComplete={(itineraryId) => {
            onMissionComplete?.(itineraryId);
            setSelectedMission(prev => ({...prev, status: 'COMPLETED'}));
          }}
          onStepComplete={onStepComplete}
        />
      )}
    </div>
  );
};

export default MissionTab;
