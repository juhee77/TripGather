import React, { useState, useEffect } from 'react';
import { Map, MapMarker } from 'react-kakao-maps-sdk';
import { Users, MapPin } from 'lucide-react';
import { authFetch } from '../api/client';
import Card from '../components/UI/Card';
import PrimaryButton from '../components/UI/PrimaryButton';

const MapPage = () => {
  const [gatherings, setGatherings] = useState([]);
  const [selected, setSelected] = useState(null);
  const [joining, setJoining] = useState(false);

  useEffect(() => {
    authFetch('/api/gatherings')
      .then(res => res.json())
      .then(data => setGatherings(data.filter(g => g.lat && g.lng)))
      .catch(err => console.error('Error fetching gatherings:', err));
  }, []);

  const handleJoin = async () => {
    if (!selected) return;
    setJoining(true);
    try {
      const res = await authFetch(`/api/gatherings/${selected.id}/join`, {
        method: 'POST',
      });
      if (res.ok) {
        const updated = await res.json();
        setGatherings(prev => prev.map(g => g.id === updated.id ? updated : g));
        setSelected(updated);
        alert('참여 신청이 완료되었습니다!');
      } else {
        alert('참여에 실패했습니다.');
      }
    } catch (err) {
      alert(`오류: ${err.message}`);
    } finally {
      setJoining(false);
    }
  };

  const center = gatherings.length > 0
    ? { lat: gatherings[0].lat, lng: gatherings[0].lng }
    : { lat: 37.5665, lng: 126.9780 };

  return (
    <div style={{ width: '100vw', height: '100vh', position: 'relative', background: 'var(--bg-color)' }}>
      
      {/* Floating Header */}
      <div className="glass" style={{
        position: 'absolute', top: 0, left: 0, right: 0, zIndex: 10,
        padding: '24px 20px', paddingBottom: '16px',
        borderBottom: '1px solid rgba(255,255,255,0.1)',
        backdropFilter: 'blur(20px)', WebkitBackdropFilter: 'blur(20px)'
      }}>
        <h1 style={{ fontSize: '24px', fontWeight: 800, margin: 0, color: 'var(--text-primary)' }}>지도 🗺️</h1>
        <p style={{ fontSize: '13px', color: 'var(--text-sub)', fontWeight: 600, marginTop: '4px' }}>
          내 주변 탐색 중 <span style={{ color: 'var(--primary-orange)' }}>{gatherings.length}개</span>의 모임 발견!
        </p>
      </div>

      {/* Map Content */}
      <Map
        center={center}
        style={{ width: "100%", height: "100%" }}
        level={7}
      >
        {gatherings.map(g => (
          <MapMarker
            key={g.id}
            position={{ lat: g.lat, lng: g.lng }}
            onClick={() => setSelected(g)}
          >
            {selected?.id === g.id && (
              <div style={{
                padding: '8px 12px', color: 'var(--text-primary)', fontSize: '12px',
                fontWeight: 800, whiteSpace: 'nowrap',
                background: 'var(--surface)', borderRadius: '12px',
                boxShadow: 'var(--shadow-lg)', border: '1px solid var(--border-color)',
                transform: 'translateY(-10px)'
              }}>
                {g.title}
              </div>
            )}
          </MapMarker>
        ))}
      </Map>

      {/* Floating Info Sheet */}
      <div style={{
        position: 'absolute', bottom: '80px', left: '20px', right: '20px', zIndex: 10,
        transition: 'all 0.3s cubic-bezier(0.16, 1, 0.3, 1)',
        transform: selected ? 'translateY(0)' : 'translateY(120%)',
        opacity: selected ? 1 : 0,
        pointerEvents: selected ? 'auto' : 'none'
      }}>
        {selected && (
          <Card glass={true} padding={false} animate={false} className="animate-fade" style={{ overflow: 'hidden' }}>
            {selected.bgImageUrl && (
              <div style={{ width: '100%', height: '100px', backgroundImage: `url(${selected.bgImageUrl})`, backgroundSize: 'cover', backgroundPosition: 'center', position: 'relative' }}>
                <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(to bottom, transparent, rgba(0,0,0,0.4))' }}></div>
              </div>
            )}
            <div style={{ padding: '20px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                <span className="label-muted" style={{ color: 'var(--primary-orange)' }}>
                  {selected.category || 'GATHERING'} • {selected.dates}
                </span>
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--text-primary)', fontSize: '13px', fontWeight: 700, background: 'var(--bg-color)', padding: '4px 10px', borderRadius: '20px' }}>
                  <Users size={14} color="var(--primary-orange)" />
                  <span>{selected.currentJoining} / {selected.maxJoining}</span>
                </div>
              </div>
              <h3 style={{ fontSize: '18px', fontWeight: 800, marginBottom: '8px', color: 'var(--text-primary)', lineHeight: 1.3 }}>{selected.title}</h3>
              <p style={{ color: 'var(--text-sub)', fontSize: '14px', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '4px', fontWeight: 600 }}>
                <MapPin size={14} /> {selected.location}
              </p>
              <PrimaryButton
                onClick={handleJoin}
                disabled={joining || selected.currentJoining >= selected.maxJoining}
                style={{ width: '100%' }}
              >
                {joining ? '신청 중...' : selected.currentJoining >= selected.maxJoining ? '마감되었습니다' : '참여하기'}
              </PrimaryButton>
            </div>
          </Card>
        )}
      </div>
    </div>
  );
};

export default MapPage;

