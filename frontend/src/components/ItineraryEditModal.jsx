import React, { useState, useEffect } from 'react';
import { X, Save, MapPin, Plus, Trash2 } from 'lucide-react';
import PrimaryButton from './UI/PrimaryButton';
import { apiUrl } from '../api/client';

const ItineraryEditModal = ({ isOpen, onClose, itinerary, onUpdate }) => {
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        routePoints: []
    });
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        if (itinerary) {
            setFormData({
                title: itinerary.title || '',
                description: itinerary.description || '',
                routePoints: itinerary.routePoints ? [...itinerary.routePoints] : []
            });
        }
    }, [itinerary]);

    if (!isOpen || !itinerary) return null;

    const handlePointChange = (index, field, value) => {
        const newPoints = [...formData.routePoints];
        newPoints[index] = { ...newPoints[index], [field]: value };
        setFormData({ ...formData, routePoints: newPoints });
    };

    const addPoint = () => {
        setFormData({
            ...formData,
            routePoints: [...formData.routePoints, { label: '', dayNumber: 1, sequenceOrder: formData.routePoints.length }]
        });
    };

    const removePoint = (index) => {
        setFormData({
            ...formData,
            routePoints: formData.routePoints.filter((_, i) => i !== index)
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        try {
            const res = await fetch(apiUrl(`/api/itineraries/${itinerary.id}`), {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('token')}`
                },
                body: JSON.stringify(formData)
            });

            if (res.ok) {
                const updated = await res.json();
                onUpdate(updated);
                onClose();
            } else {
                alert('일정 수정 중 오류가 발생했습니다.');
            }
        } catch (error) {
            console.error('Update failed:', error);
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content animate-slide-up" onClick={e => e.stopPropagation()} 
                 style={{ maxWidth: '600px', maxHeight: '90vh', overflowY: 'auto' }}>
                <div className="flex-between" style={{ marginBottom: '24px' }}>
                    <h2 className="heading-m">일정 수정하기</h2>
                    <button onClick={onClose} className="icon-button"><X /></button>
                </div>

                <form onSubmit={handleSubmit} className="flex-column gap-lg">
                    <div className="flex-column gap-sm">
                        <label className="label-muted">일정 제목</label>
                        <input 
                            className="input-base"
                            value={formData.title}
                            onChange={e => setFormData({...formData, title: e.target.value})}
                            placeholder="예: 제주도 해안도로 일주"
                            required
                        />
                    </div>

                    <div className="flex-column gap-sm">
                        <label className="label-muted">상세 설명</label>
                        <textarea 
                            className="input-base"
                            style={{ height: '100px', padding: '12px' }}
                            value={formData.description}
                            onChange={e => setFormData({...formData, description: e.target.value})}
                            placeholder="일정에 대한 설명을 적어주세요."
                        />
                    </div>

                    <div className="flex-column gap-sm">
                        <div className="flex-between">
                            <label className="label-muted">경로 정보 ({formData.routePoints.length})</label>
                            <button type="button" onClick={addPoint} className="text-button" style={{ color: 'var(--primary-orange)' }}>
                                <Plus size={16} /> 장소 추가
                            </button>
                        </div>
                        
                        <div className="flex-column gap-md">
                            {formData.routePoints.map((point, index) => (
                                <div key={index} className="flex gap-sm align-center">
                                    <div className="flex-center" style={{ 
                                        width: '24px', height: '24px', 
                                        borderRadius: '50%', backgroundColor: 'var(--bg-color)',
                                        fontSize: '12px', fontWeight: 800
                                    }}>{index + 1}</div>
                                    <input 
                                        className="input-base"
                                        style={{ flex: 1 }}
                                        value={point.label}
                                        onChange={e => handlePointChange(index, 'label', e.target.value)}
                                        placeholder="장소 이름"
                                    />
                                    <button type="button" onClick={() => removePoint(index)} style={{ color: 'var(--text-muted)' }}>
                                        <Trash2 size={18} />
                                    </button>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div style={{ marginTop: '12px' }}>
                        <PrimaryButton 
                            type="submit"
                            style={{ width: '100%' }}
                            disabled={isSubmitting}
                        >
                            <Save size={18} /> {isSubmitting ? '수정 중...' : '변경 내용 저장하기'}
                        </PrimaryButton>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default ItineraryEditModal;
