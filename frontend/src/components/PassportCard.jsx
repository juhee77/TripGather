import React from 'react';
import { Plane, Award, MapPin } from 'lucide-react';
import './PassportCard.css';

const PassportCard = ({ user, stampsCount }) => {
  return (
    <div className="passport-card-container animate-fade">
      <div className="passport-card">
        {/* Holographic reflection overlay */}
        <div className="hologram-overlay"></div>
        
        <div className="passport-header">
          <Plane size={24} color="rgba(255,255,255,0.8)" style={{ transform: 'rotate(45deg)' }} />
          <span className="passport-type">TRIP & GATHER PASSPORT</span>
        </div>

        <div className="passport-body">
          <div className="passport-photo-section">
            <div 
              className="passport-photo" 
              style={{ 
                backgroundImage: user?.profileImageUrl ? `url(${user.profileImageUrl})` : 'none',
              }}
            >
              {!user?.profileImageUrl && (
                <span className="passport-photo-placeholder">{user?.name ? user.name[0] : 'U'}</span>
              )}
            </div>
          </div>
          
          <div className="passport-info-section">
            <div className="info-group">
              <label>Name</label>
              <div className="info-value name-value">{user?.name || 'Traveler'}</div>
            </div>
            
            <div className="info-group">
              <label>Nationality</label>
              <div className="info-value">Republic of Korea</div>
            </div>
            
            <div className="stats-row">
              <div className="stat-pill">
                <Award size={14} color="#FF5C00" />
                <span>{stampsCount} Stamps</span>
              </div>
              <div className="stat-pill">
                <MapPin size={14} color="#6366F1" />
                <span>Explorer</span>
              </div>
            </div>
          </div>
        </div>

        <div className="passport-footer">
          <div className="mrz-code">
            P&lt;KOR{user?.name?.toUpperCase()}&lt;&lt;&lt;&lt;&lt;&lt;&lt;&lt;&lt;&lt;&lt;&lt;&lt;&lt;&lt;<br/>
            TD00{stampsCount}9&lt;1KOR9001155M2612318&lt;&lt;&lt;&lt;&lt;&lt;&lt;&lt;&lt;0
          </div>
        </div>
      </div>
    </div>
  );
};

export default PassportCard;
