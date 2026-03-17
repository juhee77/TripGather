import React from 'react';
import { MapPin, Users, Calendar, MessageCircle, MoreHorizontal } from 'lucide-react';
import './FeedCard.css';

const FeedCard = ({ title, host, date, location, joining, bgImage, commentCount = 0 }) => {
  return (
    <div className="feed-card">
      <div 
        className="feed-header" 
        style={{ backgroundImage: bgImage ? `url(${bgImage})` : 'none' }}
      >
        <div className="feed-status-tag glass" style={{ color: 'var(--primary-orange)' }}>
          모집 중
        </div>
      </div>
      
      <div className="feed-content">
        <div className="feed-top-row">
          <span className="feed-category">TRIP & GATHER</span>
          <span className="text-s" style={{ fontSize: '11px' }}>15 hours ago</span>
        </div>
        
        <h3 className="feed-title">{title}</h3>
        
        <div className="feed-host-section">
          <div className="host-avatar"></div>
          <div className="host-info">
            <span>By <strong>{host}</strong></span>
          </div>
        </div>
        
        <div className="feed-details-grid">
          <div className="detail-pill">
            <Calendar size={16} color="var(--primary-orange)" strokeWidth={2.5} />
            {date}
          </div>
          <div className="detail-pill">
            <MapPin size={16} color="#EF4444" strokeWidth={2.5} />
            {location}
          </div>
          <div className="detail-pill">
            <Users size={16} color="var(--secondary-blue)" strokeWidth={2.5} />
            {joining} participants
          </div>
          <div className="detail-pill">
            <span style={{ fontSize: '16px' }}>🔥</span>
            Lively crowd
          </div>
        </div>

        <div className="feed-footer">
          <div className="social-stats">
            <div className="stat-item">
              <MessageCircle size={16} />
              {commentCount}
            </div>
            <div className="stat-item">
              <MoreHorizontal size={16} />
            </div>
          </div>
          <button className="view-detail-btn">상세 보기</button>
        </div>
      </div>
    </div>
  );
};

export default FeedCard;
