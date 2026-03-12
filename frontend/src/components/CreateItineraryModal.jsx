import React, { useState } from 'react';
import { X, Type, FileText, Send } from 'lucide-react';

const CreateItineraryModal = ({ onClose, onCreated }) => {
    const [formData, setFormData] = useState({
        title: '',
        author: 'Jihyun (지현)',
        description: ''
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        const newItinerary = {
            ...formData,
            routePoints: [] // Future: add functionality to add points
        };

        try {
            const response = await fetch('http://localhost:8080/api/itineraries', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(newItinerary),
            });

            if (response.ok) {
                const savedItinerary = await response.json();
                onCreated(savedItinerary);
                onClose();
            } else {
                const errText = await response.text();
                alert(`일정 저장에 실패했습니다: ${errText}`);
            }
        } catch (error) {
            console.error("Error creating itinerary:", error);
            alert(`네트워크 오류: ${error.message}`);
        }
    };

    const inputStyle = {
        width: '100%', padding: '16px 16px 16px 44px', borderRadius: '16px',
        border: '1px solid var(--border)', background: 'var(--bg-color)',
        fontSize: '16px', fontWeight: 500, outline: 'none', transition: 'border 0.2s',
        color: 'var(--text-main)'
    };

    return (
        <div style={{
            position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
            backgroundColor: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(8px)', zIndex: 1100,
            display: 'flex', justifyContent: 'center', alignItems: 'flex-end'
        }}>
            <div style={{
                background: 'var(--surface)', width: '100%', maxWidth: '480px', maxHeight: '90vh', overflowY: 'auto',
                borderTopLeftRadius: '28px', borderTopRightRadius: '28px',
                padding: '30px 24px', animation: 'slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1)',
                boxShadow: '0 -10px 40px rgba(0,0,0,0.2)'
            }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                    <h2 style={{ fontSize: '24px', fontWeight: 800 }}>새 일정 만들기 ✈️</h2>
                    <button onClick={onClose} style={{
                        padding: '8px', background: 'var(--bg-color)', borderRadius: '50%',
                        display: 'flex', alignItems: 'center', justifyContent: 'center'
                    }}>
                        <X size={20} color="var(--text-main)" />
                    </button>
                </div>
                <p style={{ color: 'var(--text-sub)', fontSize: '15px', marginBottom: '24px' }}>
                    나만의 여행 계획을 기록하고 공유해보세요.
                </p>

                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
                    <div>
                        <label style={{ display: 'block', fontSize: '15px', fontWeight: 700, marginBottom: '8px', color: 'var(--text-main)' }}>일정 제목</label>
                        <div style={{ position: 'relative' }}>
                            <Type size={18} color="var(--text-sub)" style={{ position: 'absolute', top: '16px', left: '16px' }} />
                            <input
                                required name="title" value={formData.title} onChange={handleChange}
                                placeholder="예: 3박 4일 도쿄 미식 여행"
                                style={inputStyle}
                                onFocus={(e) => e.target.style.border = '1px solid var(--primary)'}
                                onBlur={(e) => e.target.style.border = '1px solid var(--border)'}
                            />
                        </div>
                    </div>

                    <div>
                        <label style={{ display: 'block', fontSize: '15px', fontWeight: 700, marginBottom: '8px', color: 'var(--text-main)' }}>상세 설명</label>
                        <div style={{ position: 'relative' }}>
                            <FileText size={18} color="var(--text-sub)" style={{ position: 'absolute', top: '16px', left: '16px' }} />
                            <textarea
                                required name="description" value={formData.description} onChange={handleChange}
                                placeholder="이번 여행의 주요 테마나 방문하고 싶은 곳을 적어주세요."
                                style={{ ...inputStyle, height: '120px', resize: 'none' }}
                                onFocus={(e) => e.target.style.border = '1px solid var(--primary)'}
                                onBlur={(e) => e.target.style.border = '1px solid var(--border)'}
                            />
                        </div>
                    </div>

                    <button type="submit" style={{
                        background: 'var(--primary)', color: 'white',
                        border: 'none', cursor: 'pointer',
                        height: '60px', borderRadius: '20px', fontSize: '18px', fontWeight: 700,
                        marginTop: '16px', boxShadow: '0 8px 16px rgba(255, 123, 84, 0.3)',
                        display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '10px'
                    }}>
                        발권하기 <Send size={20} />
                    </button>
                </form>
            </div>
        </div>
    );
};

export default CreateItineraryModal;
