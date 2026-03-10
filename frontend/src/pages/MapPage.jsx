import React, { useState } from 'react';
import { Map, MapMarker } from 'react-kakao-maps-sdk';

const MapPage = () => {
  const [info, setInfo] = useState(null);

  const markers = [
    {
      position: { lat: 37.5665, lng: 126.9780 },
      content: "이번 주말 서울 모임 (2/4 명)",
    },
    {
      position: { lat: 37.5145, lng: 127.0495 },
      content: "오늘 저녁 한강 러닝 🏃‍♂️",
    },
    {
      position: { lat: 37.5211, lng: 126.9242 },
      content: "여의도 맛집 탐방! 🍕",
    }
  ];

  return (
    <div className="page" style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <header className="page-header" style={{ borderBottom: 'none', background: 'var(--surface)', zIndex: 10 }}>
        <h1 className="page-title">지도 🗺️</h1>
        <p className="page-subtitle" style={{ fontSize: '14px', color: 'var(--text-sub)' }}>내 주변 모임 탐색</p>
      </header>

      <div style={{ flex: 1, position: 'relative' }}>
        <Map
          center={{ lat: 37.5665, lng: 126.9780 }}
          style={{ width: "100%", height: "100%" }}
          level={7}
        >
          {markers.map((marker, index) => (
            <MapMarker
              key={index}
              position={marker.position}
              onClick={() => setInfo(marker)}
            >
              {info && info.content === marker.content && (
                <div style={{ padding: "5px", color: "#000", fontSize: "12px", width: "150px", textAlign: "center" }}>
                  {marker.content}
                </div>
              )}
            </MapMarker>
          ))}
        </Map>
        
        {/* Floating action sheet / info box over the map */}
        <div style={{
          position: 'absolute',
          bottom: '20px',
          left: '20px',
          right: '20px',
          background: 'var(--surface)',
          padding: '20px',
          borderRadius: '16px',
          boxShadow: 'var(--shadow-md)',
          zIndex: 10
        }}>
          {info ? (
            <div>
              <h3 style={{ fontSize: '16px', marginBottom: '8px' }}>{info.content}</h3>
              <p style={{ color: 'var(--text-sub)', fontSize: '13px', marginBottom: '16px' }}>현재 2명의 멤버가 참여 대기 중입니다.</p>
              <button className="btn-primary" style={{ width: '100%' }}>참여하기</button>
            </div>
          ) : (
            <div style={{ textAlign: 'center', color: 'var(--text-sub)' }}>
              <p>지도에서 핀을 선택해보세요!</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default MapPage;
