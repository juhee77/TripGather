import React from 'react';
import { MemberStatus } from '../constants/enums';
import { ArrowLeft, Users, MoreHorizontal, Shield, Send } from 'lucide-react';
import { useUser } from '../contexts/UserContext';
import { useChatViewModel } from '../viewmodels/useChatViewModel';

const ChatRoom = ({ gathering, onBack, onStartDM }) => {
    const { user: currentUser } = useUser();
    const {
        messages,
        input,
        setInput,
        isDrawerOpen,
        setIsDrawerOpen,
        scrollRef,
        handleSend,
        formatTime,
        isHost
    } = useChatViewModel(gathering, currentUser);

    if (!currentUser || !gathering) {
        return (
            <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'white', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <div className="animate-pulse" style={{ color: 'var(--text-muted)', fontWeight: 600 }}>에이전트 연결 중...</div>
            </div>
        );
    }

    return (
        <div className="animate-fade" style={{ 
            position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, 
            background: 'var(--bg-color)', zIndex: 200, display: 'flex', flexDirection: 'column',
            overflow: 'hidden'
        }}>
            {/* Unified Room Header */}
            <header className="glass-premium" style={{ 
                padding: '20px', display: 'flex', alignItems: 'center', gap: '16px',
                borderBottom: '1px solid var(--border-color)',
                borderRadius: '0 0 24px 24px',
                zIndex: 210, background: 'rgba(255, 255, 255, 0.9)',
                backdropFilter: 'blur(20px)'
            }}>
                <button onClick={onBack} className="icon-circle glass" style={{ width: '42px', height: '42px', border: '1px solid var(--border-color)' }}>
                    <ArrowLeft size={20} color="var(--text-primary)" />
                </button>
                <div style={{ flex: 1 }}>
                    <h3 style={{ color: 'var(--text-primary)', fontWeight: 900, fontSize: '16px', margin: 0 }}>{gathering.title}</h3>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginTop: '2px' }}>
                        <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: 'var(--primary-orange)' }}></span>
                        <span style={{ fontSize: '11px', color: 'var(--text-secondary)', fontWeight: 700 }}>
                            AGENT GROUP 채팅
                        </span>
                    </div>
                </div>
                <div style={{ display: 'flex', gap: '8px' }}>
                    <button 
                        onClick={() => setIsDrawerOpen(!isDrawerOpen)} 
                        className="icon-circle glass" 
                        style={{ 
                            width: '42px', height: '42px', 
                            background: isDrawerOpen ? 'var(--primary-orange)' : 'white',
                            border: '1px solid var(--border-color)'
                        }}
                    >
                        <Users size={20} color={isDrawerOpen ? "white" : "var(--text-primary)"} />
                    </button>
                </div>
            </header>

            <div style={{ flex: 1, display: 'flex', position: 'relative', overflow: 'hidden', background: 'var(--bg-color)' }}>
                {/* Chat Area */}
                <div 
                    ref={scrollRef}
                    className="hide-scrollbar"
                    style={{ flex: 1, overflowY: 'auto', padding: '24px 20px', display: 'flex', flexDirection: 'column', gap: '12px' }}
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
                                marginBottom: (idx < messages.length - 1 && messages[idx+1].senderEmail === m.senderEmail) ? '-8px' : '8px'
                            }}>
                                {showName && (
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '6px', marginLeft: '4px' }}>
                                        <div style={{ width: '24px', height: '24px', borderRadius: '8px', background: 'var(--primary-gradient)', fontSize: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white', fontWeight: 800 }}>
                                            {(m.senderName || 'A')[0]}
                                        </div>
                                        <span style={{ fontSize: '12px', color: 'var(--text-secondary)', fontWeight: 800 }}>
                                            {m.senderName || '익명 요원'}
                                        </span>
                                        {isHost(m.senderEmail) && <Shield size={10} color="var(--primary-orange)" />}
                                    </div>
                                )}
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
                                    <span style={{ fontSize: '10px', color: 'var(--text-muted)', fontWeight: 600, marginBottom: '2px' }}>
                                        {formatTime(m.sentAt)}
                                    </span>
                                </div>
                            </div>
                        );
                    })}
                </div>

                {/* Participant Drawer */}
                <aside style={{
                    width: isDrawerOpen ? '280px' : '0',
                    background: 'rgba(255, 255, 255, 0.95)',
                    backdropFilter: 'blur(30px)',
                    borderLeft: '1px solid var(--border-color)',
                    transition: 'width 0.4s cubic-bezier(0.16, 1, 0.3, 1)',
                    display: 'flex',
                    flexDirection: 'column',
                    overflow: 'hidden',
                    zIndex: 205
                }}>
                    <div style={{ padding: '32px 24px', minWidth: '280px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                            <h4 style={{ color: 'var(--text-primary)', fontSize: '16px', fontWeight: 900, margin: 0, display: 'flex', alignItems: 'center', gap: '8px' }}>
                                요원 목록 <span style={{ background: 'var(--primary-orange)', color: 'white', fontSize: '11px', padding: '2px 8px', borderRadius: '10px' }}>{(gathering.members?.filter(m => m.status === MemberStatus.APPROVED).length || 0) + 1}</span>
                            </h4>
                        </div>
                        
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
                            {/* Host First */}
                            <div className="glass" style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '12px', borderRadius: '16px', border: '1px solid rgba(255, 92, 0, 0.1)' }}>
                                <div style={{ width: '40px', height: '40px', borderRadius: '14px', background: 'var(--primary-gradient)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 800, color: 'white', fontSize: '14px' }}>
                                    {(typeof gathering.host === 'string' ? gathering.host : (gathering.host?.name || 'H'))[0]}
                                </div>
                                <div style={{ flex: 1 }}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                                        <span style={{ color: 'var(--text-primary)', fontSize: '14px', fontWeight: 800 }}>{typeof gathering.host === 'string' ? gathering.host : gathering.host?.name}</span>
                                        <span title="Host">👑</span>
                                    </div>
                                    <span style={{ color: 'var(--primary-orange)', fontSize: '10px', fontWeight: 800 }}>MASTER AGENT</span>
                                </div>
                            </div>

                            <div style={{ height: '1px', background: 'var(--border-color)', margin: '4px 0' }}></div>

                            {/* Members */}
                            {gathering.members?.filter(m => m.status === MemberStatus.APPROVED).map(m => (
                                <div key={m.id} style={{ display: 'flex', alignItems: 'center', gap: '12px', padding: '4px 8px' }}>
                                    <div style={{ 
                                        width: '40px', height: '40px', borderRadius: '14px', 
                                        background: m.user.profileImageUrl ? `url(${m.user.profileImageUrl}) center/cover` : 'var(--bg-color)',
                                        display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, color: 'var(--text-secondary)', fontSize: '14px',
                                        border: '1px solid var(--border-color)'
                                    }}>
                                        {!m.user.profileImageUrl && m.user.name[0]}
                                    </div>
                                    <div style={{ flex: 1 }}>
                                        <span style={{ color: 'var(--text-primary)', fontSize: '14px', fontWeight: 700 }}>{m.user.name}</span>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                                            <span style={{ width: '5px', height: '5px', borderRadius: '50%', background: '#4ADE80' }}></span>
                                            <span style={{ color: 'var(--text-muted)', fontSize: '11px', fontWeight: 600 }}>ONLINE</span>
                                        </div>
                                    </div>
                                    {currentUser.email !== m.user.email && (
                                        <button 
                                            onClick={() => onStartDM(m.user)}
                                            className="glass" 
                                            style={{ padding: '6px 12px', borderRadius: '10px', fontSize: '11px', color: 'var(--primary-orange)', fontWeight: 800, border: '1px solid rgba(255, 92, 0, 0.1)', background: 'white' }}
                                        >
                                            DM
                                        </button>
                                    )}
                                </div>
                            ))}
                        </div>
                    </div>
                </aside>
            </div>

            {/* Premium Input Area */}
            <footer style={{ padding: '16px 20px 32px', borderTop: '1px solid var(--border-color)', background: 'white', zIndex: 210 }}>
                <form onSubmit={handleSend} style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                    <div style={{ flex: 1, position: 'relative' }}>
                        <input 
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            placeholder="명령을 입력하세요 (메시지)..."
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

            <style>{`
                @keyframes bubbleUp {
                    from { transform: translateY(10px); opacity: 0; }
                    to { transform: translateY(0); opacity: 1; }
                }
            `}</style>
        </div>
    );
};

export default ChatRoom;
