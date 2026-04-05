import React from 'react';
import './Card.css';

const Card = ({ children, className = '', animate = true, glass = true, padding = true, onClick, style }) => {
  const classes = [
    'ui-card',
    animate ? 'animate-fade' : '',
    glass ? 'glass' : 'surface-solid',
    padding ? 'padding-md' : '',
    className
  ].join(' ').trim();

  return (
    <div className={classes} onClick={onClick} style={style}>
      {children}
    </div>
  );
};

export default Card;
