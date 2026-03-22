import React, { useState, useEffect } from 'react';
import FeedCard from '../components/FeedCard';
import CreateGatheringModal from '../components/CreateGatheringModal';
import GatheringDetailModal from '../components/GatheringDetailModal';
import TicketCard from '../components/TicketCard';
import ItineraryTab from '../components/ItineraryTab';
import ChatTab from '../components/ChatTab';
import RouteDetailModal from '../components/RouteDetailModal'; // Added
import { useUser } from '../contexts/UserContext';
import { Search, Map as MapIcon, Plus, MessageCircle } from 'lucide-react';
import { authFetch } from '../api/client'; // Added

const Home = () => {
  const { user: currentUser } = useUser();
  const [gatherings, setGatherings] = useState([]);
  const [activeTab, setActiveTab] = useState('발견');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedGathering, setSelectedGathering] = useState(null);
  const [selectedRegion, setSelectedRegion] = useState('전체');
  const regions = ['전체', '강남구', '서초구', '송파구', '마포구', '용산구', '성동구', '종로구', '부산 해운대구', '제주도'];
  const [myJoinedIds, setMyJoinedIds] = useState(() => {
    try {
      const saved = localStorage.getItem('myJoinedIds');
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });
  const [activeMissions, setActiveMissions] = useState([]);
  const [selectedMission, setSelectedMission] = useState(null);

  const fetchGatherings = () => {
    fetch('http://localhost:8080/api/gatherings')
      .then(res => res.json())
      .then(data => setGatherings(data))
      .catch(err => console.error("Error fetching gatherings:", err));
  };

  useEffect(() => {
    fetchGatherings();
  }, []);

  useEffect(() => {
    if (currentUser) { // Changed from `user` to `currentUser` to match context variable
      authFetch('/api/missions/me')
        .then(res => res.json())
        .then(data => setActiveMissions(data.filter(m => m.status === 'ACTIVE')))
        .catch(err => console.error(err));
    }
  }, [currentUser]); // Dependency changed to `currentUser`

  const handleGatheringCreated = (newGathering) => {
    // Optionally prepend to list or refetch
    fetchGatherings();
  };

  const tabs = ['발견', '내 모임', '일정']; // This line will be replaced by the new nav structure

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
            onChange={(e) => setSelectedRegion(e.target.value)}
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
          <button className="icon-circle glass" style={{ width: '44px', height: '44px' }}>
            <Search size={20} color="var(--text-primary)" />
          </button>
        </div>
      </header>

      {/* Modern High-End Tabs */}
      <div style={{ 
        padding: '0 20px', 
        marginTop: '20px',
        marginBottom: '24px'
      }}>
        <nav style={{ display: 'flex', gap: '24px', overflowX: 'auto', paddingBottom: '10px' }} className="hide-scrollbar">
          {['발견', '일정', '나의 미션', '내 모임'].map((tab) => (
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
            {gatherings.filter(g => selectedRegion === '전체' || (g.location && g.location.includes(selectedRegion))).map((g, idx) => (
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
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {activeMissions.length === 0 ? (
              <div className="glass" style={{ textAlign: 'center', padding: '60px 24px', borderRadius: 'var(--radius-lg)', background: 'white' }}>
                <div style={{ fontSize: '48px', marginBottom: '16px' }}>🚀</div>
                <p style={{ fontWeight: 700, color: 'var(--text-primary)' }}>현재 진행 중인 ми션이 없습니다.</p>
                <button className="primary-btn" onClick={() => setActiveTab('일정')} style={{marginTop: '16px'}}>일정 보러가기</button>
              </div>
            ) : (
              activeMissions.map((m, idx) => (
                <div key={m.id} className="animate-fade" style={{ animationDelay: `${idx * 0.1}s` }}>
                  <TicketCard
                    itinerary={{
                      id: m.itineraryId,
                      title: m.itineraryTitle,
                      author: m.itineraryAuthor,
                      description: 'Proceed with this active mission!',
                      createdAt: m.startedAt,
                      steps: m.steps
                    }}
                    onViewRoute={(itinerary) => setSelectedMission(itinerary)}
                  />
                </div>
              ))
            )}
            {selectedMission && (
                <RouteDetailModal 
                    itinerary={selectedMission}
                    onClose={() => setSelectedMission(null)}
                    onEdit={() => {}} 
                    onDelete={() => {}}
                />
            )}
          </div>
        )}

        {activeTab === '내 모임' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {gatherings.filter(g => {
              const isHost = typeof g.host === 'string' ? g.host === currentUser?.name : g.host?.email === currentUser?.email;
              const isApproved = g.members?.some(m => m.user.email === currentUser?.email && (m.status === 'APPROVED' || m.status === 'PENDING'));
              return isHost || isApproved;
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
              const isApproved = g.members?.some(m => m.user.email === currentUser?.email && (m.status === 'APPROVED' || m.status === 'PENDING'));
              return isHost || isApproved;
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
                <div style={{ fontSize: '48px' }}>🔍</div>
                <p style={{ fontWeight: 700, color: 'var(--text-primary)' }}>아직 참여 중인 모임이 없습니다.</p>
                <button className="primary-btn" onClick={() => setActiveTab('발견')}>모임 탐색하기</button>
              </div>
            )}
          </div>
        )}

        {activeTab === '일정' && (
          <ItineraryTab />
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
          onUpdate={fetchGatherings}
          onDelete={(id) => {
            setGatherings(prev => prev.filter(g => g.id !== id));
            setSelectedGathering(null);
          }}
          onJoin={(updatedGathering) => {
            fetchGatherings(); // Full refresh to get status
          }}
        />
      )}
    </div>
  );
};

export default Home;
