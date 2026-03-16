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
      localStorage.setItem('accessToken', token);
      // AuthContext의 상태를 갱신하기 위해 로그인이 필요할 수 있음
      // 현재 AuthContext는 초기화 시 localStorage를 읽으므로 
      // 강제로 페이지를 새로고침하거나 상태 업데이트 함수를 호출해야 함
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
