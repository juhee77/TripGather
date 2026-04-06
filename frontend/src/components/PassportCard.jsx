import React from 'react';
import { Plane, Award, MapPin, ShieldCheck, Globe } from 'lucide-react';
import './PassportCard.css';

const PassportCard = ({ user, stampsCount }) => {
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
