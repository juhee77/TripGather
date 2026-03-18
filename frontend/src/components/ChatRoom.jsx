import React, { useState, useEffect, useRef } from 'react';
import { ArrowLeft, Send, Users, Shield, MoreHorizontal } from 'lucide-react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import { useUser } from '../contexts/UserContext';

const ChatRoom = ({ gathering, onBack }) => {
    const { user: currentUser } = useUser();
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const [stompClient, setStompClient] = useState(null);
    const [isDrawerOpen, setIsDrawerOpen] = useState(false);
    const scrollRef = useRef(null);

    const isHost = (email) => {
        if (!gathering.host) return false;
        return typeof gathering.host === 'string' ? gathering.host === email : gathering.host.email === email;
    };

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
        // 채팅 내역 불러오기
        fetch(`http://localhost:8080/api/chat/${gathering.id}/history`)
            .then(res => res.json())
            .then(data => setMessages(data))
            .catch(err => console.error("History fetch error:", err));

        // WebSocket 연결
        const socket = new SockJS('http://localhost:8080/ws-stomp');
        const client = new Client({
            webSocketFactory: () => socket,
            debug: (str) => console.log(str),
            onConnect: () => {
                console.log('Connected to WebSocket');
                client.subscribe(`/topic/chat/${gathering.id}`, (message) => {
                    const newMessage = JSON.parse(message.body);
                    setMessages(prev => [...prev, newMessage]);
                });
            },
            onStompError: (frame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
            },
        });

        client.activate();
        setStompClient(client);

        return () => {
            if (client) client.deactivate();
        };
    }, [gathering.id]);

    useEffect(() => {
        if (scrollRef.current) {
            scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
        }
    }, [messages]);

    const handleSend = (e) => {
        e.preventDefault();
        if (!input.trim() || !stompClient) return;

        const chatMessage = {
            content: input,
            senderEmail: currentUser.email
        };

        stompClient.publish({
            destination: `/app/chat/${gathering.id}/send`,
            body: JSON.stringify(chatMessage)
        });

        setInput('');
    };

    return (
        <div className="animate-fade" style={{ 
            position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, 
            background: 'var(--bg-dark)', zIndex: 200, display: 'flex', flexDirection: 'column',
            overflow: 'hidden'
        }}>
            {/* Room Header */}
            <header className="glass" style={{ 
                padding: '16px 20px', display: 'flex', alignItems: 'center', gap: '16px',
                borderBottom: '1px solid rgba(255,255,255,0.1)',
                borderRadius: '0 0 24px 24px',
                zIndex: 210
            }}>
                <button onClick={onBack} className="icon-circle glass" style={{ width: '40px', height: '40px' }}>
                    <ArrowLeft size={20} color="white" />
                </button>
                <div style={{ flex: 1 }}>
                    <h3 className="text-s" style={{ color: 'white', fontWeight: 900, marginBottom: '2px' }}>{gathering.title}</h3>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: '#4ADE80' }}></span>
                        <span style={{ fontSize: '11px', color: 'rgba(255,255,255,0.4)', fontWeight: 600 }}>
                            {(gathering.members?.filter(m => m.status === 'APPROVED').length || 0) + 1} agents active
                        </span>
                    </div>
                </div>
                <div style={{ display: 'flex', gap: '8px' }}>
                    <button 
                        onClick={() => setIsDrawerOpen(!isDrawerOpen)} 
                        className="icon-circle glass" 
                        style={{ width: '40px', height: '40px', background: isDrawerOpen ? 'var(--primary-orange)' : '' }}
                    >
                        <Users size={20} color="white" />
                    </button>
                    <button className="icon-circle glass" style={{ width: '40px', height: '40px' }}>
                        <MoreHorizontal size={20} color="white" />
                    </button>
                </div>
            </header>

            <div style={{ flex: 1, display: 'flex', position: 'relative', overflow: 'hidden' }}>
                {/* Chat Area */}
                <div 
                    ref={scrollRef}
                    className="hide-scrollbar"
                    style={{ flex: 1, overflowY: 'auto', padding: '24px 20px', display: 'flex', flexDirection: 'column', gap: '16px' }}
                >
                    {messages.map((m, idx) => {
                        const isMe = m.senderEmail === currentUser.email;
                        const showName = !isMe && (idx === 0 || messages[idx-1].senderEmail !== m.senderEmail);
                        
                        return (
                            <div key={idx} style={{ 
                                display: 'flex', 
                                flexDirection: 'column', 
                                alignItems: isMe ? 'flex-end' : 'flex-start',
                                animation: 'bubbleUp 0.3s ease-out',
                                marginBottom: (idx < messages.length - 1 && messages[idx+1].senderEmail === m.senderEmail) ? '-8px' : '4px'
                            }}>
                                {showName && (
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '4px', marginBottom: '4px', marginLeft: '12px' }}>
                                        <span style={{ fontSize: '11px', color: 'rgba(255,255,255,0.4)', fontWeight: 700 }}>
                                            {m.senderName}
                                        </span>
                                        {isHost(m.senderEmail) && <Shield size={10} color="var(--primary-orange)" />}
                                    </div>
                                )}
                                <div style={{ display: 'flex', alignItems: 'flex-end', gap: '8px', flexDirection: isMe ? 'row-reverse' : 'row' }}>
                                    <div className={isMe ? 'glass-premium' : 'glass'} style={{ 
                                        padding: '12px 18px', 
                                        borderRadius: isMe ? '20px 20px 4px 20px' : '20px 20px 20px 4px',
                                        maxWidth: '100%',
                                        background: isMe ? 'var(--primary-gradient)' : 'rgba(255,255,255,0.05)',
                                        border: isMe ? 'none' : '1px solid rgba(255,255,255,0.05)',
                                        boxShadow: isMe ? '0 8px 20px rgba(255, 92, 0, 0.2)' : 'none'
                                    }}>
                                        <p style={{ fontSize: '14px', color: 'white', margin: 0, lineHeight: '1.5', fontWeight: 500 }}>{m.content}</p>
                                    </div>
                                    <span style={{ fontSize: '10px', color: 'rgba(255,255,255,0.3)', fontWeight: 600, marginBottom: '4px' }}>
                                        {formatTime(m.sentAt)}
                                    </span>
                                </div>
                            </div>
                        );
                    })}
                </div>

                {/* Participant Drawer */}
                <aside style={{
                    width: isDrawerOpen ? '260px' : '0',
                    background: 'rgba(20, 20, 35, 0.95)',
                    backdropFilter: 'blur(30px)',
                    borderLeft: '1px solid rgba(255,255,255,0.05)',
                    transition: 'width 0.3s cubic-bezier(0.16, 1, 0.3, 1)',
                    display: 'flex',
                    flexDirection: 'column',
                    overflow: 'hidden',
                    zIndex: 205
                }}>
                    <div style={{ padding: '24px 20px', minWidth: '260px' }}>
                        <h4 style={{ color: 'white', fontSize: '15px', fontWeight: 800, marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                            참여자 <span style={{ color: 'var(--primary-orange)' }}>{gathering.members?.filter(m => m.status === 'APPROVED').length + 1}</span>
                        </h4>
                        
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                            {/* Host First */}
                            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                                <div style={{ width: '40px', height: '40px', borderRadius: '14px', background: 'var(--primary-gradient)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 800, color: 'white', fontSize: '14px' }}>
                                    {typeof gathering.host === 'string' ? gathering.host[0] : gathering.host?.name?.[0]}
                                </div>
                                <div style={{ flex: 1 }}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                                        <span style={{ color: 'white', fontSize: '14px', fontWeight: 700 }}>{typeof gathering.host === 'string' ? gathering.host : gathering.host?.name}</span>
                                        <span title="Host">👑</span>
                                    </div>
                                    <span style={{ color: 'rgba(255,255,255,0.3)', fontSize: '11px', fontWeight: 600 }}>Host</span>
                                </div>
                            </div>

                            {/* Members */}
                            {gathering.members?.filter(m => m.status === 'APPROVED').map(m => (
                                <div key={m.id} style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                                    <div style={{ 
                                        width: '40px', height: '40px', borderRadius: '14px', 
                                        background: m.user.profileImageUrl ? `url(${m.user.profileImageUrl}) center/cover` : 'rgba(255,255,255,0.05)',
                                        display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, color: 'white', fontSize: '14px',
                                        border: '1px solid rgba(255,255,255,0.1)'
                                    }}>
                                        {!m.user.profileImageUrl && m.user.name[0]}
                                    </div>
                                    <div style={{ flex: 1 }}>
                                        <span style={{ color: 'white', fontSize: '14px', fontWeight: 600 }}>{m.user.name}</span>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                                            <span style={{ width: '4px', height: '4px', borderRadius: '50%', background: '#4ADE80' }}></span>
                                            <span style={{ color: 'rgba(255,255,255,0.3)', fontSize: '11px', fontWeight: 600 }}>Active</span>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </aside>
            </div>

            {/* Input Area */}
            <footer style={{ padding: '20px', borderTop: '1px solid rgba(255,255,255,0.1)', background: 'rgba(13, 13, 25, 0.8)', backdropFilter: 'blur(20px)', zIndex: 210 }}>
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

export default ChatRoom;
