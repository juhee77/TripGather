import React, { useState } from 'react';
import { MissionStatus } from '../constants/enums';
import { ChevronRight } from 'lucide-react';
import RouteDetailModal from './RouteDetailModal';
import TicketContainer from './UI/TicketContainer';

const MissionTab = ({
  activeMissions,
  onMissionComplete,
  onStepComplete
}) => {
  const [selectedMissionId, setSelectedMissionId] = useState(null);
  const selectedMission = activeMissions?.find(m => m.id === selectedMissionId);

  if (!activeMissions || activeMissions.length === 0) {
    return (
      <div style={{ padding: '24px', textAlign: 'center', color: 'var(--text-secondary)' }}>
        <p>현재 참여 중인 미션이 없습니다.</p>
        <p style={{ fontSize: '14px', marginTop: '8px' }}>일정 탭에서 새로운 미션을 시작해보세요!</p>
      </div>
    );
  }

  return (
    <div style={{ 
      display: 'grid', 
      gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', 
      gap: '24px',
      background: 'var(--bg-color)',
      minHeight: '100%'
    }}>
      {activeMissions?.filter(m => m.status === MissionStatus.ACTIVE).map((mission, idx) => {
        const isCompleted = mission?.status === MissionStatus.COMPLETED;
        const completedSteps = mission?.steps?.filter(s => s.isCompleted || s.completed).length || 0;
        const totalSteps = mission?.steps?.length || 0;
        const progress = isCompleted ? 100 : (totalSteps > 0 ? Math.round((completedSteps / totalSteps) * 100) : 0);

        const topSection = (
          <div style={{ 
            background: progress === 100 ? '#10B981' : 'var(--primary-gradient)', 
            padding: '12px 20px', 
            display: 'flex', 
            justifyContent: 'space-between',
            alignItems: 'center'
          }}>
            <span style={{ color: 'white', fontSize: '11px', fontWeight: 900, letterSpacing: '1px' }}>
              {progress === 100 ? 'MISSION CLEAR' : 'ACTIVE MISSION'}
            </span>
            <div style={{ background: 'rgba(255,255,255,0.2)', padding: '2px 8px', borderRadius: '4px' }}>
              <span style={{ color: 'white', fontSize: '10px', fontWeight: 900 }}>NO. {mission.id}</span>
            </div>
          </div>
        );

        const bottomSection = (
          <div style={{ padding: '0' }}>
            <div className="flex-between" style={{ alignItems: 'flex-start' }}>
              <div style={{ flex: 1 }}>
                <span className="label-muted">DESTINATION</span>
                <h3 style={{ margin: '4px 0 0 0', fontSize: '20px', fontWeight: 900, color: 'var(--text-primary)' }}>
                  {mission?.itineraryTitle || 'Unknown Mission'}
                </h3>
              </div>
              <div style={{ textAlign: 'right' }}>
                <span className="label-muted">STOPS</span>
                <div style={{ fontSize: '18px', fontWeight: 900, color: 'var(--text-primary)' }}>{completedSteps}/{totalSteps}</div>
              </div>
            </div>
            
            <div className="ticket-divider" style={{ margin: '16px 0' }} />

            <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
              <div style={{ flex: 1 }}>
                <div className="flex-between" style={{ marginBottom: '6px' }}>
                  <span className="label-muted">PROGRESS</span>
                  <span style={{ fontSize: '12px', fontWeight: 900, color: 'var(--primary-orange)' }}>{progress}%</span>
                </div>
                <div style={{ background: 'var(--bg-color)', borderRadius: '4px', height: '6px', overflow: 'hidden' }}>
                  <div style={{ 
                    height: '100%', 
                    background: progress === 100 ? '#10B981' : 'var(--primary-gradient)', 
                    width: `${progress}%`,
                    transition: 'width 0.8s cubic-bezier(0.34, 1.56, 0.64, 1)'
                  }} />
                </div>
              </div>
              <button 
                className="icon-circle" 
                style={{ width: '40px', height: '40px', background: 'white', border: '1px solid var(--border-color)' }}
              >
                <ChevronRight size={18} color="var(--text-primary)" />
              </button>
            </div>

            <div style={{ marginTop: '16px', display: 'flex', gap: '8px' }}>
              <span style={{ fontSize: '12px', color: 'var(--text-muted)', fontWeight: 600 }}>🏷️ {mission?.itineraryLocation || 'Location'}</span>
              <span style={{ fontSize: '12px', color: 'var(--text-muted)', fontWeight: 600 }}>📅 {mission?.itineraryDates || 'Dates'}</span>
            </div>
          </div>
        );

        return (
          <TicketContainer
            key={mission?.id}
            onClick={() => setSelectedMissionId(mission.id)}
            topSection={topSection}
            bottomSection={bottomSection}
            className="mission-ticket"
          />
        );
      })}

      {selectedMission && (
        <RouteDetailModal
          itinerary={{
            ...selectedMission.itinerary,
            id: selectedMission.id, 
            steps: selectedMission.steps,
            status: selectedMission.status,
            itineraryId: selectedMission.itineraryId
          }}
          onClose={() => setSelectedMissionId(null)}
          onStepComplete={onStepComplete}
        />
      )}
    </div>
  );
};

export default MissionTab;
