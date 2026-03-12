import React, { useState, useEffect } from 'react';
import TicketCard from './TicketCard';
import CreateItineraryModal from './CreateItineraryModal';
import { Plus, RotateCcw } from 'lucide-react';

const ItineraryTab = () => {
    const [itineraries, setItineraries] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);

    const fetchItineraries = () => {
        setLoading(true);
        fetch('http://localhost:8080/api/itineraries')
            .then(res => res.json())
            .then(data => {
                setItineraries(data);
                setLoading(false);
            })
            .catch(err => {
                console.error("Error fetching itineraries:", err);
                setLoading(false);
            });
    };

    useEffect(() => {
        fetchItineraries();
    }, []);

    const handleCreated = (newItinerary) => {
        setItineraries(prev => [newItinerary, ...prev]);
    };

    return (
        <div style={{
            background: '#0B0B15', // Night Flight dark theme
            color: '#E0E0E0',
            minHeight: 'calc(100vh - 200px)',
            margin: '0 -20px', // Compensation for parent padding
            padding: '20px',
            position: 'relative',
            overflow: 'hidden'
        }}>
            {/* Ambient Background Glows */}
            <div style={{
                position: 'absolute',
                top: '-50px',
                right: '-50px',
                width: '150px',
                height: '150px',
                background: 'rgba(255, 123, 84, 0.1)',
                filter: 'blur(60px)',
                borderRadius: '50%',
                zIndex: 0
            }} />
            <div style={{
                position: 'absolute',
                bottom: '100px',
                left: '-30px',
                width: '100px',
                height: '100px',
                background: 'rgba(100, 100, 255, 0.1)',
                filter: 'blur(50px)',
                borderRadius: '50%',
                zIndex: 0
            }} />

            <div style={{ position: 'relative', zIndex: 1 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                    <h2 style={{ fontSize: '18px', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <span style={{ color: 'var(--primary)' }}>•</span> MY FLIGHTS
                    </h2>
                    <button 
                        onClick={fetchItineraries}
                        style={{ color: '#888', display: 'flex', alignItems: 'center', gap: '4px', fontSize: '12px', background: 'none', border: 'none', cursor: 'pointer' }}
                    >
                        <RotateCcw size={14} /> REFRESH
                    </button>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                    {itineraries.map(it => (
                        <TicketCard
                            key={it.id}
                            title={it.title}
                            author={it.author}
                            date={it.createdAt ? new Date(it.createdAt).toLocaleDateString() : undefined}
                            description={it.description}
                        />
                    ))}
                    {!loading && itineraries.length === 0 && (
                        <div style={{ 
                            textAlign: 'center', 
                            padding: '60px 20px', 
                            background: 'rgba(255,255,255,0.03)', 
                            borderRadius: '16px',
                            border: '1px dashed rgba(255,255,255,0.1)'
                        }}>
                            <p style={{ color: '#888', marginBottom: '16px' }}>저장된 일정이 없습니다.</p>
                            <button 
                                onClick={() => setIsModalOpen(true)}
                                style={{ 
                                    padding: '10px 24px', 
                                    background: 'var(--primary)', 
                                    color: 'white', 
                                    border: 'none', 
                                    borderRadius: '8px', 
                                    fontWeight: 700, 
                                    cursor: 'pointer' 
                                }}
                            >
                                첫 일정 만들기
                            </button>
                        </div>
                    )}
                    {loading && (
                        <div style={{ textAlign: 'center', padding: '40px 0', color: '#888' }}>
                            Fetching flight data...
                        </div>
                    )}
                </div>
            </div>

            {/* Floating Action Button inside Tab */}
            <button
                onClick={() => setIsModalOpen(true)}
                style={{
                    position: 'fixed',
                    bottom: '100px',
                    right: 'calc(50% - 240px + 24px)',
                    background: 'var(--primary)',
                    color: 'white',
                    width: '56px',
                    height: '56px',
                    borderRadius: '28px',
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    boxShadow: '0 8px 16px rgba(255, 123, 84, 0.4)',
                    zIndex: 100,
                    border: 'none',
                    cursor: 'pointer'
                }}
            >
                <Plus size={28} strokeWidth={2.5} />
            </button>

            {/* Create Modal */}
            {isModalOpen && (
                <CreateItineraryModal 
                    onClose={() => setIsModalOpen(false)}
                    onCreated={handleCreated}
                />
            )}
        </div>
    );
};

export default ItineraryTab;
