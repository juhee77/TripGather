import React, { useEffect } from 'react';
import ChatTab from '../components/ChatTab';
import { useNotification } from '../contexts/NotificationContext';

const ChatPage = () => {
  const { resetUnreadCount } = useNotification();

  useEffect(() => {
    resetUnreadCount();
  }, [resetUnreadCount]);

  return (
    <div className="page" style={{ paddingBottom: '80px' }}>
      <header className="page-header" style={{ marginBottom: '0' }}>
        <h1 className="page-title">채팅 💬</h1>
        <p className="page-subtitle">참여 중인 모임 및 1:1 대화</p>
      </header>
      <ChatTab />
    </div>
  );
};

export default ChatPage;
