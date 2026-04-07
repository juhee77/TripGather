import React, { useState, useEffect, useRef } from 'react';
import { X, MapPin, ChevronRight, Trash2, Edit3, Navigation, CheckCircle, Camera, Check, Clock, Image as ImageIcon } from 'lucide-react';
import { useUser } from '../contexts/UserContext';
import { authFetch } from '../api/client';
import ModalHeader from './UI/ModalHeader';
import ModalFooter from './UI/ModalFooter';
import PrimaryButton from './UI/PrimaryButton';

const RouteDetailModal = ({ itinerary, onClose, onEdit, onDelete, onStepComplete }) => {
    const [isVisible, setIsVisible] = useState(false);
    const { user: currentUser } = useUser();
    
    // Mission Progress States
    const [localItinerary, setLocalItinerary] = useState(itinerary);
    const [checkingStepId, setCheckingStepId] = useState(null);
    const [memo, setMemo] = useState('');
    const [photoFile, setPhotoFile] = useState(null);
    const [previewUrl, setPreviewUrl] = useState(null);
    const [submittingStep, setSubmittingStep] = useState(false);
    
    const fileInputRef = useRef(null);

    // Identifiers
    const isMission = !!localItinerary.steps;
    const userMissionId = localItinerary.itineraryId ? localItinerary.id : (localItinerary.missionId || localItinerary.id);
    const isAuthor = currentUser && (currentUser.name === (localItinerary.author || localItinerary.itineraryAuthor));

    const formatDate = (dateStr) => {
        if (!dateStr) return '';
        const d = new Date(dateStr);
        return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    };

    useEffect(() => {
        setIsVisible(true);
    }, []);

    useEffect(() => {
        setLocalItinerary(itinerary);
    }, [itinerary]);

    const groupedByDay = (() => {
        const sourcePoints = localItinerary.steps || localItinerary.routePoints || [];
        const points = [...sourcePoints].sort((a, b) =>
            a.dayNumber !== b.dayNumber ? a.dayNumber - b.dayNumber : a.sequenceOrder - b.sequenceOrder
        );

        const map = {};
        points.forEach(p => {
            const key = p.dayNumber ?? 1;
            if (!map[key]) map[key] = { dayLabel: p.dayLabel || `Day ${key}`, points: [] };
            map[key].points.push(p);
        });
        return Object.values(map);
    })();

    const totalSteps = localItinerary.steps?.length || 0;
    const completedSteps = localItinerary.steps?.filter(s => s.isCompleted || s.completed)?.length || 0;
    const progressPercent = totalSteps === 0 ? 0 : Math.round((completedSteps / totalSteps) * 100);

    const handlePhotoChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setPhotoFile(file);
            const reader = new FileReader();
            reader.onloadend = () => setPreviewUrl(reader.result);
            reader.readAsDataURL(file);
        }
    };

    const handleCompleteStepInternal = async (stepId) => {
        if (submittingStep) return;
        setSubmittingStep(true);
        try {
            let finalPhotoUrl = (previewUrl && !photoFile) ? previewUrl : '';
            
            if (photoFile) {
                const fd = new FormData();
                fd.append('file', photoFile);
                const uploadRes = await authFetch('/api/files/upload', {
                    method: 'POST',
                    body: fd
                });
                if (uploadRes.ok) {
                    const data = await uploadRes.json();
                    finalPhotoUrl = data.url;
                } else {
                    throw new Error("Photo upload failed");
                }
            }

            const res = await authFetch(`/api/missions/${userMissionId}/steps/${stepId}/complete`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ memo, photoUrl: finalPhotoUrl })
            });

            if (res.ok) {
                const updatedStep = await res.json();
                setLocalItinerary(prev => ({
                    ...prev,
                    steps: prev.steps.map(s => s.id === stepId ? updatedStep : s)
                }));
                // Notify parent
                if (onStepComplete) onStepComplete(userMissionId, stepId, updatedStep);
                
                setCheckingStepId(null);
                setMemo('');
                setPhotoFile(null);
                setPreviewUrl(null);
            } else {
                const errorText = await res.text();
                alert(`인증 실패: ${errorText}`);
            }
        } catch (e) {
            console.error("Error completing step", e);
            alert("인증 처리 중 오류가 발생했습니다.");
        } finally {
            setSubmittingStep(false);
        }
    };

    return (
        <div className="modal-overlay">
            <div className={`modal-content animate-fade`} style={{ height: '94vh' }}>
                <ModalHeader 
                    title={localItinerary.title || localItinerary.itineraryTitle}
                    subtitle={isMission ? "MISSION IN PROGRESS" : "ITINERARY DETAILS"}
                    onClose={onClose}
                    actions={
                        isAuthor && !isMission && (
                            <button onClick={onEdit} className="icon-circle" style={{ width: '40px', height: '40px' }}>
                                <Edit3 size={18} color="var(--text-primary)" />
                            </button>
                        )
                    }
                />

                <div className="hide-scrollbar" style={{ flex: 1, overflowY: 'auto', padding: '24px' }}>
                    {/* Header Info Card */}
                    <div className="ticket-wrapper" style={{ padding: '24px', marginBottom: '32px', background: 'white' }}>
                        <div className="flex-between" style={{ marginBottom: '12px' }}>
                            <span className="label-orange">JOURNEY LOG</span>
                            <div className="status-pill">{isMission ? 'ACTIVE' : 'PLANNED'}</div>
                        </div>
                        <p className="text-s" style={{ color: 'var(--text-main)', fontSize: '15px', lineHeight: 1.6 }}>
                            {localItinerary.description || localItinerary.itinerary?.description}
                        </p>
                        
                        {isMission && (
                            <div style={{ marginTop: '24px' }}>
                                <div className="flex-between" style={{ marginBottom: '8px' }}>
                                    <span className="label-muted">MISSION PROGRESS</span>
                                    <span style={{ fontWeight: 900, color: 'var(--primary-orange)' }}>{progressPercent}%</span>
                                </div>
                                <div style={{ height: '8px', background: 'var(--bg-color)', borderRadius: '4px', overflow: 'hidden' }}>
                                    <div style={{ 
                                        width: `${progressPercent}%`, 
                                        height: '100%', 
                                        background: 'var(--primary-gradient)',
                                        transition: 'width 1s cubic-bezier(0.34, 1.56, 0.64, 1)'
                                    }} />
                                </div>
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
                                    }}>DAY {dIdx + 1}</div>
                                    <span style={{ fontWeight: 800, color: 'var(--text-primary)' }}>{day.dayLabel}</span>
                                </div>
                                <div style={{ flex: 1, height: '1px', background: 'var(--border-color)', marginLeft: '16px' }} />
                            </div>

                            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                                {day.points.map((point, pIdx) => {
                                    const isDone = point.isCompleted || point.completed;
                                    const isChecking = checkingStepId === point.id;

                                    return (
                                        <div key={pIdx} className="ticket-wrapper" style={{ 
                                            padding: '20px', 
                                            border: isDone ? '1px solid #A7F3D0' : '1px solid var(--border-color)',
                                            background: isDone ? '#F0FDF4' : 'white'
                                        }}>
                                            <div className="flex-between" style={{ marginBottom: '8px' }}>
                                                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                                    <div style={{ 
                                                        width: '24px', height: '24px', borderRadius: '50%', 
                                                        background: isDone ? '#10B981' : 'var(--bg-color)',
                                                        color: isDone ? 'white' : 'var(--text-muted)',
                                                        fontSize: '12px', fontWeight: 900, display: 'flex', justifyContent: 'center', alignItems: 'center'
                                                    }}>
                                                        {isDone ? <span className="success-animation"><Check size={14} /></span> : point.sequenceOrder}
                                                    </div>
                                                    <h4 style={{ fontWeight: 800, color: 'var(--text-primary)' }}>{point.label}</h4>
                                                </div>
                                                {isDone && (
                                                    <button 
                                                        onClick={() => setCheckingStepId(point.id)} 
                                                        style={{ background: 'none', color: 'var(--text-muted)' }}
                                                    >
                                                        <Edit3 size={14} />
                                                    </button>
                                                )}
                                            </div>

                                            {isDone && point.photoUrl && (
                                                <img src={point.photoUrl} alt="memory" style={{ 
                                                    width: '100%', height: '180px', objectFit: 'cover', 
                                                    borderRadius: '12px', marginTop: '12px', border: '1px solid var(--border-color)' 
                                                }} />
                                            )}

                                            {isDone && point.memo && (
                                                <div style={{ 
                                                    marginTop: '12px', padding: '12px', background: 'white', 
                                                    borderRadius: '12px', fontSize: '14px', borderLeft: '4px solid #10B981',
                                                    color: 'var(--text-main)', fontWeight: 500
                                                }}>
                                                    {point.memo}
                                                </div>
                                            )}

                                            {isMission && !isDone && !isChecking && (
                                                <button 
                                                    onClick={() => {
                                                        setCheckingStepId(point.id);
                                                        setMemo(point.memo || '');
                                                        setPreviewUrl(point.photoUrl || null);
                                                    }}
                                                    className="secondary-btn w-full" 
                                                    style={{ marginTop: '12px', height: '48px', fontSize: '13px' }}
                                                >
                                                    <Camera size={16} style={{ marginRight: '8px' }} /> 인증하고 스탬프 찍기
                                                </button>
                                            )}

                                            {isChecking && (
                                                <div style={{ marginTop: '16px', borderTop: '1px dashed var(--border-color)', paddingTop: '16px' }}>
                                                    <textarea 
                                                        value={memo} 
                                                        onChange={e => setMemo(e.target.value)} 
                                                        placeholder="이곳에서의 추억을 기록해보세요..." 
                                                        style={{ 
                                                            width: '100%', padding: '12px', borderRadius: '12px', 
                                                            background: 'var(--bg-color)', border: '1px solid var(--border-color)',
                                                            fontSize: '14px', minHeight: '100px', resize: 'none'
                                                        }} 
                                                    />
                                                    
                                                    <div style={{ marginTop: '12px' }}>
                                                        {previewUrl ? (
                                                            <div style={{ position: 'relative' }}>
                                                                <img src={previewUrl} style={{ width: '100%', height: '160px', objectFit: 'cover', borderRadius: '12px' }} alt="preview" />
                                                                <button 
                                                                    onClick={() => { setPhotoFile(null); setPreviewUrl(null); }} 
                                                                    style={{ position: 'absolute', top: '8px', right: '8px', background: 'rgba(0,0,0,0.5)', color: 'white', borderRadius: '50%', padding: '6px' }}
                                                                >
                                                                    <X size={16} />
                                                                </button>
                                                            </div>
                                                        ) : (
                                                            <button 
                                                                onClick={() => fileInputRef.current.click()} 
                                                                style={{ 
                                                                    width: '100%', height: '100px', borderRadius: '12px', 
                                                                    border: '2px dashed var(--border-color)', display: 'flex', flexDirection: 'column',
                                                                    justifyContent: 'center', alignItems: 'center', gap: '8px', color: 'var(--text-muted)'
                                                                }}
                                                            >
                                                                <ImageIcon size={24} />
                                                                <span style={{ fontSize: '13px', fontWeight: 700 }}>사진 첨부 (옵션)</span>
                                                            </button>
                                                        )}
                                                        <input type="file" ref={fileInputRef} style={{ display: 'none' }} accept="image/*" onChange={handlePhotoChange} />
                                                    </div>

                                                    <div style={{ display: 'flex', gap: '10px', marginTop: '16px' }}>
                                                        <button onClick={() => setCheckingStepId(null)} className="secondary-btn" style={{ flex: 1 }}>취소</button>
                                                        <PrimaryButton 
                                                            onClick={() => handleCompleteStepInternal(point.id)} 
                                                            disabled={submittingStep}
                                                            style={{ flex: 2 }}
                                                        >
                                                            {submittingStep ? '처리 중...' : '인증 완료'}
                                                        </PrimaryButton>
                                                    </div>
                                                </div>
                                            )}
                                        </div>
                                    );
                                })}
                            </div>
                        </div>
                    ))}
                </div>

                <ModalFooter>
                    {isMission ? (
                        <div className="flex-between w-full">
                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: 'var(--text-muted)' }}>
                                <Clock size={16} />
                                <span style={{ fontSize: '13px', fontWeight: 600 }}>{progressPercent}% Completed</span>
                            </div>
                            <PrimaryButton 
                                disabled={progressPercent < 100}
                                onClick={async () => {
                                    const res = await authFetch(`/api/missions/complete/${userMissionId}`, { method: 'POST' });
                                    if (res.ok) {
                                        alert("축하합니다! 여행의 모든 미션을 완료했습니다. 🏆");
                                        onClose();
                                    }
                                }}
                            >
                                미션 최종 완료
                            </PrimaryButton>
                        </div>
                    ) : (
                        <PrimaryButton 
                            onClick={async () => {
                                const res = await authFetch(`/api/missions/start/${localItinerary.id}`, { method: 'POST' });
                                if (res.ok) {
                                    alert("챌린지가 시작되었습니다! 나의 미션 탭에서 확인하세요. 🚀");
                                    onClose();
                                }
                            }}
                        >
                            챌린지 참여하기
                        </PrimaryButton>
                    )}
                </ModalFooter>
            </div>
        </div>
    );
};

export default RouteDetailModal;
