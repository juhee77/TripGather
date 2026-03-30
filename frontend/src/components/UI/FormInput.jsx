import React from 'react';

const FormInput = ({ label, icon: Icon, dark = false, ...props }) => {
  return (
    <div>
      {label && (
        <label className={dark ? 'label-orange' : 'text-s'} style={{ 
          color: dark ? 'var(--primary-orange)' : 'var(--text-secondary)',
          display: 'block', 
          marginBottom: '8px',
          fontWeight: 800,
          fontSize: '13px'
        }}>
          {label}
        </label>
      )}
      <div style={{ position: 'relative' }}>
        {Icon && (
          <Icon 
            size={18} 
            color={dark ? 'rgba(255,255,255,0.3)' : 'var(--text-muted)'} 
            style={{ position: 'absolute', top: '16px', left: '16px' }} 
          />
        )}
        <input 
          className={dark ? 'input-night' : 'glass'}
          style={!dark ? { 
            width: '100%', padding: '16px 16px 16px 48px', borderRadius: '16px', border: '1px solid var(--border-color)',
            color: 'var(--text-primary)', fontSize: '16px', fontWeight: 600, outline: 'none'
          } : {}}
          {...props} 
        />
      </div>
    </div>
  );
};

export default FormInput;
