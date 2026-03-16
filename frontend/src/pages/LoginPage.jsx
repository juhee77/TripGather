import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import './LoginPage.css';

function LoginPage() {
  const [isLogin, setIsLogin] = useState(true);
  const [formData, setFormData] = useState({ name: '', email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
  const { login, signup } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (isLogin) {
        await login(formData.email, formData.password);
      } else {
        await signup(formData.name, formData.email, formData.password);
      }
      navigate('/gather');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h1 className="brand-logo">TripGather</h1>
        <p className="brand-subtitle">여행 일정을 공유하고 동네 친구를 만나보세요</p>
        
        <div className="tab-buttons">
          <button 
            className={`tab-btn ${isLogin ? 'active' : ''}`}
            onClick={() => setIsLogin(true)}
          >로그인</button>
          <button 
            className={`tab-btn ${!isLogin ? 'active' : ''}`}
            onClick={() => setIsLogin(false)}
          >회원가입</button>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          {!isLogin && (
            <div className="input-group">
              <label>이름</label>
              <input 
                type="text" 
                placeholder="홍길동"
                value={formData.name}
                onChange={(e) => setFormData({...formData, name: e.target.value})}
                required
              />
            </div>
          )}
          <div className="input-group">
            <label>이메일</label>
            <input 
              type="email" 
              placeholder="example@test.com"
              value={formData.email}
              onChange={(e) => setFormData({...formData, email: e.target.value})}
              required
            />
          </div>
          <div className="input-group">
            <label>비밀번호</label>
            <input 
              type="password" 
              placeholder="••••••••"
              value={formData.password}
              onChange={(e) => setFormData({...formData, password: e.target.value})}
              required
            />
          </div>

          {error && <p className="error-msg">{error}</p>}

          <button type="submit" className="submit-btn" disabled={loading}>
            {loading ? '처리 중...' : (isLogin ? '로그인' : '가입하기')}
          </button>
        </form>

        <div className="sns-login-divider">
          <span>또는 SNS 로그인 (준비 중)</span>
        </div>
        
        <div className="sns-buttons">
          <button className="sns-btn kakao" disabled>카카오로 시작하기</button>
          <button className="sns-btn google" disabled>Google로 시작하기</button>
        </div>
      </div>
    </div>
  );
}

export default LoginPage;
