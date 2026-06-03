import React, { useState, useEffect } from 'react';
import FeedCard from '../components/FeedCard';
import TicketCard from '../components/TicketCard';
import TripCard from '../components/TripCard';
import ItineraryTab from '../components/ItineraryTab';
import ChatTab from '../components/ChatTab';
import ProfileTab from '../components/ProfileTab';
import TravelInsightWidget from '../components/TravelInsightWidget';
import { useUser } from '../contexts/UserContext';
import { useGatheringsViewModel } from '../viewmodels/useGatheringsViewModel';
import { useItinerariesViewModel } from '../viewmodels/useItinerariesViewModel';
import { Search, Map as MapIcon, Plus, MessageCircle, Plane } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { MemberStatus } from '../constants/enums';
import JourneyRepository from '../repositories/JourneyRepository';
import { authFetch } from '../api/client';

const Home = () => {
  const navigate = useNavigate();
  const { user: currentUser, refetch: refetchUser } = useUser();
  const {
    gatherings,
    selectedRegion,
    searchQuery,
    availableOnly,
    isLoading,
    actions: { handleRegionChange, handleSearchQueryChange, handleAvailableOnlyChange, likeGathering }
  } = useGatheringsViewModel();

  const [activeTab, setActiveTab] = useState(() => sessionStorage.getItem('tg_activeTab') || '라운지');

  useEffect(() => {
    sessionStorage.setItem('tg_activeTab', activeTab);
  }, [activeTab]);

  const [showOnlyHosted, setShowOnlyHosted] = useState(false);
  const [journeyItineraries, setJourneyItineraries] = useState([]);
  const [sortBy, setSortBy] = useState('latest'); // 'latest' or 'startDate'
  const regions = ['전체', '강남구', '서초구', '송파구', '마포구', '용산구', '성동구', '종로구', '부산 해운대구', '제주도'];
  const { itineraries } = useItinerariesViewModel();

  const [trips, setTrips] = useState([]);

  useEffect(() => {
    if (!currentUser?.email) return;
    const loadJourneys = () => {
      JourneyRepository.fetchMine(currentUser.email)
        .then(setJourneyItineraries)
        .catch((err) => console.error('Failed to fetch journeys:', err));
    };
    
    const loadTrips = async () => {
      try {
        const res = await authFetch('/api/trips');
        if (res.ok) setTrips(await res.json());
      } catch (err) {
        console.error('Failed to fetch trips:', err);
      }
    };
    
    const refreshData = () => {
      loadJourneys();
      loadTrips();
      refetchUser().catch((err) => console.error('Failed to refetch user:', err));
    };
    
    refreshData();

    // Refetch when window regains focus (e.g. after coming back from edit/detail)
    window.addEventListener('focus', refreshData);
    return () => window.removeEventListener('focus', refreshData);
  }, [currentUser?.email, refetchUser]);

  const handleEditItinerary = (itinerary) => {
    navigate(`/itinerary/edit/${itinerary.id}`);
  };

  const handleRemoveJourney = async (itineraryId) => {
    try {
      await JourneyRepository.remove(itineraryId);
      setJourneyItineraries(prev => prev.filter(j => j.id !== itineraryId));
    } catch (err) {
      alert('여정 제거에 실패했습니다.');
    }
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
          <h1 className="heading-l" style={{ 
            marginTop: '4px',
            wordBreak: 'keep-all',
            whiteSpace: 'nowrap',
            fontSize: 'clamp(1.5rem, 5vw, 2rem)'
          }}>라운지</h1>
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
                const isHost = (g.host && (typeof g.host === 'string' ? g.host === currentUser?.name : g.host?.email === currentUser?.email)) ||
                               (!g.host && g.linkedItinerary?.authorEmail === currentUser?.email);
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
                  background: 'var(--surface-solid)',
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
            <TravelInsightWidget user={currentUser} />
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
          {['라운지', '여행 피드', '내 여행', '내 여권'].map((tab) => (
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
            {isLoading ? (
              // Skeleton UI
              [...Array(3)].map((_, idx) => (
                <div key={`skeleton-${idx}`} className="ticket-skeleton" style={{ animationDelay: `${idx * 0.15}s` }} />
              ))
            ) : (
              <>
                {gatherings.filter(g => {
                  // The QueryDSL backend already handles location, searchQuery, and availableOnly.
                  // We only apply the showOnlyHosted filter on the client side since it strictly checks current user matching.
                  const hostMatch = !showOnlyHosted || (
                    currentUser && (
                      (g.host && (
                        (typeof g.host === 'string' && g.host === currentUser.name) ||
                        (g.host.email === currentUser.email)
                      )) ||
                      (!g.host && g.linkedItinerary?.authorEmail === currentUser.email)
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
                      likedByCurrentUser={g.likedByCurrentUser}
                      onLike={() => likeGathering(g.id)}
                    />
                  </div>
                ))}
                
                {/* Empty State */}
                {gatherings.filter(g => selectedRegion === '전체' || (g.location && g.location.includes(selectedRegion))).length === 0 && (
                  <div style={{ 
                    textAlign: 'center', 
                    padding: '80px 0',
                    background: 'var(--surface)',
                    borderRadius: 'var(--radius-lg)',
                    border: '1px dashed var(--border-color)',
                    marginTop: '20px'
                  }}>
                    <div style={{ 
                      width: '80px', height: '80px', 
                      margin: '0 auto 16px auto', 
                      background: 'rgba(255, 92, 0, 0.1)',
                      borderRadius: '50%',
                      display: 'flex', alignItems: 'center', justifyContent: 'center'
                    }}>
                      <Plane size={40} color="var(--primary-orange)" />
                    </div>
                    <h3 style={{ fontSize: '18px', fontWeight: 900, color: 'var(--text-primary)', marginBottom: '8px' }}>탑승할 항공편이 없습니다</h3>
                    <p className="text-s" style={{ color: 'var(--text-secondary)' }}>선택하신 지역에 예정된 일정이 없네요.<br/>새로운 여정을 개설해 보는 건 어떨까요?</p>
                  </div>
                )}
              </>
            )}
          </div>
        )}

        {activeTab === '내 여행' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h2 style={{ fontSize: '18px', fontWeight: 900 }}>나의 여행 허브</h2>
              <button 
                onClick={() => navigate('/trip/create')}
                style={{ 
                  background: 'var(--text-primary)', color: 'white', 
                  border: 'none', padding: '8px 12px', borderRadius: '12px',
                  fontWeight: 800, fontSize: '13px', display: 'flex', alignItems: 'center', gap: '4px',
                  cursor: 'pointer'
                }}>
                <Plus size={16} /> 새 여행
              </button>
            </div>

            {trips.map((trip, idx) => (
              <TripCard 
                key={trip.id} 
                trip={trip} 
                onClick={() => navigate(`/trip/${trip.id}`)} 
              />
            ))}

            {trips.length === 0 && (
              <div className="glass" style={{
                textAlign: 'center', padding: '60px 24px', borderRadius: 'var(--radius-lg)',
                display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '16px',
                background: 'white', border: '1px solid var(--border-color)'
              }}>
                <div style={{ fontSize: '48px' }}>✈️</div>
                <p style={{ fontWeight: 700, color: 'var(--text-primary)', textAlign: 'center' }}>
                  아직 계획된 여행이 없습니다.<br />
                  <span style={{ fontSize: '13px', color: 'var(--text-secondary)', fontWeight: 600 }}>
                    새로운 여행을 만들고 일정을 채워보세요!
                  </span>
                </p>
                <button className="primary-btn" onClick={() => navigate('/trip/create')}>새 여행 만들기</button>
              </div>
            )}
          </div>
        )}

        {activeTab === '여행 피드' && (
          <ItineraryTab
            onAddToJourney={async () => {
              try {
                const mine = await JourneyRepository.fetchMine();
                setJourneyItineraries(mine);
              } catch (e) {
                console.error(e);
              }
              setActiveTab('내 여행');
            }}
            onEdit={handleEditItinerary}
          />
        )}

        {activeTab === '내 여권' && (
          <ProfileTab />
        )}
      </div>

      {['라운지', '여행 피드'].includes(activeTab) && (
        <div style={{ 
          position: 'fixed', 
          bottom: '100px', 
          right: '24px', 
          zIndex: 1000,
          display: 'flex',
          flexDirection: 'column',
          gap: '16px',
          padding: '10px'
        }}>
          <button
            onClick={() => {
              if (activeTab === '라운지') navigate('/create');
              else if (activeTab === '여행 피드') navigate('/itinerary/create');
              else navigate('/itinerary/create');
            }}
            className="primary-btn fab-button"
            style={{
              width: '64px',
              height: '64px', 
              borderRadius: '50%', 
              padding: 0,
              boxShadow: '0 12px 24px rgba(255, 92, 0, 0.4)',
              zIndex: 90,
            }}
          >
            <Plus size={32} strokeWidth={3} />
          </button>
        </div>
      )}

    </div>
  );
};

export default Home;
