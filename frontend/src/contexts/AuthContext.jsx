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
      let errorMessage = '로그인 실패';
      try {
        const errData = await res.json();
        errorMessage = errData.message || errorMessage;
      } catch (e) {
        errorMessage = res.statusText || errorMessage;
      }
      throw new Error(errorMessage);
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
      let errorMessage = '회원가입 실패';
      try {
        const errData = await res.json();
        errorMessage = errData.message || errorMessage;
      } catch (e) {
        errorMessage = res.statusText || errorMessage;
      }
      throw new Error(errorMessage);
    }

    const data = await res.json();
    localStorage.setItem('token', data.accessToken);
    setToken(data.accessToken);
    setUser({ id: data.userId, name: data.name, email: data.email });
    return data;
  };

  useEffect(() => {
    const fetchProfile = async () => {
      if (token && !user) {
        try {
          const res = await fetch(apiUrl('/api/users/me'), {
            headers: { 'Authorization': `Bearer ${token}` }
          });
          if (res.ok) {
            const userData = await res.json();
            setUser({ id: userData.id, name: userData.name, email: userData.email, profileImageUrl: userData.profileImageUrl });
          } else if (res.status === 401) {
            logout();
          }
        } catch (err) {
          console.error('Auto-loading profile failed:', err);
        }
      }
      setLoading(false);
    };
    fetchProfile();
  }, [token, user, logout]);

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
