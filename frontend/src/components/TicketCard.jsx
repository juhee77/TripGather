import React from 'react';
import { Plane, Calendar, MapPin } from 'lucide-react';
import { authFetch } from '../api/client';

const TicketCard = ({ itinerary, onViewRoute }) => {
    const { title, author, description, createdAt } = itinerary;
    const date = createdAt ? new Date(createdAt).toLocaleDateString() : '2026-03-12';
    
    return (
        <div className="animate-fade" style={{ borderRadius: 'var(--radius-lg)', overflow: 'hidden', marginBottom: '20px' }}>
            <div className="glass" style={{
                background: 'white',
                border: '1px solid var(--border-color)',
                display: 'flex',
                flexDirection: 'column',
                position: 'relative',
                boxShadow: 'var(--shadow-premium)'
            }}>
                {/* Top Ticket Section */}
                <div style={{
                    padding: '30px 24px',
                    background: 'white',
                    borderBottom: '2.5px dashed var(--border-color)',
                    position: 'relative'
                }}>
                    {/* Premium Cutouts */}
                    <div style={{ position: 'absolute', bottom: '-12px', left: '-12px', width: '24px', height: '24px', backgroundColor: '#0B0B15', borderRadius: '50%', zIndex: 2 }} />
                    <div style={{ position: 'absolute', bottom: '-12px', right: '-12px', width: '24px', height: '24px', backgroundColor: '#0B0B15', borderRadius: '50%', zIndex: 2 }} />

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                        <div style={{ 
                            display: 'flex', 
                            alignItems: 'center', 
                            gap: '8px',
                            color: 'var(--primary-orange)',
                            fontSize: '12px',
                            fontWeight: 900,
                            letterSpacing: '1.5px'
                        }}>
                            <Plane size={18} fill="var(--primary-orange)" /> TRIP GATHER AIR
                        </div>
                        <div style={{ 
                            background: 'var(--bg-color)', 
                            padding: '6px 14px', 
                            borderRadius: 'var(--radius-full)',
                            fontSize: '10px',
                            fontWeight: 800,
                            color: 'var(--text-muted)',
                            letterSpacing: '0.5px'
                        }}>BUSINESS CLASS</div>
                    </div>

                    <h3 className="heading-m" style={{ color: 'var(--text-primary)', marginBottom: '24px' }}>
                        {title}
                    </h3>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
                        <div>
                            <p style={{ fontSize: '10px', color: 'var(--text-muted)', fontWeight: 800, textTransform: 'uppercase', marginBottom: '6px' }}>Passenger</p>
                            <p style={{ fontSize: '15px', fontWeight: 700, color: 'var(--text-primary)' }}>{author}</p>
                        </div>
                        <div>
                            <p style={{ fontSize: '10px', color: 'var(--text-muted)', fontWeight: 800, textTransform: 'uppercase', marginBottom: '6px' }}>Date of Flight</p>
                            <p style={{ fontSize: '15px', fontWeight: 700, color: 'var(--text-primary)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                                <Calendar size={14} color="var(--primary-orange)" strokeWidth={2.5} /> {date}
                            </p>
                        </div>
                    </div>

                    <div style={{ marginTop: '20px', paddingTop: '20px', borderTop: '1px solid var(--bg-color)' }}>
                        <p style={{ fontSize: '10px', color: 'var(--text-muted)', fontWeight: 800, textTransform: 'uppercase', marginBottom: '8px' }}>Flight Path</p>
                        <p className="text-s" style={{ fontWeight: 600, display: 'flex', gap: '8px' }}>
                            <MapPin size={16} color="var(--secondary-purple)" strokeWidth={2.5} style={{ flexShrink: 0 }} />
                            {description}
                        </p>
                    </div>
                </div>

                {/* Bottom Ticket Section (Gate/Boarding) */}
                <div style={{
                    background: '#FAFAFB',
                    padding: '20px 24px',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    gap: '12px'
                }}>
                    <div style={{ flex: 1, display: 'none' }}> {/* Hide barcode for more button space */}
                        <div style={{
                            height: '30px', 
                            width: '80px', 
                            marginBottom: '6px',
                            background: 'repeating-linear-gradient(90deg, #1A1A1E, #1A1A1E 2px, transparent 2px, transparent 4px, #1A1A1E 4px, #1A1A1E 5px, transparent 5px, transparent 8px)',
                            opacity: 0.8
                        }}></div>
                    </div>
                    
                    <div style={{ display: 'flex', gap: '10px', width: '100%', justifyContent: 'flex-end' }}>
                        <button 
                            onClick={() => onViewRoute(itinerary)}
                            className="glass"
                            style={{ 
                                padding: '12px 20px', 
                                fontSize: '14px', 
                                fontWeight: 700,
                                color: 'var(--text-primary)',
                                borderRadius: 'var(--radius-md)',
                                background: 'white',
                                border: '1px solid var(--border-color)',
                                flex: 1
                            }}
                        >
                            상세 보기
                        </button>
                        <button 
                            onClick={async () => {
                                if (itinerary.steps) {
                                    onViewRoute(itinerary);
                                    return;
                                }
                                try {
                                    const res = await authFetch(`http://localhost:8080/api/missions/start/${itinerary.id}`, { method: 'POST' });
                                    if (res.ok) {
                                        const missionData = await res.json();
                                        onViewRoute(missionData);
                                    } else {
                                        onViewRoute(itinerary);
                                    }
                                } catch (e) { 
                                    console.error("Could not start mission:", e); 
                                    onViewRoute(itinerary);
                                }
                            }}
                            className="primary-btn"
                            style={{ 
                                padding: '12px 20px', 
                                fontSize: '14px', 
                                flex: 1.5,
                                whiteSpace: 'nowrap'
                            }}
                        >
                            {itinerary.steps ? '미션 계속하기' : '챌린지 참여하기'}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default TicketCard;
