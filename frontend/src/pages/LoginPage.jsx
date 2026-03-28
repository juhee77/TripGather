import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthViewModel } from '../viewmodels/useAuthViewModel';
import { API_BASE } from '../api/client';
import './LoginPage.css';

function LoginPage() {
  const {
    isLogin,
    formData,
    error,
    successMessage,
    setSuccessMessage,
    loading,
    handleInputChange,
    toggleMode,
    handleSubmit
  } = useAuthViewModel();

  const navigate = useNavigate();

  React.useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    if (params.get('verified') === 'true') {
      setSuccessMessage('인증이 성공적으로 완료되었습니다! 이제 로그인이 가능합니다.');
    }
  }, [setSuccessMessage]);

  return (
    <div className="login-container">
      <div className="login-card">
        <h1 className="brand-logo">TripGather</h1>
        <p className="brand-subtitle">여행 일정을 공유하고 동네 친구를 만나보세요</p>
        
        <div className="tab-buttons">
          <button 
            className={`tab-btn ${isLogin ? 'active' : ''}`}
            onClick={toggleMode}
            disabled={isLogin}
          >로그인</button>
          <button 
            className={`tab-btn ${!isLogin ? 'active' : ''}`}
            onClick={toggleMode}
            disabled={!isLogin}
          >회원가입</button>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          {!isLogin && (
            <div className="input-group">
              <label>이름</label>
                <input 
                  type="text" 
                  name="name"
                  placeholder="홍길동"
                  value={formData.name}
                  onChange={handleInputChange}
                  required
                />
            </div>
          )}
          <div className="input-group">
            <label>이메일</label>
            <input 
              type="email" 
              name="email"
              placeholder="example@test.com"
              value={formData.email}
              onChange={handleInputChange}
              required
            />
          </div>
          <div className="input-group">
            <label>비밀번호</label>
            <input 
              type="password" 
              name="password"
              placeholder="••••••••"
              value={formData.password}
              onChange={handleInputChange}
              required
            />
          </div>

          {successMessage && <p className="success-msg">{successMessage}</p>}
          {error && <p className="error-msg">{error}</p>}

          <button type="submit" className="submit-btn" disabled={loading}>
            {loading ? '처리 중...' : (isLogin ? '로그인' : '가입하기')}
          </button>
        </form>

        <div className="sns-login-divider">
          <span>또는 SNS 계정으로 시작하기</span>
        </div>
        
        <div className="sns-buttons">
          <button 
            className="sns-btn kakao" 
            onClick={() => window.location.href = `${API_BASE}/oauth2/authorization/kakao`}
          >카카오로 시작하기</button>
          <button 
            className="sns-btn naver" 
            onClick={() => window.location.href = `${API_BASE}/oauth2/authorization/naver`}
          >네이버로 시작하기</button>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;
