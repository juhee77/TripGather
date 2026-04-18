import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { apiUrl } from '../api/client';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('token'));
  const [loading] = useState(false); // Auth verified by token presence initially, UserContext will handle deep verification

  useEffect(() => {
    // Sync token if localStorage changes in other tabs
    const handleStorageChange = () => {
      const savedToken = localStorage.getItem('token');
      if (savedToken !== token) {
        setToken(savedToken);
      }
    };
    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, [token]);

  const saveAuth = (token, userData) => {
    localStorage.setItem('token', token);
    if (userData) {
      localStorage.setItem('user', JSON.stringify(userData));
    }
    setToken(token);
  };

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('myJoinedIds'); // Clean up related local storage
    setToken(null);
    window.location.href = '/login'; 
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
      } catch (_e) {
        errorMessage = res.statusText || errorMessage;
      }
      throw new Error(errorMessage);
    }

    const data = await res.json();
    saveAuth(data.accessToken, { id: data.userId, name: data.name, email: data.email });
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
      } catch (_e) {
        errorMessage = res.statusText || errorMessage;
      }
      throw new Error(errorMessage);
    }

    const data = await res.json();
    saveAuth(data.accessToken, { id: data.userId, name: data.name, email: data.email });
    return data;
  };

  // Simplified AuthContext doesn't need to auto-load profile anymore
  // UserContext handles profile loading based on the token in localStorage

  const value = {
    token,
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

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
