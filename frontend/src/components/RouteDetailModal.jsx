import React, { useEffect, useState } from 'react';
import { X, MapPin, ChevronRight, Share2, Heart, MessageCircle } from 'lucide-react';

const RouteDetailModal = ({ itinerary, onClose }) => {
    const [isVisible, setIsVisible] = useState(false);

    useEffect(() => {
        setIsVisible(true);
    }, []);

    // Placeholder route points if none exist
    const displayPoints = itinerary.routePoints && itinerary.routePoints.length > 0 
        ? itinerary.routePoints 
        : [
            { label: 'Departure City', sequenceOrder: 1 },
            { label: 'Transit Hub', sequenceOrder: 2 },
            { label: 'Destination Oasis', sequenceOrder: 3 }
        ];

    return (
        <div style={{
            position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
            backgroundColor: 'rgba(0,0,0,0.85)', backdropFilter: 'blur(12px)', zIndex: 2000,
            display: 'flex', justifyContent: 'center', alignItems: 'center'
        }}>
            <div style={{
                background: '#0D0D19', 
                width: '100%', maxWidth: '480px', height: '100vh', 
                position: 'relative', display: 'flex', flexDirection: 'column',
                transition: 'transform 0.4s cubic-bezier(0.16, 1, 0.3, 1)',
                transform: isVisible ? 'translateY(0)' : 'translateY(100%)',
                color: 'white', overflow: 'hidden'
            }}>
                {/* Header (Insta Style) */}
                <header style={{ 
                    padding: '16px 20px', 
                    display: 'flex', 
                    justifyContent: 'space-between', 
                    alignItems: 'center',
                    borderBottom: '1px solid rgba(255,255,255,0.05)'
                }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                        <div style={{ width: '32px', height: '32px', borderRadius: '50%', background: 'linear-gradient(45deg, #f09433 0%, #e6683c 25%, #dc2743 50%, #cc2366 75%, #bc1888 100%)', padding: '2px' }}>
                            <div style={{ width: '100%', height: '100%', borderRadius: '50%', background: '#0D0D19', overflow: 'hidden' }}>
                                <img src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${itinerary.author}`} alt="author" style={{ width: '100%', height: '100%' }} />
                            </div>
                        </div>
                        <div>
                            <div style={{ fontSize: '14px', fontWeight: 700 }}>{itinerary.author}</div>
                            <div style={{ fontSize: '11px', color: '#888' }}>Travel Log • {itinerary.title}</div>
                        </div>
                    </div>
                    <button onClick={onClose} style={{ padding: '8px', color: 'white' }}>
                        <X size={24} />
                    </button>
                </header>

                {/* Main Content (Vertical Log) */}
                <div className="hide-scrollbar" style={{ flex: 1, overflowY: 'auto', padding: '30px 24px' }}>
                    
                    {/* Hero Section */}
                    <div style={{ marginBottom: '40px', position: 'relative' }}>
                        <h1 style={{ fontSize: '32px', fontWeight: 900, marginBottom: '8px', background: 'linear-gradient(to right, #fff, #888)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
                            {itinerary.title}
                        </h1>
                        <p style={{ color: '#aaa', fontSize: '15px', lineHeight: '1.6' }}>
                            {itinerary.description}
                        </p>
                    </div>

                    {/* Timeline Log */}
                    <div style={{ position: 'relative', paddingLeft: '40px' }}>
                        {/* Vertical line through points */}
                        <div style={{ 
                            position: 'absolute', left: '15px', top: '10px', bottom: '10px', 
                            width: '2px', background: 'linear-gradient(to bottom, var(--primary), #333)', opacity: 0.3 
                        }} />

                        {displayPoints.map((point, index) => (
                            <div key={index} style={{ 
                                position: 'relative', 
                                marginBottom: '48px',
                                animation: `slideUp 0.5s ease-out forwards ${index * 0.15}s`,
                                opacity: 0,
                                transform: 'translateY(20px)'
                            }}>
                                {/* Dot */}
                                <div style={{ 
                                    position: 'absolute', left: '-30px', top: '4px',
                                    width: '12px', height: '12px', borderRadius: '50%',
                                    background: index === 0 ? 'var(--primary)' : index === displayPoints.length - 1 ? '#fff' : '#444',
                                    boxShadow: index === 0 ? '0 0 10px var(--primary)' : 'none',
                                    zIndex: 2
                                }} />
                                
                                <div style={{ 
                                    background: 'rgba(255,255,255,0.03)',
                                    borderRadius: '16px',
                                    padding: '16px 20px',
                                    border: '1px solid rgba(255,255,255,0.05)'
                                }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
                                        <span style={{ fontSize: '10px', color: 'var(--primary)', fontWeight: 800, letterSpacing: '1px' }}>
                                            STEP {point.sequenceOrder || index + 1}
                                        </span>
                                        <span style={{ fontSize: '10px', color: '#666' }}>ROUTE MARK</span>
                                    </div>
                                    <h3 style={{ fontSize: '18px', fontWeight: 700, marginBottom: '8px', color: '#eee' }}>
                                        {point.label}
                                    </h3>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', color: '#888' }}>
                                        <MapPin size={12} /> {itinerary.location || 'Exploring locally'}
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>

                    {/* Footer Actions (Feed Style) */}
                    <div style={{ 
                        marginTop: '20px', 
                        paddingTop: '30px', 
                        borderTop: '1px solid rgba(255,255,255,0.05)',
                        display: 'flex',
                        gap: '20px'
                    }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '14px', fontWeight: 600 }}>
                            <Heart size={20} /> 128
                        </div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '14px', fontWeight: 600 }}>
                            <MessageCircle size={20} /> 24
                        </div>
                        <div style={{ marginLeft: 'auto' }}>
                            <Share2 size={20} />
                        </div>
                    </div>
                </div>

                {/* Bottom CTA */}
                <footer style={{ padding: '20px', borderTop: '1px solid rgba(255,255,255,0.05)' }}>
                    <button 
                        onClick={onClose}
                        style={{
                            width: '100%', height: '56px', borderRadius: '16px',
                            background: 'white', color: 'black', fontWeight: 800, fontSize: '16px',
                            display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '10px'
                        }}
                    >
                        JOURNEY CONTINUES <ChevronRight size={20} />
                    </button>
                </footer>
            </div>

            <style>{`
                @keyframes slideUp {
                    from { transform: translateY(20px); opacity: 0; }
                    to { transform: translateY(0); opacity: 1; }
                }
            `}</style>
        </div>
    );
};

export default RouteDetailModal;
