import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { MapPin, Edit3, Plane, Check } from 'lucide-react';
import { useUser } from '../contexts/UserContext';
import { authFetch } from '../api/client';
import ModalHeader from '../components/UI/ModalHeader';
import ModalFooter from '../components/UI/ModalFooter';
import PrimaryButton from '../components/UI/PrimaryButton';
import JourneyRepository from '../repositories/JourneyRepository';

const ItineraryDetailPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [, setIsVisible] = useState(false);
    const { user: currentUser } = useUser();
    
    const [localItinerary, setLocalItinerary] = useState(null);
    const [loading, setLoading] = useState(true);
    const [showAddModal, setShowAddModal] = useState(false);
    const [myJourneys, setMyJourneys] = useState([]);
    const [selectedTargetId, setSelectedTargetId] = useState('');
    const [selectedDay, setSelectedDay] = useState(1);
    const [actionLoading, setActionLoading] = useState(false);

    useEffect(() => {
        setIsVisible(true);
        const fetchDetails = async () => {
            setLoading(true);
            try {
                const res = await authFetch(`/api/itineraries/${id}`);
                if (!res.ok) throw new Error("Failed to load itinerary");
                const data = await res.json();
                setLocalItinerary(data);
            } catch (err) {
                console.error(err);
            } finally {
                setLoading(false);
            }
        };
        if (id) fetchDetails();
    }, [id]);

    if (loading) return <div style={{ padding: '40px', textAlign: 'center' }}>로딩 중...</div>;
    if (!localItinerary) return <div style={{ padding: '40px', textAlign: 'center' }}>데이터를 찾을 수 없습니다.</div>;

    const onClose = () => navigate(-1);
    const onEdit = () => navigate(`/itinerary/edit/${id}`);

    const isOwner = currentUser && (
        currentUser.email === localItinerary.ownerEmail
    );
    const isAuthor = currentUser && (
        currentUser.name === localItinerary.author ||
        currentUser.email === localItinerary.authorEmail
    );

    const saveItinerary = async (updated) => {
        try {
            const res = await authFetch(`/api/itineraries/${id}`, {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(updated)
            });
            if (res.ok) {
                const data = await res.json();
                setLocalItinerary(data);
                return true;
            }
        } catch (err) {
            console.error("Save failed:", err);
            alert("저장에 실패했습니다.");
        }
        return false;
    };

    const addStop = (dayNum) => {
        const newPoints = [...(localItinerary.routePoints || [])];
        const dayPoints = newPoints.filter(p => p.dayNumber === dayNum);
        const lastSeq = dayPoints.length > 0 ? Math.max(...dayPoints.map(p => p.sequenceOrder)) : 0;
        
        newPoints.push({
            label: 'New Stop',
            dayNumber: dayNum,
            dayLabel: dayPoints[0]?.dayLabel || `Day ${dayNum}`,
            sequenceOrder: lastSeq + 1,
            startTime: '12:00',
            endTime: '13:00'
        });
        
        saveItinerary({ ...localItinerary, routePoints: newPoints });
    };

    const removeStop = (idx) => {
        const newPoints = [...(localItinerary.routePoints || [])];
        newPoints.splice(idx, 1);
        saveItinerary({ ...localItinerary, routePoints: newPoints });
    };

    const renameStop = (idx, newLabel) => {
        const newPoints = [...(localItinerary.routePoints || [])];
        newPoints[idx].label = newLabel;
        saveItinerary({ ...localItinerary, routePoints: newPoints });
    };

    const handleDrop = (fromIdx, toIdx) => {
        if (fromIdx === toIdx) return;
        const newPoints = [...(localItinerary.routePoints || [])];
        const [moved] = newPoints.splice(fromIdx, 1);
        newPoints.splice(toIdx, 0, moved);
        const reordered = newPoints.map((p, i) => ({ ...p, sequenceOrder: i + 1 }));
        saveItinerary({ ...localItinerary, routePoints: reordered });
    };

    const handleClone = async () => {
        if (!currentUser?.email) return alert('로그인이 필요합니다.');
        setActionLoading(true);
        try {
            const res = await JourneyRepository.add(localItinerary.id, currentUser.email);
            alert('새 여행으로 추가되었습니다. ✈️');
            if (res && res.id) {
                navigate(`/itinerary/${res.id}`);
            } else {
                navigate('/');
            }
        } catch (e) {
            alert('추가에 실패했습니다.');
        } finally {
            setActionLoading(false);
        }
    };

    const handleMerge = async () => {
        if (!selectedTargetId) return alert('추가할 여정을 선택해주세요.');
        setActionLoading(true);
        try {
            const res = await authFetch(`/api/my-trips/merge?sourceId=${localItinerary.id}&targetId=${selectedTargetId}&targetDay=${selectedDay}`, {
                method: 'POST'
            });
            if (res.ok) {
                alert('기존 여행에 추가되었습니다! 🗺️');
                navigate(`/itinerary/${selectedTargetId}`);
            } else {
                alert('추가에 실패했습니다.');
            }
        } catch (e) {
            alert('오류가 발생했습니다.');
        } finally {
            setActionLoading(false);
        }
    };

    const groupedByDay = (() => {
        const sourcePoints = localItinerary.routePoints || [];
        const pointsWithIdx = sourcePoints.map((p, index) => ({ ...p, originalIndex: index }));
        
        const map = {};
        pointsWithIdx.forEach(p => {
            const key = p.dayNumber ?? 1;
            if (!map[key]) map[key] = { dayLabel: p.dayLabel || `Day ${key}`, points: [] };
            map[key].points.push(p);
        });
        return Object.entries(map).sort((a, b) => Number(a[0]) - Number(b[0])).map(([dayNum, data]) => ({
            dayNumber: Number(dayNum),
            ...data
        }));
    })();

    return (
        <div className="animate-fade" style={{ background: 'var(--bg-lite)', minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', paddingBottom: '100px' }}>
                <ModalHeader 
                    title={localItinerary.title}
                    subtitle="ITINERARY DETAILS"
                    onClose={onClose}
                    actions={
                        isAuthor && (
                            <button onClick={onEdit} className="icon-circle" style={{ width: '40px', height: '40px' }}>
                                <Edit3 size={18} color="var(--text-primary)" />
                            </button>
                        )
                    }
                />

                <div className="hide-scrollbar" style={{ flex: 1, overflowY: 'auto', padding: '24px' }}>
                    {/* Header Info Card */}
                    <div className="ticket-wrapper" style={{ padding: '24px', marginBottom: '32px', background: 'white', position: 'relative' }}>
                        {isOwner && (
                            <div style={{ 
                                position: 'absolute', top: '12px', right: '12px', display: 'flex', alignItems: 'center', gap: '8px',
                                padding: '6px 12px', background: 'var(--bg-lite)', borderRadius: '12px', border: '1px solid var(--border-color)'
                            }}>
                                <span style={{ fontSize: '11px', fontWeight: 900, color: 'var(--text-muted)' }}>FEED SHARE</span>
                                <button
                                    onClick={async () => {
                                        const currentPublic = localItinerary.isPublic ?? localItinerary.publicStatus;
                                        const newStatus = !currentPublic;
                                        const success = await saveItinerary({ ...localItinerary, isPublic: newStatus, publicStatus: newStatus });
                                        if (success) {
                                            setLocalItinerary(prev => ({ ...prev, isPublic: newStatus, publicStatus: newStatus }));
                                        }
                                    }}
                                    style={{
                                        width: '36px', height: '18px', borderRadius: '9px',
                                        background: (localItinerary.isPublic || localItinerary.publicStatus) ? 'var(--primary-orange)' : '#DEE2E6',
                                        position: 'relative', border: 'none', cursor: 'pointer', transition: 'all 0.2s'
                                    }}
                                >
                                    <div style={{
                                        width: '12px', height: '12px', borderRadius: '50%', background: 'white',
                                        position: 'absolute', top: '3px', left: (localItinerary.isPublic || localItinerary.publicStatus) ? '21px' : '3px',
                                        transition: 'all 0.2s'
                                    }} />
                                </button>
                            </div>
                        )}
                        <div className="flex-between" style={{ marginBottom: '12px' }}>
                            <div style={{ display: 'flex', gap: '8px' }}>
                                <span className="label-orange">JOURNEY LOG</span>
                                {(localItinerary.isPublic || localItinerary.publicStatus) && <span style={{ padding: '2px 8px', background: 'var(--primary-orange)', color: 'white', borderRadius: '6px', fontSize: '9px', fontWeight: 900 }}>PUBLIC</span>}
                            </div>
                            <div className="status-pill">SAVED</div>
                        </div>
                        <p className="text-s" style={{ color: 'var(--text-main)', fontSize: '15px', lineHeight: 1.6 }}>
                            {localItinerary.description}
                        </p>
                        
                        {localItinerary.startDate && (
                            <div style={{ marginTop: '16px', display: 'flex', gap: '16px', fontSize: '13px', color: 'var(--text-secondary)' }}>
                                <span>📅 {localItinerary.startDate} ~ {localItinerary.endDate}</span>
                                {localItinerary.location && <span>📍 {localItinerary.location}</span>}
                            </div>
                        )}
                    </div>

                    {groupedByDay.map((day, dIdx) => (
                        <div key={dIdx} style={{ marginBottom: '40px' }}>
                            <div className="flex-between" style={{ marginBottom: '20px' }}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                                    <div style={{ 
                                        background: 'var(--text-primary)', color: 'white', 
                                        padding: '4px 12px', borderRadius: '8px', fontSize: '12px', fontWeight: 900 
                                    }}>DAY {day.dayNumber}</div>
                                    <span style={{ fontWeight: 800, color: 'var(--text-primary)' }}>{day.dayLabel}</span>
                                </div>
                                <div style={{ flex: 1, height: '1px', background: 'var(--border-color)', marginLeft: '16px' }} />
                                {isOwner && (
                                    <button 
                                        onClick={() => addStop(day.dayNumber)}
                                        style={{ 
                                            marginLeft: '16px', background: 'var(--highlight-muted)', border: 'none', 
                                            padding: '4px 12px', borderRadius: '20px', fontSize: '12px', fontWeight: 800,
                                            color: 'var(--primary-orange)', cursor: 'pointer'
                                        }}
                                    >
                                        + ADD STOP
                                    </button>
                                )}
                            </div>

                            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                                {day.points.map((point, pIdx) => (
                                    <div 
                                        key={pIdx} 
                                        className="ticket-wrapper animate-fade" 
                                        draggable={isOwner}
                                        onDragStart={(e) => {
                                            if (!isOwner) return;
                                            e.dataTransfer.setData('sourceIdx', point.originalIndex);
                                            e.currentTarget.style.opacity = '0.4';
                                        }}
                                        onDragEnd={(e) => {
                                            e.currentTarget.style.opacity = '1';
                                        }}
                                        onDragOver={(e) => isOwner && e.preventDefault()}
                                        onDrop={(e) => {
                                            if (!isOwner) return;
                                            e.preventDefault();
                                            const from = parseInt(e.dataTransfer.getData('sourceIdx'));
                                            handleDrop(from, point.originalIndex);
                                        }}
                                        style={{ 
                                            padding: '20px', 
                                            border: '1px solid var(--border-color)',
                                            background: 'white',
                                            cursor: isOwner ? 'grab' : 'default',
                                            position: 'relative'
                                        }}
                                    >
                                        {isOwner && (
                                            <button 
                                                onClick={() => removeStop(point.originalIndex)}
                                                style={{ 
                                                    position: 'absolute', top: '12px', right: '12px', background: 'none', border: 'none', 
                                                    color: '#EF4444', cursor: 'pointer', padding: '4px'
                                                }}
                                            >
                                                <Edit3 size={14} style={{ opacity: 0.6 }} />
                                            </button>
                                        )}

                                        <div className="flex-between" style={{ marginBottom: '8px' }}>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                                                <div style={{ 
                                                    width: '24px', height: '24px', borderRadius: '50%', 
                                                    background: 'var(--bg-color)',
                                                    color: 'var(--text-muted)',
                                                    fontSize: '12px', fontWeight: 900, display: 'flex', justifyContent: 'center', alignItems: 'center'
                                                }}>
                                                    {pIdx + 1}
                                                </div>
                                                <div style={{ flex: 1 }}>
                                                    {isOwner ? (
                                                        <input 
                                                            defaultValue={point.label}
                                                            onBlur={(e) => renameStop(point.originalIndex, e.target.value)}
                                                            className="clean-input"
                                                            style={{ fontWeight: 800, color: 'var(--text-primary)', border: 'none', background: 'transparent', width: '100%', fontSize: '15px' }}
                                                        />
                                                    ) : (
                                                        <h4 style={{ fontWeight: 800, color: 'var(--text-primary)', margin: 0 }}>{point.label}</h4>
                                                    )}
                                                    
                                                    {(point.startTime || point.endTime) && (
                                                        <div style={{ 
                                                            marginTop: '8px', padding: '10px 14px', background: 'var(--highlight-muted)', 
                                                            borderRadius: '12px', display: 'flex', flexWrap: 'wrap', gap: '12px', alignItems: 'center'
                                                        }}>
                                                            {point.startTime && (
                                                                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                                                    <Plane size={12} color="var(--primary-orange)" style={{ transform: 'rotate(90deg)' }} />
                                                                    <span className="label-muted" style={{ fontSize: '10px', color: 'var(--text-secondary)' }}>ARRIVAL</span>
                                                                    <span style={{ fontSize: '13px', fontWeight: 800, color: 'var(--text-primary)' }}>{point.startTime}</span>
                                                                </div>
                                                            )}
                                                            {point.startTime && point.endTime && (
                                                                <div style={{ width: '1px', height: '12px', background: 'var(--border-color)' }} />
                                                            )}
                                                            {point.endTime && (
                                                                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                                                    <Plane size={12} color="var(--primary-orange)" />
                                                                    <span className="label-muted" style={{ fontSize: '10px', color: 'var(--text-secondary)' }}>DEPARTURE</span>
                                                                    <span style={{ fontSize: '13px', fontWeight: 800, color: 'var(--text-primary)' }}>{point.endTime}</span>
                                                                </div>
                                                            )}
                                                        </div>
                                                    )}
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    ))}
                </div>

                <ModalFooter>
                    <div style={{ display: 'flex', gap: '10px', width: '100%' }}>
                        {!isOwner && (
                            <PrimaryButton
                                style={{ flex: 1 }}
                                onClick={async () => {
                                    if (!currentUser?.email) return alert('로그인이 필요합니다.');
                                    // Fetch my journeys to show in selection
                                    try {
                                        const mine = await JourneyRepository.fetchMine(currentUser.email);
                                        setMyJourneys(mine);
                                        setShowAddModal(true);
                                    } catch (e) {
                                        handleClone(); // Fallback to direct clone
                                    }
                                }}
                            >
                                내 여행에 추가
                            </PrimaryButton>
                        )}
                        {isOwner && (
                            <div style={{ flex: 1, textAlign: 'center', color: 'var(--text-muted)', fontSize: '13px', fontWeight: 600 }}>
                                내 여권을 확인하거나 여행 피드에서 다른 영감을 찾아보세요! ✈️
                            </div>
                        )}
                    </div>
                </ModalFooter>

                {/* Add to Trip Modal */}
                {showAddModal && (
                    <div className="fixed-full flex-center" style={{ zIndex: 2000, background: 'rgba(0,0,0,0.4)', backdropFilter: 'blur(4px)' }}>
                        <div className="glass card animate-pop" style={{ width: '90%', maxWidth: '400px', padding: '24px', position: 'relative' }}>
                            <h3 className="heading-s" style={{ marginBottom: '8px' }}>내 여행에 추가하기</h3>
                            <p style={{ fontSize: '13px', color: 'var(--text-sub)', marginBottom: '20px' }}>이 여정을 어떻게 보관할까요?</p>
                            
                            <button 
                                onClick={handleClone}
                                disabled={actionLoading}
                                style={{ 
                                    width: '100%', padding: '16px', borderRadius: '16px', border: '1px solid var(--border-color)', 
                                    background: 'white', display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer', marginBottom: '12px',
                                    textAlign: 'left'
                                }}
                            >
                                <div style={{ fontSize: '24px' }}>✈️</div>
                                <div>
                                    <div style={{ fontWeight: 800 }}>새로운 일정으로 만들기</div>
                                    <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>독립적인 새 여정으로 내 목록에 추가합니다.</div>
                                </div>
                            </button>

                            <div style={{ margin: '20px 0', display: 'flex', alignItems: 'center', gap: '10px' }}>
                                <div style={{ flex: 1, height: '1px', background: 'var(--border-color)' }} />
                                <span style={{ fontSize: '11px', fontWeight: 900, color: 'var(--text-muted)' }}>OR ADD TO EXISTING</span>
                                <div style={{ flex: 1, height: '1px', background: 'var(--border-color)' }} />
                            </div>

                            <div style={{ marginBottom: '16px' }}>
                                <label style={{ fontSize: '11px', fontWeight: 900, color: 'var(--text-muted)', marginBottom: '6px', display: 'block' }}>여행 선택</label>
                                <select 
                                    value={selectedTargetId}
                                    onChange={(e) => setSelectedTargetId(e.target.value)}
                                    className="clean-input"
                                    style={{ width: '100%', background: 'var(--bg-color)', padding: '12px', borderRadius: '12px', border: '1px solid var(--border-color)' }}
                                >
                                    <option value="">여행을 선택해주세요</option>
                                    {myJourneys.map(j => (
                                        <option key={j.id} value={j.id}>{j.title}</option>
                                    ))}
                                </select>
                            </div>

                            {selectedTargetId && (
                                <div style={{ marginBottom: '24px' }}>
                                    <label style={{ fontSize: '11px', fontWeight: 900, color: 'var(--text-muted)', marginBottom: '6px', display: 'block' }}>추가할 일차 선택</label>
                                    <div style={{ display: 'flex', gap: '8px', overflowX: 'auto', paddingBottom: '8px' }}>
                                        {[1, 2, 3, 4, 5, 6, 7].map(day => (
                                            <button
                                                key={day}
                                                onClick={() => setSelectedDay(day)}
                                                style={{
                                                    minWidth: '40px', height: '40px', borderRadius: '10px',
                                                    background: selectedDay === day ? 'var(--text-primary)' : 'var(--bg-color)',
                                                    color: selectedDay === day ? 'white' : 'var(--text-secondary)',
                                                    border: '1px solid var(--border-color)',
                                                    fontWeight: 800, cursor: 'pointer'
                                                }}
                                            >
                                                {day}
                                            </button>
                                        ))}
                                    </div>
                                </div>
                            )}

                            <div style={{ display: 'flex', gap: '10px' }}>
                                <button onClick={() => setShowAddModal(false)} className="clean-input" style={{ flex: 1, border: 'none', background: 'var(--bg-color)', cursor: 'pointer', fontWeight: 800, padding: '12px', borderRadius: '12px' }}>취소</button>
                                <PrimaryButton 
                                    onClick={handleMerge} 
                                    disabled={!selectedTargetId || actionLoading}
                                    style={{ flex: 1.5 }}
                                >
                                    {actionLoading ? '추가 중...' : '기본 여정에 병합'}
                                </PrimaryButton>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default ItineraryDetailPage;
