import React, { useState, useEffect } from 'react';
import { useUser } from '../contexts/UserContext';
import { Pencil, X, MapPin, CheckCircle, Clock } from 'lucide-react';
import { authFetch } from '../api/client';

const MyPage = () => {
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

  useEffect(() => {
    if (user) {
      setMissionsLoading(true);
      authFetch('http://localhost:8080/api/missions/me')
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
        const uploadRes = await authFetch('http://localhost:8080/api/files/upload', {
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
    <div className="app-container animate-fade" style={{ background: 'var(--bg-color)', minHeight: '100vh', paddingBottom: '100px' }}>
      <header className="page-header glass" style={{ padding: '24px 20px', position: 'sticky', top: 0, zIndex: 10, borderRadius: '0 0 var(--radius-lg) var(--radius-lg)' }}>
        <h1 className="page-title">마이페이지 👤</h1>
        <p className="page-subtitle">나의 프로필 및 설정</p>
      </header>
      <div style={{ padding: '20px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
          <div
            style={{
              width: '60px',
              height: '60px',
              borderRadius: '30px',
              backgroundColor: user?.profileImageUrl ? 'transparent' : '#e0e0e0',
              backgroundImage: user?.profileImageUrl ? `url(${user.profileImageUrl})` : undefined,
              backgroundSize: 'cover',
              backgroundPosition: 'center',
            }}
          />
          <div style={{ flex: 1 }}>
            <h3 style={{ fontSize: '18px', fontWeight: 700 }}>{user?.name ?? '사용자'}</h3>
            <p style={{ color: 'var(--text-sub)', fontSize: '14px', marginTop: '4px' }}>
              {user?.bio || '소개를 입력해 주세요.'}
            </p>
            {typeof user?.points === 'number' && (
              <p style={{ color: 'var(--primary)', fontSize: '13px', fontWeight: 600, marginTop: '6px' }}>
                {user.points} pts
              </p>
            )}
          </div>
          <button
            type="button"
            onClick={openEdit}
            style={{
              padding: '12px',
              background: 'white',
              borderRadius: '50%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: '0 4px 12px rgba(0,0,0,0.05)',
              zIndex: 5,
              cursor: 'pointer'
            }}
            aria-label="프로필 수정"
          >
            <Pencil size={18} color="var(--primary-orange)" />
          </button>
        </div>
      </div>

      <div style={{ padding: '0 20px', marginTop: '20px' }}>
        <h3 style={{ fontSize: '18px', fontWeight: 800, color: 'var(--text-primary)', marginBottom: '16px' }}>나의 미션 히스토리 🗺️</h3>
        
        {missionsLoading ? (
            <div style={{ textAlign: 'center', padding: '40px 0', color: 'var(--text-muted)' }}>가져오는 중...</div>
        ) : myMissions.length === 0 ? (
            <div className="glass" style={{ textAlign: 'center', padding: '40px 20px', borderRadius: 'var(--radius-lg)', background: 'white' }}>
              <MapPin size={32} color="var(--text-muted)" style={{ margin: '0 auto 12px auto' }} />
              <p style={{ color: 'var(--text-secondary)', fontWeight: 600 }}>아직 참여한 미션이 없습니다.</p>
            </div>
        ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              {myMissions.map((m, idx) => (
                <div key={m.id} className="glass animate-fade" style={{ background: 'white', padding: '20px', borderRadius: 'var(--radius-lg)', animationDelay: `${idx * 0.1}s`, border: '1px solid var(--border-color)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                    <h4 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text-primary)' }}>{m.itineraryTitle}</h4>
                    <span style={{ 
                      fontSize: '11px', fontWeight: 800, padding: '4px 10px', borderRadius: 'var(--radius-full)',
                      background: m.status === 'COMPLETED' ? 'rgba(81, 207, 102, 0.1)' : 'rgba(255, 92, 0, 0.1)',
                      color: m.status === 'COMPLETED' ? '#2B8A3E' : 'var(--primary-orange)',
                      display: 'flex', alignItems: 'center', gap: '4px'
                    }}>
                      {m.status === 'COMPLETED' ? <CheckCircle size={12} /> : <Clock size={12} />}
                      {m.status === 'COMPLETED' ? 'COMPLETED' : 'ACTIVE'}
                    </span>
                  </div>
                  <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '12px' }}>By {m.itineraryAuthor}</p>
                  <p style={{ fontSize: '12px', color: 'var(--text-muted)', fontWeight: 600 }}>
                    시작일: {new Date(m.startedAt).toLocaleDateString()}
                    {m.completedAt && ` • 완료일: ${new Date(m.completedAt).toLocaleDateString()}`}
                  </p>
                </div>
              ))}
            </div>
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
