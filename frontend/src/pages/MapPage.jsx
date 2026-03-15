import React, { useState, useEffect } from 'react';
import { Map, MapMarker } from 'react-kakao-maps-sdk';
import { Users } from 'lucide-react';

const MapPage = () => {
  const [gatherings, setGatherings] = useState([]);
  const [selected, setSelected] = useState(null);
  const [joining, setJoining] = useState(false);

  useEffect(() => {
    fetch('http://localhost:8080/api/gatherings')
      .then(res => res.json())
      .then(data => setGatherings(data.filter(g => g.lat && g.lng)))
      .catch(err => console.error('Error fetching gatherings:', err));
  }, []);

  const handleJoin = async () => {
    if (!selected) return;
    setJoining(true);
    try {
      const res = await fetch(`http://localhost:8080/api/gatherings/${selected.id}/join`, {
        method: 'POST',
      });
      if (res.ok) {
        const updated = await res.json();
        setGatherings(prev => prev.map(g => g.id === updated.id ? updated : g));
        setSelected(updated);
      } else {
        alert('참여에 실패했습니다.');
      }
    } catch (err) {
      alert(`오류: ${err.message}`);
    } finally {
      setJoining(false);
    }
  };

  // 마커가 있을 때 지도 중심: 모임 평균 좌표 or 서울 기본값
  const center = gatherings.length > 0
    ? { lat: gatherings[0].lat, lng: gatherings[0].lng }
    : { lat: 37.5665, lng: 126.9780 };

  return (
    <div className="page" style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <header className="page-header" style={{ borderBottom: 'none', background: 'var(--surface)', zIndex: 10 }}>
        <h1 className="page-title">지도 🗺️</h1>
        <p className="page-subtitle" style={{ fontSize: '14px', color: 'var(--text-sub)' }}>
          내 주변 모임 탐색 ({gatherings.length}개)
        </p>
      </header>

      <div style={{ flex: 1, position: 'relative' }}>
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
                  padding: '6px 10px', color: '#000', fontSize: '12px',
                  fontWeight: 700, whiteSpace: 'nowrap',
                  background: 'white', borderRadius: '8px'
                }}>
                  {g.title}
                </div>
              )}
            </MapMarker>
          ))}
        </Map>

        {/* Floating info sheet */}
        <div style={{
          position: 'absolute', bottom: '20px', left: '20px', right: '20px',
          background: 'var(--surface)', padding: '20px', borderRadius: '16px',
          boxShadow: 'var(--shadow-md)', zIndex: 10
        }}>
          {selected ? (
            <div>
              <p style={{ fontSize: '11px', color: 'var(--primary)', fontWeight: 700, marginBottom: '4px', letterSpacing: '1px' }}>
                {selected.category || 'GATHERING'} • {selected.dates}
              </p>
              <h3 style={{ fontSize: '16px', fontWeight: 700, marginBottom: '6px' }}>{selected.title}</h3>
              <p style={{ color: 'var(--text-sub)', fontSize: '13px', marginBottom: '4px' }}>📍 {selected.location}</p>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--text-sub)', fontSize: '13px', marginBottom: '16px' }}>
                <Users size={14} />
                <span>{selected.currentJoining} / {selected.maxJoining}명 참여 중</span>
              </div>
              <button
                className="btn-primary"
                style={{ width: '100%', opacity: joining ? 0.7 : 1 }}
                onClick={handleJoin}
                disabled={joining || selected.currentJoining >= selected.maxJoining}
              >
                {joining ? '처리 중...' : selected.currentJoining >= selected.maxJoining ? '마감' : '참여하기'}
              </button>
            </div>
          ) : (
            <div style={{ textAlign: 'center', color: 'var(--text-sub)' }}>
              <p>지도에서 핀을 선택해보세요!</p>
              {gatherings.length === 0 && (
                <p style={{ fontSize: '12px', marginTop: '8px' }}>모임을 불러오는 중...</p>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default MapPage;

