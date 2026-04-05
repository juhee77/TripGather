import React, { useState } from 'react';
import { MessageSquare, Users, ChevronRight, Search } from 'lucide-react';
import ChatRoom from './ChatRoom';
import DMChatRoom from './DMChatRoom';
import Card from './UI/Card';

const ChatTab = ({ joinedGatherings }) => {
    const [selectedRoom, setSelectedRoom] = useState(null);
    const [selectedDMUser, setSelectedDMUser] = useState(null);
    const [chatType, setChatType] = useState('group'); // 'group' or 'dm'
    const [dmPartners, setDmPartners] = useState([]);
    const [loadingDMs, setLoadingDMs] = useState(false);

    const handleStartDM = (user) => {
        setSelectedRoom(null);
        setSelectedDMUser(user);
    };

    React.useEffect(() => {
        if (chatType === 'dm') {
            setLoadingDMs(true);
            import('../repositories/ChatRepository').then(repo => {
                repo.default.getDMPartners()
                    .then(data => {
                        setDmPartners(data || []);
                        setLoadingDMs(false);
                    })
                    .catch(err => {
                        console.error("Error fetching DM partners:", err);
                        setLoadingDMs(false);
                    });
            });
        }
    }, [chatType]);

    if (selectedDMUser) {
        return <DMChatRoom otherUser={selectedDMUser} onBack={() => setSelectedDMUser(null)} />;
    }

    if (selectedRoom) {
        return <ChatRoom gathering={selectedRoom} onBack={() => setSelectedRoom(null)} onStartDM={handleStartDM} />;
    }

    return (
        <div className="animate-fade" style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
            {/* Unified Filter Tabs */}
            <div style={{ display: 'flex', background: 'var(--highlight-muted)', padding: '6px', borderRadius: '16px', gap: '4px' }}>
                <button 
                    onClick={() => setChatType('group')}
                    style={{ 
                        flex: 1, padding: '10px', borderRadius: '12px', fontSize: '13px', fontWeight: 800,
                        background: chatType === 'group' ? 'white' : 'transparent',
                        color: chatType === 'group' ? 'var(--primary-orange)' : 'var(--text-muted)',
                        boxShadow: chatType === 'group' ? '0 4px 12px rgba(0,0,0,0.05)' : 'none',
                        transition: 'all 0.2s'
                    }}
                >
                    모임 채팅
                </button>
                <button 
                    onClick={() => setChatType('dm')}
                    style={{ 
                        flex: 1, padding: '10px', borderRadius: '12px', fontSize: '13px', fontWeight: 800,
                        background: chatType === 'dm' ? 'white' : 'transparent',
                        color: chatType === 'dm' ? 'var(--primary-orange)' : 'var(--text-muted)',
                        boxShadow: chatType === 'dm' ? '0 4px 12px rgba(0,0,0,0.05)' : 'none',
                        transition: 'all 0.2s'
                    }}
                >
                    1:1 DM
                </button>
            </div>

            {/* Premium Search Bar */}
            <div className="glass" style={{ 
                padding: '12px 18px', borderRadius: '18px', display: 'flex', alignItems: 'center', gap: '12px',
                border: '1px solid var(--border-color)', background: 'white'
            }}>
                <Search size={18} color="var(--text-muted)" />
                <input 
                    placeholder={chatType === 'group' ? "채팅방 검색..." : "대화 상대 검색..."}
                    style={{ background: 'none', border: 'none', outline: 'none', color: 'var(--text-primary)', fontSize: '14px', fontWeight: 600, width: '100%' }} 
                />
            </div>

            {/* Unified Room List */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {chatType === 'group' ? (
                    joinedGatherings.length > 0 ? (
                        joinedGatherings.map((g, idx) => (
                            <Card 
                                key={g.id}
                                onClick={() => setSelectedRoom(g)}
                                className="chat-item-card"
                                animate
                                style={{ padding: '16px 20px', cursor: 'pointer', border: '1px solid var(--border-color)' }}
                            >
                                <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                                    <div style={{ 
                                        width: '56px', height: '56px', borderRadius: '18px', 
                                        background: g.bgImageUrl ? `url(${g.bgImageUrl}) center/cover` : 'var(--primary-gradient)',
                                        flexShrink: 0, boxShadow: '0 8px 16px rgba(0,0,0,0.1)'
                                    }}>
                                        {!g.bgImageUrl && <div style={{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}><MessageSquare size={22} color="white" /></div>}
                                    </div>
                                    
                                    <div style={{ flex: 1 }}>
                                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '4px' }}>
                                            <h3 style={{ color: 'var(--text-primary)', fontWeight: 800, margin: 0, fontSize: '15px' }}>{g.title}</h3>
                                            <span style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 600 }}>방금 전</span>
                                        </div>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                            <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: 0, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '180px' }}>
                                                새로운 메시지가 있습니다.
                                            </p>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: '4px', background: 'rgba(255, 92, 0, 0.08)', padding: '2px 6px', borderRadius: '6px' }}>
                                                <Users size={10} color="var(--primary-orange)" />
                                                <span style={{ fontSize: '10px', color: 'var(--primary-orange)', fontWeight: 800 }}>{g.currentJoining}</span>
                                            </div>
                                        </div>
                                    </div>
                                    <ChevronRight size={18} color="var(--border-color)" />
                                </div>
                            </Card>
                        ))
                    ) : (
                        <Card glass={false} style={{ textAlign: 'center', padding: '60px 24px', border: '1px dashed var(--border-color)', background: 'transparent' }}>
                            <div style={{ fontSize: '40px', marginBottom: '16px' }}>💬</div>
                            <p style={{ color: 'var(--text-muted)', fontWeight: 600, fontSize: '14px' }}>참여 중인 대화방이 없습니다.</p>
                        </Card>
                    )
                ) : (
                    <>
                        {dmPartners.length > 0 ? (
                            dmPartners.map((partner, idx) => (
                                <Card 
                                    key={partner.email}
                                    onClick={() => handleStartDM(partner)}
                                    className="chat-item-card"
                                    animate
                                    style={{ padding: '16px 20px', cursor: 'pointer', border: '1px solid var(--border-color)' }}
                                >
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                                        <div style={{ 
                                            width: '56px', height: '56px', borderRadius: '18px', 
                                            background: partner.profileImageUrl ? `url(${partner.profileImageUrl}) center/cover` : 'var(--bg-color)',
                                            display: 'flex', alignItems: 'center', justifyContent: 'center',
                                            flexShrink: 0, boxShadow: '0 8px 16px rgba(0,0,0,0.05)',
                                            border: '1px solid var(--border-color)', fontSize: '20px', fontWeight: 800, color: 'var(--text-muted)'
                                        }}>
                                            {!partner.profileImageUrl && partner.name[0]}
                                        </div>
                                        <div style={{ flex: 1 }}>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '4px' }}>
                                                <h3 style={{ color: 'var(--text-primary)', fontWeight: 800, margin: 0, fontSize: '15px' }}>{partner.name}</h3>
                                                <span style={{ fontSize: '11px', color: 'var(--text-muted)', fontWeight: 600 }}>1:1 DM</span>
                                            </div>
                                            <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: 0, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{partner.email}</p>
                                        </div>
                                        <ChevronRight size={18} color="var(--border-color)" />
                                    </div>
                                </Card>
                            ))
                        ) : (
                            !loadingDMs && (
                                <Card glass={false} style={{ textAlign: 'center', padding: '60px 24px', border: '1px dashed var(--border-color)', background: 'transparent' }}>
                                    <div style={{ fontSize: '40px', marginBottom: '16px' }}>👤</div>
                                    <p style={{ color: 'var(--text-muted)', fontWeight: 600, fontSize: '14px' }}>최근 DM 내역이 없습니다.<br/>이웃에게 대화를 건네보세요!</p>
                                </Card>
                            )
                        )}
                        {loadingDMs && (
                            <div style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)', fontSize: '13px' }}>대화 상대를 찾는 중...</div>
                        )}
                    </>
                )}
            </div>

            <style>{`
                @keyframes slideUp {
                    from { transform: translateY(20px); opacity: 0; }
                    to { transform: translateY(0); opacity: 1; }
                }
            `}</style>
        </div>
    );
};

export default ChatTab;
