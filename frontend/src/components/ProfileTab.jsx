import React, { useState, useEffect } from 'react';
import PassportCard from './PassportCard';
import StampBook from './StampBook';
import { authFetch } from '../api/client';
import { useUser } from '../contexts/UserContext';
import { LogOut, Settings } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';

import { useUserViewModel } from '../viewmodels/useUserViewModel';
import ChatRepository from '../repositories/ChatRepository';

const ProfileTab = () => {
  const { user, loading: userLoading, updateProfile } = useUserViewModel();
  const { logout } = useAuth();
  const [stamps, setStamps] = useState([]);
  const [loading, setLoading] = useState(false);
  const [hasFetched, setHasFetched] = useState(false);

  useEffect(() => {
    if (user && !hasFetched) {
      setLoading(true);
      authFetch('/api/missions/me/stamps')
        .then(res => res.json())
        .then(data => {
          setStamps(Array.isArray(data) ? data : []);
          setLoading(false);
          setHasFetched(true);
        })
        .catch(err => {
          console.error("Error fetching stamps:", err);
          setLoading(false);
        });
    }
  }, [user, hasFetched]);

  return (
    <div style={{ paddingBottom: '40px' }}>
      
      {/* Profile Actions */}
      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginBottom: '24px' }}>
        <button className="glass text-s" style={{ padding: '8px 16px', borderRadius: 'var(--radius-full)', display: 'flex', alignItems: 'center', gap: '6px', fontWeight: 600, border: '1px solid var(--border-color)', color: 'var(--text-primary)' }}>
          <Settings size={16} /> 설정
        </button>
        <button onClick={logout} className="glass text-s" style={{ padding: '8px 16px', borderRadius: 'var(--radius-full)', display: 'flex', alignItems: 'center', gap: '6px', fontWeight: 600, border: '1px solid rgba(239, 68, 68, 0.2)', color: '#EF4444' }}>
          <LogOut size={16} /> 로그아웃
        </button>
      </div>

      <PassportCard user={user} stampsCount={stamps.length} />
      
      <StampBook stamps={stamps} loading={loading} />

    </div>
  );
};

export default ProfileTab;
