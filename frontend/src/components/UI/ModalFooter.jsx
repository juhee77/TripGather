import React from 'react';

const ModalFooter = ({ children, dark = false }) => {
  return (
    <footer style={{ 
      padding: '28px', 
      borderTop: `1px solid ${dark ? 'rgba(255,255,255,0.1)' : 'var(--border-color)'}`, 
      background: dark ? 'rgba(13, 13, 25, 0.8)' : 'var(--surface-solid)',
      backdropFilter: dark ? 'blur(10px)' : 'none'
    }}>
      {children}
    </footer>
  );
};

export default ModalFooter;
