import React, { useEffect } from 'react';
import { MissionStatus } from '../constants/enums';
import TicketCard from './TicketCard';
import { Plus, RotateCcw } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useItinerariesViewModel } from '../viewmodels/useItinerariesViewModel';
import { useMissionsViewModel } from '../viewmodels/useMissionsViewModel';
import JourneyRepository from '../repositories/JourneyRepository';

const ItineraryTab = ({ onMissionStart, onAddToJourney }) => {
    const navigate = useNavigate();
    const {
        itineraries,
        isLoading: loading,
        actions: { refreshItineraries }
    } = useItinerariesViewModel();

    const openEditor = (id = null) => {
        if (id) navigate(`/itinerary/edit/${id}`);
        else navigate('/itinerary/create');
    };

    const { 
        activeMissions, 
        actions: { startMission: apiStartMission } 
    } = useMissionsViewModel();
    const [boardTarget, setBoardTarget] = React.useState(null);
 
    // Map itineraries to include mission data if already participating
    const mappedItineraries = itineraries.map(it => {
        const mission = activeMissions.find(m => m.itineraryId === it.id && m.status !== MissionStatus.COMPLETED);
        return mission
            ? { ...it, ...mission, itineraryId: it.id, isParticipating: true }
            : { ...it, itineraryId: it.id };
    });

    const handleStartMission = async (itinerary) => {
        setBoardTarget(itinerary);
    };

    const handleBoardToChallenge = async () => {
        if (!boardTarget?.id) return;
        try {
            await apiStartMission(boardTarget.id);
            setBoardTarget(null);
            if (onMissionStart) onMissionStart();
        } catch (err) {
            console.error("Failed to start mission:", err);
        }
    };

    const handleBoardToJourney = () => {
        if (!boardTarget?.id) return;
        JourneyRepository.add(boardTarget.id)
            .then(() => {
                setBoardTarget(null);
                if (onAddToJourney) onAddToJourney();
            })
            .catch((err) => {
                console.error('Failed to add journey:', err);
            });
    };

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
            {/* Ambient Background Glows - Subtle for Light Mode */}
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
                            MY FLIGHTS
                        </h2>
                        <p style={{ fontSize: '12px', color: 'var(--text-secondary)', fontWeight: 600, marginTop: '4px' }}>Upcoming & Past Flight Plans</p>
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
                    {mappedItineraries.map((it, idx) => (
                        <div
                            key={`${it.itineraryId ?? it.id}-${it.missionId ?? 'none'}`}
                            className="animate-fade"
                            style={{ animationDelay: `${idx * 0.1}s` }}
                        >
                            <TicketCard
                                itinerary={it}
                                onViewRoute={(itinerary) => {
                                    if(itinerary.missionId) navigate(`/mission/${itinerary.missionId}`);
                                    else navigate(`/itinerary/${itinerary.id}`);
                                }}
                                onStartMission={handleStartMission}
                                onEdit={(it) => openEditor(it.id)}
                            />
                        </div>
                    ))}
                    {!loading && mappedItineraries.length === 0 && (
                        <div className="glass animate-fade" style={{ 
                            textAlign: 'center', 
                            padding: '80px 30px', 
                            borderRadius: 'var(--radius-lg)',
                            border: '1px dashed var(--border-color)',
                            background: 'white'
                        }}>
                            <div style={{ fontSize: '56px', marginBottom: '20px' }}>✈️</div>
                            <p style={{ color: 'var(--text-secondary)', marginBottom: '24px', fontWeight: 600 }}>아직 등록된 비행 계획이 없습니다.</p>
                            <button className="primary-btn" onClick={() => openEditor()}>
                                첫 비행기표 발권하기
                            </button>
                        </div>
                    )}
                    {loading && (
                        <div style={{ textAlign: 'center', padding: '60px 0', color: 'var(--text-muted)', fontWeight: 600 }}>
                            <span className="animate-pulse">Loading Flight Data...</span>
                        </div>
                    )}
                </div>
            </div>
            {boardTarget && (
                <div style={{
                    position: 'fixed',
                    inset: 0,
                    background: 'rgba(15,23,42,0.35)',
                    zIndex: 1200,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    padding: '20px'
                }}>
                    <div style={{
                        width: '100%',
                        maxWidth: '420px',
                        background: 'white',
                        borderRadius: '20px',
                        border: '1px solid var(--border-color)',
                        padding: '20px'
                    }}>
                        <h3 style={{ marginBottom: '8px', color: 'var(--text-primary)' }}>BOARD 방식 선택</h3>
                        <p style={{ marginBottom: '16px', color: 'var(--text-secondary)', fontSize: '14px' }}>
                            "{boardTarget.title || boardTarget.itineraryTitle}"을 어디에 넣을까요?
                        </p>
                        <div style={{ display: 'flex', gap: '10px' }}>
                            <button className="secondary-btn" style={{ flex: 1 }} onClick={() => setBoardTarget(null)}>취소</button>
                            <button className="secondary-btn" style={{ flex: 1 }} onClick={handleBoardToJourney}>여정에 넣기</button>
                            <button className="primary-btn" style={{ flex: 1 }} onClick={handleBoardToChallenge}>챌린지에 넣기</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ItineraryTab;
