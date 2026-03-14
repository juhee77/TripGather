import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { apiUrl } from '../api/client';

const UserContext = createContext(null);

export function UserProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchMe = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(apiUrl('/api/users/me'));
      if (!res.ok) throw new Error(`서버 오류 ${res.status}`);
      const data = await res.json();
      setUser(data);
      return data;
    } catch (err) {
      console.error('Failed to fetch current user:', err);
      setError(err.message);
      setUser(null);
      return null;
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchMe();
  }, [fetchMe]);

  const updateProfile = useCallback(async (id, payload) => {
    try {
      const res = await fetch(apiUrl(`/api/users/${id}`), {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
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

  const value = {
    user,
    loading,
    error,
    refetch: fetchMe,
    updateProfile,
  };

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
