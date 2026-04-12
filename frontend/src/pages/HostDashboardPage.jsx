import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronLeft } from 'lucide-react';
import { useUser } from '../contexts/UserContext';
import { authFetch } from '../api/client';
import { MemberStatus } from '../constants/enums';

const HostDashboardPage = () => {
  const navigate = useNavigate();
  const { user } = useUser();
  
  const [hostedGatherings, setHostedGatherings] = useState([]);
  const [loadingHosted, setLoadingHosted] = useState(false);
  const [leaveRequests, setLeaveRequests] = useState([]);

  useEffect(() => {
    if (user) {
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

      authFetch('/api/missions/host/requests')
        .then(res => res.json())
        .then(data => setLeaveRequests(Array.isArray(data) ? data : []))
        .catch(err => console.error("Error fetching leave requests:", err));
    }
  }, [user]);

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

  const handleApproveLeave = async (missionId) => {
    if (!window.confirm("이 사용자의 미션 탈퇴를 승인하시겠습니까?")) return;
    try {
      const res = await authFetch(`/api/missions/${missionId}/leave/approve`, {
        method: 'POST'
      });
      if (res.ok) {
        setLeaveRequests(prev => prev.filter(req => req.id !== missionId));
        alert("탈퇴 처리가 완료되었습니다.");
      }
    } catch (err) {
      console.error("Error approving leave:", err);
    }
  };

  const pendingApplicants = hostedGatherings.flatMap(g => 
    (g.members || []).filter(m => m.status === MemberStatus.PENDING && (!user || m.user.id !== user.id)).map(m => ({ ...m, gatheringId: g.id, gatheringTitle: g.title }))
  );

  return (
    <div className="app-container animate-fade" style={{ background: 'var(--bg-lite)', minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <header className="page-header" style={{ display: 'flex', alignItems: 'center', padding: '20px', gap: '16px', background: 'white', position: 'sticky', top: 0, zIndex: 100, borderBottom: '1px solid var(--border-color)', borderRadius: '0 0 24px 24px' }}>
        <button onClick={() => navigate(-1)} className="icon-circle" style={{ width: '40px', height: '40px', padding: 0 }}>
          <ChevronLeft size={24} color="var(--text-primary)" />
        </button>
        <h1 className="heading-m" style={{ margin: 0, fontSize: '20px' }}>나의 터미널 ✈️</h1>
      </header>
      
      <div style={{ flex: 1, padding: '24px', overflowY: 'auto', paddingBottom: '40px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '20px' }}>
          <div>
            <span className="label-orange">MANAGEMENT TERMINAL</span>
            <h3 style={{ fontSize: '24px', fontWeight: 900, color: 'var(--text-primary)', marginTop: '4px' }}>
              주최 및 요청 관리
            </h3>
          </div>
          <div style={{ 
            background: 'var(--text-primary)', color: 'white', 
            padding: '6px 14px', borderRadius: '10px', fontSize: '13px', fontWeight: 800 
          }}>
            {pendingApplicants.length + leaveRequests.length} PENDING
          </div>
        </div>

        {pendingApplicants.length + leaveRequests.length > 0 ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {/* Pending Applicants List */}
            {pendingApplicants.map((app, idx) => (
              <div key={`${app.gatheringId}-${app.user.id}`} className="ticket-wrapper" style={{ 
                padding: '20px 24px', borderRadius: '20px', display: 'flex', alignItems: 'center', gap: '16px',
                animation: 'fadeIn 0.4s ease-out forwards', animationDelay: `${idx * 0.05}s`, opacity: 0
              }}>
                <div style={{ 
                  width: '48px', height: '48px', borderRadius: '12px', 
                  background: 'var(--bg-color)', display: 'flex', alignItems: 'center', justifyContent: 'center', 
                  fontWeight: 900, color: 'var(--text-primary)', fontSize: '16px', border: '1px solid var(--border-color)' 
                }}>
                  {app.user.name[0]}
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <span style={{ fontWeight: 800, fontSize: '16px', color: 'var(--text-primary)' }}>{app.user.name}</span>
                    <span className="status-pill" style={{ background: '#F0FDF4', color: '#10B981', fontSize: '10px' }}>APPLICANT</span>
                  </div>
                  <p style={{ fontSize: '13px', color: 'var(--text-muted)', fontWeight: 600, marginTop: '2px' }}>{app.gatheringTitle}</p>
                </div>
                <div style={{ display: 'flex', gap: '8px' }}>
                  <button 
                    onClick={() => handleAction(app.gatheringId, app.user.id, 'approve')}
                    className="primary-btn" 
                    style={{ padding: '10px 20px', borderRadius: '12px', fontSize: '13px', height: '40px' }}
                  >
                    승인
                  </button>
                  <button 
                    onClick={() => handleAction(app.gatheringId, app.user.id, 'reject')}
                    className="secondary-btn" 
                    style={{ padding: '10px 20px', borderRadius: '12px', fontSize: '13px', height: '40px' }}
                  >
                    거절
                  </button>
                </div>
              </div>
            ))}

            {/* Leave Requests List */}
            {leaveRequests.map((req, idx) => (
              <div key={`mission-${req.id}`} className="ticket-wrapper" style={{ 
                padding: '20px 24px', borderRadius: '20px', display: 'flex', alignItems: 'center', gap: '16px',
                animation: 'fadeIn 0.4s ease-out forwards', animationDelay: `${(pendingApplicants.length + idx) * 0.05}s`, opacity: 0
              }}>
                <div style={{ 
                  width: '48px', height: '48px', borderRadius: '12px', 
                  background: '#FEF2F2', display: 'flex', alignItems: 'center', justifyContent: 'center', 
                  fontWeight: 900, color: '#EF4444', fontSize: '16px', border: '1px solid #FEE2E2' 
                }}>
                  {(req.userName || 'U')[0]}
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <span style={{ fontWeight: 800, fontSize: '16px', color: 'var(--text-primary)' }}>{req.userName}</span>
                    <span className="status-pill" style={{ background: '#FEF2F2', color: '#EF4444', fontSize: '10px' }}>LEAVE REQUEST</span>
                  </div>
                  <p style={{ fontSize: '13px', color: 'var(--text-muted)', fontWeight: 600, marginTop: '4px' }}>{req.itineraryTitle}</p>
                </div>
                <button 
                  onClick={() => handleApproveLeave(req.id)}
                  className="secondary-btn" 
                  style={{ padding: '10px 20px', borderRadius: '12px', fontSize: '13px', height: '40px', color: '#EF4444', borderColor: '#FEE2E2' }}
                >
                  탈퇴 승인
                </button>
              </div>
            ))}
          </div>
        ) : (
          <div className="ticket-wrapper" style={{ 
            padding: '48px 24px', borderRadius: '24px', textAlign: 'center', 
            border: '2px dashed var(--border-color)', background: 'white' 
          }}>
            <p style={{ color: 'var(--text-muted)', fontWeight: 700, fontSize: '15px' }}>현재 대기 중인 요청이 없습니다.</p>
            <span style={{ fontSize: '12px', color: 'var(--text-muted)', opacity: 0.7 }}>터미널이 한적합니다.</span>
          </div>
        )}
      </div>
    </div>
  );
};

export default HostDashboardPage;
