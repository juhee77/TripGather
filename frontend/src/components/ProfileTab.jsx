/* eslint-disable */
import React, { useState, useEffect } from 'react';
import PassportCard from './PassportCard';
import StampBook from './StampBook';
import { authFetch } from '../api/client';
import { LogOut, Settings } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { useUserViewModel } from '../viewmodels/useUserViewModel';
import { useNavigate } from 'react-router-dom';

const ProfileTab = () => {
  const navigate = useNavigate();
  const { user } = useUserViewModel();
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

      <PassportCard user={user} stampsCount={stamps.length} />
      
      <StampBook stamps={stamps} loading={loading} />

    </div>
  );
};

export default ProfileTab;
