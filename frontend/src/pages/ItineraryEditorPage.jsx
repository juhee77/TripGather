import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { X, Type, FileText, Send, Plus, Trash2, MapPin, Clock, ChevronLeft, Plane } from 'lucide-react';
import { authFetch } from '../api/client';
import { useUser } from '../contexts/UserContext';
import FormInput from '../components/UI/FormInput';
import PrimaryButton from '../components/UI/PrimaryButton';
import stampPlaceholder from '../assets/stamp-placeholder.png';

const ItineraryEditorPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { user: currentUser } = useUser();
    const isEdit = !!id;
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        stampImageUrl: '',
        routePoints: []
    });
    const [saving, setSaving] = useState(false);
    const [stampPreview, setStampPreview] = useState(null);
    const stampInputRef = React.useRef(null);

    useEffect(() => {
        if (id) {
            const fetchItinerary = async () => {
                try {
                    const res = await authFetch(`/api/itineraries/${id}`);
                    if (res.ok) {
                        const itinerary = await res.json();
                        setFormData({
                            title: itinerary.title || '',
                            description: itinerary.description || '',
                            stampImageUrl: itinerary.stampImageUrl || '',
                            routePoints: itinerary.routePoints ? [...itinerary.routePoints].sort((a, b) => 
                                a.dayNumber !== b.dayNumber ? a.dayNumber - b.dayNumber : a.sequenceOrder - b.sequenceOrder
                            ) : []
                        });
                        if (itinerary.stampImageUrl) setStampPreview(itinerary.stampImageUrl);
                    }
                } catch (err) {
                    console.error("Failed to load itinerary for edit", err);
                }
            };
            fetchItinerary();
        }
    }, [id]);

    const handleStampUpload = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        const reader = new FileReader();
        reader.onloadend = () => setStampPreview(reader.result);
        reader.readAsDataURL(file);

        try {
            const fd = new FormData();
            fd.append('file', file);
            const res = await authFetch('/api/files/upload', {
                method: 'POST',
                body: fd
            });
            if (res.ok) {
                const data = await res.json();
                setFormData(prev => ({ ...prev, stampImageUrl: data.url }));
            }
        } catch (err) {
            console.error("Stamp upload failed", err);
        }
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const addPoint = (dayNum) => {
        const pointsInDay = formData.routePoints.filter(p => (p.dayNumber || 1) === dayNum);
        const nextOrder = pointsInDay.length + 1;
        
        const newPoint = {
            dayNumber: dayNum,
            dayLabel: `Day ${dayNum}`,
            sequenceOrder: nextOrder,
            label: '',
            visitTime: '12:00',
            lat: 37.5665,
            lng: 126.9780
        };
        
        setFormData(prev => ({
            ...prev,
            routePoints: [...prev.routePoints, newPoint]
        }));
    };

    const removePoint = (index) => {
        setFormData(prev => {
            const newPoints = prev.routePoints.filter((_, i) => i !== index);
            return { ...prev, routePoints: newPoints };
        });
    };

    const updatePoint = (index, field, value) => {
        setFormData(prev => {
            const newPoints = [...prev.routePoints];
            newPoints[index] = { ...newPoints[index], [field]: value };
            return { ...prev, routePoints: newPoints };
        });
    };

    const addDay = () => {
        const maxDay = formData.routePoints.reduce((max, p) => Math.max(max, p.dayNumber || 1), 0);
        addPoint(maxDay + 1);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!formData.routePoints || formData.routePoints.length === 0) {
            alert('최소 한 개의 비행 계획(Stops)을 추가해야 합니다. 여행 경로를 완성해 주세요! ✈️');
            return;
        }
        setSaving(true);
        try {
            const payload = {
                ...formData,
                author: isEdit ? undefined : currentUser?.name,
                authorEmail: isEdit ? undefined : currentUser?.email
            };
            const url = isEdit ? `/api/itineraries/${id}` : '/api/itineraries';
            const method = isEdit ? 'PATCH' : 'POST';
            const response = await authFetch(url, {
                method,
                body: JSON.stringify(payload),
            });
            if (response.ok) {
                navigate(-1);
            } else {
                const errText = await response.text();
                alert(`저장에 실패했습니다: ${errText}`);
            }
        } catch (error) {
            console.error("Error saving itinerary:", error);
            alert(`네트워크 오류: ${error.message}`);
        } finally {
            setSaving(false);
        }
    };

    const days = Array.from(new Set(formData.routePoints.map(p => p.dayNumber || 1))).sort((a, b) => a - b);
    if (days.length === 0) days.push(1);

    return (
        <div className="animate-fade" style={{ background: 'var(--bg-lite)', minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
            <header style={{ 
                display: 'flex', alignItems: 'center', padding: '20px', gap: '16px', background: 'white', 
                position: 'sticky', top: 0, zIndex: 100, borderBottom: '1px solid var(--border-color)',
                borderRadius: '0 0 24px 24px'
            }}>
                <button type="button" onClick={() => navigate(-1)} className="icon-circle" style={{ width: '40px', height: '40px', padding: 0 }}>
                  <ChevronLeft size={24} color="var(--text-primary)" />
                </button>
                <h1 className="heading-m" style={{ margin: 0, fontSize: '20px' }}>{isEdit ? 'EDIT VOYAGE ✈️' : 'NEW CHECK-IN 🎫'}</h1>
            </header>

            <form onSubmit={handleSubmit} className="hide-scrollbar" style={{ flex: 1, overflowY: 'auto', padding: '32px 24px', display: 'flex', flexDirection: 'column', gap: '32px' }}>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
                        <FormInput 
                            label="VOYAGE TITLE"
                            icon={Type}
                            name="title"
                            value={formData.title}
                            onChange={handleChange}
                            placeholder="Enter your trip title..."
                        />

                        <FormInput 
                            label="INTEL & INTENTION"
                            icon={FileText}
                            name="description"
                            value={formData.description}
                            onChange={handleChange}
                            placeholder="Briefly describe the journey..."
                            as="textarea"
                            style={{ height: '120px', resize: 'none', lineHeight: '1.6' }}
                        />

                        <div>
                            <label style={{ fontSize: '13px', fontWeight: 800, color: 'var(--text-secondary)', marginBottom: '8px', display: 'block' }}>STAMP DESIGN (REWARD)</label>
                            <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
                                <div 
                                    onClick={() => stampInputRef.current.click()}
                                    style={{ 
                                        width: '80px', height: '80px', borderRadius: '16px', background: 'var(--bg-color)', border: '2px dashed var(--border-color)',
                                        display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', overflow: 'hidden', position: 'relative'
                                    }}
                                >
                                    {stampPreview ? (
                                        <img src={stampPreview} alt="stamp preview" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                                    ) : (
                                        <img src={stampPlaceholder} alt="default stamp" style={{ width: '100%', height: '100%', objectFit: 'cover', opacity: 0.5 }} />
                                    )}
                                </div>
                                <div style={{ flex: 1 }}>
                                    <p style={{ fontSize: '13px', color: 'var(--text-muted)', marginBottom: '8px' }}>
                                        {stampPreview ? 'Custom stamp selected' : 'Default TripGather stamp will be used.'}
                                    </p>
                                    <button 
                                        type="button"
                                        onClick={() => stampInputRef.current.click()}
                                        className="glass"
                                        style={{ fontSize: '12px', padding: '8px 16px', borderRadius: '8px', color: 'var(--text-secondary)', border: '1px solid var(--border-color)', fontWeight: 'bold' }}
                                    >
                                        CHOOSE IMAGE
                                    </button>
                                </div>
                                <input type="file" ref={stampInputRef} onChange={handleStampUpload} accept="image/*" style={{ display: 'none' }} />
                            </div>
                        </div>
                    </div>

                    <div style={{ marginTop: '8px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                            <label style={{ fontSize: '13px', color: 'var(--text-secondary)', fontWeight: 800, letterSpacing: '1px' }}>FLIGHT PATH (STOPS)</label>
                            <button 
                                type="button" 
                                onClick={addDay} 
                                className="glass"
                                style={{ 
                                    fontSize: '11px', fontWeight: 800, color: 'var(--primary-orange)', padding: '8px 16px', borderRadius: 'var(--radius-full)', border: '1px solid var(--border-color)',
                                    display: 'flex', alignItems: 'center', gap: '6px'
                                }}
                            >
                                <Plus size={14} color="var(--primary-orange)" /> NEXT DAY
                            </button>
                        </div>

                        {days.map(dayNum => (
                            <div key={dayNum} className="glass" style={{ marginBottom: '24px', padding: '20px', borderRadius: '24px', border: '1px solid var(--border-color)', background: 'var(--bg-color)' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                                    <h4 style={{ fontSize: '14px', color: 'var(--text-primary)', fontWeight: 800, letterSpacing: '0.5px', margin: 0 }}>DAY {dayNum}</h4>
                                    <button 
                                        type="button" 
                                        onClick={() => addPoint(dayNum)} 
                                        style={{ fontSize: '11px', fontWeight: 700, color: 'var(--text-muted)', background: 'none', border: 'none', cursor: 'pointer' }}
                                    >
                                        + ADD TARGET
                                    </button>
                                </div>

                                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                                    {formData.routePoints.map((point, globalIndex) => {
                                        if ((point.dayNumber || 1) !== dayNum) return null;
                                        return (
                                            <div key={globalIndex} className="animate-fade" style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                                                <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '8px' }}>
                                                    <div style={{ position: 'relative' }}>
                                                        <MapPin size={16} color="var(--primary-orange)" style={{ position: 'absolute', top: '13px', left: '14px' }} />
                                                        <input 
                                                            required 
                                                            value={point.label} 
                                                            onChange={(e) => updatePoint(globalIndex, 'label', e.target.value)} 
                                                            placeholder="Location target..." 
                                                            className="glass"
                                                            style={{ 
                                                                width: '100%', padding: '12px 14px 12px 42px', borderRadius: '12px', border: '1px solid var(--border-color)',
                                                                color: 'var(--text-primary)', fontSize: '14px', fontWeight: 600, outline: 'none', background: 'var(--surface)'
                                                            }} 
                                                        />
                                                    </div>
                                                    
                                                    {/* Aviation Style Time Range Slider */}
                                                    <div style={{ 
                                                        padding: '16px', background: 'white', borderRadius: '16px', border: '1px solid var(--border-color)',
                                                        display: 'flex', flexDirection: 'column', gap: '12px'
                                                    }}>
                                                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', fontWeight: 800, color: 'var(--text-secondary)' }}>
                                                                <Plane size={14} color="var(--primary-orange)" style={{ transform: 'rotate(90deg)' }} /> SCHEDULE (ARR/DEP)
                                                            </div>
                                                            <button 
                                                                type="button"
                                                                onClick={() => {
                                                                    const isScheduled = !!point.startTime;
                                                                    updatePoint(globalIndex, 'startTime', isScheduled ? null : '12:00');
                                                                    updatePoint(globalIndex, 'endTime', isScheduled ? null : '13:00');
                                                                }}
                                                                style={{ 
                                                                    fontSize: '11px', fontWeight: 800, color: point.startTime ? 'var(--primary-orange)' : 'var(--text-muted)',
                                                                    background: point.startTime ? 'var(--highlight-muted)' : 'var(--bg-lite)',
                                                                    padding: '4px 10px', borderRadius: '8px', border: 'none', cursor: 'pointer'
                                                                }}
                                                            >
                                                                {point.startTime ? 'SCHEDULE SET' : '+ SET TIME'}
                                                            </button>
                                                        </div>

                                                        {point.startTime ? (
                                                            <div className="animate-fade" style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                                                                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                                                    <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                                                                        <span style={{ fontSize: '10px', fontWeight: 900, color: 'var(--text-muted)', letterSpacing: '0.05em' }}>ARR (LANDING)</span>
                                                                        <span style={{ fontSize: '14px', fontWeight: 900, color: 'var(--text-primary)' }}>{point.startTime}</span>
                                                                    </div>
                                                                    <div style={{ display: 'flex', flexDirection: 'column', gap: '2px', alignItems: 'flex-end' }}>
                                                                        <span style={{ fontSize: '10px', fontWeight: 900, color: 'var(--text-muted)', letterSpacing: '0.05em' }}>DEP (TAKE-OFF)</span>
                                                                        <span style={{ fontSize: '14px', fontWeight: 900, color: 'var(--text-primary)' }}>{point.endTime}</span>
                                                                    </div>
                                                                </div>

                                                                <div style={{ position: 'relative', height: '30px', display: 'flex', alignItems: 'center' }}>
                                                                    {/* Background Track */}
                                                                    <div style={{ position: 'absolute', width: '100%', height: '6px', background: 'var(--bg-lite)', borderRadius: '3px' }} />
                                                                    
                                                                    {/* Active Range Highlight */}
                                                                    {(() => {
                                                                        const [h1, m1] = point.startTime.split(':').map(Number);
                                                                        const [h2, m2] = point.endTime.split(':').map(Number);
                                                                        const s = h1 * 2 + (m1 === 30 ? 1 : 0);
                                                                        const e = h2 * 2 + (m2 === 30 ? 1 : 0);
                                                                        const left = (s / 47) * 100;
                                                                        const width = ((e - s) / 47) * 100;
                                                                        return (
                                                                            <div style={{ 
                                                                                position: 'absolute', left: `${left}%`, width: `${width}%`, 
                                                                                height: '6px', background: 'var(--primary-gradient)', borderRadius: '3px',
                                                                                boxShadow: '0 2px 8px rgba(255, 92, 0, 0.2)'
                                                                            }} />
                                                                        );
                                                                    })()}

                                                                    {/* Overlaying two hidden inputs to function as range slider */}
                                                                    <input 
                                                                        type="range" min="0" max="47" step="1"
                                                                        value={(() => {
                                                                            const [h, m] = point.startTime.split(':').map(Number);
                                                                            return h * 2 + (m === 30 ? 1 : 0);
                                                                        })()}
                                                                        onChange={(e) => {
                                                                            const val = Math.min(parseInt(e.target.value), (point.endTime ? parseInt(point.endTime.split(':')[0]) * 2 + (point.endTime.split(':')[1] === '30' ? 1 : 0) : 47));
                                                                            const h = Math.floor(val / 2);
                                                                            const m = val % 2 === 0 ? "00" : "30";
                                                                            updatePoint(globalIndex, 'startTime', `${h.toString().padStart(2, '0')}:${m}`);
                                                                        }}
                                                                        style={{ 
                                                                            position: 'absolute', width: '100%', appearance: 'none', background: 'none', pointerEvents: 'none', zIndex: 3,
                                                                            WebkitAppearance: 'none'
                                                                        }}
                                                                        className="dual-range-thumb"
                                                                    />
                                                                    <input 
                                                                        type="range" min="0" max="47" step="1"
                                                                        value={(() => {
                                                                            const [h, m] = point.endTime.split(':').map(Number);
                                                                            return h * 2 + (m === 30 ? 1 : 0);
                                                                        })()}
                                                                        onChange={(e) => {
                                                                            const val = Math.max(parseInt(e.target.value), (point.startTime ? parseInt(point.startTime.split(':')[0]) * 2 + (point.startTime.split(':')[1] === '30' ? 1 : 0) : 0));
                                                                            const h = Math.floor(val / 2);
                                                                            const m = val % 2 === 0 ? "00" : "30";
                                                                            updatePoint(globalIndex, 'endTime', `${h.toString().padStart(2, '0')}:${m}`);
                                                                        }}
                                                                        style={{ 
                                                                            position: 'absolute', width: '100%', appearance: 'none', background: 'none', pointerEvents: 'none', zIndex: 4,
                                                                            WebkitAppearance: 'none'
                                                                        }}
                                                                        className="dual-range-thumb"
                                                                    />
                                                                </div>
                                                                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '9px', color: 'var(--text-muted)', fontWeight: 700 }}>
                                                                    <span>MIDNIGHT</span>
                                                                    <span>NOON</span>
                                                                    <span>23:30</span>
                                                                </div>
                                                            </div>
                                                        ) : (
                                                            <div style={{ fontSize: '12px', color: 'var(--text-muted)', textAlign: 'center', padding: '8px', fontStyle: 'italic' }}>
                                                                No specific flight schedule set for this stop.
                                                            </div>
                                                        )}
                                                    </div>
                                                </div>
                                                <button type="button" onClick={() => removePoint(globalIndex)} className="icon-circle" style={{ width: '36px', height: '40px', background: 'rgba(239, 68, 68, 0.1)', cursor: 'pointer', alignSelf: 'flex-start', marginTop: '4px' }}>
                                                    <Trash2 size={16} color="#EF4444" />
                                                </button>
                                            </div>
                                        );
                                    })}
                                    {formData.routePoints.filter(p => (p.dayNumber || 1) === dayNum).length === 0 && (
                                        <p style={{ fontSize: '12px', color: 'var(--text-muted)', textAlign: 'center', padding: '12px', fontWeight: 600, border: '1px dashed var(--border-color)', borderRadius: '12px' }}>No targets assigned for this day</p>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>

                <div style={{ position: 'fixed', bottom: 0, left: 0, right: 0, padding: '20px', background: 'white', borderTop: '1px solid var(--border-color)', zIndex: 10 }}>
                    <PrimaryButton 
                        onClick={handleSubmit}
                        loading={saving}
                    >
                        {isEdit ? 'CONFIRM UPDATE' : 'BOARDING PASS ISSUED'} <Send size={20} style={{ marginLeft: '8px' }} />
                    </PrimaryButton>
                </div>
            </form>
        </div>
    );
};

export default ItineraryEditorPage;
