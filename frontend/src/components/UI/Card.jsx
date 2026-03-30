import React from 'react';
import './Card.css';

const Card = ({ children, className = '', animate = true, glass = true, padding = true }) => {
  const classes = [
    'ui-card',
    animate ? 'animate-fade' : '',
    glass ? 'glass' : 'surface-solid',
    padding ? 'padding-md' : '',
    className
  ].join(' ').trim();

  return (
    <div className={classes}>
      {children}
    </div>
  );
};

export default Card;
