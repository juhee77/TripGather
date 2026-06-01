import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, MapPin, Calendar, List, Map, Globe, CheckSquare, Star } from 'lucide-react';
import { authFetch } from '../api/client';
import PackingList from '../components/PackingList';
import TranslatorWidget from '../components/TranslatorWidget';
import ReviewSection from '../components/ReviewSection';
import ItineraryTab from '../components/ItineraryTab';

const TripHubPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [trip, setTrip] = useState(null);
  const [activeTab, setActiveTab] = useState('일정');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchTrip();
  }, [id]);

  const fetchTrip = async () => {
    try {
      const res = await authFetch(`/api/trips/${id}`);
      if (res.ok) setTrip(await res.json());
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading...</div>;
  if (!trip) return <div>여행 정보를 찾을 수 없습니다.</div>;

  const TABS = [
    { id: '일정', icon: <List size={18} /> },
    { id: '추천', icon: <Map size={18} /> },
    { id: '번역', icon: <Globe size={18} /> },
    { id: '준비물', icon: <CheckSquare size={18} /> },
    { id: '리뷰', icon: <Star size={18} /> }
  ];

  return (
    <div style={{ background: 'var(--bg-lite)', minHeight: '100vh', paddingBottom: '100px' }}>
      {/* Header */}
      <header className="glass page-header" style={{
        position: 'sticky', top: 0, zIndex: 100, padding: '20px',
        display: 'flex', alignItems: 'center', gap: '12px',
        borderBottom: '1px solid var(--border-color)',
        background: 'rgba(255,255,255,0.9)'
      }}>
        <button onClick={() => navigate(-1)} style={{ background: 'none', border: 'none', cursor: 'pointer' }}>
          <ArrowLeft size={24} color="var(--text-primary)" />
        </button>
        <div>
          <span className="label-orange">TRIP HUB</span>
          <h1 className="heading-l" style={{ marginTop: '2px' }}>{trip.title}</h1>
        </div>
      </header>

      {/* Info Card */}
      <div style={{ padding: '20px' }}>
        <div style={{
          background: 'white', borderRadius: '16px', padding: '16px',
          boxShadow: '0 4px 12px rgba(0,0,0,0.05)', border: '1px solid var(--border-color)'
        }}>
          <div style={{ display: 'flex', gap: '12px', marginBottom: '8px' }}>
            <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--text-secondary)' }}>
              <MapPin size={16} style={{ display: 'inline', marginRight: '4px' }}/>
              {trip.destination || '미정'}
            </span>
            <span style={{ fontSize: '14px', fontWeight: 700, color: 'var(--text-secondary)' }}>
              <Calendar size={16} style={{ display: 'inline', marginRight: '4px' }}/>
              {trip.startDate} ~ {trip.endDate}
            </span>
          </div>
          <span style={{
            background: 'var(--primary-orange)', color: 'white',
            padding: '4px 10px', borderRadius: '12px', fontSize: '12px', fontWeight: 800
          }}>
            {trip.status}
          </span>
        </div>
      </div>

      {/* Tabs */}
      <div style={{ padding: '0 20px', overflowX: 'auto' }} className="hide-scrollbar">
        <div style={{ display: 'flex', gap: '8px' }}>
          {TABS.map(tab => (
            <button key={tab.id} onClick={() => setActiveTab(tab.id)}
              style={{
                display: 'flex', alignItems: 'center', gap: '6px',
                padding: '10px 16px', borderRadius: '12px',
                background: activeTab === tab.id ? 'var(--text-primary)' : 'white',
                color: activeTab === tab.id ? 'white' : 'var(--text-secondary)',
                border: '1px solid var(--border-color)', fontWeight: 800, whiteSpace: 'nowrap'
              }}>
              {tab.icon} {tab.id}
            </button>
          ))}
        </div>
      </div>

      {/* Content */}
      <div style={{ padding: '20px' }}>
        {activeTab === '일정' && <div><ItineraryTab tripId={trip.id} /></div>}
        {activeTab === '추천' && <div>추천 코스 준비 중...</div>}
        {activeTab === '번역' && <TranslatorWidget country={trip.country} />}
        {activeTab === '준비물' && <PackingList tripId={trip.id} />}
        {activeTab === '리뷰' && <ReviewSection tripId={trip.id} />}
      </div>
    </div>
  );
};

export default TripHubPage;
