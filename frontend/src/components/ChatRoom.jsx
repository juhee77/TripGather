import React from 'react';
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

    return (
        <div className="animate-fade" style={{ 
            position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, 
            background: 'var(--bg-color)', zIndex: 200, display: 'flex', flexDirection: 'column',
            overflow: 'hidden'
        }}>
            {/* Room Header */}
            <header className="glass" style={{ 
                padding: '16px 20px', display: 'flex', alignItems: 'center', gap: '16px',
                borderBottom: '1px solid var(--border-color)',
                borderRadius: '0 0 24px 24px',
                zIndex: 210, background: 'var(--surface)'
            }}>
                <button onClick={onBack} className="icon-circle glass" style={{ width: '40px', height: '40px' }}>
                    <ArrowLeft size={20} color="var(--text-primary)" />
                </button>
                <div style={{ flex: 1 }}>
                    <h3 className="text-s" style={{ color: 'var(--text-primary)', fontWeight: 900, marginBottom: '2px', fontSize: '15px' }}>{gathering.title}</h3>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: '#4ADE80' }}></span>
                        <span style={{ fontSize: '11px', color: 'var(--text-secondary)', fontWeight: 600 }}>
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
                        <Users size={20} color={isDrawerOpen ? "white" : "var(--text-primary)"} />
                    </button>
                    <button className="icon-circle glass" style={{ width: '40px', height: '40px' }}>
                        <MoreHorizontal size={20} color="var(--text-primary)" />
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
                                        <span style={{ fontSize: '11px', color: 'var(--text-secondary)', fontWeight: 700 }}>
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
                                        background: isMe ? 'var(--primary-gradient)' : 'var(--surface)',
                                        border: isMe ? 'none' : '1px solid var(--border-color)',
                                        boxShadow: isMe ? '0 8px 20px rgba(255, 92, 0, 0.2)' : '0 2px 8px rgba(0,0,0,0.03)'
                                    }}>
                                        <p style={{ fontSize: '14px', color: isMe ? 'white' : 'var(--text-primary)', margin: 0, lineHeight: '1.5', fontWeight: 500 }}>{m.content}</p>
                                    </div>
                                    <span style={{ fontSize: '10px', color: 'var(--text-muted)', fontWeight: 600, marginBottom: '4px' }}>
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
                    background: 'var(--surface)',
                    backdropFilter: 'blur(30px)',
                    borderLeft: '1px solid var(--border-color)',
                    transition: 'width 0.3s cubic-bezier(0.16, 1, 0.3, 1)',
                    display: 'flex',
                    flexDirection: 'column',
                    overflow: 'hidden',
                    zIndex: 205
                }}>
                    <div style={{ padding: '24px 20px', minWidth: '260px' }}>
                        <h4 style={{ color: 'var(--text-primary)', fontSize: '15px', fontWeight: 800, marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '8px' }}>
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
                                        <span style={{ color: 'var(--text-primary)', fontSize: '14px', fontWeight: 700 }}>{typeof gathering.host === 'string' ? gathering.host : gathering.host?.name}</span>
                                        <span title="Host">👑</span>
                                    </div>
                                    <span style={{ color: 'var(--text-muted)', fontSize: '11px', fontWeight: 600 }}>Host</span>
                                </div>
                                {currentUser.email !== (typeof gathering.host === 'string' ? gathering.host : gathering.host?.email) && (
                                    <button 
                                        onClick={() => onStartDM(typeof gathering.host === 'string' ? { email: gathering.host, name: gathering.host } : gathering.host)}
                                        className="glass" 
                                        style={{ padding: '6px 12px', borderRadius: '8px', fontSize: '11px', color: 'var(--primary-orange)', fontWeight: 800, border: '1px solid rgba(255, 92, 0, 0.2)' }}
                                    >
                                        DM
                                    </button>
                                )}
                            </div>

                            {/* Members */}
                            {gathering.members?.filter(m => m.status === 'APPROVED').map(m => (
                                <div key={m.id} style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                                    <div style={{ 
                                        width: '40px', height: '40px', borderRadius: '14px', 
                                        background: m.user.profileImageUrl ? `url(${m.user.profileImageUrl}) center/cover` : 'var(--bg-color)',
                                        display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, color: 'var(--text-secondary)', fontSize: '14px',
                                        border: '1px solid var(--border-color)'
                                    }}>
                                        {!m.user.profileImageUrl && m.user.name[0]}
                                    </div>
                                    <div style={{ flex: 1 }}>
                                        <span style={{ color: 'var(--text-primary)', fontSize: '14px', fontWeight: 600 }}>{m.user.name}</span>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                                            <span style={{ width: '4px', height: '4px', borderRadius: '50%', background: '#4ADE80' }}></span>
                                            <span style={{ color: 'var(--text-muted)', fontSize: '11px', fontWeight: 600 }}>Active</span>
                                        </div>
                                    </div>
                                    {currentUser.email !== m.user.email && (
                                        <button 
                                            onClick={() => onStartDM(m.user)}
                                            className="glass" 
                                            style={{ padding: '6px 12px', borderRadius: '8px', fontSize: '11px', color: 'var(--primary-orange)', fontWeight: 800, border: '1px solid rgba(255, 92, 0, 0.2)' }}
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

            {/* Input Area */}
            <footer style={{ padding: '20px', borderTop: '1px solid var(--border-color)', background: 'var(--surface)', backdropFilter: 'blur(20px)', zIndex: 210 }}>
                <form onSubmit={handleSend} style={{ display: 'flex', gap: '12px' }}>
                    <div style={{ flex: 1, position: 'relative' }}>
                        <input 
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            placeholder="메시지를 입력하세요..."
                            className="glass"
                            style={{ 
                                width: '100%', padding: '16px 20px', borderRadius: '16px', border: '1px solid var(--border-color)',
                                background: 'white', color: 'var(--text-primary)', outline: 'none', fontSize: '15px',
                                boxShadow: 'inset 0 2px 4px rgba(0,0,0,0.02)'
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
