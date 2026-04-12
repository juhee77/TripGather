import React, { useState, useEffect } from 'react';
import FeedCard from '../components/FeedCard';
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
import { useNavigate } from 'react-router-dom';
import { MemberStatus } from '../constants/enums';

const Home = () => {
  const navigate = useNavigate();
  const { user: currentUser } = useUser();
  const {
    gatherings,
    selectedRegion,
    searchQuery,
    availableOnly,
    actions: { handleRegionChange, handleSearchQueryChange, handleAvailableOnlyChange, refreshGatherings }
  } = useGatheringsViewModel();

  const [activeTab, setActiveTab] = useState('라운지');
  const [showOnlyHosted, setShowOnlyHosted] = useState(false);
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
    // Refresh missions and other data after edit
    fetchMissions();
    refreshGatherings(); // Added to ensure UI consistency
  };

  const handleEditItinerary = (itinerary) => {
    navigate(`/itinerary/edit/${itinerary.id}`);
  };

  return (
    <div className="animate-fade" style={{ background: 'var(--bg-lite)', minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {/* Premium Header with Boarding Pass Aesthetic */}
      <header className="glass page-header" style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        padding: '28px 20px',
        position: 'sticky',
        top: 0,
        zIndex: 100,
        borderRadius: '0 0 var(--radius-xl) var(--radius-xl)',
        background: 'rgba(255, 255, 255, 0.9)',
        borderBottom: '1px solid var(--border-color)'
      }}>
        <div>
          <span className="label-orange">GATHERING TERMINAL</span>
          <h1 className="heading-l" style={{ marginTop: '4px' }}>라운지</h1>
        </div>
        <div style={{ display: 'flex', gap: '10px' }}>
          <input 
            type="text" 
            placeholder="모임 검색..." 
            value={searchQuery || ''}
            onChange={(e) => handleSearchQueryChange(e.target.value)}
            style={{ 
              background: 'white', padding: '12px 18px', borderRadius: '16px', fontSize: '14px',
              border: '1px solid var(--border-color)', outline: 'none', flex: '1 1 150px',
              maxWidth: '250px',
              fontWeight: 600,
              color: 'var(--text-primary)'
            }}
          />
          <select 
            value={selectedRegion} 
            onChange={(e) => handleRegionChange(e.target.value)}
            style={{ 
              background: 'white', 
              padding: '12px 18px', 
              borderRadius: '16px', 
              fontSize: '14px', 
              fontWeight: 800,
              border: '1px solid var(--border-color)',
              color: 'var(--text-primary)',
              appearance: 'none',
              cursor: 'pointer',
              boxShadow: '0 4px 12px rgba(0,0,0,0.03)'
            }}
          >
            {regions.map(r => (
              <option key={r} value={r}>{r === '전체' ? '📍 전체 지역' : r}</option>
            ))}
          </select>
          <button 
            title="모집 중 모임만 보기"
            onClick={() => handleAvailableOnlyChange(!availableOnly)}
            className="icon-circle" 
            style={{ 
              width: '48px', height: '48px',
              flexShrink: 0,
              background: availableOnly ? 'var(--primary-gradient)' : 'white',
              border: '1px solid var(--border-color)',
              color: availableOnly ? 'white' : 'var(--text-primary)',
              boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
              transition: 'all 0.3s'
            }}
          >
            <Search size={20} />
          </button>
          <button 
            title="내가 호스트인 모임만 보기 (작업 예정)"
            onClick={() => setShowOnlyHosted(!showOnlyHosted)}
            className="icon-circle" 
            style={{ 
              width: '48px', height: '48px',
              flexShrink: 0,
              background: showOnlyHosted ? 'var(--primary-gradient)' : 'white',
              border: '1px solid var(--border-color)',
              color: showOnlyHosted ? 'white' : 'var(--text-primary)',
              boxShadow: '0 4px 12px rgba(0,0,0,0.05)'
            }}
          >
            <Plus size={22} style={{ transform: showOnlyHosted ? 'rotate(45deg)' : 'none', transition: 'all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1)' }} />
          </button>
        </div>
      </header>

      {/* Primary Boarding Pass Dashboard (Light Mode) */}
      {currentUser && (
        <div style={{ padding: '0 20px', marginTop: '20px' }}>
          {(() => {
            const myUpcoming = gatherings
              .filter(g => {
                const isHost = typeof g.host === 'string' ? g.host === currentUser?.name : g.host?.email === currentUser?.email;
                const isApproved = g.members?.some(m => m.user.email === currentUser?.email && (m.status === MemberStatus.APPROVED));
                return (isHost || isApproved);
              })
              .sort((a, b) => new Date(a.dates) - new Date(b.dates))[0];

            if (!myUpcoming) return null;

            return (
              <div 
                onClick={() => navigate(`/gathering/${myUpcoming.id}`)}
                className="ticket-wrapper animate-fade" 
                style={{ 
                  borderRadius: '28px',
                  background: 'white',
                  border: '1px solid var(--border-color)',
                  padding: 0
                }}
              >
                <div style={{ 
                  background: 'var(--primary-gradient)', 
                  padding: '14px 24px', 
                  display: 'flex', 
                  justifyContent: 'space-between', 
                  alignItems: 'center' 
                }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <MapIcon size={16} color="white" />
                    <span style={{ color: 'white', fontSize: '11px', fontWeight: 900, letterSpacing: '1.2px' }}>UPCOMING BOARDING</span>
                  </div>
                  <div style={{ background: 'rgba(255,255,255,0.2)', padding: '4px 10px', borderRadius: '6px' }}>
                    <span style={{ color: 'white', fontSize: '10px', fontWeight: 900 }}>GATE {myUpcoming.id.toString().substring(0, 2)}</span>
                  </div>
                </div>

                <div style={{ padding: '24px' }}>
                  <div className="flex-between">
                    <div style={{ flex: 1 }}>
                      <span className="label-muted">DEPARTURE</span>
                      <div style={{ fontSize: '24px', fontWeight: 900, color: 'var(--text-primary)', marginTop: '4px' }}>SEOUL</div>
                    </div>
                    <div style={{ flex: 0.5, textAlign: 'center', position: 'relative' }}>
                      <div className="ticket-divider" style={{ borderTop: '2px solid #F1F5F9', margin: 0 }}>
                        <div style={{ 
                          position: 'absolute', top: '-11px', left: '50%', transform: 'translateX(-50%)',
                          background: 'white', padding: '0 10px'
                        }}>
                          <MapIcon size={20} color="var(--primary-orange)" />
                        </div>
                      </div>
                    </div>
                    <div style={{ flex: 1, textAlign: 'right' }}>
                      <span className="label-muted">DESTINATION</span>
                      <div style={{ fontSize: '24px', fontWeight: 900, color: 'var(--text-primary)', marginTop: '4px' }}>
                        {myUpcoming.location?.split(' ')[0].toUpperCase() || 'TRIP'}
                      </div>
                    </div>
                  </div>

                  <div className="ticket-divider" style={{ margin: '20px 0' }} />

                  <div className="flex-between">
                    <div>
                      <span className="label-muted">PASSENGER / FLIGHT</span>
                      <p className="info-value" style={{ fontSize: '16px' }}>{currentUser.name} • {myUpcoming.title}</p>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                      <span className="label-muted">BOARDING DATE</span>
                      <p className="info-value" style={{ fontSize: '16px', color: 'var(--primary-orange)' }}>
                        {new Date(myUpcoming.dates).toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            );
          })()}
          
          {/* AI Travel Insight Widget */}
          <div style={{ marginTop: '20px' }}>
            <TravelInsightWidget user={currentUser} myMissions={activeMissions} />
          </div>
        </div>
      )}

      {/* Premium Navigation Tabs (Light Mode) */}
      <div style={{ 
        padding: '0 20px', 
        marginTop: '28px',
        marginBottom: '28px'
      }}>
        <nav style={{ display: 'flex', gap: '12px', overflowX: 'auto', paddingBottom: '4px' }} className="hide-scrollbar">
          {['라운지', '비행 계획', '챌린지', '내 여정', '내 여권'].map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              style={{
                padding: '12px 24px',
                borderRadius: '16px',
                background: activeTab === tab ? 'var(--text-primary)' : 'white',
                color: activeTab === tab ? 'white' : 'var(--text-secondary)',
                fontWeight: 800,
                fontSize: '15px',
                whiteSpace: 'nowrap',
                border: '1px solid var(--border-color)',
                boxShadow: activeTab === tab ? '0 8px 20px rgba(15, 23, 42, 0.15)' : 'none',
                transition: 'all 0.3s ease'
              }}
            >
              {tab}
            </button>
          ))}
        </nav>
      </div>

      <div style={{ padding: '0 20px', flex: 1 }}>
        {activeTab === '라운지' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {gatherings.filter(g => {
              // The QueryDSL backend already handles location, searchQuery, and availableOnly.
              // We only apply the showOnlyHosted filter on the client side since it strictly checks current user matching.
              const hostMatch = !showOnlyHosted || (
                currentUser && g.host && (
                  (typeof g.host === 'string' && g.host === currentUser.name) ||
                  (g.host.email === currentUser.email)
                )
              );
              return hostMatch;
            }).map((g, idx) => (
              <div 
                key={g.id} 
                onClick={() => navigate(`/gathering/${g.id}`)} 
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
                    )) ? g.members?.filter(m => m.status === MemberStatus.PENDING).length || 0 : 0
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

        {activeTab === '챌린지' && (
          <MissionTab 
            activeMissions={activeMissions} 
            onMissionComplete={completeMission}
            onStepComplete={completeStep}
          />
        )}

        {activeTab === '내 여정' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {gatherings.filter(g => {
              const isHost = typeof g.host === 'string' ? g.host === currentUser?.name : g.host?.email === currentUser?.email;
              const isApproved = g.members?.some(m => m.user.email === currentUser?.email && (m.status === MemberStatus.APPROVED));
              const isCompleted = g.currentJoining >= g.maxJoining;
              return (isHost || isApproved) && isCompleted;
            }).map((g, idx) => (
              <div 
                key={g.id} 
                onClick={() => navigate(`/gathering/${g.id}`)} 
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
                    )) ? g.members?.filter(m => m.status === MemberStatus.PENDING).length || 0 : 0
                  }
                />
              </div>
            ))}
            {gatherings.filter(g => {
              const isHost = typeof g.host === 'string' ? g.host === currentUser?.name : g.host?.email === currentUser?.email;
              const isApproved = g.members?.some(m => m.user.email === currentUser?.email && (m.status === MemberStatus.APPROVED));
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
                <button className="primary-btn" onClick={() => setActiveTab('라운지')}>모임 탐색하기</button>
              </div>
            )}
          </div>
        )}

        {activeTab === '비행 계획' && (
          <ItineraryTab onMissionStart={() => { fetchMissions(); setActiveTab('챌린지'); }} onEdit={handleEditItinerary} />
        )}

        {activeTab === '내 여권' && (
          <ProfileTab />
        )}
      </div>

      {/* Unified Floating Action Button */}
      {['라운지', '비행 계획', '챌린지'].includes(activeTab) && (
        <button
          onClick={() => {
            if (activeTab === '라운지') navigate('/create');
            else navigate('/itinerary/create');
          }}
          className="primary-btn fab-button"
          style={{
            padding: 0,
            zIndex: 90,
          }}
        >
          <Plus size={32} strokeWidth={3} />
        </button>
      )}

    </div>
  );
};

export default Home;
