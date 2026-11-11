import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { API_BASE } from '../api/client';
import PrimaryButton from '../components/UI/PrimaryButton';
import './LoginPage.css';

const backLinkStyle = {
  display: 'block', textAlign: 'center', marginTop: '16px',
  fontSize: '13px', fontWeight: 700, color: 'var(--text-secondary)'
};

/**
 * 비밀번호 재설정 메일 요청 화면.
 *
 * 가입된 이메일인지 알려주지 않는다. 서버가 가입 여부와 무관하게 같은 응답을 주는데,
 * 화면에서 "가입되지 않은 이메일입니다" 라고 말해 버리면 그 배려가 무의미해진다.
 */
const ForgotPasswordPage = () => {
  const [email, setEmail] = useState('');
  const [sent, setSent] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`${API_BASE}/api/auth/password-reset/request`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email }),
      });
      if (!res.ok) {
        const body = await res.json().catch(() => null);
        throw new Error(body?.message || '요청에 실패했습니다.');
      }
      setSent(true);
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
        <p className="brand-subtitle">비밀번호를 재설정합니다</p>

        {sent ? (
          <div className="auth-form">
            <p className="success-msg">
              메일을 보냈습니다. 받은 편지함에서 링크를 열어 새 비밀번호를 설정해주세요.
            </p>
            <p style={{ fontSize: '13px', color: 'var(--text-secondary)', fontWeight: 600, lineHeight: 1.6 }}>
              링크는 30분 동안만 유효합니다. 메일이 보이지 않으면 스팸함도 확인해보세요.
              카카오·네이버로 가입한 계정은 비밀번호가 없으니 해당 버튼으로 로그인해주세요.
            </p>
            <Link to="/login" style={backLinkStyle}>로그인으로 돌아가기</Link>
          </div>
        ) : (
          <form onSubmit={handleSubmit} className="auth-form">
            <div className="input-group">
              <label htmlFor="reset-email">이메일</label>
              <input
                id="reset-email"
                type="email"
                placeholder="example@test.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            {error && <p className="error-msg">{error}</p>}

            <PrimaryButton type="submit" variant="primary" className="submit-btn"
                           disabled={loading} loading={loading}>
              재설정 링크 받기
            </PrimaryButton>

            <Link to="/login" style={backLinkStyle}>로그인으로 돌아가기</Link>
          </form>
        )}
      </div>
    </div>
  );
};

export default ForgotPasswordPage;
