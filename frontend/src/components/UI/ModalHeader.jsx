import React from 'react';
import { X } from 'lucide-react';

const ModalHeader = ({ title, subtitle, onClose, actions, dark = false }) => {
  const headerStyle = {
    padding: '24px 28px',
    borderBottom: `1px solid ${dark ? 'rgba(255,255,255,0.1)' : 'var(--border-color)'}`,
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    background: dark ? 'rgba(255,255,255,0.02)' : 'transparent'
  };

  return (
    <header style={headerStyle}>
      <div>
        <h2 className="heading-m" style={{ color: dark ? 'white' : 'var(--text-primary)', marginBottom: '4px' }}>
          {title}
        </h2>
        {subtitle && (
          <p style={{ fontSize: '12px', color: dark ? 'rgba(255,255,255,0.5)' : 'var(--text-secondary)', fontWeight: 700 }}>
            {subtitle}
          </p>
        )}
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
        {actions}
        <button onClick={onClose} className={`icon-circle ${dark ? 'glass' : ''}`} style={{ width: '40px', height: '40px' }}>
          <X size={20} color={dark ? 'white' : 'var(--text-primary)'} />
        </button>
      </div>
    </header>
  );
};

export default ModalHeader;
