import React, { useState } from 'react';
import { Bookmark, Award, Clock, Share2 } from 'lucide-react';
import './StampBook.css';

const StampItem = ({ stamp }) => {
  const [isFlipped, setIsFlipped] = useState(false);

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'short', day: 'numeric' });
  };

  const handleShare = async (e) => {
    e.stopPropagation(); // 카드 뒤집히는 것 방지
    const shareData = {
      title: 'TripGather 디지털 스탬프',
      text: `우리 동네 여정 플랫폼 TripGather에서 [${stamp.missionTitle}] 스탬프를 획득했습니다! 🗺️✨`,
      url: window.location.origin
    };

    if (navigator.share) {
      try {
        await navigator.share(shareData);
      } catch (err) {
        console.error('Error sharing stamp:', err);
      }
    } else {
      try {
        await navigator.clipboard.writeText(`${shareData.text}\n${shareData.url}`);
        alert('스탬프 정보가 클립보드에 복사되었습니다! SNS에 인증해보세요.');
      } catch (err) {
        console.error('Failed to copy to clipboard:', err);
      }
    }
  };

  return (
    <div className="stamp-item-wrapper" onClick={() => setIsFlipped(!isFlipped)}>
      <div className={`stamp-card-3d ${isFlipped ? 'flipped' : ''}`}>
        
        {/* Front: The Official Stamp */}
        <div className="stamp-side stamp-front-light" style={{ position: 'relative' }}>
          <div className="stamp-inner-border">
            {stamp.stampImageUrl ? (
              <img src={stamp.stampImageUrl} alt="stamp" className="stamp-graphic" />
            ) : (
              <div className="stamp-seal-default">
                <Award size={32} />
                <span className="stamp-text-bold">VISITED</span>
              </div>
            )}
            {/* Stamp Ink Seal Animation Effect */}
            <div className="stamp-ink-seal" />
            <div className="stamp-location-label">{stamp.missionTitle}</div>
          </div>
        </div>

        {/* Back: Travel Memory Log */}
        <div className="stamp-side stamp-back-light">
          <div className="memory-log-header">
            <Bookmark size={14} color="var(--primary-orange)" />
            <span>TRAVEL LOG</span>
          </div>
          <div className="memory-log-content">
            <h4 className="mission-name-small">{stamp.missionTitle}</h4>
            <div className="memory-date-row">
              <Clock size={12} />
              <span>{formatDate(stamp.completedAt)}</span>
            </div>
          </div>
          <div>
            <div className="mission-clear-badge" style={{ marginBottom: '6px' }}>MISSION CLEAR</div>
            <button className="stamp-share-btn" onClick={handleShare}>
              <Share2 size={12} /> SNS 공유
            </button>
          </div>
        </div>

      </div>
    </div>
  );
};

const StampBook = ({ stamps, loading }) => {
  if (loading) {
    return (
      <div className="stamp-book-terminal-loading">
        <div className="spinner-orange"></div>
        <p>기록을 불러오는 중...</p>
      </div>
    );
  }

  return (
    <div className="stamp-terminal-container animate-fade">
      <div className="stamp-terminal-header">
        <div>
          <span className="label-orange">CHECK-IN HISTORY</span>
          <h3 className="heading-l" style={{ margin: '4px 0 0 0', fontWeight: 900 }}>스탬프 켈렉션</h3>
        </div>
        <div className="stamp-count-badge">
          {stamps.length} STAMPS
        </div>
      </div>

      <div className="stamp-board-grid">
        {stamps.length === 0 ? (
          <div className="stamp-empty-board">
            <div className="empty-icon">🗺️</div>
            <p className="empty-text">아직 수집한 스탬프가 없습니다.</p>
            <span className="empty-hint">새로운 장소로 떠나 미션을 완료해보세요!</span>
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
