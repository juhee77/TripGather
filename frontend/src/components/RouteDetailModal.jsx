import { X, MapPin, ChevronRight, Share2, Heart, MessageCircle, Trash2, Edit3, Navigation } from 'lucide-react';

const RouteDetailModal = ({ itinerary, onClose, onEdit, onDelete }) => {
    const [isVisible, setIsVisible] = useState(false);

    useEffect(() => {
        setIsVisible(true);
    }, []);

    const groupedByDay = (() => {
        const points = itinerary.routePoints?.length > 0
            ? [...itinerary.routePoints].sort((a, b) =>
                a.dayNumber !== b.dayNumber ? a.dayNumber - b.dayNumber : a.sequenceOrder - b.sequenceOrder
              )
            : [
                { dayNumber: 1, dayLabel: 'Departure Day', sequenceOrder: 1, label: 'Incheon Int\'l Airport' },
                { dayNumber: 1, dayLabel: 'Departure Day', sequenceOrder: 2, label: 'Hotel Check-in' },
                { dayNumber: 2, dayLabel: 'Exploration Day', sequenceOrder: 1, label: 'Eiffel Tower' },
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
            backgroundColor: 'rgba(0,0,0,0.85)', backdropFilter: 'blur(20px)', zIndex: 2000,
            display: 'flex', justifyContent: 'center', alignItems: 'center'
        }}>
            <div className="glass-dark" style={{
                width: '100%', maxWidth: '480px', height: '100vh',
                position: 'relative', display: 'flex', flexDirection: 'column',
                transition: 'transform 0.5s cubic-bezier(0.16, 1, 0.3, 1)',
                transform: isVisible ? 'translateY(0)' : 'translateY(100%)',
                color: 'white', overflow: 'hidden',
                border: 'none',
                borderRadius: 'var(--radius-lg) var(--radius-lg) 0 0'
            }}>
                {/* Premium Blurred Header */}
                <header style={{
                    padding: '20px 24px',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    borderBottom: '1px solid rgba(255,255,255,0.1)',
                    background: 'rgba(13, 13, 25, 0.5)',
                    backdropFilter: 'blur(10px)'
                }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                        <div style={{ 
                            width: '40px', 
                            height: '40px', 
                            borderRadius: 'var(--radius-full)', 
                            background: 'var(--primary-gradient)', 
                            padding: '2px',
                            boxShadow: '0 0 15px rgba(255, 92, 0, 0.3)'
                        }}>
                            <div style={{ width: '100%', height: '100%', borderRadius: 'var(--radius-full)', background: '#0D0D19', overflow: 'hidden' }}>
                                <img src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${itinerary.author}`} alt="author" style={{ width: '100%', height: '100%' }} />
                            </div>
                        </div>
                        <div>
                            <div style={{ fontSize: '15px', fontWeight: 800 }}>{itinerary.author}</div>
                            <div style={{ fontSize: '11px', color: 'rgba(255,255,255,0.5)', fontWeight: 600 }}>TRAVEL LOG • JOURNEY</div>
                        </div>
                    </div>
                    <button onClick={onClose} className="icon-circle glass" style={{ width: '40px', height: '40px', background: 'rgba(255,255,255,0.1)' }}>
                        <X size={20} color="white" />
                    </button>
                </header>

                <div className="hide-scrollbar" style={{ flex: 1, overflowY: 'auto', padding: '32px 24px' }}>
                    {/* Hero Section */}
                    <div style={{ marginBottom: '48px', animation: 'fadeIn 0.6s ease-out' }}>
                        <h1 className="heading-l" style={{ 
                            marginBottom: '12px', 
                            background: 'linear-gradient(to bottom, #FFFFFF 0%, #A5B4FC 100%)', 
                            WebkitBackgroundClip: 'text', 
                            WebkitTextFillColor: 'transparent',
                            lineHeight: 1.2
                        }}>
                            {itinerary.title}
                        </h1>
                        <p className="text-s" style={{ color: 'rgba(255,255,255,0.6)', lineHeight: '1.7', fontSize: '15px' }}>
                            {itinerary.description}
                        </p>
                    </div>

                    {/* Enhanced Timeline */}
                    {groupedByDay.map((day, dayIdx) => (
                        <div key={dayIdx} style={{ marginBottom: '48px', animation: `fadeIn 0.5s ease-out forwards ${dayIdx * 0.15}s`, opacity: 0 }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '24px' }}>
                                <div style={{
                                    background: 'var(--primary-gradient)',
                                    borderRadius: '12px', padding: '6px 16px',
                                    fontSize: '12px', fontWeight: 900, letterSpacing: '1px', color: 'white',
                                    boxShadow: '0 8px 20px rgba(255, 92, 0, 0.2)'
                                }}>
                                    DAY {dayIdx + 1}
                                </div>
                                <span style={{ color: 'white', fontSize: '15px', fontWeight: 700 }}>{day.dayLabel}</span>
                                <div style={{ flex: 1, height: '1px', background: 'rgba(255,255,255,0.1)' }} />
                            </div>

                            <div style={{ position: 'relative', paddingLeft: '40px' }}>
                                <div style={{
                                    position: 'absolute', left: '11px', top: '12px', bottom: '12px',
                                    width: '2px', background: 'linear-gradient(to bottom, var(--primary-orange), var(--secondary-purple), transparent)'
                                }} />

                                {day.points.map((point, ptIdx) => (
                                    <div key={ptIdx} style={{
                                        position: 'relative', marginBottom: '20px',
                                        animation: `fadeIn 0.4s ease-out forwards ${(dayIdx * 0.1) + (ptIdx * 0.1)}s`,
                                        opacity: 0
                                    }}>
                                        <div style={{
                                            position: 'absolute', left: '-35px', top: '18px',
                                            width: '12px', height: '12px', borderRadius: '50%',
                                            background: ptIdx === 0 ? 'white' : 'transparent',
                                            border: '3px solid var(--primary-orange)',
                                            boxShadow: ptIdx === 0 ? '0 0 15px var(--primary-orange)' : 'none',
                                            zIndex: 2,
                                            transition: 'all 0.3s ease'
                                        }} />

                                        <div className="glass-dark" style={{
                                            padding: '20px',
                                            borderRadius: 'var(--radius-md)',
                                            border: '1px solid rgba(255,255,255,0.08)',
                                            background: 'rgba(255,255,255,0.03)'
                                        }}>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                                                <span style={{ fontSize: '10px', color: 'var(--primary-orange)', fontWeight: 900, letterSpacing: '1px' }}>
                                                    STOP {point.sequenceOrder}
                                                </span>
                                                <Navigation size={14} color="rgba(255,255,255,0.3)" />
                                            </div>
                                            <h4 style={{ fontSize: '17px', fontWeight: 700, color: 'white', marginBottom: '6px' }}>
                                                {point.label}
                                            </h4>
                                            {(point.lat && point.lng) && (
                                                <p style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', color: 'rgba(255,255,255,0.4)', fontWeight: 500 }}>
                                                    <MapPin size={12} color="var(--secondary-purple)" /> 
                                                    {point.lat.toFixed(4)}, {point.lng.toFixed(4)}
                                                </p>
                                            )}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    ))}
                </div>

                {/* Sticky Action Footer */}
                <footer className="glass" style={{ 
                    padding: '24px', 
                    borderTop: '1px solid rgba(255,255,255,0.1)', 
                    display: 'flex', 
                    flexDirection: 'column', 
                    gap: '16px',
                    background: 'rgba(13, 13, 25, 0.8)'
                }}>
                    <div style={{ display: 'flex', gap: '14px' }}>
                        <button
                            onClick={onEdit}
                            className="glass"
                            style={{
                                flex: 1, height: '60px', borderRadius: 'var(--radius-md)',
                                color: 'white', fontWeight: 800, fontSize: '15px',
                                display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '10px',
                                border: '1px solid rgba(255,255,255,0.2)'
                            }}
                        >
                            <Edit3 size={18} /> EDIT JOURNEY
                        </button>
                        <button
                            onClick={onDelete}
                            className="glass"
                            style={{
                                width: '60px', height: '60px', borderRadius: 'var(--radius-md)',
                                display: 'flex', justifyContent: 'center', alignItems: 'center',
                                border: '1px solid rgba(239, 68, 68, 0.2)',
                                background: 'rgba(239, 68, 68, 0.05)'
                            }}
                        >
                            <Trash2 size={20} color="#EF4444" />
                        </button>
                    </div>
                    <button
                        onClick={onClose}
                        className="primary-btn"
                        style={{
                            width: '100%', height: '60px', borderRadius: 'var(--radius-md)',
                            fontSize: '17px', gap: '12px'
                        }}
                    >
                        COMPLETE MISSION <ChevronRight size={20} />
                    </button>
                </footer>
            </div>
        </div>
    );
};

export default RouteDetailModal;
