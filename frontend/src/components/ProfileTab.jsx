import React, { useState, useEffect } from 'react';
import PassportCard from './PassportCard';
import StampBook from './StampBook';
import { LogOut, Settings } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { useUser } from '../contexts/UserContext';
import { useNavigate } from 'react-router-dom';
import { authFetch } from '../api/client';

const ProfileTab = () => {
  const navigate = useNavigate();
  const { user, refetch } = useUser();
  const { logout } = useAuth();
  const [stamps, setStamps] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    refetch().catch(err => console.error("Failed to refetch user in ProfileTab:", err));

    setLoading(true);
    authFetch('/api/stamps/me')
      .then(res => {
        if (!res.ok) throw new Error("Failed to fetch stamps");
        return res.json();
      })
      .then(data => {
        setStamps(data);
      })
      .catch(err => console.error("Error loading stamps:", err))
      .finally(() => setLoading(false));

    const handleFocus = () => {
      refetch().catch(err => console.error("Failed to refetch user on focus in ProfileTab:", err));
    };

    window.addEventListener('focus', handleFocus);
    return () => window.removeEventListener('focus', handleFocus);
  }, [refetch]);

  return (
    <div style={{ paddingBottom: '100px' }} className="animate-fade">
      
      {/* Premium Profile Actions */}
      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginBottom: '28px' }}>
        <button onClick={() => navigate('/profile/hosting')} className="primary-btn" style={{ padding: '10px 20px', borderRadius: '14px', fontSize: '13px' }}>
           🧑‍✈️ 터미널 (주최/요청 관리)
        </button>
        <button className="secondary-btn" style={{ padding: '10px 20px', borderRadius: '14px', fontSize: '13px' }}>
          <Settings size={16} />
        </button>
        <button onClick={logout} className="secondary-btn" style={{ padding: '10px 20px', borderRadius: '14px', fontSize: '13px', color: '#EF4444', borderColor: 'rgba(239, 68, 68, 0.2)' }}>
          <LogOut size={16} style={{ marginRight: '8px' }} /> LOGOUT
        </button>
      </div>

      <PassportCard user={user} stamps={stamps} />
      
      <StampBook stamps={stamps} loading={loading} />

    </div>
  );
};

export default ProfileTab;
