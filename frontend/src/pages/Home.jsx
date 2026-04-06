import React, { useState, useEffect } from 'react';
import FeedCard from '../components/FeedCard';
import CreateGatheringModal from '../components/CreateGatheringModal';
import GatheringDetailModal from '../components/GatheringDetailModal';
import ItineraryEditModal from '../components/ItineraryEditModal';
import TicketCard from '../components/TicketCard';
import ItineraryTab from '../components/ItineraryTab';
import ChatTab from '../components/ChatTab';
import ProfileTab from '../components/ProfileTab';
import MissionTab from '../components/MissionTab'; // Added
import TravelInsightWidget from '../components/TravelInsightWidget'; // Added Phase 4
import { useUser } from '../contexts/UserContext';
import { useGatheringsViewModel } from '../viewmodels/useGatheringsViewModel';
import { useMissionsViewModel } from '../viewmodels/useMissionsViewModel'; // Added
import { Search, Map as MapIcon, Plus, MessageCircle } from 'lucide-react';
import { authFetch } from '../api/client';

const Home = () => {
  const { user: currentUser } = useUser();
  const {
    gatherings,
    selectedRegion,
    actions: { handleRegionChange, refreshGatherings }
  } = useGatheringsViewModel();

  const [activeTab, setActiveTab] = useState('발견');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [editingItinerary, setEditingItinerary] = useState(null);
  const [showOnlyHosted, setShowOnlyHosted] = useState(false);
  const [selectedGathering, setSelectedGathering] = useState(null);
  const regions = ['전체', '강남구', '서초구', '송파구', '마포구', '용산구', '성동구', '종로구', '부산 해운대구', '제주도'];
  const [myJoinedIds, setMyJoinedIds] = useState(() => {
    try {
      const saved = localStorage.getItem('myJoinedIds');
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });

  const {
    activeMissions,
    actions: { fetchMissions, startMission, completeMission, completeStep }
  } = useMissionsViewModel();

  useEffect(() => {
    if (currentUser) fetchMissions();
  }, [currentUser, fetchMissions]);

  const handleGatheringCreated = (newGathering) => {
    refreshGatherings();
  };

  const tabs = ['발견', '내 모임', '일정']; // This line will be replaced by the new nav structure

  const handleUpdateItinerary = (updated) => {
    // Refresh missions and itineraries after edit
    fetchMissions();
    alert('일정이 성공적으로 수정되었습니다.');
  };

  const handleEditItinerary = (itinerary) => {
    setEditingItinerary(itinerary);
    setIsEditModalOpen(true);
  };

  return (
    <div className="app-container animate-fade">
      {/* Premium Header with Glassmorphism */}
      <header className="glass page-header" style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        padding: '24px 20px',
        position: 'sticky',
        top: 0,
        zIndex: 100,
        borderRadius: '0 0 var(--radius-lg) var(--radius-lg)'
      }}>
        <div>
          <h1 className="heading-l">모임</h1>
          <p className="text-s" style={{ 
            color: 'var(--primary-orange)', 
            fontWeight: 800, 
            textTransform: 'uppercase',
            fontSize: '12px',
            letterSpacing: '1px'
          }}>Discover & Join</p>
        </div>
        <div style={{ display: 'flex', gap: '12px' }}>
          <select 
            value={selectedRegion} 
            onChange={(e) => handleRegionChange(e.target.value)}
            style={{ 
              background: 'var(--bg-color)', 
              padding: '10px 16px', 
              borderRadius: 'var(--radius-full)', 
              fontSize: '14px', 
              fontWeight: 600,
              border: '1px solid var(--border-color)',
              color: 'var(--text-primary)',
              appearance: 'none',
              cursor: 'pointer',
              outline: 'none',
              boxShadow: 'inset 0 2px 4px rgba(0,0,0,0.02)'
            }}
          >
            {regions.map(r => (
              <option key={r} value={r}>{r === '전체' ? '📍 전체 지역' : r}</option>
            ))}
          </select>
          <button 
            onClick={() => setShowOnlyHosted(!showOnlyHosted)}
            className="icon-circle glass" 
            style={{ 
              width: '44px', height: '44px',
              background: showOnlyHosted ? 'var(--primary-gradient)' : 'white',
              border: showOnlyHosted ? 'none' : '1px solid var(--border-color)',
              color: showOnlyHosted ? 'white' : 'var(--text-primary)'
            }}
            title="내가 주최한 모임만 보기"
          >
            <Plus size={20} style={{ transform: showOnlyHosted ? 'rotate(45deg)' : 'none', transition: 'transform 0.3s' }} />
          </button>
          <button className="icon-circle glass" style={{ width: '44px', height: '44px' }}>
            <Search size={20} color="var(--text-primary)" />
          </button>
        </div>
      </header>

      {/* Next Trip Boarding Pass Dashboard [NEW Phase 2] */}
      {currentUser && (
        <div style={{ padding: '0 20px', marginTop: '10px' }}>
          {(() => {
            const myUpcoming = gatherings
              .filter(g => {
                const isHost = typeof g.host === 'string' ? g.host === currentUser?.name : g.host?.email === currentUser?.email;
                const isApproved = g.members?.some(m => m.user.email === currentUser?.email && (m.status === 'APPROVED'));
                return (isHost || isApproved);
              })
              .sort((a, b) => new Date(a.dates) - new Date(b.dates))[0];

            if (!myUpcoming) return null;

            return (
              <div 
                onClick={() => setSelectedGathering(myUpcoming)}
                className="glass animate-pop" 
                style={{ 
                  borderRadius: '24px', 
                  overflow: 'hidden', 
                  cursor: 'pointer',
                  border: '1px solid rgba(255, 92, 0, 0.2)',
                  boxShadow: '0 12px 30px rgba(255, 92, 0, 0.1)'
                }}
              >
                <div style={{ 
                  background: 'var(--primary-gradient)', 
                  padding: '12px 20px', 
                  display: 'flex', 
                  justifyContent: 'space-between', 
                  alignItems: 'center' 
                }}>
                  <span style={{ color: 'white', fontSize: '11px', fontWeight: 900, letterSpacing: '1px' }}>BOARDING PASS</span>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <div style={{ width: '6px', height: '6px', borderRadius: '50%', background: '#51cf66', boxShadow: '0 0 10px #51cf66' }} />
                    <span style={{ color: 'white', fontSize: '11px', fontWeight: 800 }}>READY TO GO</span>
                  </div>
                </div>
                <div style={{ padding: '20px', background: 'white', position: 'relative' }}>
                  {/* Perforated Line Decoration */}
                  <div style={{ position: 'absolute', left: '-10px', top: '50%', transform: 'translateY(-50%)', width: '20px', height: '20px', borderRadius: '50%', background: 'var(--bg-lite)' }} />
                  <div style={{ position: 'absolute', right: '-10px', top: '50%', transform: 'translateY(-50%)', width: '20px', height: '20px', borderRadius: '50%', background: 'var(--bg-lite)' }} />
                  
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div style={{ flex: 1 }}>
                      <h4 style={{ fontSize: '12px', color: 'var(--text-sub)', fontWeight: 800, marginBottom: '4px' }}>DESTINATION</h4>
                      <div style={{ fontSize: '20px', fontWeight: 900, color: 'var(--text-primary)' }}>{myUpcoming.location?.split(' ')[0] || 'SEOUL'}</div>
                    </div>
                    <div style={{ flex: 1, textAlign: 'center', position: 'relative' }}>
                      <div style={{ position: 'absolute', top: '50%', left: 0, right: 0, height: '2px', background: '#eee', zIndex: 0 }} />
                      <div style={{ position: 'relative', zIndex: 1, background: 'white', display: 'inline-block', padding: '0 10px' }}>
                        <MapIcon size={20} color="var(--primary-orange)" />
                      </div>
                    </div>
                    <div style={{ flex: 1, textAlign: 'right' }}>
                      <h4 style={{ fontSize: '12px', color: 'var(--text-sub)', fontWeight: 800, marginBottom: '4px' }}>GATE</h4>
                      <div style={{ fontSize: '20px', fontWeight: 900, color: 'var(--text-primary)' }}>{myUpcoming.id.toString().substring(0, 3)}</div>
                    </div>
                  </div>

                  <div style={{ marginTop: '16px', paddingTop: '16px', borderTop: '1px dashed #eee', display: 'flex', justifyContent: 'space-between' }}>
                    <div>
                      <label style={{ fontSize: '10px', color: 'var(--text-sub)', fontWeight: 800, display: 'block' }}>RESERVATION</label>
                      <span style={{ fontSize: '13px', fontWeight: 700 }}>{myUpcoming.title}</span>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                      <label style={{ fontSize: '10px', color: 'var(--text-sub)', fontWeight: 800, display: 'block' }}>DATE</label>
                      <span style={{ fontSize: '13px', fontWeight: 700 }}>{new Date(myUpcoming.dates).toLocaleDateString()}</span>
                    </div>
                  </div>
                </div>
              </div>
            );
          })()}
          
          {/* AI Travel Insight Widget [NEW Phase 4] */}
          <div style={{ marginTop: '20px' }}>
            <TravelInsightWidget user={currentUser} myMissions={activeMissions} />
          </div>
        </div>
      )}

      {/* Modern High-End Tabs */}
      <div style={{ 
        padding: '0 20px', 
        marginTop: '20px',
        marginBottom: '24px'
      }}>
        <nav style={{ display: 'flex', gap: '24px', overflowX: 'auto', paddingBottom: '10px' }} className="hide-scrollbar">
          {['발견', '일정', '나의 미션', '내 모임', '내 여권'].map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={activeTab === tab ? 'glass' : ''}
              style={{
                padding: '10px 20px',
                borderRadius: 'var(--radius-full)',
                background: activeTab === tab ? 'var(--primary-gradient)' : 'transparent',
                color: activeTab === tab ? 'white' : 'var(--text-secondary)',
                fontWeight: 700,
                fontSize: '15px',
                whiteSpace: 'nowrap',
                border: activeTab === tab ? 'none' : '1px solid var(--border-color)',
                boxShadow: activeTab === tab ? '0 10px 20px -5px rgba(255, 92, 0, 0.3)' : 'none'
              }}
            >
              {tab}
            </button>
          ))}
        </nav>
      </div>

      <div style={{ padding: '0 20px', flex: 1 }}>
        {activeTab === '발견' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {gatherings.filter(g => {
              const regionMatch = selectedRegion === '전체' || (g.location && g.location.includes(selectedRegion));
              const hostMatch = !showOnlyHosted || (
                currentUser && g.host && (
                  (typeof g.host === 'string' && g.host === currentUser.name) ||
                  (g.host.email === currentUser.email)
                )
              );
              return regionMatch && hostMatch;
            }).map((g, idx) => (
              <div 
                key={g.id} 
                onClick={() => setSelectedGathering(g)} 
                style={{ cursor: 'pointer', animationDelay: `${idx * 0.1}s` }}
                className="animate-fade"
              >
                <FeedCard
                  title={g.title}
                  host={typeof g.host === 'string' ? g.host : g.host?.name}
                  date={g.dates}
                  location={g.location}
                  joining={`${g.currentJoining}/${g.maxJoining}`}
                  bgImage={g.bgImageUrl}
                  pendingCount={
                    (currentUser && g.host && (
                      (typeof g.host === 'string' && g.host === currentUser.name) ||
                      (g.host.email === currentUser.email)
                    )) ? g.members?.filter(m => m.status === 'PENDING').length || 0 : 0
                  }
                />
              </div>
            ))}
            {gatherings.filter(g => selectedRegion === '전체' || (g.location && g.location.includes(selectedRegion))).length === 0 && (
              <div style={{ textAlign: 'center', padding: '100px 0' }}>
                <div style={{ fontSize: '40px', marginBottom: '12px' }}>🏜️</div>
                <p className="text-s" style={{ color: 'var(--text-secondary)', fontWeight: 600 }}>선택하신 지역에 모임이 없습니다.</p>
              </div>
            )}
          </div>
        )}

        {activeTab === '나의 미션' && (
          <MissionTab 
            activeMissions={activeMissions} 
            onMissionComplete={completeMission}
            onStepComplete={completeStep}
          />
        )}

        {activeTab === '내 모임' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {gatherings.filter(g => {
              const isHost = typeof g.host === 'string' ? g.host === currentUser?.name : g.host?.email === currentUser?.email;
              const isApproved = g.members?.some(m => m.user.email === currentUser?.email && (m.status === 'APPROVED'));
              const isCompleted = g.currentJoining >= g.maxJoining;
              return (isHost || isApproved) && isCompleted;
            }).map((g, idx) => (
              <div 
                key={g.id} 
                onClick={() => setSelectedGathering(g)} 
                style={{ cursor: 'pointer', animationDelay: `${idx * 0.1}s` }}
                className="animate-fade"
              >
                <FeedCard
                  title={g.title}
                  host={typeof g.host === 'string' ? g.host : g.host?.name}
                  date={g.dates}
                  location={g.location}
                  joining={`${g.currentJoining}/${g.maxJoining}`}
                  bgImage={g.bgImageUrl}
                  pendingCount={
                    (currentUser && g.host && (
                      (typeof g.host === 'string' && g.host === currentUser.name) ||
                      (g.host.email === currentUser.email)
                    )) ? g.members?.filter(m => m.status === 'PENDING').length || 0 : 0
                  }
                />
              </div>
            ))}
            {gatherings.filter(g => {
              const isHost = typeof g.host === 'string' ? g.host === currentUser?.name : g.host?.email === currentUser?.email;
              const isApproved = g.members?.some(m => m.user.email === currentUser?.email && (m.status === 'APPROVED'));
              const isCompleted = g.currentJoining >= g.maxJoining;
              return (isHost || isApproved) && isCompleted;
            }).length === 0 && (
              <div className="glass" style={{ 
                textAlign: 'center', 
                padding: '60px 24px',
                borderRadius: 'var(--radius-lg)',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                gap: '16px',
                background: 'white',
                border: '1px solid var(--border-color)'
              }}>
                <div style={{ fontSize: '48px' }}>🚀</div>
                <p style={{ fontWeight: 700, color: 'var(--text-primary)', textAlign: 'center' }}>
                  모집이 완료되어 확정된 모임만 여기에 표시됩니다.<br/>
                  <span style={{ fontSize: '13px', color: 'var(--text-secondary)', fontWeight: 600 }}>진행 중인 모임은 '내 여권 &gt; 주최 관리'에서 확인하세요.</span>
                </p>
                <button className="primary-btn" onClick={() => setActiveTab('발견')}>모임 탐색하기</button>
              </div>
            )}
          </div>
        )}

        {activeTab === '일정' && (
          <ItineraryTab onMissionStart={() => { fetchMissions(); setActiveTab('나의 미션'); }} onEdit={handleEditItinerary} />
        )}

        {activeTab === '내 여권' && (
          <ProfileTab />
        )}
      </div>

      {/* Floating Action Button - Enhanced */}
      {activeTab === '발견' && (
        <button
          onClick={() => setIsModalOpen(true)}
          className="primary-btn"
          style={{
            position: 'fixed',
            bottom: '110px',
            right: 'calc(50% - 240px + 24px)',
            width: '64px',
            height: '64px',
            borderRadius: '50%',
            padding: 0,
            zIndex: 90,
            boxShadow: '0 20px 40px rgba(255, 92, 0, 0.4)'
          }}
        >
          <Plus size={32} strokeWidth={3} />
        </button>
      )}

      {/* Modals remain the same but will eventually need updates */}
      {isModalOpen && (
        <CreateGatheringModal
          onClose={() => setIsModalOpen(false)}
          onCreated={handleGatheringCreated}
        />
      )}

      {selectedGathering && (
        <GatheringDetailModal
          gathering={selectedGathering}
          onClose={() => setSelectedGathering(null)}
          onUpdate={() => {
            refreshGatherings();
            // Refresh local selectedGathering to show new member status immediately
            const updated = gatherings.find(g => g.id === selectedGathering.id);
            if (updated) setSelectedGathering(updated);
          }}
          onDelete={(id) => {
            refreshGatherings();
            setSelectedGathering(null);
          }}
          onJoin={(updatedGathering) => {
            refreshGatherings();
            setSelectedGathering(updatedGathering);
          }}
        />
      )}

      {isEditModalOpen && (
        <ItineraryEditModal 
          isOpen={isEditModalOpen}
          onClose={() => setIsEditModalOpen(false)}
          itinerary={editingItinerary}
          onUpdate={handleUpdateItinerary}
        />
      )}
    </div>
  );
};

export default Home;
