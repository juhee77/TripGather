import React from 'react';
import { Plane, Calendar, MapPin } from 'lucide-react';

const TicketCard = ({ itinerary, onViewRoute }) => {
    const { title, author, description, createdAt } = itinerary;
    const date = createdAt ? new Date(createdAt).toLocaleDateString() : '2026-03-20';
    
    return (
        <div style={{
            background: 'white',
            borderRadius: '16px',
            boxShadow: '0 8px 24px rgba(0,0,0,0.06)',
            display: 'flex',
            flexDirection: 'column',
            position: 'relative',
            overflow: 'hidden',
            border: '1px solid #eee',
            marginBottom: '16px'
        }}>
            {/* Top Ticket Section */}
            <div style={{
                padding: '24px 20px',
                background: 'white',
                borderBottom: '2px dashed #e0e0e0',
                position: 'relative'
            }}>
                {/* Cutouts */}
                <div style={{ position: 'absolute', bottom: '-10px', left: '-11px', width: '20px', height: '20px', backgroundColor: '#0B0B15', borderRadius: '50%', border: '1px solid #eee', borderRightColor: 'transparent', borderTopColor: 'transparent', borderBottomColor: 'transparent', transform: 'rotate(45deg)' }} />
                <div style={{ position: 'absolute', bottom: '-10px', right: '-11px', width: '20px', height: '20px', backgroundColor: '#0B0B15', borderRadius: '50%', border: '1px solid #eee', borderLeftColor: 'transparent', borderTopColor: 'transparent', borderBottomColor: 'transparent', transform: 'rotate(-45deg)' }} />

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', fontSize: '12px', fontWeight: 800, color: 'var(--primary)', letterSpacing: '1px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <Plane size={16} fill="var(--primary)" /> TRIP AIRLINES
                    </div>
                    <div style={{ color: '#888' }}>ITINERARY</div>
                </div>

                <h3 style={{ fontSize: '22px', fontWeight: 800, color: '#222', marginBottom: '20px', lineHeight: '1.3' }}>
                    {title}
                </h3>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                    <div style={{ display: 'flex', flexDirection: 'column' }}>
                        <span style={{ fontSize: '10px', color: '#888', fontWeight: 700, textTransform: 'uppercase', marginBottom: '4px' }}>Passenger / Author</span>
                        <span style={{ fontSize: '14px', fontWeight: 700, color: '#333' }}>{author}</span>
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column' }}>
                        <span style={{ fontSize: '10px', color: '#888', fontWeight: 700, textTransform: 'uppercase', marginBottom: '4px' }}>Departure</span>
                        <span style={{ fontSize: '14px', fontWeight: 700, color: '#333', display: 'flex', alignItems: 'center', gap: '4px' }}>
                            <Calendar size={12} color="#888" /> {date}
                        </span>
                    </div>
                </div>

                <div style={{ marginTop: '16px', paddingTop: '16px', borderTop: '1px solid #f0f0f0' }}>
                    <span style={{ fontSize: '10px', color: '#888', fontWeight: 700, textTransform: 'uppercase', marginBottom: '8px', display: 'block' }}>Route Details</span>
                    <p style={{ fontSize: '14px', color: '#555', lineHeight: '1.5', margin: 0, display: 'flex', alignItems: 'flex-start', gap: '8px' }}>
                        <MapPin size={16} color="var(--primary)" style={{ flexShrink: 0, marginTop: '2px' }} />
                        {description}
                    </p>
                </div>
            </div>

            {/* Bottom Ticket Section */}
            <div style={{
                background: '#fcfcfc',
                padding: '16px 20px',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center'
            }}>
                <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                    <div style={{
                        height: '24px', width: '80%', marginBottom: '4px', opacity: 0.6,
                        background: 'repeating-linear-gradient(90deg, #333, #333 2px, transparent 2px, transparent 4px, #333 4px, #333 5px, transparent 5px, transparent 8px)'
                    }}></div>
                    <div style={{ fontFamily: 'monospace', fontSize: '11px', color: '#888', letterSpacing: '2px' }}>
                        RT-8X9-{Math.floor(Math.random() * 9000) + 1000}
                    </div>
                </div>
                <button 
                    onClick={() => onViewRoute(itinerary)}
                    style={{
                        background: 'var(--primary)', color: 'white', padding: '10px 20px',
                        borderRadius: '8px', fontWeight: 800, fontSize: '13px', border: 'none', cursor: 'pointer',
                        boxShadow: '0 4px 10px rgba(255, 123, 84, 0.3)'
                    }}
                >
                    VIEW ROUTE
                </button>
            </div>
        </div>
    );
};

export default TicketCard;
