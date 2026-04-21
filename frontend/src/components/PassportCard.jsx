import React from 'react';
import { Plane, Award, MapPin, ShieldCheck, Globe } from 'lucide-react';
import './PassportCard.css';

const PassportCard = ({ user, stamps = [] }) => {
  const stampsCount = stamps.length;
  
  return (
    <div className="passport-card-wrapper animate-fade">
      {/* Decorative Passport texture/background */}
      <div className="passport-card-light">
        <div className="passport-header-section">
          <div className="flex-between">
            <Globe size={24} color="var(--primary-orange)" />
            <span className="passport-label">REPUBLIC OF TRIPGATHER</span>
            <ShieldCheck size={20} color="var(--primary-orange)" />
          </div>
          <h2 className="passport-title">PASSPORT</h2>
        </div>

        <div className="passport-main-body">
          <div className="passport-photo-container">
            <div 
              className="passport-photo-frame" 
              style={{ 
                backgroundImage: user?.profileImageUrl ? `url(${user.profileImageUrl})` : 'none',
              }}
            >
              {!user?.profileImageUrl && (
                <div className="photo-placeholder">{user?.name ? user.name[0] : 'U'}</div>
              )}
            </div>
            <div className="passport-stamp-seal">TG</div>
          </div>
          
          <div className="passport-details-grid">
            <div className="detail-item">
              <label>SURNAME / NAME</label>
              <div className="detail-value highlight">{user?.name?.toUpperCase() || 'TRAVELER'}</div>
            </div>
            
            <div className="detail-item">
              <label>NATIONALITY</label>
              <div className="detail-value">TRIPPER</div>
            </div>
            
            <div className="detail-item">
              <label>AUTHORITY</label>
              <div className="detail-value">TripGather Center</div>
            </div>

            <div className="detail-row">
              <div style={{ flex: 1 }}>
                <label>STAMPS</label>
                <div className="detail-value-pill">
                  <Award size={14} /> {stampsCount} COLLECTED
                </div>
              </div>
              <div style={{ flex: 1 }}>
                <label>RANK</label>
                <div className="detail-value-pill rank">
                  <MapPin size={14} /> EXPLORER
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Real Stamp Gallery Section */}
        <div style={{ 
          margin: '0 24px 20px', padding: '16px', background: 'rgba(255, 255, 255, 0.4)', 
          borderRadius: '16px', border: '1px solid rgba(0, 0, 0, 0.05)',
          backdropFilter: 'blur(10px)'
        }}>
          <div style={{ fontSize: '10px', fontWeight: 800, color: 'var(--text-muted)', marginBottom: '10px', letterSpacing: '1px' }}>
            ACTUAL STAMP COLLECTION (MEMORIES)
          </div>
          <div style={{ display: 'flex', gap: '10px', overflowX: 'auto', paddingBottom: '4px' }} className="hide-scrollbar">
            {stamps.length > 0 ? (
              stamps.map((stamp, idx) => (
                <div key={idx} style={{ flexShrink: 0, position: 'relative' }}>
                  <img 
                    src={stamp.stampImageUrl || 'https://via.placeholder.com/60'} 
                    alt="stamp" 
                    style={{ 
                      width: '60px', height: '60px', borderRadius: '50%', 
                      border: '2px solid white', boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
                      transform: `rotate(${idx % 2 === 0 ? '-5deg' : '5deg'})`
                    }} 
                  />
                  <div style={{ 
                    position: 'absolute', bottom: '-4px', right: '-4px', 
                    background: 'var(--primary-orange)', color: 'white', 
                    width: '18px', height: '18px', borderRadius: '50%', 
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontSize: '10px', fontWeight: 900, border: '2px solid white'
                  }}>
                    ✓
                  </div>
                </div>
              ))
            ) : (
              <div style={{ fontSize: '12px', color: 'var(--text-muted)', padding: '10px 0', width: '100%', textAlign: 'center', fontStyle: 'italic' }}>
                완료된 미션이 없습니다. 첫 스탬프를 잭팟하세요! 🎰
              </div>
            )}
          </div>
        </div>

        <div className="passport-mrz-zone">
          <div className="mrz-text">
            P&lt;KOR{user?.name?.toUpperCase() || 'TRAVELER'}&lt;&lt;&lt;&lt;&lt;&lt;&lt;&lt;&lt;&lt;&lt;&lt;&lt;&lt;&lt;<br/>
            TG{stampsCount?.toString().padStart(4, '0')}PASS&lt;{user?.id ? user.id.toString().padStart(6, '0') : '000000'}KOR&lt;&lt;&lt;&lt;&lt;&lt;
          </div>
        </div>
      </div>
    </div>
  );
};

export default PassportCard;
