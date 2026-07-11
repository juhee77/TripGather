import React, { createContext, useContext, useState, useEffect } from 'react';
import { useUser } from './UserContext';
import { apiUrl } from '../api/client';

const NotificationContext = createContext();

export const NotificationProvider = ({ children }) => {
    const { currentUser } = useUser();
    const [unreadCount, setUnreadCount] = useState(0);
    const [notifications, setNotifications] = useState([]);

    useEffect(() => {
        if (!currentUser) return;

        console.log("Subscribing to notifications...");
        const eventSource = new EventSource(apiUrl(`/api/notifications/subscribe?token=${localStorage.getItem('token')}`));

        eventSource.addEventListener('chat-received', (event) => {
            const data = JSON.parse(event.data);
            console.log("Chat notification received:", data);
            setUnreadCount(prev => prev + 1);
            setNotifications(prev => [data, ...prev]);
        });

        eventSource.addEventListener('dm-received', (event) => {
            const data = JSON.parse(event.data);
            console.log("DM notification received:", data);
            setUnreadCount(prev => prev + 1);
            setNotifications(prev => [data, ...prev]);
        });

        eventSource.addEventListener('gathering-requested', (event) => {
            const data = JSON.parse(event.data);
            console.log("Gathering request received:", data);
            setUnreadCount(prev => prev + 1);
            setNotifications(prev => [{
                id: Date.now(),
                type: 'gathering-requested',
                message: `[참가 신청] '${data.gatheringTitle}' 모임에 ${data.applicantName}님이 참가 신청을 했습니다.`,
                ...data
            }, ...prev]);
        });

        eventSource.addEventListener('gathering-approved', (event) => {
            const data = JSON.parse(event.data);
            console.log("Gathering approval received:", data);
            setUnreadCount(prev => prev + 1);
            setNotifications(prev => [{
                id: Date.now(),
                type: 'gathering-approved',
                message: `[승인 완료] '${data.gatheringTitle}' 모임 참여가 승인되었습니다! 🎉`,
                ...data
            }, ...prev]);
        });

        eventSource.addEventListener('gathering-rejected', (event) => {
            const data = JSON.parse(event.data);
            console.log("Gathering rejection received:", data);
            setUnreadCount(prev => prev + 1);
            setNotifications(prev => [{
                id: Date.now(),
                type: 'gathering-rejected',
                message: `[참가 거절] '${data.gatheringTitle}' 모임 참여가 거절되었습니다.`,
                ...data
            }, ...prev]);
        });

        eventSource.onerror = (err) => {
            console.error("SSE Error:", err);
            eventSource.close();
        };

        return () => {
            eventSource.close();
        };
    }, [currentUser]);

    const resetUnreadCount = () => setUnreadCount(0);

    return (
        <NotificationContext.Provider value={{ unreadCount, notifications, resetUnreadCount }}>
            {children}
        </NotificationContext.Provider>
    );
};

// eslint-disable-next-line react-refresh/only-export-components
export const useNotification = () => useContext(NotificationContext);
