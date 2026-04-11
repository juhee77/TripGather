import React, { useState, useEffect } from 'react';
import { useUser } from '../contexts/UserContext';
import { useAuth } from '../contexts/AuthContext';
import { Pencil, X, MapPin, CheckCircle, Clock, LogOut } from 'lucide-react';
import { authFetch } from '../api/client';
import RouteDetailModal from '../components/RouteDetailModal';
import { MissionStatus } from '../constants/enums';

const MyPage = () => {
  const { logout } = useAuth();
  const { user, loading, error, refetch, updateProfile } = useUser();
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [editName, setEditName] = useState('');
  const [editBio, setEditBio] = useState('');
  const [editProfileImageUrl, setEditProfileImageUrl] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const [saving, setSaving] = useState(false);
  const fileInputRef = React.useRef(null);
  
  const [myMissions, setMyMissions] = useState([]);
  const [missionsLoading, setMissionsLoading] = useState(false);
  const [selectedMission, setSelectedMission] = useState(null);

  useEffect(() => {
    if (user) {
      setMissionsLoading(true);
      authFetch('/api/missions/me')
        .then(res => res.json())
        .then(data => setMyMissions(data))
        .catch(err => console.error("Failed to load missions:", err))
        .finally(() => setMissionsLoading(false));
    }
  }, [user]);

  const openEdit = () => {
    if (user) {
      setEditName(user.name ?? '');
      setEditBio(user.bio ?? '');
      setEditProfileImageUrl(user.profileImageUrl ?? '');
      setPreviewUrl(user.profileImageUrl ?? '');
      setSelectedFile(null);
      setIsEditOpen(true);
    }
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setSelectedFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewUrl(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSaveProfile = async (e) => {
    e.preventDefault();
    if (!user?.id) return;
    setSaving(true);
    
    let finalImageUrl = editProfileImageUrl;

    if (selectedFile) {
      const formData = new FormData();
      formData.append('file', selectedFile);
      try {
        const uploadRes = await authFetch('/api/files/upload', {
          method: 'POST',
          body: formData,
        });
        if (uploadRes.ok) {
          const { url } = await uploadRes.json();
          finalImageUrl = url;
        }
      } catch (err) {
        console.error("Profile image upload failed", err);
      }
    }

    try {
      await updateProfile(user.id, { 
        name: editName.trim(), 
        bio: editBio.trim(),
        profileImageUrl: finalImageUrl
      });
      setIsEditOpen(false);
    } catch (err) {
      alert(`프로필 수정 실패: ${err.message}`);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="page">
        <header className="page-header">
          <h1 className="page-title">마이페이지 👤</h1>
          <p className="page-subtitle">나의 프로필 및 설정</p>
        </header>
        <div style={{ padding: '20px', textAlign: 'center', color: 'var(--text-sub)' }}>
          로딩 중...
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="page">
        <header className="page-header">
          <h1 className="page-title">마이페이지 👤</h1>
          <p className="page-subtitle">나의 프로필 및 설정</p>
        </header>
        <div style={{ padding: '20px', textAlign: 'center', color: 'var(--text-sub)' }}>
          <p>프로필을 불러올 수 없습니다.</p>
          <button type="button" className="primary-btn" style={{ marginTop: '12px' }} onClick={refetch}>
            다시 시도
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="app-container animate-fade" style={{ background: 'var(--bg-lite)', minHeight: '100vh', paddingBottom: '100px' }}>
      <header className="page-header" style={{ padding: '30px 20px 20px', textAlign: 'center' }}>
        <h1 className="page-title" style={{ fontSize: '24px', letterSpacing: '-0.5px' }}>디지털 패스포트 📖</h1>
        <p className="page-subtitle">나의 여행 기록과 스탬프</p>
      </header>

      <div style={{ padding: '0 20px' }}>
        {/* Passport Spread View */}
        <div className="glass" style={{ 
          borderRadius: '24px', 
          overflow: 'hidden', 
          border: '1px solid rgba(255,255,255,0.4)',
          boxShadow: 'var(--shadow-premium)',
          background: 'linear-gradient(135deg, rgba(255,255,255,0.9), rgba(255,255,255,0.7))'
        }}>
          {/* ID PAGE (Upper Section) */}
          <div style={{ 
            padding: '24px', 
            borderBottom: '2px dashed var(--border-color)',
            position: 'relative',
            background: 'rgba(255, 92, 0, 0.03)'
          }}>
            <div style={{ position: 'absolute', top: '15px', right: '20px', opacity: 0.2 }}>
              <MapPin size={80} color="var(--primary-orange)" strokeWidth={1} />
            </div>
            
            <div style={{ display: 'flex', gap: '20px', alignItems: 'flex-start' }}>
              {/* Profile Photo Area (Passport Style) */}
              <div 
                style={{
                  width: '100px',
                  height: '120px',
                  borderRadius: '8px',
                  backgroundColor: '#f0f0f0',
                  backgroundImage: user?.profileImageUrl ? `url(${user.profileImageUrl})` : undefined,
                  backgroundSize: 'cover',
                  backgroundPosition: 'center',
                  border: '4px solid white',
                  boxShadow: 'var(--shadow-sm)',
                  flexShrink: 0
                }}
              />
              
              <div style={{ flex: 1 }}>
                <div style={{ marginBottom: '12px' }}>
                  <label style={{ fontSize: '10px', fontWeight: 800, color: 'var(--text-sub)', textTransform: 'uppercase' }}>Surname / Given Names</label>
                  <h3 style={{ fontSize: '20px', fontWeight: 800, color: 'var(--text-primary)', marginTop: '-2px' }}>{user?.name ?? 'TRAVELER'}</h3>
                </div>
                
                <div style={{ marginBottom: '12px' }}>
                  <label style={{ fontSize: '10px', fontWeight: 800, color: 'var(--text-sub)', textTransform: 'uppercase' }}>Nationality / Points</label>
                  <p style={{ fontSize: '14px', fontWeight: 700, color: 'var(--primary-orange)' }}>EARTH / {user?.points ?? 0} PTS</p>
                </div>

                <div>
                  <label style={{ fontSize: '10px', fontWeight: 800, color: 'var(--text-sub)', textTransform: 'uppercase' }}>Personal Note</label>
                  <p style={{ fontSize: '13px', color: 'var(--text-secondary)', lineHeight: 1.4 }}>
                    {user?.bio || '환영합니다! 당신의 여행을 기록해 보세요.'}
                  </p>
                </div>
              </div>
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '15px' }}>
              <button 
                onClick={openEdit}
                style={{ padding: '8px 16px', background: 'white', borderRadius: 'var(--radius-full)', border: '1px solid var(--border-color)', fontSize: '12px', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '6px', cursor: 'pointer' }}
              >
                <Pencil size={14} /> EDIT ID
              </button>
              <button 
                onClick={logout}
                style={{ padding: '8px 16px', background: 'rgba(0,0,0,0.05)', borderRadius: 'var(--radius-full)', border: 'none', fontSize: '12px', fontWeight: 800, display: 'flex', alignItems: 'center', gap: '6px', cursor: 'pointer', color: 'var(--text-sub)' }}
              >
                <LogOut size={14} /> EXIT
              </button>
            </div>
          </div>

          {/* STAMP PAGE (Lower Section) */}
          <div style={{ padding: '30px 24px', background: 'white' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <h3 style={{ fontSize: '16px', fontWeight: 800, color: '#333', letterSpacing: '1px' }}>VISAS / STAMPS 🔖</h3>
              <span style={{ fontSize: '11px', color: 'var(--text-sub)', fontWeight: 600 }}>PAGE 01 OF 01</span>
            </div>

            {missionsLoading ? (
              <div style={{ textAlign: 'center', padding: '40px 0', color: 'var(--text-muted)' }}>기록을 불러오는 중...</div>
            ) : myMissions.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '60px 20px', border: '2px dashed #eee', borderRadius: '16px' }}>
                <MapPin size={32} color="#eee" style={{ marginBottom: '12px' }} />
                <p style={{ color: '#bbb', fontSize: '14px' }}>아직 찍힌 스탬프가 없습니다.<br/>여행을 떠나 미션을 완료해 보세요!</p>
              </div>
            ) : (
              <div style={{ 
                display: 'grid', 
                gridTemplateColumns: 'repeat(2, 1fr)', 
                gap: '20px', 
                position: 'relative' 
              }}>
                {myMissions.map((m, idx) => {
                  const isCompleted = m.status === MissionStatus.COMPLETED;
                  // 랜덤 회전각 계산 (도장 느낌)
                  const rotation = ((idx * 7) % 20) - 10; 
                  
                  return (
                    <div 
                      key={m.id} 
                      onClick={() => setSelectedMission(m)}
                      style={{ 
                        aspectRatio: '1/1',
                        border: isCompleted ? '3px double rgba(255, 92, 0, 0.4)' : '1px solid #eee',
                        borderRadius: '12px',
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        justifyContent: 'center',
                        gap: '8px',
                        cursor: 'pointer',
                        transform: isCompleted ? `rotate(${rotation}deg)` : 'none',
                        transition: 'transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275)',
                        background: isCompleted ? 'rgba(255, 92, 0, 0.02)' : 'white',
                        position: 'relative',
                        padding: '10px'
                      }}
                      className={isCompleted ? "animate-pop" : ""}
                    >
                      {isCompleted ? (
                        <div style={{ 
                          width: '100%', 
                          height: '100%', 
                          display: 'flex', 
                          flexDirection: 'column', 
                          alignItems: 'center', 
                          justifyContent: 'center',
                          opacity: 0.8
                        }}>
                          <div style={{ color: 'var(--primary-orange)', fontWeight: 900, fontSize: '12px', textAlign: 'center' }}>
                            {m.itineraryTitle.substring(0, 10)}
                          </div>
                          <CheckCircle size={36} color="var(--primary-orange)" strokeWidth={2.5} style={{ margin: '8px 0' }} />
                          <div style={{ fontSize: '10px', color: 'var(--text-sub)', fontWeight: 800 }}>
                            {new Date(m.completedAt || m.startedAt).toLocaleDateString()}
                          </div>
                        </div>
                      ) : (
                        <>
                          <Clock size={24} color="#eee" />
                          <div style={{ fontSize: '10px', color: '#bbb', textAlign: 'center', fontWeight: 600 }}>
                            {m.itineraryTitle.substring(0, 8)}...
                          </div>
                        </>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>

        {/* Selected Mission Details Modal */}
        {selectedMission && (
          <RouteDetailModal
            itinerary={{
                id: selectedMission.itineraryId,
                title: selectedMission.itineraryTitle,
                author: selectedMission.itineraryAuthor,
                description: 'Mission Details & Log',
                createdAt: selectedMission.startedAt,
                steps: selectedMission.steps
            }}
            onClose={() => setSelectedMission(null)}
          />
        )}
      </div>

      {/* Edit Profile Modal */}
      {isEditOpen && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0,0,0,0.6)',
            backdropFilter: 'blur(4px)',
            zIndex: 1000,
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'flex-end',
          }}
        >
          <div
            style={{
              background: 'var(--surface)',
              width: '100%',
              maxWidth: '480px',
              borderTopLeftRadius: '20px',
              borderTopRightRadius: '20px',
              padding: '24px',
              boxShadow: '0 -10px 40px rgba(0,0,0,0.1)',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <h2 style={{ fontSize: '20px', fontWeight: 800 }}>프로필 수정</h2>
              <button
                type="button"
                onClick={() => setIsEditOpen(false)}
                style={{
                  padding: '8px',
                  background: 'var(--bg-color)',
                  borderRadius: '50%',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <X size={20} color="var(--text-main)" />
              </button>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '12px', marginBottom: '24px' }}>
              <div 
                onClick={() => fileInputRef.current.click()}
                style={{
                  width: '100px',
                  height: '100px',
                  borderRadius: '50%',
                  backgroundColor: 'var(--bg-color)',
                  backgroundImage: previewUrl ? `url(${previewUrl})` : undefined,
                  backgroundSize: 'cover',
                  backgroundPosition: 'center',
                  cursor: 'pointer',
                  position: 'relative',
                  border: '2px solid var(--primary)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  overflow: 'hidden'
                }}
              >
                {!previewUrl && <Pencil size={24} color="var(--text-sub)" />}
                <div style={{
                  position: 'absolute',
                  bottom: 0,
                  width: '100%',
                  background: 'rgba(0,0,0,0.5)',
                  color: 'white',
                  fontSize: '11px',
                  textAlign: 'center',
                  padding: '4px 0',
                  fontWeight: 600
                }}>
                  변경
                </div>
              </div>
              <input 
                type="file" 
                accept="image/*" 
                ref={fileInputRef} 
                style={{ display: 'none' }} 
                onChange={handleImageChange} 
              />
            </div>

            <form onSubmit={handleSaveProfile} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '14px', fontWeight: 600, marginBottom: '6px', color: 'var(--text-main)' }}>
                  이름
                </label>
                <input
                  type="text"
                  value={editName}
                  onChange={(e) => setEditName(e.target.value)}
                  placeholder="이름"
                  style={{
                    width: '100%',
                    padding: '12px 16px',
                    borderRadius: '12px',
                    border: '1px solid var(--border)',
                    background: 'var(--bg-color)',
                    fontSize: '16px',
                  }}
                />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '14px', fontWeight: 600, marginBottom: '6px', color: 'var(--text-main)' }}>
                  소개
                </label>
                <textarea
                  value={editBio}
                  onChange={(e) => setEditBio(e.target.value)}
                  placeholder="한 줄 소개"
                  rows={3}
                  style={{
                    width: '100%',
                    padding: '12px 16px',
                    borderRadius: '12px',
                    border: '1px solid var(--border)',
                    background: 'var(--bg-color)',
                    fontSize: '16px',
                    resize: 'vertical',
                  }}
                />
              </div>
              <button
                type="submit"
                disabled={saving}
                className="primary-btn"
                style={{ width: '100%', height: '48px', fontSize: '16px', fontWeight: 700 }}
              >
                {saving ? '저장 중...' : '저장'}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default MyPage;
