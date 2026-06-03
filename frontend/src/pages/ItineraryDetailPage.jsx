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
    const [myTrips, setMyTrips] = useState([]);
    const [selectedTripId, setSelectedTripId] = useState('');
    const [actionLoading, setActionLoading] = useState(false);
    const [isEditMode, setIsEditMode] = useState(false);

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
            // 1. 일정(Itinerary) 클론 생성
            const clonedItinerary = await JourneyRepository.add(localItinerary.id, currentUser.email);
            if (!clonedItinerary || !clonedItinerary.id) {
                throw new Error("Cloning itinerary failed");
            }

            // 2. 클론된 일정을 담을 새로운 여행(Trip) 생성
            const tripPayload = {
                title: `${clonedItinerary.title || '새로운 여행'}`,
                destination: clonedItinerary.location || '목적지 미정',
                country: clonedItinerary.location || '국가 미정',
                startDate: clonedItinerary.startDate || null,
                endDate: clonedItinerary.endDate || null,
                bgImageUrl: clonedItinerary.stampImageUrl || null,
                status: 'PLANNING'
            };

            const tripRes = await authFetch('/api/trips', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(tripPayload)
            });

            if (!tripRes.ok) {
                throw new Error("Creating trip failed");
            }

            const newTrip = await tripRes.json();

            // 3. 생성된 여행(Trip)에 클론된 일정(Itinerary) 연결
            const linkRes = await authFetch(`/api/trips/${newTrip.id}/itineraries/${clonedItinerary.id}`, {
                method: 'POST'
            });

            if (!linkRes.ok) {
                throw new Error("Linking itinerary to trip failed");
            }

            alert('새 여행으로 추가되었습니다. ✈️');
            navigate(`/trip/${newTrip.id}`);
        } catch (e) {
            console.error("Clone error: ", e);
            alert('추가에 실패했습니다.');
        } finally {
            setActionLoading(false);
        }
    };

    const handleAddToTrip = async () => {
        if (!selectedTripId) return alert('추가할 여행을 선택해주세요.');
        setActionLoading(true);
        try {
            // 1. 일정(Itinerary) 클론 생성
            const clonedItinerary = await JourneyRepository.add(localItinerary.id, currentUser.email);
            if (!clonedItinerary || !clonedItinerary.id) {
                throw new Error("Cloning itinerary failed");
            }

            // 2. 선택한 기존 여행(Trip)에 클론된 일정 연결
            const linkRes = await authFetch(`/api/trips/${selectedTripId}/itineraries/${clonedItinerary.id}`, {
                method: 'POST'
            });

            if (linkRes.ok) {
                alert('기존 여행에 추가되었습니다! 🗺️');
                navigate(`/trip/${selectedTripId}`);
            } else {
                throw new Error("Linking itinerary to existing trip failed");
            }
        } catch (e) {
            console.error("Add to trip error: ", e);
            alert('추가에 실패했습니다.');
        } finally {
            setActionLoading(false);
        }
    };

    const isAllFinished = localItinerary.routePoints?.length > 0 && 
        localItinerary.routePoints.every(p => p.isCompleted);

    const handleApplyStamp = async () => {
        setActionLoading(true);
        try {
            // Using placeholder for now
            const stampUrl = '/src/assets/stamp-placeholder.png';
            const success = await saveItinerary({ ...localItinerary, stampImageUrl: stampUrl });
            if (success) {
                alert('축하합니다! 여행 완료 스탬프가 찍혔습니다. ✈️');
            }
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
                        <div className="flex-between" style={{ marginBottom: '12px', alignItems: 'flex-start' }}>
                            <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                                <span className="label-orange">JOURNEY LOG</span>
                                {(localItinerary.isPublic || localItinerary.publicStatus) && <span style={{ padding: '2px 8px', background: 'var(--primary-orange)', color: 'white', borderRadius: '6px', fontSize: '9px', fontWeight: 900 }}>PUBLIC</span>}
                                {localItinerary.stampImageUrl && <span style={{ padding: '2px 8px', background: '#4ADE80', color: 'white', borderRadius: '6px', fontSize: '9px', fontWeight: 900 }}>STAMPED</span>}
                                {isOwner && (
                                    <div style={{ 
                                        display: 'flex', alignItems: 'center', gap: '8px',
                                        padding: '4px 10px', background: 'var(--bg-lite)', borderRadius: '10px', border: '1px solid var(--border-color)',
                                        marginLeft: '8px'
                                    }}>
                                        <span style={{ fontSize: '10px', fontWeight: 900, color: 'var(--text-secondary)' }}>FEED SHARE</span>
                                        <button
                                            onClick={async (e) => {
                                                e.stopPropagation();
                                                const currentPublic = localItinerary.isPublic ?? localItinerary.publicStatus;
                                                const newStatus = !currentPublic;
                                                const success = await saveItinerary({ ...localItinerary, isPublic: newStatus, publicStatus: newStatus });
                                                if (success) {
                                                    setLocalItinerary(prev => ({ ...prev, isPublic: newStatus, publicStatus: newStatus }));
                                                }
                                            }}
                                            style={{
                                                width: '32px', height: '16px', borderRadius: '8px',
                                                background: (localItinerary.isPublic || localItinerary.publicStatus) ? 'var(--primary-orange)' : '#DEE2E6',
                                                position: 'relative', border: 'none', cursor: 'pointer', transition: 'all 0.2s'
                                            }}
                                        >
                                            <div style={{
                                                width: '10px', height: '10px', borderRadius: '50%', background: 'white',
                                                position: 'absolute', top: '3px', left: (localItinerary.isPublic || localItinerary.publicStatus) ? '19px' : '3px',
                                                transition: 'all 0.2s'
                                            }} />
                                        </button>
                                    </div>
                                )}
                            </div>
                            <div className="status-pill" style={{ flexShrink: 0 }}>SAVED</div>
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

                        {/* Stamp Image Display */}
                        {localItinerary.stampImageUrl && (
                            <div style={{ 
                                position: 'absolute', bottom: '10px', right: '20px', 
                                transform: 'rotate(-15deg)', pointerEvents: 'none',
                                animation: 'stamp-pop 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275)'
                            }}>
                                <img 
                                    src={localItinerary.stampImageUrl} 
                                    alt="Stamp" 
                                    style={{ 
                                        width: '120px', height: '120px', opacity: 0.8, 
                                        filter: 'sepia(0.3) hue-rotate(-10deg) contrast(1.1)',
                                        dropShadow: '0 4px 8px rgba(0,0,0,0.1)'
                                    }} 
                                />
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
                                        onClick={() => setIsEditMode(!isEditMode)}
                                        style={{ 
                                            marginLeft: '16px', background: isEditMode ? 'var(--primary-gradient)' : 'var(--bg-color)', border: '1px solid var(--border-color)', 
                                            padding: '4px 12px', borderRadius: '12px', fontSize: '12px', fontWeight: 800,
                                            color: isEditMode ? 'white' : 'var(--text-primary)', cursor: 'pointer'
                                        }}
                                    >
                                        {isEditMode ? '완료' : '편집'}
                                    </button>
                                )}
                                {isOwner && isEditMode && (
                                    <button 
                                        onClick={() => addStop(day.dayNumber)}
                                        style={{ 
                                            marginLeft: '8px', background: 'var(--highlight-muted)', border: 'none', 
                                            padding: '4px 12px', borderRadius: '12px', fontSize: '12px', fontWeight: 800,
                                            color: 'var(--primary-orange)', cursor: 'pointer'
                                        }}
                                    >
                                        + ADD
                                    </button>
                                )}
                            </div>

                            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                                {day.points.map((point, pIdx) => (
                                    <div 
                                        key={pIdx} 
                                        className="ticket-wrapper animate-fade" 
                                        draggable={isOwner && isEditMode}
                                        onDragStart={(e) => {
                                            if (!isOwner || !isEditMode) return;
                                            e.dataTransfer.setData('sourceIdx', point.originalIndex);
                                            e.currentTarget.style.opacity = '0.4';
                                        }}
                                        onDragEnd={(e) => {
                                            e.currentTarget.style.opacity = '1';
                                        }}
                                        onDragOver={(e) => isOwner && isEditMode && e.preventDefault()}
                                        onDrop={(e) => {
                                            if (!isOwner || !isEditMode) return;
                                            e.preventDefault();
                                            const from = parseInt(e.dataTransfer.getData('sourceIdx'));
                                            handleDrop(from, point.originalIndex);
                                        }}
                                        style={{ 
                                            padding: '20px', 
                                            border: '1px solid var(--border-color)',
                                            background: 'white',
                                            cursor: isOwner && isEditMode ? 'grab' : 'default',
                                            position: 'relative',
                                            opacity: point.isCompleted ? 0.7 : 1
                                        }}
                                    >
                                        {isOwner && isEditMode && (
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
                                                <div 
                                                    onClick={(e) => {
                                                        if (isOwner && !isEditMode) {
                                                            e.stopPropagation();
                                                            const newPoints = [...(localItinerary.routePoints || [])];
                                                            newPoints[point.originalIndex].isCompleted = !newPoints[point.originalIndex].isCompleted;
                                                            saveItinerary({ ...localItinerary, routePoints: newPoints });
                                                        }
                                                    }}
                                                    style={{ 
                                                    width: '24px', height: '24px', borderRadius: '50%', 
                                                    background: point.isCompleted ? 'var(--primary-orange)' : 'var(--bg-color)',
                                                    color: point.isCompleted ? 'white' : 'var(--text-muted)',
                                                    cursor: (isOwner && !isEditMode) ? 'pointer' : 'default',
                                                    fontSize: '12px', fontWeight: 900, display: 'flex', justifyContent: 'center', alignItems: 'center',
                                                    border: point.isCompleted ? 'none' : '1px solid var(--border-color)',
                                                    transition: 'all 0.2s'
                                                }}>
                                                    {point.isCompleted ? <Check size={14} strokeWidth={4} /> : (pIdx + 1)}
                                                </div>
                                                <div style={{ flex: 1 }}>
                                                    {isOwner && isEditMode ? (
                                                        <input 
                                                            defaultValue={point.label}
                                                            onBlur={(e) => renameStop(point.originalIndex, e.target.value)}
                                                            className="clean-input"
                                                            style={{ fontWeight: 800, color: 'var(--text-primary)', border: 'none', background: 'transparent', width: '100%', fontSize: '15px' }}
                                                        />
                                                    ) : (
                                                        <h4 style={{ fontWeight: 800, color: point.isCompleted ? 'var(--text-muted)' : 'var(--text-primary)', margin: 0, textDecoration: point.isCompleted ? 'line-through' : 'none' }}>{point.label}</h4>
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
                                    try {
                                        const res = await authFetch('/api/trips');
                                        if (res.ok) {
                                            const tripsData = await res.json();
                                            setMyTrips(tripsData);
                                            setShowAddModal(true);
                                        } else {
                                            throw new Error("Failed to load trips");
                                        }
                                    } catch (e) {
                                        handleClone(); // Fallback to direct clone
                                    }
                                }}
                            >
                                내 여행에 추가
                            </PrimaryButton>
                        )}
                        {isOwner && (
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', width: '100%' }}>
                                {isAllFinished && !localItinerary.stampImageUrl && (
                                    <PrimaryButton
                                        onClick={handleApplyStamp}
                                        style={{ background: 'var(--primary-gradient)', color: 'white', fontWeight: 900 }}
                                    >
                                        여정 완료 스탬프 찍기 ✈️
                                    </PrimaryButton>
                                )}
                                <div style={{ flex: 1, textAlign: 'center', color: 'var(--text-muted)', fontSize: '13px', fontWeight: 600 }}>
                                    {isAllFinished 
                                        ? "모든 일정을 완수하셨네요! 멋진 여행이었습니다. 😊"
                                        : "내 여권을 확인하거나 여행 피드에서 다른 영감을 찾아보세요! ✈️"}
                                </div>
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
                                    <div style={{ fontWeight: 800 }}>새로운 여행으로 만들기</div>
                                    <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>독립적인 새 여행 카드를 생성하고 일정을 추가합니다.</div>
                                </div>
                            </button>

                            <div style={{ margin: '20px 0', display: 'flex', alignItems: 'center', gap: '10px' }}>
                                <div style={{ flex: 1, height: '1px', background: 'var(--border-color)' }} />
                                <span style={{ fontSize: '11px', fontWeight: 900, color: 'var(--text-muted)' }}>OR ADD TO EXISTING</span>
                                <div style={{ flex: 1, height: '1px', background: 'var(--border-color)' }} />
                            </div>

                            <div style={{ marginBottom: '24px' }}>
                                <label style={{ fontSize: '11px', fontWeight: 900, color: 'var(--text-muted)', marginBottom: '6px', display: 'block' }}>여행 선택</label>
                                <select 
                                    value={selectedTripId}
                                    onChange={(e) => setSelectedTripId(e.target.value)}
                                    className="clean-input"
                                    style={{ width: '100%', background: 'var(--bg-color)', padding: '12px', borderRadius: '12px', border: '1px solid var(--border-color)' }}
                                >
                                    <option value="">여행을 선택해주세요</option>
                                    {myTrips.map(t => (
                                        <option key={t.id} value={t.id}>{t.title}</option>
                                    ))}
                                </select>
                            </div>

                            <div style={{ display: 'flex', gap: '10px' }}>
                                <button onClick={() => setShowAddModal(false)} className="clean-input" style={{ flex: 1, border: 'none', background: 'var(--bg-color)', cursor: 'pointer', fontWeight: 800, padding: '12px', borderRadius: '12px' }}>취소</button>
                                <PrimaryButton 
                                    onClick={handleAddToTrip} 
                                    disabled={!selectedTripId || actionLoading}
                                    style={{ flex: 1.5 }}
                                >
                                    {actionLoading ? '추가 중...' : '여행에 추가'}
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
