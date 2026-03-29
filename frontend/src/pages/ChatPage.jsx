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
    
    // Fetch both hosted and joined gatherings
    Promise.all([
      authFetch('/api/gatherings/my/hosted').then(res => res.json()),
      authFetch('/api/gatherings/my/joined').then(res => res.json())
    ]).then(([hosted, joined]) => {
      // Remove duplicates just in case (though status should keep them separate)
      const combined = [...hosted];
      joined.forEach(j => {
        if (!combined.find(c => c.id === j.id)) {
          combined.push(j);
        }
      });
      setGatherings(combined);
    }).catch(err => console.error("Error fetching my gatherings:", err));
  }, [resetUnreadCount]);

  const joinedGatherings = gatherings; // Already filtered by backend

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
