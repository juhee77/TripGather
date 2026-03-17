import React, { useState, useEffect } from 'react';
import { X, Type, FileText, Send, Plus, Trash2, MapPin, Clock } from 'lucide-react';
import { authFetch } from '../api/client';

const ItineraryEditorModal = ({ itinerary, onClose, onSaved }) => {
    const isEdit = !!itinerary;
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        routePoints: []
    });
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        if (itinerary) {
            setFormData({
                title: itinerary.title || '',
                description: itinerary.description || '',
                routePoints: itinerary.routePoints ? [...itinerary.routePoints].sort((a, b) => 
                    a.dayNumber !== b.dayNumber ? a.dayNumber - b.dayNumber : a.sequenceOrder - b.sequenceOrder
                ) : []
            });
        }
    }, [itinerary]);

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
        setSaving(true);
        try {
            const url = isEdit ? `/api/itineraries/${itinerary.id}` : '/api/itineraries';
            const method = isEdit ? 'PATCH' : 'POST';
            const response = await authFetch(url, {
                method,
                body: JSON.stringify(formData),
            });
            if (response.ok) {
                const saved = await response.json();
                onSaved(saved);
                onClose();
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
        <div style={{
            position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
            backgroundColor: 'rgba(0,0,0,0.8)', backdropFilter: 'blur(25px)', zIndex: 3000,
            display: 'flex', justifyContent: 'center', alignItems: 'flex-end'
        }}>
            <div className="glass-dark" style={{
                width: '100%', maxWidth: '500px', height: '94vh',
                borderTopLeftRadius: '32px', borderTopRightRadius: '32px',
                display: 'flex', flexDirection: 'column', overflow: 'hidden',
                boxShadow: '0 -20px 60px rgba(0,0,0,0.5)',
                border: 'none',
                animation: 'slideUp 0.5s cubic-bezier(0.16, 1, 0.3, 1)'
            }}>
                <header style={{ 
                    padding: '24px 28px', 
                    borderBottom: '1px solid rgba(255,255,255,0.1)', 
                    display: 'flex', 
                    justifyContent: 'space-between', 
                    alignItems: 'center',
                    background: 'rgba(255,255,255,0.02)'
                }}>
                    <div>
                        <h2 className="heading-m" style={{ color: 'white', marginBottom: '4px' }}>
                            {isEdit ? 'EDIT VOYAGE ✈️' : 'NEW CHECK-IN 🎫'}
                        </h2>
                        <p style={{ fontSize: '12px', color: 'rgba(255,255,255,0.5)', fontWeight: 600 }}>Configure your travel mission</p>
                    </div>
                    <button onClick={onClose} className="icon-circle glass" style={{ width: '40px', height: '40px' }}>
                        <X size={20} color="white" />
                    </button>
                </header>

                <form onSubmit={handleSubmit} className="hide-scrollbar" style={{ flex: 1, overflowY: 'auto', padding: '32px 28px', display: 'flex', flexDirection: 'column', gap: '32px' }}>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
                        <div>
                            <label className="text-s" style={{ color: 'var(--primary-orange)', fontWeight: 900, marginBottom: '12px', display: 'block', letterSpacing: '1px' }}>VOYAGE TITLE</label>
                            <div style={{ position: 'relative' }}>
                                <Type size={18} color="rgba(255,255,255,0.3)" style={{ position: 'absolute', top: '16px', left: '16px' }} />
                                <input 
                                    required 
                                    name="title" 
                                    value={formData.title} 
                                    onChange={handleChange} 
                                    placeholder="Enter your trip title..." 
                                    className="glass"
                                    style={{ 
                                        width: '100%', padding: '16px 16px 16px 48px', borderRadius: '16px', border: '1px solid rgba(255,255,255,0.1)',
                                        color: 'white', fontSize: '16px', fontWeight: 600, outline: 'none'
                                    }} 
                                />
                            </div>
                        </div>

                        <div>
                            <label className="text-s" style={{ color: 'var(--primary-orange)', fontWeight: 900, marginBottom: '12px', display: 'block', letterSpacing: '1px' }}>INTEL & INTENTION</label>
                            <div style={{ position: 'relative' }}>
                                <FileText size={18} color="rgba(255,255,255,0.3)" style={{ position: 'absolute', top: '16px', left: '16px' }} />
                                <textarea 
                                    name="description" 
                                    value={formData.description} 
                                    onChange={handleChange} 
                                    placeholder="Briefly describe the journey..." 
                                    className="glass"
                                    style={{ 
                                        width: '100%', padding: '16px 16px 16px 48px', borderRadius: '16px', border: '1px solid rgba(255,255,255,0.1)',
                                        color: 'white', fontSize: '15px', fontWeight: 500, outline: 'none', height: '120px', resize: 'none', lineHeight: '1.6'
                                    }} 
                                />
                            </div>
                        </div>
                    </div>

                    <div style={{ marginTop: '8px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                            <label className="text-s" style={{ color: 'var(--secondary-purple)', fontWeight: 900, letterSpacing: '1px' }}>FLIGHT PATH (STOPS)</label>
                            <button 
                                type="button" 
                                onClick={addDay} 
                                className="glass"
                                style={{ 
                                    fontSize: '11px', fontWeight: 800, color: 'white', padding: '8px 16px', borderRadius: 'var(--radius-full)', border: '1px solid rgba(99, 102, 241, 0.4)',
                                    display: 'flex', alignItems: 'center', gap: '6px'
                                }}
                            >
                                <Plus size={14} color="var(--secondary-purple)" /> NEXT DAY
                            </button>
                        </div>

                        {days.map(dayNum => (
                            <div key={dayNum} className="glass" style={{ marginBottom: '24px', padding: '20px', borderRadius: '24px', border: '1px solid rgba(255,255,255,0.05)', background: 'rgba(255,255,255,0.02)' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                                    <h4 className="text-s" style={{ color: 'white', fontWeight: 900, letterSpacing: '0.5px' }}>DAY {dayNum}</h4>
                                    <button 
                                        type="button" 
                                        onClick={() => addPoint(dayNum)} 
                                        style={{ fontSize: '11px', fontWeight: 700, color: 'rgba(255,255,255,0.4)', background: 'none', border: 'none', cursor: 'pointer' }}
                                    >
                                        + ADD TARGET
                                    </button>
                                </div>

                                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                                    {formData.routePoints.filter(p => (p.dayNumber || 1) === dayNum).map((point, idx) => {
                                        const globalIndex = formData.routePoints.indexOf(point);
                                        return (
                                            <div key={idx} className="animate-fade" style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                                                <div style={{ flex: 1, position: 'relative' }}>
                                                    <MapPin size={16} color="var(--primary-orange)" style={{ position: 'absolute', top: '13px', left: '14px' }} />
                                                    <input 
                                                        required 
                                                        value={point.label} 
                                                        onChange={(e) => updatePoint(globalIndex, 'label', e.target.value)} 
                                                        placeholder="Location target..." 
                                                        className="glass"
                                                        style={{ 
                                                            width: '100%', padding: '12px 14px 12px 42px', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.05)',
                                                            color: 'white', fontSize: '14px', fontWeight: 600, outline: 'none'
                                                        }} 
                                                    />
                                                </div>
                                                <button type="button" onClick={() => removePoint(globalIndex)} className="icon-circle" style={{ width: '36px', height: '36px', background: 'rgba(239, 68, 68, 0.1)' }}>
                                                    <Trash2 size={16} color="#EF4444" />
                                                </button>
                                            </div>
                                        );
                                    })}
                                    {formData.routePoints.filter(p => (p.dayNumber || 1) === dayNum).length === 0 && (
                                        <p style={{ fontSize: '12px', color: 'rgba(255,255,255,0.2)', textAlign: 'center', padding: '12px', fontWeight: 600, border: '1px dashed rgba(255,255,255,0.1)', borderRadius: '12px' }}>No targets assigned for this day</p>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                </form>

                <footer style={{ 
                    padding: '28px', 
                    borderTop: '1px solid rgba(255,255,255,0.1)', 
                    background: 'rgba(13, 13, 25, 0.8)',
                    backdropFilter: 'blur(10px)'
                }}>
                    <button 
                        type="submit" 
                        onClick={handleSubmit}
                        disabled={saving}
                        className="primary-btn"
                        style={{
                            width: '100%', height: '64px', borderRadius: '16px', fontSize: '18px'
                        }}
                    >
                        {saving ? 'MISSION ENCRYPTING...' : (isEdit ? 'CONFIRM UPDATE' : 'BOARDING PASS ISSUED')} <Send size={20} />
                    </button>
                </footer>
            </div>
            <style jsx>{`
                @keyframes slideUp {
                    from { transform: translateY(100%); }
                    to { transform: translateY(0); }
                }
            `}</style>
        </div>
    );
};

export default ItineraryEditorModal;
