import React from 'react';

/**
 * TicketContainer
 * 항공권(Boarding Pass) 스타일의 공통 컨테이너
 * @param {React.Node} topSection - 절취선 위쪽 콘텐츠 (주로 이미지나 핵심 정보)
 * @param {React.Node} bottomSection - 절취선 아래쪽 콘텐츠 (세부 정보, 버튼 등)
 * @param {string} className - 추가 스타일 클래스
 */
const TicketContainer = ({ topSection, bottomSection, className = '', onClick }) => {
  return (
    <div 
      className={`ticket-wrapper animate-fade ${className}`} 
      onClick={onClick}
      style={{ cursor: onClick ? 'pointer' : 'default' }}
    >
      {/* Top Section */}
      <div className="ticket-top">
        {topSection}
      </div>

      {/* Perforated Divider */}
      <div className="ticket-divider" />

      {/* Bottom Section */}
      <div className="ticket-bottom" style={{ padding: '0 20px 20px 20px' }}>
        {bottomSection}
      </div>
    </div>
  );
};

export default TicketContainer;
