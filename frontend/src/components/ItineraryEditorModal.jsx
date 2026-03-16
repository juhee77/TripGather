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

    // Grouping for UI
    const days = Array.from(new Set(formData.routePoints.map(p => p.dayNumber || 1))).sort((a, b) => a - b);
    if (days.length === 0) days.push(1);

    const inputStyle = {
        width: '100%', padding: '12px 12px 12px 40px', borderRadius: '12px',
        border: '1px solid var(--border)', background: 'var(--bg-color)',
        fontSize: '15px', fontWeight: 500, outline: 'none', color: 'var(--text-main)'
    };

    return (
        <div style={{
            position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
            backgroundColor: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(10px)', zIndex: 3000,
            display: 'flex', justifyContent: 'center', alignItems: 'flex-end'
        }}>
            <div style={{
                background: 'var(--surface)', width: '100%', maxWidth: '500px', height: '92vh',
                borderTopLeftRadius: '28px', borderTopRightRadius: '28px',
                display: 'flex', flexDirection: 'column', overflow: 'hidden',
                boxShadow: '0 -10px 40px rgba(0,0,0,0.3)',
                animation: 'slideUp 0.3s ease-out'
            }}>
                <header style={{ padding: '24px', borderBottom: '1px solid var(--border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                        <h2 style={{ fontSize: '20px', fontWeight: 800 }}>{isEdit ? '일정 수정하기 ✏️' : '새 일정 만들기 ✈️'}</h2>
                        <p style={{ fontSize: '13px', color: 'var(--text-sub)' }}>나만의 일일 계획을 세워보세요.</p>
                    </div>
                    <button onClick={onClose} style={{ padding: '8px', background: 'var(--bg-color)', borderRadius: '50%', border: 'none' }}>
                        <X size={20} color="var(--text-main)" />
                    </button>
                </header>

                <form onSubmit={handleSubmit} style={{ flex: 1, overflowY: 'auto', padding: '24px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                        <div>
                            <label style={{ display: 'block', fontSize: '14px', fontWeight: 700, marginBottom: '6px' }}>일정 제목</label>
                            <div style={{ position: 'relative' }}>
                                <Type size={16} color="var(--text-sub)" style={{ position: 'absolute', top: '14px', left: '14px' }} />
                                <input required name="title" value={formData.title} onChange={handleChange} placeholder="예: 도쿄 3박 4일 미식 여행" style={inputStyle} />
                            </div>
                        </div>

                        <div>
                            <label style={{ display: 'block', fontSize: '14px', fontWeight: 700, marginBottom: '6px' }}>상세 설명</label>
                            <div style={{ position: 'relative' }}>
                                <FileText size={16} color="var(--text-sub)" style={{ position: 'absolute', top: '14px', left: '14px' }} />
                                <textarea name="description" value={formData.description} onChange={handleChange} placeholder="여행의 주요 테마를 설명해주세요." style={{ ...inputStyle, height: '80px', resize: 'none' }} />
                            </div>
                        </div>
                    </div>

                    <div style={{ marginTop: '10px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                            <label style={{ fontSize: '15px', fontWeight: 800, color: 'var(--primary)' }}>데일리 스케줄</label>
                            <button type="button" onClick={addDay} style={{ fontSize: '12px', fontWeight: 700, color: 'var(--primary)', background: 'none', border: 'none', display: 'flex', alignItems: 'center', gap: '4px' }}>
                                <Plus size={14} /> DAY 추가
                            </button>
                        </div>

                        {days.map(dayNum => (
                            <div key={dayNum} style={{ marginBottom: '24px', padding: '16px', borderRadius: '16px', background: 'rgba(255,255,255,0.03)', border: '1px solid var(--border)' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                                    <h4 style={{ fontSize: '14px', fontWeight: 800 }}>DAY {dayNum}</h4>
                                    <button type="button" onClick={() => addPoint(dayNum)} style={{ fontSize: '11px', fontWeight: 600, color: '#888', background: 'none', border: 'none' }}>
                                        + 스팟 추가
                                    </button>
                                </div>

                                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                                    {formData.routePoints.filter(p => (p.dayNumber || 1) === dayNum).map((point, idx) => {
                                        const globalIndex = formData.routePoints.indexOf(point);
                                        return (
                                            <div key={idx} style={{ display: 'flex', gap: '8px', alignItems: 'flex-start' }}>
                                                <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '6px' }}>
                                                    <div style={{ position: 'relative' }}>
                                                        <MapPin size={14} color="var(--primary)" style={{ position: 'absolute', top: '11px', left: '10px' }} />
                                                        <input 
                                                            required 
                                                            value={point.label} 
                                                            onChange={(e) => updatePoint(globalIndex, 'label', e.target.value)} 
                                                            placeholder="어디를 방문하나요? (예: 도쿄역)" 
                                                            style={{ ...inputStyle, paddingLeft: '32px', height: '36px', fontSize: '13px' }} 
                                                        />
                                                    </div>
                                                </div>
                                                <button type="button" onClick={() => removePoint(globalIndex)} style={{ padding: '8px', color: '#ff4b4b' }}>
                                                    <Trash2 size={16} />
                                                </button>
                                            </div>
                                        );
                                    })}
                                    {formData.routePoints.filter(p => (p.dayNumber || 1) === dayNum).length === 0 && (
                                        <p style={{ fontSize: '12px', color: '#666', textAlign: 'center', padding: '10px' }}>아직 등록된 스팟이 없습니다.</p>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                </form>

                <footer style={{ padding: '20px', borderTop: '1px solid var(--border)', background: 'var(--surface)' }}>
                    <button 
                        type="submit" 
                        onClick={handleSubmit}
                        disabled={saving}
                        style={{
                            width: '100%', height: '56px', borderRadius: '16px', background: 'var(--primary)', 
                            color: 'white', fontWeight: 800, fontSize: '17px', border: 'none', cursor: 'pointer',
                            display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '10px',
                            boxShadow: '0 8px 16px rgba(255, 123, 84, 0.3)'
                        }}
                    >
                        {saving ? '저장 중...' : (isEdit ? '수정 완료' : '일정 발권하기')} <Send size={20} />
                    </button>
                </footer>
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
