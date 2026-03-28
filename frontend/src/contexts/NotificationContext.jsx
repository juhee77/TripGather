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

export const useNotification = () => useContext(NotificationContext);
