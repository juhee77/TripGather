import React from 'react';
import { Plane, Calendar, MapPin, Users } from 'lucide-react';
import './FeedCard.css';

const FeedCard = ({ title, host, date, location, joining }) => {
  // Extract simple parts if available (e.g "2026-03-12 18:00" -> Date and Time)
  const dtParts = date ? date.split(' ') : ['TBD', ''];
  const pDate = dtParts[0] || 'TBD';
  const pTime = dtParts[1] || '';

  return (
    <div className="feed-card">
      <div className="ticket-top">
        <div className="ticket-airline-header">
          <div className="airline-logo">
            <Plane size={16} fill="var(--primary)" /> TRIP AIRLINES
          </div>
          <div>ECONOMY</div>
        </div>

        <h3 className="ticket-title">{title}</h3>

        <div className="flight-info-grid">
          <div className="info-block">
            <span className="info-label">Date</span>
            <span className="info-value" style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
              <Calendar size={12} color="#888" /> {pDate}
            </span>
          </div>
          <div className="info-block">
            <span className="info-label">Boarding Time</span>
            <span className="info-value">{pTime || 'TBD'}</span>
          </div>
          <div className="info-block">
            <span className="info-label">Terminal</span>
            <span className="info-value" style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
              <MapPin size={12} color="#888" /> {location}
            </span>
          </div>
        </div>

        <div className="flight-info-grid" style={{ marginBottom: 0 }}>
          <div className="info-block" style={{ gridColumn: 'span 2' }}>
            <span className="info-label">Passenger / Host</span>
            <div className="host-info" style={{ marginTop: 0, paddingTop: 4, borderTop: 'none' }}>
              <div className="host-avatar"></div>
              <span className="host-name">{host}</span>
            </div>
          </div>
          <div className="info-block">
            <span className="info-label">Party</span>
            <span className="info-value" style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
              <Users size={12} color="#888" /> {joining}
            </span>
          </div>
        </div>
      </div>

      <div className="ticket-bottom">
        <div className="barcode-area">
          <div className="barcode-lines"></div>
          <div className="barcode-text">TKT-B8X9-{Math.floor(Math.random() * 9000) + 1000}</div>
        </div>
        <button className="btn-board" style={{ pointerEvents: 'none' }}>
          BOARD
        </button>
      </div>
    </div>
  );
};

export default FeedCard;

