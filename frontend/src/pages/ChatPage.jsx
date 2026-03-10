import React from 'react';

const ChatPage = () => {
  return (
    <div className="page">
      <header className="page-header">
        <h1 className="page-title">채팅 💬</h1>
        <p className="page-subtitle">참여 중인 모임 대화</p>
      </header>
      <div style={{ padding: '20px', textAlign: 'center', color: 'var(--text-sub)' }}>
        <p>아직 참여 중인 대화가 없습니다.</p>
      </div>
    </div>
  );
};

export default ChatPage;
