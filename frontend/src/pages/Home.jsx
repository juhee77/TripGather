import React, { useState, useEffect } from 'react';
import FeedCard from '../components/FeedCard';
import CreateGatheringModal from '../components/CreateGatheringModal';
import GatheringDetailModal from '../components/GatheringDetailModal';
import TicketCard from '../components/TicketCard';
import ItineraryTab from '../components/ItineraryTab';
import ChatTab from '../components/ChatTab';
import { useUser } from '../contexts/UserContext';
import { Search, Map as MapIcon, Plus, MessageCircle } from 'lucide-react';

const Home = () => {
  const { user: currentUser } = useUser();
  const [gatherings, setGatherings] = useState([]);
  const [activeTab, setActiveTab] = useState('발견');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedGathering, setSelectedGathering] = useState(null);
  const [myJoinedIds, setMyJoinedIds] = useState(() => {
    try {
      const saved = localStorage.getItem('myJoinedIds');
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });

  const fetchGatherings = () => {
    fetch('http://localhost:8080/api/gatherings')
      .then(res => res.json())
      .then(data => setGatherings(data))
      .catch(err => console.error("Error fetching gatherings:", err));
  };

  useEffect(() => {
    fetchGatherings();
  }, []);

  const handleGatheringCreated = (newGathering) => {
    // Optionally prepend to list or refetch
    fetchGatherings();
  };

  const tabs = ['발견', '내 모임', '채팅', '일정', '지도'];

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
          <div style={{ 
            background: 'var(--bg-color)', 
            padding: '10px 16px', 
            borderRadius: 'var(--radius-full)', 
            fontSize: '14px', 
            fontWeight: 600,
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            boxShadow: 'inset 0 2px 4px rgba(0,0,0,0.02)'
          }}>
            <span style={{ fontSize: '16px' }}>📍</span> 강남구
          </div>
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
        <div className="hide-scrollbar" style={{ 
          display: 'flex', 
          gap: '8px', 
          overflowX: 'auto',
          paddingBottom: '4px'
        }}>
          {tabs.map(tab => (
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
        </div>
      </div>

      <div style={{ padding: '0 20px', flex: 1 }}>
        {activeTab === '발견' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {gatherings.map((g, idx) => (
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
                />
              </div>
            ))}
            {gatherings.length === 0 && (
              <div style={{ textAlign: 'center', padding: '100px 0' }}>
                <div className="animate-spin" style={{ marginBottom: '12px' }}>🔄</div>
                <p className="text-s">모임을 불러오는 중입니다...</p>
              </div>
            )}
          </div>
        )}

        {activeTab === '내 모임' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {gatherings.filter(g => {
              const isHost = typeof g.host === 'string' ? g.host === currentUser?.name : g.host?.email === currentUser?.email;
              const isApproved = g.members?.some(m => m.user.email === currentUser?.email && m.status === 'APPROVED');
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
                />
              </div>
            ))}
            {gatherings.filter(g => {
              const isHost = typeof g.host === 'string' ? g.host === currentUser?.name : g.host?.email === currentUser?.email;
              const isApproved = g.members?.some(m => m.user.email === currentUser?.email && m.status === 'APPROVED');
              return isHost || isApproved;
            }).length === 0 && (
              <div className="glass" style={{ 
                textAlign: 'center', 
                padding: '60px 24px',
                borderRadius: 'var(--radius-lg)',
                display: 'flex',
                direction: 'column',
                flexDirection: 'column',
                alignItems: 'center',
                gap: '16px'
              }}>
                <div style={{ fontSize: '48px' }}>🔍</div>
                <p style={{ fontWeight: 600, color: 'var(--text-secondary)' }}>아직 참여 중인 모임이 없습니다.</p>
                <button className="primary-btn" onClick={() => setActiveTab('발견')}>모임 탐색하기</button>
              </div>
            )}
          </div>
        )}

        {activeTab === '채팅' && (
          <ChatTab 
            joinedGatherings={gatherings.filter(g => {
              const isHost = typeof g.host === 'string' ? g.host === currentUser?.name : g.host?.email === currentUser?.email;
              const isApproved = g.members?.some(m => m.user.email === currentUser?.email && m.status === 'APPROVED');
              return isHost || isApproved;
            })} 
          />
        )}

        {activeTab === '일정' && (
          <ItineraryTab />
        )}

        {activeTab === '지도' && (
          <div className="glass" style={{ 
            textAlign: 'center', 
            padding: '80px 24px',
            borderRadius: 'var(--radius-lg)',
            background: 'linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 100%)',
            border: 'none'
          }}>
            <div style={{ fontSize: '64px', marginBottom: '20px' }}>🗺️</div>
            <h3 className="heading-m" style={{ marginBottom: '12px' }}>지도로 보기</h3>
            <p className="text-s" style={{ color: 'var(--secondary-blue)', fontWeight: 600 }}>
              내 주변 모임 지도는 하단 [지도] 버튼을 눌러<br/>더 크고 선명하게 확인하세요!
            </p>
          </div>
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
