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
        } catch (e) {
            return "";
        }
    };

    useEffect(() => {
        // DM 내역 불러오기
        authFetch(`/api/dm/history/${otherUser.email}`)
            .then(res => res.json())
            .then(data => setMessages(data))
            .catch(err => console.error("DM History fetch error:", err));

        // 읽음 처리 API 호출
        const markAsRead = () => {
            authFetch(`/api/dm/read/${otherUser.email}`, {
                method: 'PUT'
            }).catch(err => console.error("Mark as read error:", err));
        };

        markAsRead();

        // WebSocket 연결
        const socket = new SockJS(apiUrl('/ws-stomp'));
        const client = new Client({
            webSocketFactory: () => socket,
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
        setStompClient(client);

        return () => {
            if (client) client.deactivate();
        };
    }, [otherUser.email, currentUser.email]);

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

    return (
        <div className="animate-fade" style={{ 
            position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, 
            background: 'var(--bg-dark)', zIndex: 300, display: 'flex', flexDirection: 'column' 
        }}>
            {/* Header */}
            <header className="glass" style={{ 
                padding: '16px 20px', display: 'flex', alignItems: 'center', gap: '16px',
                borderBottom: '1px solid rgba(255,255,255,0.1)',
                borderRadius: '0 0 24px 24px'
            }}>
                <button onClick={onBack} className="icon-circle glass" style={{ width: '40px', height: '40px' }}>
                    <ArrowLeft size={20} color="white" />
                </button>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{ 
                        width: '40px', height: '40px', borderRadius: '14px', 
                        background: otherUser.profileImageUrl ? `url(${otherUser.profileImageUrl}) center/cover` : 'var(--primary-gradient)',
                        display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 800, color: 'white'
                    }}>
                        {!otherUser.profileImageUrl && otherUser.name[0]}
                    </div>
                    <div>
                        <h3 className="text-s" style={{ color: 'white', fontWeight: 900, marginBottom: '2px' }}>{otherUser.name}</h3>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: '#4ADE80' }}></span>
                            <span style={{ fontSize: '11px', color: 'rgba(255,255,255,0.4)', fontWeight: 600 }}>대화 가능</span>
                        </div>
                    </div>
                </div>
            </header>

            {/* Chat Area */}
            <div 
                ref={scrollRef}
                className="hide-scrollbar"
                style={{ flex: 1, overflowY: 'auto', padding: '24px 20px', display: 'flex', flexDirection: 'column', gap: '16px' }}
            >
                {messages.map((m, idx) => {
                    const isMe = m.senderEmail === currentUser.email;
                    return (
                        <div key={idx} style={{ 
                            display: 'flex', 
                            flexDirection: 'column', 
                            alignItems: isMe ? 'flex-end' : 'flex-start',
                            animation: 'bubbleUp 0.3s ease-out'
                        }}>
                            <div style={{ display: 'flex', alignItems: 'flex-end', gap: '8px', flexDirection: isMe ? 'row-reverse' : 'row' }}>
                                <div className={isMe ? 'glass-premium' : 'glass'} style={{ 
                                    padding: '12px 18px', 
                                    borderRadius: isMe ? '20px 20px 4px 20px' : '20px 20px 20px 4px',
                                    maxWidth: '100%',
                                    background: isMe ? 'var(--primary-gradient)' : 'rgba(255,255,255,0.05)',
                                    border: isMe ? 'none' : '1px solid rgba(255,255,255,0.05)',
                                    boxShadow: isMe ? '0 8px 20px rgba(255, 92, 0, 0.2)' : 'none'
                                }}>
                                    <p style={{ fontSize: '14px', color: 'white', margin: 0, lineHeight: '1.5' }}>{m.content}</p>
                                </div>
                                <div style={{ display: 'flex', flexDirection: 'column', alignItems: isMe ? 'flex-end' : 'flex-start', gap: '2px' }}>
                                    {isMe && m.isRead && (
                                        <span style={{ fontSize: '10px', color: 'var(--primary-orange)', fontWeight: 800 }}>읽음</span>
                                    )}
                                    <span style={{ fontSize: '10px', color: 'rgba(255,255,255,0.3)', fontWeight: 600 }}>
                                        {formatTime(m.sentAt)}
                                    </span>
                                </div>
                            </div>
                        </div>
                    );
                })}
            </div>

            {/* Input Area */}
            <footer style={{ padding: '20px', borderTop: '1px solid rgba(255,255,255,0.1)', background: 'rgba(13, 13, 25, 0.8)', backdropFilter: 'blur(20px)' }}>
                <form onSubmit={handleSend} style={{ display: 'flex', gap: '12px' }}>
                    <div style={{ flex: 1, position: 'relative' }}>
                        <input 
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            placeholder="메시지를 입력하세요..."
                            className="glass"
                            style={{ 
                                width: '100%', padding: '16px 20px', borderRadius: '16px', border: '1px solid rgba(255,255,255,0.1)',
                                background: 'rgba(255,255,255,0.02)', color: 'white', outline: 'none', fontSize: '15px' 
                            }}
                        />
                    </div>
                    <button 
                        type="submit" 
                        disabled={!input.trim()}
                        className="primary-btn" 
                        style={{ width: '56px', height: '52px', borderRadius: '16px', padding: 0 }}
                    >
                        <Send size={20} />
                    </button>
                </form>
            </footer>

            <style jsx>{`
                @keyframes bubbleUp {
                    from { transform: scale(0.95); opacity: 0; }
                    to { transform: scale(1); opacity: 1; }
                }
            `}</style>
        </div>
    );
};

export default DMChatRoom;
