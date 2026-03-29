import React, { useState } from 'react';
import { Image as ImageIcon } from 'lucide-react';
import './StampBook.css';

const StampItem = ({ stamp }) => {
  const [isFlipped, setIsFlipped] = useState(false);

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'short', day: 'numeric' });
  };

  return (
    <div className="stamp-item-container" onClick={() => setIsFlipped(!isFlipped)}>
      <div className={`stamp-card ${isFlipped ? 'flipped' : ''}`}>
        
        {/* Front: The Stamp Identity */}
        <div className="stamp-face stamp-front">
          {stamp.stampImageUrl ? (
            <img src={stamp.stampImageUrl} alt="stamp" className="stamp-image" />
          ) : (
            <div className="stamp-border">
              <div className="stamp-inner">
                <span className="stamp-clear-text">CLEAR</span>
                <div className="stamp-details">
                  <span className="stamp-mission">{stamp.missionTitle}</span>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Back: The Attached Memory / Photo */}
        <div className="stamp-face stamp-back">
          <div className="stamp-info-back">
            <span className="stamp-mission-name">{stamp.missionTitle}</span>
            <span className="stamp-date">{formatDate(stamp.completedAt)}</span>
            <div className="stamp-badge">MISSION COMPLETE</div>
          </div>
        </div>

      </div>
    </div>
  );
};

const StampBook = ({ stamps, loading }) => {
  if (loading) {
    return (
      <div className="stamp-book-loading">
        <span className="animate-pulse">Loading Stamps...</span>
      </div>
    );
  }

  return (
    <div className="stamp-book-container animate-fade" style={{ animationDelay: '0.2s' }}>
      <div className="stamp-book-header">
        <h3 className="heading-m">Stamp Collection</h3>
        <p className="text-s">Your completed missions and memories</p>
      </div>

      <div className="stamp-grid">
        {stamps.length === 0 ? (
          <div className="stamp-empty-state">
            <div style={{ fontSize: '40px', marginBottom: '16px' }}>📭</div>
            <p>아직 수집한 스탬프가 없습니다.</p>
            <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>진행 중인 미션 체크인을 완료해보세요!</span>
          </div>
        ) : (
          stamps.map(stamp => (
            <StampItem key={stamp.missionId} stamp={stamp} />
          ))
        )}
      </div>
    </div>
  );
};

export default StampBook;
