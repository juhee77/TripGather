import React, { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

const OAuth2RedirectHandler = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const token = params.get('token');

    if (token) {
      localStorage.setItem('token', token);
      window.location.href = '/'; 
    } else {
      navigate('/login', { replace: true });
    }
  }, [location, navigate]);

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
      <p>로그인 중입니다. 잠시만 기다려주세요...</p>
    </div>
  );
};

export default OAuth2RedirectHandler;
