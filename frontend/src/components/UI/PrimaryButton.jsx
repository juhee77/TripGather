import React from 'react';

const PrimaryButton = ({ 
  children, 
  onClick, 
  disabled, 
  loading, 
  variant = 'primary', // 'primary', 'secondary', 'glass', 'night', 'outline'
  className = '',
  style, 
  ...props 
}) => {
  const getVariantClass = () => {
    switch (variant) {
      case 'secondary': return 'secondary-btn';
      case 'glass': return 'glass-btn';
      case 'night': return 'night-btn';
      case 'outline': return 'outline-btn';
      default: return 'primary-btn';
    }
  };

  return (
    <button 
      onClick={onClick}
      disabled={disabled || loading}
      className={`${getVariantClass()} ${className}`}
      style={{
        opacity: (disabled || loading) ? 0.6 : 1,
        cursor: (disabled || loading) ? 'not-allowed' : 'pointer',
        ...style
      }}
      {...props}
    >
      {loading ? 'PROCESSING...' : children}
    </button>
  );
};

export default PrimaryButton;
