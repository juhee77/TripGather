import React from 'react';

const MyPage = () => {
  return (
    <div className="page">
      <header className="page-header">
        <h1 className="page-title">마이페이지 👤</h1>
        <p className="page-subtitle">나의 프로필 및 설정</p>
      </header>
      <div style={{ padding: '20px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
          <div style={{ width: '60px', height: '60px', borderRadius: '30px', backgroundColor: '#e0e0e0' }} />
          <div>
            <h3>지현 ✨</h3>
            <p style={{ color: 'var(--text-sub)' }}>여행을 좋아하는 사람</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default MyPage;
