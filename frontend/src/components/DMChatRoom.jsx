import React, { useState, useEffect, useRef } from 'react';
import { ArrowLeft, Send, Shield } from 'lucide-react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { useUser } from '../contexts/UserContext';
import { authFetch, apiUrl } from '../api/client';

const DMChatRoom = ({ otherUser, onBack }) => {
    const { user: currentUser } = useUser();
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const [stompClient, setStompClient] = useState(null);
    const scrollRef = useRef(null);

    const formatTime = (dateStr) => {
        if (!dateStr) return "";
        try {
            const date = new Date(dateStr);
            return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', hour12: true });
        } catch (_e) {
            return "";
        }
    };

    useEffect(() => {
        if (!otherUser?.email || !currentUser?.email) return;

        // DM 내역 불러오기
        authFetch(`/api/dm/history/${otherUser.email}`)
            .then(res => res.json())
            .then(data => setMessages(data))
            .catch(err => console.error("DM History fetch error:", err));

        // 읽음 처리 API 호출
        const markAsRead = () => {
            if (!otherUser?.email) return;
            authFetch(`/api/dm/read/${otherUser.email}`, {
                method: 'PUT'
            }).catch(err => console.error("Mark as read error:", err));
        };

        markAsRead();

        // WebSocket 연결
        const socket = new SockJS(apiUrl('/ws-stomp'));
        const token = localStorage.getItem('token');
        const client = new Client({
            webSocketFactory: () => socket,
            connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
            debug: (str) => console.log(str),
            onConnect: () => {
                console.log('Connected to DM WebSocket');
                // 나에게 오는 DM 구독
                client.subscribe(`/topic/dm/${currentUser.email}`, (message) => {
                    const newMessage = JSON.parse(message.body);
                    if (newMessage.senderEmail === otherUser.email || newMessage.senderEmail === currentUser.email) {
                        setMessages(prev => [...prev, newMessage]);
                        // 내가 받은 메시지라면 즉시 읽음 처리
                        if (newMessage.receiverEmail === currentUser.email) {
                            markAsRead();
                        }
                    }
                });

                // 상대방이 내 메시지를 읽었을 때 알림 구독
                client.subscribe(`/topic/dm/read/${currentUser.email}`, (message) => {
                    const notification = JSON.parse(message.body);
                    if (notification.readerEmail === otherUser.email) {
                        setMessages(prev => prev.map(m => 
                            m.senderEmail === currentUser.email ? { ...m, isRead: true } : m
                        ));
                    }
                });
            },
        });

        client.activate();
        // eslint-disable-next-line react-hooks/set-state-in-effect
        setStompClient(client);

        return () => {
            if (client) client.deactivate();
        };
    }, [otherUser?.email, currentUser?.email]);

    useEffect(() => {
        if (scrollRef.current) {
            scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
        }
    }, [messages]);

    const handleSend = (e) => {
        e.preventDefault();
        if (!input.trim() || !stompClient) return;

        const dmRequest = {
            senderEmail: currentUser.email,
            receiverEmail: otherUser.email,
            content: input
        };

        stompClient.publish({
            destination: `/app/dm/send`,
            body: JSON.stringify(dmRequest)
        });

        setInput('');
    };

    if (!currentUser || !otherUser) {
        return (
            <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'white', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <div className="animate-pulse" style={{ color: 'var(--text-muted)', fontWeight: 600 }}>메시지 동기화 중...</div>
            </div>
        );
    }

    return (
        <div className="animate-fade" style={{ 
            position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, 
            background: 'var(--bg-color)', zIndex: 300, display: 'flex', flexDirection: 'column',
            overflow: 'hidden'
        }}>
            {/* Premium DM Header */}
            <header className="glass-premium" style={{ 
                padding: '20px', display: 'flex', alignItems: 'center', gap: '16px',
                borderBottom: '1px solid var(--border-color)',
                borderRadius: '0 0 24px 24px',
                zIndex: 310, background: 'rgba(255, 255, 255, 0.9)',
                backdropFilter: 'blur(20px)'
            }}>
                <button onClick={onBack} className="icon-circle glass" style={{ width: '42px', height: '42px', border: '1px solid var(--border-color)' }}>
                    <ArrowLeft size={20} color="var(--text-primary)" />
                </button>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{ 
                        width: '42px', height: '42px', borderRadius: '14px', 
                        background: otherUser.profileImageUrl ? `url(${otherUser.profileImageUrl}) center/cover` : 'var(--primary-gradient)',
                        display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 800, color: 'white',
                        boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
                    }}>
                        {!otherUser.profileImageUrl && otherUser.name[0]}
                    </div>
                    <div>
                        <h3 style={{ color: 'var(--text-primary)', fontWeight: 900, fontSize: '16px', margin: 0 }}>{otherUser.name}</h3>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginTop: '2px' }}>
                            <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: '#4ADE80' }}></span>
                            <span style={{ fontSize: '11px', color: 'var(--text-secondary)', fontWeight: 700 }}>1:1 SECURE LINE</span>
                        </div>
                    </div>
                </div>
            </header>

            {/* Chat Area */}
            <div 
                ref={scrollRef}
                className="hide-scrollbar"
                style={{ flex: 1, overflowY: 'auto', padding: '24px 20px', display: 'flex', flexDirection: 'column', gap: '12px' }}
            >
                {messages.map((m, idx) => {
                    const isMe = m.senderEmail === currentUser.email;
                    return (
                        <div key={idx} style={{ 
                            display: 'flex', 
                            flexDirection: 'column', 
                            alignItems: isMe ? 'flex-end' : 'flex-start',
                            animation: 'bubbleUp 0.3s ease-out',
                            marginBottom: '4px'
                        }}>
                            <div style={{ display: 'flex', alignItems: 'flex-end', gap: '8px', flexDirection: isMe ? 'row-reverse' : 'row' }}>
                                <div className={isMe ? 'glass-premium' : ''} style={{ 
                                    padding: '12px 18px', 
                                    borderRadius: isMe ? '20px 20px 4px 20px' : '4px 20px 20px 20px',
                                    maxWidth: '85%',
                                    background: isMe ? 'var(--primary-gradient)' : 'white',
                                    border: isMe ? 'none' : '1px solid var(--border-color)',
                                    boxShadow: isMe ? '0 8px 20px rgba(255, 92, 0, 0.15)' : '0 2px 8px rgba(0,0,0,0.02)',
                                    position: 'relative'
                                }}>
                                    <p style={{ fontSize: '14px', color: isMe ? 'white' : 'var(--text-primary)', margin: 0, lineHeight: '1.5', fontWeight: 500, wordBreak: 'break-word' }}>
                                        {m.content}
                                    </p>
                                </div>
                                <div style={{ display: 'flex', flexDirection: 'column', alignItems: isMe ? 'flex-end' : 'flex-start', gap: '2px' }}>
                                    {isMe && m.isRead && (
                                        <span style={{ fontSize: '10px', color: 'var(--primary-orange)', fontWeight: 800 }}>읽음</span>
                                    )}
                                    <span style={{ fontSize: '10px', color: 'var(--text-muted)', fontWeight: 600 }}>
                                        {formatTime(m.sentAt)}
                                    </span>
                                </div>
                            </div>
                        </div>
                    );
                })}
            </div>

            {/* Premium Input Area */}
            <footer style={{ padding: '16px 20px 32px', borderTop: '1px solid var(--border-color)', background: 'white', zIndex: 310 }}>
                <form onSubmit={handleSend} style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                    <div style={{ flex: 1, position: 'relative' }}>
                        <input 
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            placeholder="비밀 메시지 입력..."
                            style={{ 
                                width: '100%', padding: '16px 20px', borderRadius: '18px', border: '1px solid var(--border-color)',
                                background: 'var(--bg-color)', color: 'var(--text-primary)', outline: 'none', fontSize: '15px',
                                fontWeight: 600
                            }}
                        />
                    </div>
                    <button 
                        type="submit" 
                        disabled={!input.trim()}
                        className="primary-btn" 
                        style={{ width: '52px', height: '52px', borderRadius: '18px', padding: 0, boxShadow: '0 8px 16px rgba(255, 92, 0, 0.2)' }}
                    >
                        <Send size={20} />
                    </button>
                </form>
            </footer>

            <style jsx>{`
                @keyframes bubbleUp {
                    from { transform: translateY(10px); opacity: 0; }
                    to { transform: translateY(0); opacity: 1; }
                }
            `}</style>
        </div>
    );
};

export default DMChatRoom;
