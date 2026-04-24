import React, { useEffect } from 'react';
import TicketCard from './TicketCard';
import { Plus, RotateCcw } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useItinerariesViewModel } from '../viewmodels/useItinerariesViewModel';
import JourneyRepository from '../repositories/JourneyRepository';

const ItineraryTab = ({ onAddToJourney }) => {
    const navigate = useNavigate();
    const {
        itineraries,
        isLoading: loading,
        actions: { refreshItineraries }
    } = useItinerariesViewModel();

    // Legacy add logic removed in favor of ItineraryDetailPage's new Clone/Merge modal

    useEffect(() => {
        refreshItineraries();
    }, [refreshItineraries]);

    return (
        <div style={{
            background: 'transparent', 
            color: 'var(--text-primary)', 
            minHeight: 'calc(100vh - 200px)', 
            padding: '24px 0', 
            position: 'relative', 
            overflow: 'hidden'
        }}>
            {/* Ambient Background Glows */}
            <div style={{ position: 'absolute', top: '0', right: '-80px', width: '260px', height: '260px', background: 'radial-gradient(circle, rgba(255, 92, 0, 0.08) 0%, transparent 70%)', filter: 'blur(60px)', borderRadius: '50%', zIndex: 0 }} />
            <div style={{ position: 'absolute', bottom: '100px', left: '-80px', width: '220px', height: '220px', background: 'radial-gradient(circle, rgba(99, 102, 241, 0.08) 0%, transparent 70%)', filter: 'blur(50px)', borderRadius: '50%', zIndex: 0 }} />

            <div style={{ position: 'relative', zIndex: 1 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '32px' }}>
                    <div>
                        <h2 className="heading-m" style={{ display: 'flex', alignItems: 'center', gap: '10px', color: 'var(--text-primary)' }}>
                            <span style={{ 
                                width: '12px', 
                                height: '12px', 
                                borderRadius: '50%', 
                                background: 'var(--primary-gradient)',
                                boxShadow: '0 0 15px rgba(255, 92, 0, 0.4)'
                            }}></span> 
                            여행 피드
                        </h2>
                        <p style={{ fontSize: '12px', color: 'var(--text-secondary)', fontWeight: 600, marginTop: '4px' }}>다른 여행자들의 일정을 탐색하세요</p>
                    </div>
                    <button onClick={refreshItineraries} style={{ 
                        color: 'var(--text-secondary)', 
                        display: 'flex', 
                        alignItems: 'center', 
                        gap: '6px', 
                        fontSize: '11px', 
                        fontWeight: 700,
                        background: 'white', 
                        padding: '8px 14px',
                        borderRadius: 'var(--radius-full)',
                        border: '1px solid var(--border-color)',
                        boxShadow: '0 2px 8px rgba(0,0,0,0.03)'
                    }}>
                        <RotateCcw size={14} /> REFRESH
                    </button>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                    {itineraries.map((it, idx) => (
                        <div
                            key={it.id}
                            className="animate-fade"
                            style={{ animationDelay: `${idx * 0.1}s` }}
                        >
                            <TicketCard
                                itinerary={it}
                                onViewRoute={() => navigate(`/itinerary/${it.id}`)}
                                onEdit={(it) => navigate(`/itinerary/edit/${it.id}`)}
                            />
                        </div>
                    ))}
                    {!loading && itineraries.length === 0 && (
                        <div className="glass animate-fade" style={{ 
                            textAlign: 'center', 
                            padding: '80px 30px', 
                            borderRadius: 'var(--radius-lg)',
                            border: '1px dashed var(--border-color)',
                            background: 'white'
                        }}>
                            <div style={{ fontSize: '56px', marginBottom: '20px' }}>✈️</div>
                            <p style={{ color: 'var(--text-secondary)', marginBottom: '24px', fontWeight: 600 }}>아직 등록된 여행 일정이 없습니다.</p>
                            <button className="primary-btn" onClick={() => openEditor()}>
                                첫 여행 일정 만들기
                            </button>
                        </div>
                    )}
                    {loading && (
                        <div style={{ textAlign: 'center', padding: '60px 0', color: 'var(--text-muted)', fontWeight: 600 }}>
                            <span className="animate-pulse">여행 일정 불러오는 중...</span>
                        </div>
                    )}
                </div>
            </div>

            {/* Add to My Trip Dialog is now handled in ItineraryDetailPage */}
        </div>
    );
};

export default ItineraryTab;
