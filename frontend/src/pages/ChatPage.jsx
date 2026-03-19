import React, { useEffect, useState } from 'react';
import ChatTab from '../components/ChatTab';
import { useNotification } from '../contexts/NotificationContext';
import { useUser } from '../contexts/UserContext';
import { authFetch } from '../api/client';

const ChatPage = () => {
  const { resetUnreadCount } = useNotification();
  const { user: currentUser } = useUser();
  const [gatherings, setGatherings] = useState([]);

  useEffect(() => {
    resetUnreadCount();
    authFetch('/api/gatherings')
      .then(res => res.json())
      .then(data => setGatherings(data))
      .catch(err => console.error(err));
  }, [resetUnreadCount]);

  const joinedGatherings = gatherings.filter(g => {
    const isHost = typeof g.host === 'string' ? g.host === currentUser?.name : g.host?.email === currentUser?.email;
    const isApproved = g.members?.some(m => m.user.email === currentUser?.email && m.status === 'APPROVED');
    return isHost || isApproved;
  });

  return (
    <div className="page" style={{ paddingBottom: '80px' }}>
      <header className="page-header" style={{ marginBottom: '0' }}>
        <h1 className="page-title">채팅 💬</h1>
        <p className="page-subtitle">참여 중인 모임 및 1:1 대화</p>
      </header>
      <ChatTab joinedGatherings={joinedGatherings} />
    </div>
  );
};

export default ChatPage;
