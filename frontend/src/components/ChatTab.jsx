import React, { useState } from 'react';
import { MessageSquare, Users, ChevronRight, Search } from 'lucide-react';
import ChatRoom from './ChatRoom';
import DMChatRoom from './DMChatRoom';

const ChatTab = ({ joinedGatherings }) => {
    const [selectedRoom, setSelectedRoom] = useState(null);
    const [selectedDMUser, setSelectedDMUser] = useState(null);
    const [chatType, setChatType] = useState('group'); // 'group' or 'dm'

    const handleStartDM = (user) => {
        setSelectedRoom(null);
        setSelectedDMUser(user);
    };

    if (selectedDMUser) {
        return <DMChatRoom otherUser={selectedDMUser} onBack={() => setSelectedDMUser(null)} />;
    }

    if (selectedRoom) {
        return <ChatRoom gathering={selectedRoom} onBack={() => setSelectedRoom(null)} onStartDM={handleStartDM} />;
    }

    return (
        <div className="animate-fade" style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {/* Header / Search Area */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                <div style={{ display: 'flex', gap: '8px' }}>
                    <button 
                        onClick={() => setChatType('group')}
                        className="glass"
                        style={{ 
                            flex: 1, padding: '12px', borderRadius: '16px', fontSize: '14px', fontWeight: 800,
                            color: chatType === 'group' ? 'var(--primary-orange)' : 'rgba(255,255,255,0.4)',
                            background: chatType === 'group' ? 'rgba(255, 92, 0, 0.1)' : 'rgba(255,255,255,0.02)',
                            border: chatType === 'group' ? '1px solid rgba(255, 92, 0, 0.2)' : '1px solid rgba(255,255,255,0.05)',
                        }}
                    >
                        모임 채팅
                    </button>
                    <button 
                        onClick={() => setChatType('dm')}
                        className="glass"
                        style={{ 
                            flex: 1, padding: '12px', borderRadius: '16px', fontSize: '14px', fontWeight: 800,
                            color: chatType === 'dm' ? 'var(--primary-orange)' : 'rgba(255,255,255,0.4)',
                            background: chatType === 'dm' ? 'rgba(255, 92, 0, 0.1)' : 'rgba(255,255,255,0.02)',
                            border: chatType === 'dm' ? '1px solid rgba(255, 92, 0, 0.2)' : '1px solid rgba(255,255,255,0.05)',
                        }}
                    >
                        1:1 DM
                    </button>
                </div>

                <div className="glass" style={{ 
                    padding: '16px 20px', 
                    borderRadius: '20px', 
                    display: 'flex', 
                    alignItems: 'center', 
                    gap: '12px',
                    border: '1px solid rgba(255,255,255,0.05)'
                }}>
                    <Search size={18} color="rgba(255,255,255,0.3)" />
                    <input 
                        placeholder={chatType === 'group' ? "모임 채팅방 검색..." : "대화 상대 검색..."}
                        style={{ 
                            background: 'none', border: 'none', outline: 'none', color: 'white', 
                            fontSize: '14px', fontWeight: 500, width: '100%' 
                        }} 
                    />
                </div>
            </div>

            {/* Room List */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {chatType === 'group' ? (
                    joinedGatherings.map((g, idx) => (
                        <div 
                            key={g.id}
                            onClick={() => setSelectedRoom(g)}
                            className="glass"
                            style={{
                                padding: '20px',
                                borderRadius: '24px',
                                display: 'flex',
                                alignItems: 'center',
                                gap: '16px',
                                cursor: 'pointer',
                                transition: 'transform 0.2s ease',
                                animation: `slideUp 0.4s ease-out forwards`,
                                animationDelay: `${idx * 0.05}s`,
                                opacity: 0,
                                border: '1px solid rgba(255,255,255,0.05)'
                            }}
                            onMouseEnter={(e) => e.currentTarget.style.transform = 'translateY(-2px)'}
                            onMouseLeave={(e) => e.currentTarget.style.transform = 'translateY(0)'}
                        >
                            <div style={{ 
                                width: '56px', height: '56px', borderRadius: '18px', 
                                background: g.bgImageUrl ? `url(${g.bgImageUrl}) center/cover` : 'var(--primary-gradient)',
                                position: 'relative',
                                flexShrink: 0,
                                boxShadow: '0 8px 16px rgba(0,0,0,0.2)'
                            }}>
                                {!g.bgImageUrl && <MessageSquare size={24} color="white" style={{ position: 'absolute', top: '16px', left: '16px' }} />}
                            </div>
                            
                            <div style={{ flex: 1 }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '4px' }}>
                                    <h3 className="text-s" style={{ color: 'white', fontWeight: 800, margin: 0 }}>{g.title}</h3>
                                    <span style={{ fontSize: '11px', color: 'rgba(255,255,255,0.4)', fontWeight: 600 }}>방금 전</span>
                                </div>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                    <p style={{ fontSize: '13px', color: 'rgba(255,255,255,0.5)', margin: 0, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '180px' }}>
                                        모임 채팅에 참여해 보세요.
                                    </p>
                                    <div style={{ 
                                        display: 'flex', alignItems: 'center', gap: '4px', 
                                        background: 'rgba(255,255,255,0.05)', padding: '2px 6px', borderRadius: '6px' 
                                    }}>
                                        <Users size={10} color="var(--primary-orange)" />
                                        <span style={{ fontSize: '10px', color: 'var(--primary-orange)', fontWeight: 800 }}>{g.currentJoining}</span>
                                    </div>
                                </div>
                            </div>
                            <ChevronRight size={20} color="rgba(255,255,255,0.2)" />
                        </div>
                    ))
                ) : (
                    /* DM List Placeholder */
                    <div className="glass" style={{ 
                        textAlign: 'center', padding: '60px 24px', borderRadius: '32px',
                        border: '1px dashed rgba(255,255,255,0.1)'
                    }}>
                        <div style={{ fontSize: '40px', marginBottom: '16px' }}>👤</div>
                        <p style={{ color: 'rgba(255,255,255,0.4)', fontWeight: 600 }}>최근 DM 내역이 없습니다.<br/>모임 참여자 목록에서 대화를 시작해 보세요!</p>
                    </div>
                )}

                {chatType === 'group' && joinedGatherings.length === 0 && (
                    <div className="glass" style={{ 
                        textAlign: 'center', padding: '60px 24px', borderRadius: '32px',
                        border: '1px dashed rgba(255,255,255,0.1)'
                    }}>
                        <div style={{ fontSize: '40px', marginBottom: '16px' }}>💬</div>
                        <p style={{ color: 'rgba(255,255,255,0.4)', fontWeight: 600 }}>참여 중인 대화방이 없습니다.</p>
                    </div>
                )}
            </div>

            <style jsx>{`
                @keyframes slideUp {
                    from { transform: translateY(20px); opacity: 0; }
                    to { transform: translateY(0); opacity: 1; }
                }
            `}</style>
        </div>
    );
};

export default ChatTab;
