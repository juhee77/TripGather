import React, { useEffect, useState } from 'react';
import { X, MapPin, ChevronRight, Share2, Heart, MessageCircle } from 'lucide-react';

const RouteDetailModal = ({ itinerary, onClose }) => {
    const [isVisible, setIsVisible] = useState(false);

    useEffect(() => {
        setIsVisible(true);
    }, []);

    // routePoints를 dayNumber 기준으로 그룹핑
    const groupedByDay = (() => {
        const points = itinerary.routePoints?.length > 0
            ? [...itinerary.routePoints].sort((a, b) =>
                a.dayNumber !== b.dayNumber ? a.dayNumber - b.dayNumber : a.sequenceOrder - b.sequenceOrder
              )
            : [
                { dayNumber: 1, dayLabel: 'Day 1', sequenceOrder: 1, label: 'Departure' },
                { dayNumber: 1, dayLabel: 'Day 1', sequenceOrder: 2, label: 'Layover' },
                { dayNumber: 2, dayLabel: 'Day 2', sequenceOrder: 1, label: 'Destination' },
              ];

        const map = {};
        points.forEach(p => {
            const key = p.dayNumber ?? 1;
            if (!map[key]) map[key] = { dayLabel: p.dayLabel || `Day ${key}`, points: [] };
            map[key].points.push(p);
        });
        return Object.values(map);
    })();

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
                    <button onClick={onClose} style={{ padding: '8px', color: 'white', background: 'none', border: 'none', cursor: 'pointer' }}>
                        <X size={24} />
                    </button>
                </header>

                {/* Main Content */}
                <div className="hide-scrollbar" style={{ flex: 1, overflowY: 'auto', padding: '30px 24px' }}>

                    {/* Hero */}
                    <div style={{ marginBottom: '40px' }}>
                        <h1 style={{ fontSize: '28px', fontWeight: 900, marginBottom: '8px', background: 'linear-gradient(to right, #fff, #888)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
                            {itinerary.title}
                        </h1>
                        <p style={{ color: '#aaa', fontSize: '14px', lineHeight: '1.6' }}>
                            {itinerary.description}
                        </p>
                    </div>

                    {/* DAY-grouped timeline */}
                    {groupedByDay.map((day, dayIdx) => (
                        <div key={dayIdx} style={{ marginBottom: '40px', animation: `slideUp 0.4s ease-out forwards ${dayIdx * 0.1}s`, opacity: 0 }}>
                            {/* DAY Header */}
                            <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '20px' }}>
                                <div style={{
                                    background: 'linear-gradient(135deg, var(--primary), #ff4b4b)',
                                    borderRadius: '20px', padding: '4px 14px',
                                    fontSize: '11px', fontWeight: 800, letterSpacing: '1px', color: 'white'
                                }}>
                                    DAY {dayIdx + 1}
                                </div>
                                <span style={{ color: '#aaa', fontSize: '13px', fontWeight: 600 }}>{day.dayLabel}</span>
                                <div style={{ flex: 1, height: '1px', background: 'rgba(255,255,255,0.07)' }} />
                            </div>

                            {/* Points within this day */}
                            <div style={{ position: 'relative', paddingLeft: '36px' }}>
                                {/* Vertical connector line */}
                                <div style={{
                                    position: 'absolute', left: '11px', top: '10px', bottom: '10px',
                                    width: '2px', background: 'rgba(255,255,255,0.08)'
                                }} />

                                {day.points.map((point, ptIdx) => (
                                    <div key={ptIdx} style={{
                                        position: 'relative', marginBottom: '16px',
                                        animation: `slideUp 0.4s ease-out forwards ${(dayIdx * 0.1) + (ptIdx * 0.08)}s`,
                                        opacity: 0
                                    }}>
                                        {/* Step dot */}
                                        <div style={{
                                            position: 'absolute', left: '-26px', top: '16px',
                                            width: '10px', height: '10px', borderRadius: '50%',
                                            background: ptIdx === 0 && dayIdx === 0
                                                ? 'var(--primary)'
                                                : ptIdx === day.points.length - 1 && dayIdx === groupedByDay.length - 1
                                                    ? '#ffffff'
                                                    : '#444',
                                            boxShadow: ptIdx === 0 && dayIdx === 0 ? '0 0 8px var(--primary)' : 'none',
                                            zIndex: 2
                                        }} />

                                        <div style={{
                                            background: 'rgba(255,255,255,0.04)',
                                            borderRadius: '12px',
                                            padding: '14px 16px',
                                            border: '1px solid rgba(255,255,255,0.06)'
                                        }}>
                                            <div style={{ fontSize: '10px', color: 'var(--primary)', fontWeight: 800, letterSpacing: '1px', marginBottom: '4px' }}>
                                                STOP {point.sequenceOrder}
                                            </div>
                                            <h4 style={{ fontSize: '16px', fontWeight: 700, color: '#eee', marginBottom: '4px' }}>
                                                {point.label}
                                            </h4>
                                            {(point.lat && point.lng) && (
                                                <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11px', color: '#555' }}>
                                                    <MapPin size={10} /> {point.lat.toFixed(4)}, {point.lng.toFixed(4)}
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    ))}

                    {/* Footer Actions */}
                    <div style={{
                        marginTop: '20px', paddingTop: '24px',
                        borderTop: '1px solid rgba(255,255,255,0.05)',
                        display: 'flex', gap: '20px'
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
                            display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '10px',
                            border: 'none', cursor: 'pointer'
                        }}
                    >
                        JOURNEY CONTINUES <ChevronRight size={20} />
                    </button>
                </footer>
            </div>

            <style>{`
                @keyframes slideUp {
                    from { transform: translateY(16px); opacity: 0; }
                    to { transform: translateY(0); opacity: 1; }
                }
            `}</style>
        </div>
    );
};

export default RouteDetailModal;
