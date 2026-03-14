import React, { useState } from 'react';
import { useUser } from '../contexts/UserContext';
import { Pencil, X } from 'lucide-react';

const MyPage = () => {
  const { user, loading, error, refetch, updateProfile } = useUser();
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [editName, setEditName] = useState('');
  const [editBio, setEditBio] = useState('');
  const [saving, setSaving] = useState(false);

  const openEdit = () => {
    if (user) {
      setEditName(user.name ?? '');
      setEditBio(user.bio ?? '');
      setIsEditOpen(true);
    }
  };

  const handleSaveProfile = async (e) => {
    e.preventDefault();
    if (!user?.id) return;
    setSaving(true);
    try {
      await updateProfile(user.id, { name: editName.trim(), bio: editBio.trim() });
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
          <button type="button" className="btn-primary" style={{ marginTop: '12px' }} onClick={refetch}>
            다시 시도
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="page">
      <header className="page-header">
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
              padding: '10px',
              background: 'var(--bg-color)',
              borderRadius: '50%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
            aria-label="프로필 수정"
          >
            <Pencil size={18} color="var(--text-main)" />
          </button>
        </div>
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
                className="btn-primary"
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
