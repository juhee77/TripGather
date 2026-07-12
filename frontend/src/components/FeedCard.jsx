import React from 'react';
import { MapPin, Users, Calendar, MessageCircle, ArrowRight, Share2, Heart } from 'lucide-react';
import TicketContainer from './UI/TicketContainer';
import './FeedCard.css';

const FeedCard = ({ title, host, date, location, joining, bgImage, commentCount = 0, pendingCount = 0, likedByCurrentUser = false, onLike, onClick, isStandby = false }) => {
  const handleLikeClick = (e) => {
    e.stopPropagation();
    if (onLike) onLike();
  };
  const topSection = (
    <div className={`feed-header-wrapper ${isStandby ? 'standby-tag-header' : ''}`}>
      <div 
        className="feed-image" 
        style={{ backgroundImage: bgImage ? `url(${bgImage})` : 'none' }}
      >
        <div className="feed-overlay" />
        <div className="feed-status-badge" style={{ background: isStandby ? '#f59f00' : 'var(--primary-orange)' }}>
          {isStandby ? 'STANDBY' : 'REC'}
        </div>
      </div>
      
      <div className="feed-header-content">
        <div className="flex-between w-full">
          <span className="label-orange" style={{ color: isStandby ? '#f59f00' : 'var(--primary-orange)', display: 'flex', alignItems: 'center', gap: '4px' }}>
            {isStandby ? '🎫 STANDBY BAGGAGE TAG' : '✈️ BOARDING PASS • GATE 07'}
          </span>
          <div style={{ display: 'flex', gap: '8px' }}>
            {pendingCount > 0 && (
              <div className="pending-badge">
                WAITING {pendingCount}
              </div>
            )}
            <div className="status-pill" style={{ background: isStandby ? '#f59f00' : 'var(--primary-orange)' }}>OPEN</div>
          </div>
        </div>
        <h3 className="feed-title">{title}</h3>
      </div>
    </div>
  );

  const bottomSection = (
    <div className={`feed-info-wrapper ${isStandby ? 'standby-info-wrapper' : ''}`}>
      <div className="host-section">
        <div className="host-avatar-group">
          <div className="host-avatar" />
          <div className="host-name-tag">
            <span className="label-muted">HOST</span>
            <span className="info-value">{host}</span>
          </div>
        </div>
        <button className="icon-btn">
          <Share2 size={16} color="var(--text-muted)" />
        </button>
      </div>

      <div className="ticket-details-grid">
        <div className="detail-item">
          <span className="label-muted">{isStandby ? 'MEETUP TIME' : 'DEPARTURE'}</span>
          <div className="detail-content">
            <Calendar size={14} color={isStandby ? '#f59f00' : 'var(--primary-orange)'} />
            <span className="info-value">{date}</span>
          </div>
        </div>
        <div className="detail-item text-right">
          <span className="label-muted">{isStandby ? 'LOCATION' : 'DESTINATION'}</span>
          <div className="detail-content" style={{ justifyContent: 'flex-end' }}>
            <MapPin size={14} color="var(--secondary-blue)" />
            <span className="info-value">{location}</span>
          </div>
        </div>
      </div>

      <div className="feed-action-bar">
        <div className="participants-info">
          <Users size={16} color="var(--text-muted)" />
          <span className="text-s"><strong>{joining}</strong> Traveling</span>
        </div>
        <div className="social-stats">
          <button 
            className={`stat-item icon-btn ${likedByCurrentUser ? 'like-animating' : ''}`} 
            onClick={handleLikeClick}
            style={{ 
              color: likedByCurrentUser ? '#FF6B6B' : 'var(--text-muted)',
              transition: 'all 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275)'
            }}
          >
            <Heart size={18} fill={likedByCurrentUser ? '#FF6B6B' : 'none'} />
          </button>
          <div className="stat-item">
            <MessageCircle size={16} color="var(--text-muted)" />
            <span>{commentCount}</span>
          </div>
          <button className="go-btn" onClick={onClick} style={{ background: isStandby ? '#f59f00' : 'var(--text-primary)' }}>
            GO <ArrowRight size={14} />
          </button>
        </div>
      </div>
    </div>
  );

  return (
    <TicketContainer 
      topSection={topSection} 
      bottomSection={bottomSection}
      onClick={onClick}
      className={`feed-ticket ${isStandby ? 'standby-tag' : ''}`}
    />
  );
};


export default FeedCard;
