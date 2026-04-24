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

    const isAuthor = currentUser && (
        currentUser.name === localItinerary.author ||
        currentUser.email === localItinerary.authorEmail
    );

    const groupedByDay = (() => {
        const sourcePoints = localItinerary.routePoints || [];
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
                    <div className="ticket-wrapper" style={{ padding: '24px', marginBottom: '32px', background: 'white' }}>
                        <div className="flex-between" style={{ marginBottom: '12px' }}>
                            <span className="label-orange">JOURNEY LOG</span>
                            <div className="status-pill">PLANNED</div>
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
                                {day.points.map((point, pIdx) => (
                                    <div key={pIdx} className="ticket-wrapper" style={{ 
                                        padding: '20px', 
                                        border: '1px solid var(--border-color)',
                                        background: 'white'
                                    }}>
                                        <div className="flex-between" style={{ marginBottom: '8px' }}>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                                <div style={{ 
                                                    width: '24px', height: '24px', borderRadius: '50%', 
                                                    background: 'var(--bg-color)',
                                                    color: 'var(--text-muted)',
                                                    fontSize: '12px', fontWeight: 900, display: 'flex', justifyContent: 'center', alignItems: 'center'
                                                }}>
                                                    {point.sequenceOrder || pIdx + 1}
                                                </div>
                                                <div>
                                                    <h4 style={{ fontWeight: 800, color: 'var(--text-primary)', margin: 0 }}>{point.label}</h4>
                                                    {(point.startTime || point.endTime) && (
                                                        <div style={{ 
                                                            marginTop: '8px', padding: '10px 14px', background: 'var(--highlight-muted)', 
                                                            borderRadius: '12px', display: 'flex', flexWrap: 'wrap', gap: '12px', alignItems: 'center'
                                                        }}>
                                                            {point.startTime && (
                                                                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                                                    <Plane size={12} color="var(--primary-orange)" style={{ transform: 'rotate(90deg)' }} />
                                                                    <span style={{ fontSize: '10px', fontWeight: 900, color: 'var(--text-muted)' }}>ARR</span>
                                                                    <span style={{ fontSize: '13px', fontWeight: 800, color: 'var(--text-primary)' }}>{point.startTime}</span>
                                                                </div>
                                                            )}
                                                            {point.startTime && point.endTime && (
                                                                <div style={{ width: '1px', height: '12px', background: 'var(--border-color)' }} />
                                                            )}
                                                            {point.endTime && (
                                                                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                                                    <Plane size={12} color="var(--primary-orange)" />
                                                                    <span style={{ fontSize: '10px', fontWeight: 900, color: 'var(--text-muted)' }}>DEP</span>
                                                                    <span style={{ fontSize: '13px', fontWeight: 800, color: 'var(--text-primary)' }}>{point.endTime}</span>
                                                                </div>
                                                            )}
                                                            
                                                            {point.startTime && point.endTime && (
                                                                <div style={{ 
                                                                    marginLeft: 'auto', padding: '2px 8px', background: 'white', 
                                                                    borderRadius: '20px', fontSize: '10px', fontWeight: 900, color: 'var(--primary-orange)',
                                                                    border: '1px solid rgba(255, 92, 0, 0.1)'
                                                                }}>
                                                                    {(() => {
                                                                        const [h1, m1] = point.startTime.split(':').map(Number);
                                                                        const [h2, m2] = point.endTime.split(':').map(Number);
                                                                        let diff = (h2 * 60 + m2) - (h1 * 60 + m1);
                                                                        if (diff < 0) diff += 24 * 60;
                                                                        const h = Math.floor(diff / 60);
                                                                        const m = diff % 60;
                                                                        return `STAY: ${h > 0 ? `${h}h ` : ''}${m}m`;
                                                                    })()}
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
                        <PrimaryButton
                            style={{ flex: 1 }}
                            onClick={async () => {
                                if (!currentUser?.email) {
                                    alert('로그인이 필요한 기능입니다.');
                                    return;
                                }
                                try {
                                    await JourneyRepository.add(localItinerary.id, currentUser.email);
                                    alert('내 여행에 추가되었습니다. ✈️');
                                    onClose();
                                } catch (e) {
                                    alert('추가하지 못했습니다.');
                                }
                            }}
                        >
                            내 여행에 추가
                        </PrimaryButton>
                    </div>
                </ModalFooter>
            </div>
        </div>
    );
};

export default ItineraryDetailPage;
