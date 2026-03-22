import React, { useState, useEffect, useRef } from 'react';
import { X, MapPin, ChevronRight, Trash2, Edit3, Navigation, CheckCircle, Camera, Check } from 'lucide-react';
import { useUser } from '../contexts/UserContext';
import { authFetch } from '../api/client';

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

    const isAuthor = currentUser && (currentUser.name === localItinerary.author);
    const isMission = !!localItinerary.steps;

    useEffect(() => {
        setIsVisible(true);
    }, []);

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
    const completedSteps = localItinerary.steps?.filter(s => s.isCompleted)?.length || 0;
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
                const uploadRes = await authFetch('http://localhost:8080/api/files/upload', {
                    method: 'POST',
                    body: fd
                });
                if (uploadRes.ok) {
                    const data = await uploadRes.json();
                    finalPhotoUrl = data.url;
                }
            }

            const res = await authFetch(`http://localhost:8080/api/missions/${localItinerary.id}/steps/${stepId}/complete`, {
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

    return (
        <div style={{
            position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
            backgroundColor: 'rgba(0,0,0,0.85)', backdropFilter: 'blur(20px)', zIndex: 2000,
            display: 'flex', justifyContent: 'center', alignItems: 'center'
        }}>
            <div className="glass-dark" style={{
                width: '100%', maxWidth: '480px', height: '100vh',
                position: 'relative', display: 'flex', flexDirection: 'column',
                transition: 'transform 0.5s cubic-bezier(0.16, 1, 0.3, 1)',
                transform: isVisible ? 'translateY(0)' : 'translateY(100%)',
                color: 'white', overflow: 'hidden',
                border: 'none',
                borderRadius: 'var(--radius-lg) var(--radius-lg) 0 0'
            }}>
                <header style={{
                    padding: '20px 24px',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    borderBottom: '1px solid rgba(255,255,255,0.1)',
                    background: 'rgba(13, 13, 25, 0.5)',
                    backdropFilter: 'blur(10px)'
                }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                        <div style={{ 
                            width: '40px', height: '40px', borderRadius: 'var(--radius-full)', 
                            background: 'var(--primary-gradient)', padding: '2px',
                            boxShadow: '0 0 15px rgba(255, 92, 0, 0.3)'
                        }}>
                            <div style={{ width: '100%', height: '100%', borderRadius: 'var(--radius-full)', background: '#0D0D19', overflow: 'hidden' }}>
                                <img src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${localItinerary.author}`} alt="author" style={{ width: '100%', height: '100%' }} />
                            </div>
                        </div>
                        <div>
                            <div style={{ fontSize: '15px', fontWeight: 800 }}>{localItinerary.author}</div>
                            <div style={{ fontSize: '11px', color: 'rgba(255,255,255,0.5)', fontWeight: 600 }}>TRAVEL LOG • JOURNEY</div>
                        </div>
                    </div>
                    <button onClick={onClose} className="icon-circle glass" style={{ width: '40px', height: '40px', background: 'rgba(255,255,255,0.1)' }}>
                        <X size={20} color="white" />
                    </button>
                </header>

                <div className="hide-scrollbar" style={{ flex: 1, overflowY: 'auto', padding: '32px 24px' }}>
                    <div style={{ marginBottom: '32px', animation: 'fadeIn 0.6s ease-out' }}>
                        <h1 className="heading-l" style={{ 
                            marginBottom: '12px', background: 'linear-gradient(to bottom, #FFFFFF 0%, #A5B4FC 100%)', 
                            WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent', lineHeight: 1.2
                        }}>
                            {localItinerary.title}
                        </h1>
                        <p className="text-s" style={{ color: 'rgba(255,255,255,0.6)', lineHeight: '1.7', fontSize: '15px' }}>
                            {localItinerary.description}
                        </p>
                    </div>

                    {isMission && (
                        <div style={{ marginBottom: '40px', background: 'rgba(255,255,255,0.03)', padding: '20px', borderRadius: 'var(--radius-lg)', border: '1px solid rgba(255,255,255,0.05)' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px' }}>
                                <span style={{ fontWeight: 800, color: 'var(--primary-orange)', fontSize: '14px' }}>MISSION PROGRESS</span>
                                <span style={{ fontWeight: 800, color: 'white', fontSize: '14px' }}>{progressPercent}%</span>
                            </div>
                            <div style={{ width: '100%', height: '8px', background: 'rgba(255,255,255,0.1)', borderRadius: '4px', overflow: 'hidden' }}>
                                <div style={{ width: `${progressPercent}%`, height: '100%', background: 'var(--primary-gradient)', transition: 'width 0.5s ease-out' }} />
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
                                    const isStepCompleted = point.isCompleted === true;
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
                                            border: isStepCompleted ? '1px solid rgba(81, 207, 102, 0.3)' : '1px solid rgba(255,255,255,0.08)',
                                            background: isStepCompleted ? 'rgba(81, 207, 102, 0.05)' : 'rgba(255,255,255,0.03)'
                                        }}>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                                                <span style={{ fontSize: '10px', color: isStepCompleted ? '#51CF66' : 'var(--primary-orange)', fontWeight: 900, letterSpacing: '1px' }}>
                                                    {isStepCompleted ? `CHECKED IN AT ${new Date(point.completedAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}` : `STOP ${point.sequenceOrder}`}
                                                </span>
                                                {isStepCompleted ? <CheckCircle size={14} color="#51CF66" /> : <Navigation size={14} color="rgba(255,255,255,0.3)" />}
                                            </div>
                                            <h4 style={{ fontSize: '17px', fontWeight: 700, color: 'white', marginBottom: '6px' }}>
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

                                            {isMission && !isStepCompleted && !isCurrentlyChecking && (
                                                <button onClick={() => setCheckingStepId(point.id)} className="glass" style={{ marginTop: '16px', width: '100%', padding: '12px', borderRadius: '12px', color: 'white', fontSize: '13px', fontWeight: 700, display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px', border: '1px dashed rgba(255,255,255,0.2)' }}>
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
                                                        <button disabled={submittingStep} onClick={() => setCheckingStepId(null)} style={{ flex: 1, padding: '12px', borderRadius: '8px', background: 'rgba(255,255,255,0.05)', color: 'white', border: 'none', fontWeight: 600 }}>CANCEL</button>
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

                <footer className="glass" style={{ padding: '24px', borderTop: '1px solid rgba(255,255,255,0.1)', display: 'flex', flexDirection: 'column', gap: '16px', background: 'rgba(13, 13, 25, 0.8)' }}>
                    {isAuthor && !isMission && (
                        <div style={{ display: 'flex', gap: '14px' }}>
                            <button onClick={onEdit} className="glass" style={{ flex: 1, height: '60px', borderRadius: 'var(--radius-md)', color: 'white', fontWeight: 800, fontSize: '15px', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '10px', border: '1px solid rgba(255,255,255,0.2)' }}>
                                <Edit3 size={18} /> EDIT JOURNEY
                            </button>
                            <button onClick={onDelete} className="glass" style={{ width: '60px', height: '60px', borderRadius: 'var(--radius-md)', display: 'flex', justifyContent: 'center', alignItems: 'center', border: '1px solid rgba(239, 68, 68, 0.2)', background: 'rgba(239, 68, 68, 0.05)' }}>
                                <Trash2 size={20} color="#EF4444" />
                            </button>
                        </div>
                    )}
                    {(isMission && progressPercent < 100) ? (
                        <button disabled className="primary-btn" style={{ width: '100%', height: '60px', borderRadius: 'var(--radius-md)', fontSize: '15px', gap: '12px', background: 'rgba(255,255,255,0.1)', color: 'rgba(255,255,255,0.4)', boxShadow: 'none' }}>
                            COMPLETE ALL STOPS FIRST
                        </button>
                    ) : isMission ? (
                        <button onClick={async () => {
                            try {
                                const res = await authFetch(`http://localhost:8080/api/missions/complete/${localItinerary.id}`, { method: 'POST' });
                                if (res.ok) alert("Mission Completed! You can view your history in MyPage.");
                            } catch (e) {
                                console.error("Could not complete mission:", e);
                            }
                            onClose();
                        }} className="primary-btn" style={{ width: '100%', height: '60px', borderRadius: 'var(--radius-md)', fontSize: '17px', gap: '12px' }}>
                            FINALIZE MISSION <ChevronRight size={20} />
                        </button>
                    ) : null}
                </footer>
            </div>
        </div>
    );
};

export default RouteDetailModal;
