import React, { createContext, useContext, useState, useEffect, useCallback, useMemo } from 'react';
import { authFetch } from '../api/client';
import { useAuth } from './AuthContext';

const UserContext = createContext(null);

export function UserProvider({ children }) {
  const { token } = useAuth();
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('user');
    try {
      return saved ? JSON.parse(saved) : null;
    } catch (e) {
      return null;
    }
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchMe = useCallback(async () => {
    if (!token) return;
    
    setLoading(true);
    setError(null);
    try {
      const res = await authFetch('/api/users/me');
      if (!res.ok) {
         if (res.status === 401) return null;
         throw new Error(`서버 오류 ${res.status}`);
      }
      const data = await res.json();
      setUser(data);
      localStorage.setItem('user', JSON.stringify(data));
      return data;
    } catch (err) {
      console.error('Failed to fetch current user:', err);
      setError(err.message);
      setUser(null);
      return null;
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    if (token) {
      fetchMe();
    } else {
      setUser(null);
      localStorage.removeItem('user');
    }
  }, [token, fetchMe]);

  const updateProfile = useCallback(async (id, payload) => {
    try {
      const res = await authFetch(`/api/users/${id}`, {
        method: 'PATCH',
        body: JSON.stringify(payload),
      });
      if (!res.ok) throw new Error(`업데이트 실패 ${res.status}`);
      const updated = await res.json();
      setUser(updated);
      return updated;
    } catch (err) {
      console.error('Failed to update profile:', err);
      throw err;
    }
  }, []);

  const value = useMemo(() => ({
    user,
    loading,
    error,
    refetch: fetchMe,
    updateProfile,
  }), [user, loading, error, fetchMe, updateProfile]);

  return (
    <UserContext.Provider value={value}>
      {children}
    </UserContext.Provider>
  );
}

export function useUser() {
  const ctx = useContext(UserContext);
  if (!ctx) throw new Error('useUser must be used within UserProvider');
  return ctx;
}

