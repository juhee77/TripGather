import React, { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { API_BASE } from '../api/client';
import PrimaryButton from '../components/UI/PrimaryButton';
import './LoginPage.css';

const backLinkStyle = {
  display: 'block', textAlign: 'center', marginTop: '16px',
  fontSize: '13px', fontWeight: 700, color: 'var(--text-secondary)'
};

const MIN_LENGTH = 8;

/** 메일로 받은 링크에서 새 비밀번호를 설정하는 화면. */
const ResetPasswordPage = () => {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const token = params.get('token');

  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    // 서버도 같은 규칙을 보지만, 다시 입력하게 만들기 전에 여기서 먼저 알려준다.
    if (password.length < MIN_LENGTH) {
      setError(`비밀번호는 ${MIN_LENGTH}자 이상이어야 합니다.`);
      return;
    }
    if (password !== confirm) {
      setError('두 비밀번호가 일치하지 않습니다.');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`${API_BASE}/api/auth/password-reset/confirm`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token, newPassword: password }),
      });
      if (!res.ok) {
        const body = await res.json().catch(() => null);
        throw new Error(body?.message || '재설정에 실패했습니다.');
      }
      alert('비밀번호가 변경되었습니다. 새 비밀번호로 로그인해주세요.');
      navigate('/login');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  if (!token) {
    return (
      <div className="login-container">
        <div className="login-card">
          <h1 className="brand-logo">TripGather</h1>
          <p className="error-msg">재설정 링크가 올바르지 않습니다. 메일의 링크를 다시 확인해주세요.</p>
          <Link to="/forgot-password" style={backLinkStyle}>재설정 링크 다시 받기</Link>
        </div>
      </div>
    );
  }

  return (
    <div className="login-container">
      <div className="login-card">
        <h1 className="brand-logo">TripGather</h1>
        <p className="brand-subtitle">새 비밀번호를 설정해주세요</p>

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="input-group">
            <label htmlFor="new-password">새 비밀번호</label>
            <input
              id="new-password" type="password" placeholder="8자 이상"
              value={password} onChange={(e) => setPassword(e.target.value)} required
            />
          </div>
          <div className="input-group">
            <label htmlFor="confirm-password">새 비밀번호 확인</label>
            <input
              id="confirm-password" type="password" placeholder="한 번 더 입력"
              value={confirm} onChange={(e) => setConfirm(e.target.value)} required
            />
          </div>

          {error && <p className="error-msg">{error}</p>}

          <PrimaryButton type="submit" variant="primary" className="submit-btn"
                         disabled={loading} loading={loading}>
            비밀번호 변경하기
          </PrimaryButton>

          <Link to="/login" style={backLinkStyle}>로그인으로 돌아가기</Link>
        </form>
      </div>
    </div>
  );
};

export default ResetPasswordPage;
