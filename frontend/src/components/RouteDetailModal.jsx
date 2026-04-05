import React, { useState, useEffect, useRef } from 'react';
import { X, MapPin, ChevronRight, Trash2, Edit3, Navigation, CheckCircle, Camera, Check } from 'lucide-react';
import { useUser } from '../contexts/UserContext';
import { authFetch } from '../api/client';
import ModalHeader from './UI/ModalHeader';
import ModalFooter from './UI/ModalFooter';
import PrimaryButton from './UI/PrimaryButton';
import FormInput from './UI/FormInput';

const RouteDetailModal = ({ itinerary, onClose, onEdit, onDelete }) => {
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

    const isAuthor = currentUser && (currentUser.name === (localItinerary.author || localItinerary.itineraryAuthor));
    const isMission = !!localItinerary.steps;

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
        const points = sourcePoints.length > 0
            ? [...sourcePoints].sort((a, b) =>
                a.dayNumber !== b.dayNumber ? a.dayNumber - b.dayNumber : a.sequenceOrder - b.sequenceOrder
              )
            : [];

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

    const handleCompleteStep = async (stepId) => {
        setSubmittingStep(true);
        try {
            let finalPhotoUrl = '';
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
                }
            }

            const targetMissionId = localItinerary.itineraryId ? localItinerary.id : (localItinerary.missionId || localItinerary.id);
            // If we have a previewUrl but no photoFile, it means we're reusing the existing photo
            const finalPhotoUrlToSave = (previewUrl && !photoFile) ? previewUrl : finalPhotoUrl;

            const res = await authFetch(`/api/missions/${targetMissionId}/steps/${stepId}/complete`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ memo, photoUrl: finalPhotoUrlToSave })
            });

            if (res.ok) {
                const updatedStep = await res.json();
                setLocalItinerary(prev => ({
                    ...prev,
                    steps: prev.steps.map(s => s.id === stepId ? updatedStep : s)
                }));
                setCheckingStepId(null);
                setMemo('');
                setPhotoFile(null);
                setPreviewUrl(null);
            } else {
                alert("Failed to complete step");
            }
        } catch (e) {
            console.error("Error completing step", e);
        } finally {
            setSubmittingStep(false);
        }
    };

    const handleLeaveRequest = async () => {
        if (!window.confirm("정말로 이 미션을 중단하고 나가시겠습니까? 호스트의 승인 후 최종 탈퇴 처리됩니다.")) return;
        try {
            const targetMissionId = localItinerary.itineraryId ? localItinerary.id : (localItinerary.missionId || localItinerary.id);
            const res = await authFetch(`/api/missions/${targetMissionId}/leave/request`, {
                method: 'POST'
            });
            if (res.ok) {
                alert("탈퇴 요청을 보냈습니다. 호스트가 승인하면 목록에서 사라집니다.");
                onClose();
            } else {
                alert("탈퇴 요청에 실패했습니다.");
            }
        } catch (e) {
            console.error("Error requesting leave", e);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content-night" style={{ 
                height: '100vh',
                borderRadius: 'var(--radius-lg) var(--radius-lg) 0 0'
            }}>
                <ModalHeader 
                    title={localItinerary.author || localItinerary.itineraryAuthor}
                    subtitle="TRAVEL LOG • JOURNEY"
                    onClose={onClose}
                    dark
                    actions={isAuthor ? (
                        <>
                            {!isMission && (
                                <button onClick={onEdit} className="icon-circle glass" style={{ width: '40px', height: '40px' }}>
                                    <Edit3 size={18} color="white" />
                                </button>
                            )}
                            <button onClick={onDelete} className="icon-circle glass" style={{ width: '40px', height: '40px', background: 'rgba(255,107,107,0.1)' }}>
                                <Trash2 size={18} color="#FF6B6B" />
                            </button>
                        </>
                    ) : isMission && (
                        <button onClick={handleLeaveRequest} title="참여 취소 요청" className="icon-circle glass" style={{ width: '40px', height: '40px', background: 'rgba(255,107,107,0.1)' }}>
                            <Trash2 size={18} color="#FF6B6B" />
                        </button>
                    )}
                />

                <div className="hide-scrollbar" style={{ flex: 1, overflowY: 'auto', padding: '32px 24px' }}>
                    <div style={{ marginBottom: '32px', animation: 'fadeIn 0.6s ease-out' }}>
                        <h1 className="heading-l" style={{ 
                            marginBottom: '12px', background: 'linear-gradient(to bottom, #FFFFFF 0%, #A5B4FC 100%)', 
                            WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent', lineHeight: 1.2
                        }}>
                            {localItinerary.title || localItinerary.itineraryTitle}
                        </h1>
                        <p className="text-s" style={{ color: 'rgba(255,255,255,0.85)', lineHeight: '1.7', fontSize: '15px' }}>
                            {localItinerary.description || localItinerary.itinerary?.description}
                        </p>
                    </div>

                    {isMission && (
                        <div style={{ marginBottom: '40px', background: 'rgba(255,255,255,0.03)', padding: '24px', borderRadius: 'var(--radius-lg)', border: '1px solid rgba(255, 92, 0, 0.2)', position: 'relative', overflow: 'hidden' }}>
                            {/* Background glow for progress bar component */}
                            <div style={{ position: 'absolute', top: 0, left: '-50%', width: '200%', height: '100%', background: 'radial-gradient(circle at top right, rgba(255, 92, 0, 0.1) 0%, transparent 60%)', zIndex: 0 }} />
                            
                            <div style={{ position: 'relative', zIndex: 1 }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px', alignItems: 'flex-end' }}>
                                    <div>
                                        <h3 style={{ fontWeight: 900, color: 'var(--primary-orange)', fontSize: '16px', letterSpacing: '1px', marginBottom: '4px' }}>MISSION PROGRESS</h3>
                                        <p style={{ fontSize: '13px', color: 'rgba(255,255,255,0.6)', fontWeight: 600 }}>{completedSteps} / {totalSteps} Stops Completed</p>
                                    </div>
                                    <span style={{ fontWeight: 900, color: progressPercent >= 100 ? '#51CF66' : 'white', fontSize: '24px' }}>{progressPercent}%</span>
                                </div>
                                <div style={{ width: '100%', height: '10px', background: 'rgba(0,0,0,0.5)', borderRadius: '5px', overflow: 'hidden', boxShadow: 'inset 0 1px 3px rgba(0,0,0,0.5)', border: '1px solid rgba(255,255,255,0.05)' }}>
                                    <div style={{ 
                                        width: `${progressPercent}%`, 
                                        height: '100%', 
                                        background: progressPercent >= 100 ? '#51CF66' : 'var(--primary-gradient)', 
                                        transition: 'width 0.8s cubic-bezier(0.34, 1.56, 0.64, 1)', 
                                        boxShadow: progressPercent >= 100 ? '0 0 15px rgba(81, 207, 102, 0.5)' : '0 0 10px rgba(255, 92, 0, 0.5)' 
                                    }} />
                                </div>
                            </div>
                        </div>
                    )}

                    {groupedByDay.map((day, dayIdx) => (
                        <div key={dayIdx} style={{ marginBottom: '48px', animation: `fadeIn 0.5s ease-out forwards ${dayIdx * 0.15}s`, opacity: 0 }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '24px' }}>
                                <div style={{
                                    background: 'var(--primary-gradient)', borderRadius: '12px', padding: '6px 16px',
                                    fontSize: '12px', fontWeight: 900, letterSpacing: '1px', color: 'white',
                                    boxShadow: '0 8px 20px rgba(255, 92, 0, 0.2)'
                                }}>
                                    DAY {dayIdx + 1}
                                </div>
                                <span style={{ color: 'white', fontSize: '15px', fontWeight: 700 }}>{day.dayLabel}</span>
                                <div style={{ flex: 1, height: '1px', background: 'rgba(255,255,255,0.1)' }} />
                            </div>

                            <div style={{ position: 'relative', paddingLeft: '40px' }}>
                                <div style={{
                                    position: 'absolute', left: '11px', top: '12px', bottom: '12px',
                                    width: '2px', background: 'linear-gradient(to bottom, var(--primary-orange), var(--secondary-purple), transparent)'
                                }} />

                                {day.points.map((point, ptIdx) => {
                                    const isStepCompleted = point.isCompleted === true || point.completed === true;
                                    const isCurrentlyChecking = checkingStepId === point.id;
                                    
                                    return (
                                    <div key={ptIdx} style={{ position: 'relative', marginBottom: '20px', animation: `fadeIn 0.4s ease-out forwards ${(dayIdx * 0.1) + (ptIdx * 0.1)}s`, opacity: 0 }}>
                                        <div style={{
                                            position: 'absolute', left: '-35px', top: '18px',
                                            width: '12px', height: '12px', borderRadius: '50%',
                                            background: isStepCompleted ? 'var(--primary-orange)' : (ptIdx === 0 ? 'white' : 'transparent'),
                                            border: '3px solid var(--primary-orange)',
                                            boxShadow: isStepCompleted || ptIdx === 0 ? '0 0 15px var(--primary-orange)' : 'none',
                                            zIndex: 2, transition: 'all 0.3s ease'
                                        }} />

                                        <div className="glass-dark" style={{
                                            padding: '20px', borderRadius: 'var(--radius-md)',
                                            border: isStepCompleted ? '2px solid rgba(81, 207, 102, 0.4)' : '1px solid rgba(255,255,255,0.08)',
                                            background: isStepCompleted ? 'linear-gradient(145deg, rgba(81, 207, 102, 0.05) 0%, rgba(255,255,255,0.02) 100%)' : 'rgba(255,255,255,0.03)',
                                            position: 'relative',
                                            overflow: 'hidden'
                                        }}>
                                            {/* Stamp Overlay */}
                                            {isStepCompleted && (
                                                <div style={{
                                                    position: 'absolute', top: '10px', right: '10px', 
                                                    width: '80px', height: '80px', 
                                                    border: '4px solid rgba(81, 207, 102, 0.2)', borderRadius: '50%',
                                                    display: 'flex', justifyContent: 'center', alignItems: 'center',
                                                    transform: 'rotate(-15deg)', zIndex: 5, pointerEvents: 'none'
                                                }}>
                                                    <div style={{
                                                        color: 'rgba(81, 207, 102, 0.4)', fontWeight: 900, fontSize: '14px', 
                                                        letterSpacing: '2px', transform: 'rotate(-10deg)'
                                                    }}>
                                                        CLEAR
                                                    </div>
                                                </div>
                                            )}

                                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px', position: 'relative', zIndex: 10 }}>
                                                <span style={{ fontSize: '11px', color: isStepCompleted ? '#51CF66' : 'var(--primary-orange)', fontWeight: 900, letterSpacing: '1px' }}>
                                                    {isStepCompleted ? `MISSION ACCOMPLISHED` : `STOP ${point.sequenceOrder}`}
                                                </span>
                                                <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                                                    {isStepCompleted && (
                                                        <button 
                                                            onClick={(e) => {
                                                                e.stopPropagation();
                                                                setCheckingStepId(point.id);
                                                                setMemo(point.memo || '');
                                                                setPreviewUrl(point.photoUrl || null);
                                                                // Note: we don't set photoFile here because we only have the URL
                                                            }}
                                                            style={{ background: 'none', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center' }}
                                                            title="수정하기"
                                                        >
                                                            <Edit3 size={14} color="rgba(255,255,255,0.5)" />
                                                        </button>
                                                    )}
                                                    {isStepCompleted ? <CheckCircle size={16} color="#51CF66" /> : <Navigation size={14} color="rgba(255,255,255,0.3)" />}
                                                </div>
                                            </div>
                                            <h4 style={{ fontSize: '17px', fontWeight: 800, color: 'white', marginBottom: '6px', position: 'relative', zIndex: 10 }}>
                                                {point.label}
                                            </h4>
                                            
                                            {isStepCompleted && point.photoUrl && (
                                                <img src={point.photoUrl} alt="memory" style={{ width: '100%', height: '160px', objectFit: 'cover', borderRadius: '12px', marginTop: '12px', border: '1px solid rgba(255,255,255,0.1)' }} />
                                            )}
                                            {isStepCompleted && point.memo && (
                                                <p style={{ marginTop: '12px', fontSize: '14px', color: 'rgba(255,255,255,0.8)', background: 'rgba(0,0,0,0.3)', padding: '12px', borderRadius: '12px', borderLeft: '3px solid #51CF66' }}>
                                                    {point.memo}
                                                </p>
                                            )}
                                            
                                            {isStepCompleted && point.completedAt && (
                                                <div style={{ 
                                                    marginTop: '12px', 
                                                    fontSize: '11px', 
                                                    color: 'rgba(81, 207, 102, 0.6)', 
                                                    fontWeight: 700,
                                                    display: 'flex',
                                                    alignItems: 'center',
                                                    gap: '4px',
                                                    padding: '4px 8px',
                                                    background: 'rgba(81, 207, 102, 0.05)',
                                                    borderRadius: '6px',
                                                    width: 'fit-content'
                                                }}>
                                                    <Clock size={10} />
                                                    COMPLETED AT {formatDate(point.completedAt)}
                                                </div>
                                            )}

                                            {isMission && !isStepCompleted && !isCurrentlyChecking && (
                                                <button onClick={() => {
                                                    setCheckingStepId(point.id);
                                                    setMemo('');
                                                    setPreviewUrl(null);
                                                    setPhotoFile(null);
                                                }} className="glass" style={{ marginTop: '16px', width: '100%', padding: '12px', borderRadius: '12px', color: 'white', fontSize: '13px', fontWeight: 700, display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px', border: '1px dashed rgba(255,255,255,0.2)' }}>
                                                    <CheckCircle size={16} /> MARK AS REACHED
                                                </button>
                                            )}

                                            {isCurrentlyChecking && (
                                                <div style={{ marginTop: '16px', background: 'rgba(0,0,0,0.4)', padding: '16px', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.1)' }}>
                                                    <textarea value={memo} onChange={e => setMemo(e.target.value)} placeholder="Add a quick memory or note..." style={{ width: '100%', padding: '12px', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', color: 'white', border: '1px solid rgba(255,255,255,0.1)', minHeight: '80px', fontSize: '14px', marginBottom: '12px', resize: 'none' }} />
                                                    
                                                    {previewUrl ? (
                                                        <div style={{ position: 'relative', marginBottom: '12px' }}>
                                                            <img src={previewUrl} style={{ width: '100%', height: '140px', objectFit: 'cover', borderRadius: '8px' }} alt="preview" />
                                                            <button onClick={() => { setPhotoFile(null); setPreviewUrl(null); }} style={{ position: 'absolute', top: '8px', right: '8px', background: 'rgba(0,0,0,0.6)', color: 'white', border: 'none', borderRadius: '50%', padding: '4px', cursor: 'pointer' }}><X size={14} /></button>
                                                        </div>
                                                    ) : (
                                                        <button onClick={() => fileInputRef.current.click()} style={{ width: '100%', padding: '12px', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', color: 'var(--primary-orange)', border: '1px dashed rgba(255,255,255,0.2)', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px', marginBottom: '12px', cursor: 'pointer', fontWeight: 600 }}>
                                                            <Camera size={16} /> ATTACH PHOTO
                                                        </button>
                                                    )}
                                                    <input type="file" ref={fileInputRef} style={{ display: 'none' }} accept="image/*" onChange={handlePhotoChange} />

                                                    <div style={{ display: 'flex', gap: '10px' }}>
                                                        <button disabled={submittingStep} onClick={() => {
                                                            setCheckingStepId(null);
                                                            setMemo('');
                                                            setPhotoFile(null);
                                                            setPreviewUrl(null);
                                                        }} style={{ flex: 1, padding: '12px', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', color: 'white', border: 'none', fontWeight: 600 }}>CANCEL</button>
                                                        <button disabled={submittingStep} onClick={() => handleCompleteStep(point.id)} style={{ flex: 1, padding: '12px', borderRadius: '8px', background: '#51CF66', color: 'black', border: 'none', fontWeight: 800, display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '6px' }}>
                                                            {submittingStep ? 'SAVING...' : <><Check size={16} /> CONFIRM</>}
                                                        </button>
                                                    </div>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                    );
                                })}
                            </div>
                        </div>
                    ))}
                </div>

                <ModalFooter dark>
                    {isAuthor && !isMission ? (
                        <div style={{ display: 'flex', gap: '14px' }}>
                            <PrimaryButton onClick={onEdit} style={{ flex: 1, background: 'rgba(255,255,255,0.05)', border: '1px solid var(--night-border)' }}>
                                <Edit3 size={18} style={{ marginRight: '8px' }} /> EDIT JOURNEY
                            </PrimaryButton>
                            <button onClick={onDelete} className="glass" style={{ width: '60px', height: '60px', borderRadius: '16px', display: 'flex', justifyContent: 'center', alignItems: 'center', border: '1px solid rgba(239, 68, 68, 0.2)', background: 'rgba(239, 68, 68, 0.05)' }}>
                                <Trash2 size={20} color="#EF4444" />
                            </button>
                        </div>
                    ) : isMission ? (
                        progressPercent < 100 ? (
                            <PrimaryButton disabled style={{ background: 'rgba(255,255,255,0.05)', color: 'var(--night-text-muted)' }}>
                                COMPLETE ALL STOPS FIRST
                            </PrimaryButton>
                        ) : (
                            <PrimaryButton onClick={async () => {
                                try {
                                    const res = await authFetch(`/api/missions/complete/${localItinerary.id}`, { method: 'POST' });
                                    if (res.ok) {
                                        alert("축하합니다! 미션을 모두 완료하여 완료 처리되었습니다. 🏆✨");
                                        onClose();
                                    } else {
                                        const err = await res.text();
                                        alert("실패: " + err);
                                    }
                                } catch (e) {
                                    console.error("Could not complete mission:", e);
                                }
                            }} style={{ background: '#51CF66', color: '#0D0D19' }}>
                                🏆 미션 최종 완료하기 <ChevronRight size={20} />
                            </PrimaryButton>
                        )
                    ) : (
                        <PrimaryButton 
                            onClick={async () => {
                                try {
                                    const res = await authFetch(`/api/missions/start/${localItinerary.id}`, { method: 'POST' });
                                    if (res.ok) {
                                        const missionData = await res.json();
                                        setLocalItinerary(missionData);
                                    }
                                } catch (e) {
                                    console.error("Could not start mission from modal:", e);
                                }
                            }}
                        >
                            🚀 이 챌린지에 참여하기
                        </PrimaryButton>
                    )}
                </ModalFooter>
            </div>
        </div>
    );
};

export default RouteDetailModal;
