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

  const [hostedGatherings, setHostedGatherings] = useState([]);
  const [loadingHosted, setLoadingHosted] = useState(false);

  useEffect(() => {
    if (user && !hasFetched) {
      setLoadingHosted(true);
      authFetch('/api/gatherings/my/hosted')
        .then(res => res.json())
        .then(data => {
          setHostedGatherings(Array.isArray(data) ? data : []);
          setLoadingHosted(false);
        })
        .catch(err => {
          console.error("Error fetching hosted gatherings:", err);
          setLoadingHosted(false);
        });
    }
  }, [user, hasFetched]);

  const handleAction = async (gatheringId, userId, action) => {
    try {
      const res = await authFetch(`/api/gatherings/${gatheringId}/members/${userId}/${action}`, {
        method: 'POST'
      });
      if (res.ok) {
        // Refresh the list
        const updated = await authFetch('/api/gatherings/my/hosted').then(r => r.json());
        setHostedGatherings(updated);
      }
    } catch (err) {
      console.error(`Error ${action} member:`, err);
    }
  };

  const pendingApplicants = hostedGatherings.flatMap(g => 
    (g.members || []).filter(m => m.status === 'PENDING').map(m => ({ ...m, gatheringId: g.id, gatheringTitle: g.title }))
  );

  return (
    <div style={{ paddingBottom: '100px' }}>
      
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
      
      {/* Host Management Center */}
      <div style={{ marginTop: '32px', marginBottom: '32px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
          <h3 style={{ fontSize: '18px', fontWeight: 800, color: 'var(--text-primary)', display: 'flex', alignItems: 'center', gap: '8px' }}>
            주최 관리 <span style={{ background: 'var(--primary-orange)', color: 'white', fontSize: '12px', padding: '2px 8px', borderRadius: '10px' }}>{pendingApplicants.length}</span>
          </h3>
          <span style={{ fontSize: '13px', color: 'var(--text-muted)', fontWeight: 600 }}>신청자 통합 관리</span>
        </div>

        {pendingApplicants.length > 0 ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {pendingApplicants.map((app, idx) => (
              <div key={`${app.gatheringId}-${app.user.id}`} className="glass" style={{ 
                padding: '16px 20px', borderRadius: '24px', display: 'flex', alignItems: 'center', gap: '14px',
                animation: 'fadeIn 0.4s ease-out forwards', animationDelay: `${idx * 0.05}s`, opacity: 0
              }}>
                <div style={{ width: '44px', height: '44px', borderRadius: '14px', background: 'var(--bg-color)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 800, color: 'var(--text-primary)', fontSize: '15px', border: '1px solid var(--border-color)' }}>
                  {app.user.name[0]}
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <span style={{ fontWeight: 800, fontSize: '15px' }}>{app.user.name}</span>
                    <span style={{ fontSize: '11px', color: 'var(--primary-orange)', fontWeight: 800, background: 'rgba(255, 92, 0, 0.08)', padding: '2px 6px', borderRadius: '4px' }}>신청</span>
                  </div>
                  <p style={{ fontSize: '12px', color: 'var(--text-muted)', fontWeight: 500, marginTop: '2px' }}>{app.gatheringTitle}</p>
                </div>
                <div style={{ display: 'flex', gap: '6px' }}>
                  <button 
                    onClick={() => handleAction(app.gatheringId, app.user.id, 'approve')}
                    className="glass" 
                    style={{ padding: '8px 14px', borderRadius: '12px', fontSize: '12px', fontWeight: 800, color: 'white', background: 'var(--primary-orange)', border: 'none' }}
                  >
                    승인
                  </button>
                  <button 
                    onClick={() => handleAction(app.gatheringId, app.user.id, 'reject')}
                    className="glass" 
                    style={{ padding: '8px 14px', borderRadius: '12px', fontSize: '12px', fontWeight: 800, color: 'var(--text-muted)', border: '1px solid var(--border-color)' }}
                  >
                    거절
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="glass" style={{ padding: '40px 20px', borderRadius: '24px', textAlign: 'center', border: '1px dashed var(--border-color)' }}>
            <p style={{ color: 'var(--text-muted)', fontWeight: 600, fontSize: '14px' }}>현재 대기 중인 신청자가 없습니다.</p>
          </div>
        )}
      </div>

      <StampBook stamps={stamps} loading={loading} />

    </div>
  );
};

export default ProfileTab;
