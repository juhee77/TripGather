import React, { useState, useEffect } from 'react';
import TicketCard from './TicketCard';
import ItineraryEditorModal from './ItineraryEditorModal';
import RouteDetailModal from './RouteDetailModal';
import { Plus, RotateCcw } from 'lucide-react';
import { authFetch, apiUrl } from '../api/client';

const ItineraryTab = () => {
    const [itineraries, setItineraries] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isEditorOpen, setIsEditorOpen] = useState(false);
    const [selectedItinerary, setSelectedItinerary] = useState(null);
    const [editingItinerary, setEditingItinerary] = useState(null);

    const fetchItineraries = async () => {
        setLoading(true);
        try {
            const res = await authFetch('/api/itineraries');
            if (res.ok) {
                const data = await res.json();
                setItineraries(data);
            }
        } catch (err) {
            console.error("Error fetching itineraries:", err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchItineraries();
    }, []);

    const handleSaved = (savedItinerary) => {
        setItineraries(prev => {
            const exists = prev.find(it => it.id === savedItinerary.id);
            if (exists) {
                return prev.map(it => it.id === savedItinerary.id ? savedItinerary : it);
            }
            return [savedItinerary, ...prev];
        });
        // 상세 모달이 열려있다면 정보 업데이트
        if (selectedItinerary?.id === savedItinerary.id) {
            setSelectedItinerary(savedItinerary);
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('정말 이 일정을 삭제하시겠습니까?')) return;
        try {
            const res = await authFetch(`/api/itineraries/${id}`, { method: 'DELETE' });
            if (res.ok) {
                setItineraries(prev => prev.filter(it => it.id !== id));
                if (selectedItinerary?.id === id) setSelectedItinerary(null);
            }
        } catch (err) {
            console.error('Delete failed:', err);
        }
    };

    const openEditor = (it = null) => {
        setEditingItinerary(it);
        setIsEditorOpen(true);
    };

    return (
        <div style={{
            background: '#0B0B15', // Night Flight dark theme
            color: 'white', 
            minHeight: 'calc(100vh - 160px)', 
            margin: '0 -20px', 
            padding: '30px 20px', 
            position: 'relative', 
            overflow: 'hidden',
            borderRadius: 'var(--radius-lg) var(--radius-lg) 0 0'
        }}>
            {/* Ambient Background Glows */}
            <div style={{ position: 'absolute', top: '0', right: '-100px', width: '300px', height: '300px', background: 'radial-gradient(circle, rgba(255, 92, 0, 0.15) 0%, transparent 70%)', filter: 'blur(80px)', borderRadius: '50%', zIndex: 0 }} />
            <div style={{ position: 'absolute', bottom: '100px', left: '-100px', width: '250px', height: '250px', background: 'radial-gradient(circle, rgba(99, 102, 241, 0.15) 0%, transparent 70%)', filter: 'blur(60px)', borderRadius: '50%', zIndex: 0 }} />

            <div style={{ position: 'relative', zIndex: 1 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '32px' }}>
                    <div>
                        <h2 className="heading-m" style={{ display: 'flex', alignItems: 'center', gap: '10px', color: 'white' }}>
                            <span style={{ 
                                width: '12px', 
                                height: '12px', 
                                borderRadius: '50%', 
                                background: 'var(--primary-gradient)',
                                boxShadow: '0 0 15px var(--primary-orange)'
                            }}></span> 
                            MY FLIGHTS
                        </h2>
                        <p style={{ fontSize: '12px', color: 'var(--text-muted)', fontWeight: 600, marginTop: '4px' }}>Upcoming & Past Itineraries</p>
                    </div>
                    <button onClick={fetchItineraries} style={{ 
                        color: 'rgba(255,255,255,0.5)', 
                        display: 'flex', 
                        alignItems: 'center', 
                        gap: '6px', 
                        fontSize: '11px', 
                        fontWeight: 700,
                        background: 'rgba(255,255,255,0.05)', 
                        padding: '8px 14px',
                        borderRadius: 'var(--radius-full)',
                        border: '1px solid rgba(255,255,255,0.1)'
                    }}>
                        <RotateCcw size={14} /> REFRESH
                    </button>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                    {itineraries.map((it, idx) => (
                        <div key={it.id} className="animate-fade" style={{ animationDelay: `${idx * 0.1}s` }}>
                            <TicketCard
                                itinerary={it}
                                onViewRoute={(itinerary) => setSelectedItinerary(itinerary)}
                            />
                        </div>
                    ))}
                    {!loading && itineraries.length === 0 && (
                        <div className="glass-dark animate-fade" style={{ 
                            textAlign: 'center', 
                            padding: '80px 30px', 
                            borderRadius: 'var(--radius-lg)',
                            border: '1px dashed rgba(255, 255, 255, 0.15)'
                        }}>
                            <div style={{ fontSize: '56px', marginBottom: '20px' }}>✈️</div>
                            <p style={{ color: 'rgba(255,255,255,0.6)', marginBottom: '24px', fontWeight: 600 }}>아직 등록된 여행 항공권이 없습니다.</p>
                            <button className="primary-btn" onClick={() => openEditor()}>
                                첫 비행기표 발권하기
                            </button>
                        </div>
                    )}
                    {loading && (
                        <div style={{ textAlign: 'center', padding: '60px 0', color: 'rgba(255,255,255,0.4)', fontWeight: 600 }}>
                            <span className="animate-pulse">Loading Flight Data...</span>
                        </div>
                    )}
                </div>
            </div>

            {/* Floating Action Button inside Tab */}
            <button
                onClick={() => openEditor()}
                className="primary-btn"
                style={{
                    position: 'fixed', 
                    bottom: '110px', 
                    right: 'calc(50% - 240px + 24px)',
                    width: '64px', 
                    height: '64px', 
                    borderRadius: '50%', 
                    zIndex: 100,
                    boxShadow: '0 15px 30px rgba(255, 92, 0, 0.4)'
                }}
            >
                <Plus size={32} strokeWidth={3} />
            </button>

            {/* Editor Modal (Create/Edit) */}
            {isEditorOpen && (
                <ItineraryEditorModal 
                    itinerary={editingItinerary}
                    onClose={() => setIsEditorOpen(false)}
                    onSaved={handleSaved}
                />
            )}

            {/* Route Detail Modal */}
            {selectedItinerary && (
                <RouteDetailModal 
                    itinerary={selectedItinerary}
                    onClose={() => setSelectedItinerary(null)}
                    onEdit={() => openEditor(selectedItinerary)}
                    onDelete={() => handleDelete(selectedItinerary.id)}
                />
            )}
        </div>
    );
};

export default ItineraryTab;
