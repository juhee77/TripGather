import React from 'react';
import './TicketBase.css';

const TicketBase = ({ 
  children, 
  header, 
  footer, 
  className = '', 
  cutoutColor = 'var(--bg-color)',
  glass = false 
}) => {
  return (
    <div className={`ticket-base ${glass ? 'glass' : 'surface-solid'} ${className}`}>
      {header && (
        <div className="ticket-header">
          {header}
          {/* Decorative Cutouts */}
          <div className="ticket-cutout left" style={{ backgroundColor: cutoutColor }}></div>
          <div className="ticket-cutout right" style={{ backgroundColor: cutoutColor }}></div>
        </div>
      )}
      <div className="ticket-body">
        {children}
      </div>
      {footer && (
        <div className="ticket-footer">
          {footer}
        </div>
      )}
    </div>
  );
};

export default TicketBase;
