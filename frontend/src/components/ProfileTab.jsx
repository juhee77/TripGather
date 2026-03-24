import React, { useState, useEffect } from 'react';
import PassportCard from './PassportCard';
import StampBook from './StampBook';
import { authFetch } from '../api/client';
import { useUser } from '../contexts/UserContext';
import { LogOut, Settings } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';

const ProfileTab = () => {
  const { user: currentUser } = useUser();
  const { logout } = useAuth();
  const [stamps, setStamps] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (currentUser) {
      setLoading(true);
      authFetch('/api/missions/me/stamps')
        .then(res => res.json())
        .then(data => {
          setStamps(data);
          setLoading(false);
        })
        .catch(err => {
          console.error("Error fetching stamps:", err);
          setLoading(false);
        });
    }
  }, [currentUser]);

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

      <PassportCard user={currentUser} stampsCount={stamps.length} />
      
      <StampBook stamps={stamps} loading={loading} />

    </div>
  );
};

export default ProfileTab;
