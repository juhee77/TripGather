import React, { useState, useEffect } from 'react';
import FeedCard from '../components/FeedCard';
import CreateGatheringModal from '../components/CreateGatheringModal';
import { Search, Map as MapIcon, Plus } from 'lucide-react';

const Home = () => {
  const [gatherings, setGatherings] = useState([]);
  const [itineraries, setItineraries] = useState([]);
  const [activeTab, setActiveTab] = useState('발견');
  const [isModalOpen, setIsModalOpen] = useState(false);

  const fetchGatherings = () => {
    fetch('http://localhost:8080/api/gatherings')
      .then(res => res.json())
      .then(data => setGatherings(data))
      .catch(err => console.error("Error fetching gatherings:", err));
  };

  useEffect(() => {
    fetchGatherings();
      
    fetch('http://localhost:8080/api/itineraries')
      .then(res => res.json())
      .then(data => setItineraries(data))
      .catch(err => console.error("Error fetching itineraries:", err));
  }, []);

  const handleGatheringCreated = (newGathering) => {
    // Optionally prepend to list or refetch
    fetchGatherings();
  };

  const tabs = ['발견', '내 모임', '일정', '지도'];

  return (
    <div className="page" style={{ paddingBottom: '20px', position: 'relative', minHeight: '100%' }}>
      <header className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 className="page-title">모임</h1>
          <p className="page-subtitle" style={{ fontSize: '18px', color: 'var(--primary)', fontWeight: 700 }}>Gathering</p>
        </div>
        <div style={{ display: 'flex', gap: '10px' }}>
          <div style={{ background: 'var(--bg-color)', padding: '8px 12px', borderRadius: '20px', fontSize: '14px', fontWeight: 600 }}>
            📍 강남구 ▾
          </div>
          <button className="btn-circle" style={{ background: 'var(--bg-color)', width: '38px', height: '38px', borderRadius: '19px' }}>
            <Search size={18} />
          </button>
        </div>
      </header>

      {/* Tabs Layout */}
      <div style={{ display: 'flex', gap: '20px', padding: '0 20px', marginBottom: '20px', borderBottom: '1px solid var(--border)', overflowX: 'auto', WebkitOverflowScrolling: 'touch', scrollbarWidth: 'none' }}>
        {tabs.map(tab => (
          <div 
            key={tab}
            onClick={() => setActiveTab(tab)}
            style={{ 
              padding: '10px 0', 
              borderBottom: activeTab === tab ? '3px solid var(--primary)' : '3px solid transparent', 
              fontWeight: activeTab === tab ? 700 : 600, 
              color: activeTab === tab ? 'var(--primary)' : 'var(--text-sub)',
              cursor: 'pointer',
              whiteSpace: 'nowrap'
            }}
          >
            {tab}
          </div>
        ))}
      </div>

      <div style={{ padding: '0 20px' }}>
        {activeTab === '발견' && (
          <>
            {gatherings.map(g => (
              <FeedCard 
                key={g.id}
                title={g.title}
                host={g.host}
                date={g.dates}
                location={g.location}
                joining={`${g.currentJoining}/${g.maxJoining}`}
                bgImage={g.bgImageUrl}
              />
            ))}
            {gatherings.length === 0 && <p style={{textAlign: 'center', color: 'var(--text-sub)'}}>로딩 중...</p>}
          </>
        )}

        {activeTab === '내 모임' && (
          <div style={{ textAlign: 'center', color: 'var(--text-sub)', padding: '40px 0' }}>
            <p>아직 참여 중인 모임이 없습니다.</p>
            <button className="btn-primary" style={{ marginTop: '16px' }}>모임둘러보기</button>
          </div>
        )}

        {activeTab === '일정' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {itineraries.map(it => (
              <div key={it.id} style={{ background: 'var(--surface)', padding: '20px', borderRadius: '16px', border: '1px solid var(--border)', boxShadow: 'var(--shadow-sm)' }}>
                <h3 style={{ fontSize: '16px', marginBottom: '8px' }}>{it.title}</h3>
                <p style={{ color: 'var(--text-sub)', fontSize: '14px', marginBottom: '12px' }}>By {it.author}</p>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--primary)', fontSize: '13px', fontWeight: 600 }}>
                  <MapIcon size={14} /> 루트 미리보기
                </div>
                <div style={{ marginTop: '12px', padding: '12px', background: 'var(--bg-color)', borderRadius: '8px', fontSize: '13px', color: 'var(--text-main)' }}>
                  {it.description}
                </div>
              </div>
            ))}
            {itineraries.length === 0 && <p style={{textAlign: 'center', color: 'var(--text-sub)'}}>일정을 불러오는 중입니다.</p>}
          </div>
        )}

        {activeTab === '지도' && (
          <div style={{ textAlign: 'center', color: 'var(--text-sub)', padding: '40px 0' }}>
            <p>내 주변 모임 지도는 <strong>하단 네비게이션의 [지도] 탭</strong>에서 더 크게 보실 수 있습니다! 🗺️</p>
          </div>
        )}
      </div>

      {/* Floating Action Button */}
      {activeTab === '발견' && (
        <button 
          onClick={() => setIsModalOpen(true)}
          style={{
            position: 'fixed',
            bottom: '90px', // Above bottom nav
            right: 'calc(50% - 240px + 20px)', // adjust for max-width container
            background: 'var(--primary)',
            color: 'white',
            width: '56px',
            height: '56px',
            borderRadius: '28px',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            boxShadow: 'var(--shadow-md)',
            zIndex: 90
          }}
        >
          <Plus size={28} strokeWidth={2.5} />
        </button>
      )}

      {/* Create Modal */}
      {isModalOpen && (
        <CreateGatheringModal 
          onClose={() => setIsModalOpen(false)} 
          onCreated={handleGatheringCreated}
        />
      )}
    </div>
  );
};

export default Home;
