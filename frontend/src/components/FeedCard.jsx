import React from 'react';
import { MapPin, Users, Calendar, MessageCircle, MoreHorizontal } from 'lucide-react';
import './FeedCard.css';

const FeedCard = ({ title, host, date, location, joining, bgImage }) => {
  return (
    <div className="feed-card">
      {bgImage && (
        <div className="feed-header" style={{ backgroundImage: `url(${bgImage})` }}>
        </div>
      )}
      <div className="feed-content">
        <div className="feed-top-row">
          <span className="feed-time">15h ago</span>
          <span className="feed-location-tag">{location}</span>
        </div>
        <h3 className="feed-title">{title}</h3>
        
        <div className="feed-host">
          <div className="host-avatar"></div>
          <span>Host: <strong>{host}</strong> ✨</span>
        </div>
        
        <div className="feed-details">
          <div className="detail-item"><Calendar size={14} className="icon-orange" /> {date}</div>
          <div className="detail-item"><MapPin size={14} className="icon-red" /> {location}</div>
          <div className="detail-item"><Users size={14} className="icon-blue" /> {joining} joining</div>
          <div className="detail-item">🔥 Lively trip!</div>
        </div>

        <div className="feed-actions">
          <div className="action-left">
            <button className="btn-circle"><MoreHorizontal size={16} /></button>
            <button className="btn-pill"><MessageCircle size={16} /> 12</button>
          </div>
          <button className="btn-primary">참여하기</button>
        </div>
      </div>
    </div>
  );
};

export default FeedCard;
