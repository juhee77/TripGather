import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { apiUrl } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    setToken(null);
    setUser(null);
  }, []);

  const login = async (email, password) => {
    const res = await fetch(apiUrl('/api/auth/login'), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    });

    if (!res.ok) {
      const errData = await res.json();
      throw new Error(errData.message || '로그인 실패');
    }

    const data = await res.json();
    localStorage.setItem('token', data.accessToken);
    setToken(data.accessToken);
    setUser({ id: data.userId, name: data.name, email: data.email });
    return data;
  };

  const signup = async (name, email, password) => {
    const res = await fetch(apiUrl('/api/auth/register'), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email, password }),
    });

    if (!res.ok) {
      const errData = await res.json();
      throw new Error(errData.message || '회원가입 실패');
    }

    const data = await res.json();
    localStorage.setItem('token', data.accessToken);
    setToken(data.accessToken);
    setUser({ id: data.userId, name: data.name, email: data.email });
    return data;
  };

  useEffect(() => {
    if (token) {
      // 토큰이 있으면 유저 정보 가져오기 (UserContext와 협력하도록 설계 가능)
      // 여기서는 단순히 토큰 유무로 일단 처리
      setLoading(false);
    } else {
      setLoading(false);
    }
  }, [token]);

  const value = {
    token,
    user,
    isAuthenticated: !!token,
    login,
    signup,
    logout,
    loading
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
