import React, { useState, useEffect } from 'react';
import { X, Type, FileText, Send, Plus, Trash2, MapPin, Clock } from 'lucide-react';
import { authFetch } from '../api/client';
import ModalHeader from './UI/ModalHeader';
import ModalFooter from './UI/ModalFooter';
import FormInput from './UI/FormInput';
import PrimaryButton from './UI/PrimaryButton';

const ItineraryEditorModal = ({ itinerary, onClose, onSaved }) => {
    const isEdit = !!itinerary;
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
        if (itinerary) {
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
    }, [itinerary]);

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
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content hide-scrollbar" onClick={(e) => e.stopPropagation()}>
                <ModalHeader 
                    title={isEdit ? 'EDIT VOYAGE ✈️' : 'NEW CHECK-IN 🎫'}
                    subtitle="Configure your travel mission"
                    onClose={onClose}
                />

                <form onSubmit={handleSubmit} className="hide-scrollbar" style={{ flex: 1, overflowY: 'auto', padding: '32px 28px', display: 'flex', flexDirection: 'column', gap: '32px' }}>
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
                                        <Plus size={24} color="var(--text-muted)" />
                                    )}
                                </div>
                                <div style={{ flex: 1 }}>
                                    <p style={{ fontSize: '13px', color: 'var(--text-muted)', marginBottom: '8px' }}>
                                        {stampPreview ? 'Custom stamp selected' : 'No custom stamp. Auto-generated bottt will be used.'}
                                    </p>
                                    <button 
                                        type="button"
                                        onClick={() => stampInputRef.current.click()}
                                        className="glass"
                                        style={{ fontSize: '12px', padding: '8px 16px', borderRadius: '8px', color: 'var(--text-secondary)', border: '1px solid var(--border-color)", fontWeight: 'bold' }}
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
                                                <div style={{ flex: 1, position: 'relative' }}>
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
                                                <button type="button" onClick={() => removePoint(globalIndex)} className="icon-circle" style={{ width: '36px', height: '36px', background: 'rgba(239, 68, 68, 0.1)', cursor: 'pointer' }}>
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
                </form>

                <ModalFooter>
                    <PrimaryButton 
                        onClick={handleSubmit}
                        loading={saving}
                    >
                        {isEdit ? 'CONFIRM UPDATE' : 'BOARDING PASS ISSUED'} <Send size={20} style={{ marginLeft: '8px' }} />
                    </PrimaryButton>
                </ModalFooter>
            </div>
            <style>{`
                @keyframes slideUp {
                    from { transform: translateY(100%); }
                    to { transform: translateY(0); }
                }
            `}</style>
        </div>
    );
};

export default ItineraryEditorModal;
