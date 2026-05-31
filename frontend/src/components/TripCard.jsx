import React from 'react';
import { MapPin, Calendar, CheckCircle, Star, ChevronRight } from 'lucide-react';

const STATUS_LABELS = { PLANNING: '계획 중', ONGOING: '여행 중', COMPLETED: '완료' };
const STATUS_COLORS = { PLANNING: '#3b82f6', ONGOING: '#f59e0b', COMPLETED: '#10b981' };

const TripCard = ({ trip, onClick }) => {
  const statusLabel = STATUS_LABELS[trip.status] || trip.status;
  const statusColor = STATUS_COLORS[trip.status] || '#94a3b8';

  return (
    <div
      onClick={onClick}
      style={{
        background: 'white',
        borderRadius: '20px',
        overflow: 'hidden',
        border: '1px solid var(--border-color)',
        cursor: 'pointer',
        transition: 'all 0.3s ease',
        boxShadow: '0 2px 12px rgba(0,0,0,0.04)',
      }}
      className="animate-fade"
      onMouseEnter={e => e.currentTarget.style.transform = 'translateY(-2px)'}
      onMouseLeave={e => e.currentTarget.style.transform = 'translateY(0)'}
    >
      {/* Cover Image */}
      <div style={{
        height: '120px',
        background: trip.bgImageUrl
          ? `url(${trip.bgImageUrl}) center/cover`
          : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        position: 'relative'
      }}>
        <span style={{
          position: 'absolute', top: '12px', left: '12px',
          background: statusColor, color: 'white',
          padding: '4px 12px', borderRadius: '20px',
          fontSize: '11px', fontWeight: 800
        }}>
          {statusLabel}
        </span>
      </div>

      {/* Content */}
      <div style={{ padding: '16px 20px' }}>
        <h3 style={{ fontSize: '17px', fontWeight: 900, color: 'var(--text-primary)', marginBottom: '8px' }}>
          {trip.title}
        </h3>

        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '12px', marginBottom: '12px' }}>
          {trip.destination && (
            <span style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '13px', color: 'var(--text-secondary)', fontWeight: 600 }}>
              <MapPin size={14} /> {trip.destination}
            </span>
          )}
          {trip.startDate && (
            <span style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '13px', color: 'var(--text-secondary)', fontWeight: 600 }}>
              <Calendar size={14} /> {trip.startDate}{trip.endDate ? ` ~ ${trip.endDate}` : ''}
            </span>
          )}
        </div>

        {/* Stats */}
        <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
          <span style={{ fontSize: '12px', fontWeight: 700, color: 'var(--primary-orange)' }}>
            📋 일정 {trip.itineraryCount || 0}개
          </span>
          <span style={{ fontSize: '12px', fontWeight: 700, color: '#10b981' }}>
            <CheckCircle size={12} style={{ marginRight: '2px' }} />
            준비물 {trip.packingProgress || 0}%
          </span>
          {trip.reviewCount > 0 && (
            <span style={{ fontSize: '12px', fontWeight: 700, color: '#f59e0b' }}>
              <Star size={12} style={{ marginRight: '2px' }} />
              리뷰 {trip.reviewCount}
            </span>
          )}
          <ChevronRight size={16} color="var(--text-sub)" style={{ marginLeft: 'auto' }} />
        </div>
      </div>
    </div>
  );
};

export default TripCard;
