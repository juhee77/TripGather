import { Plane, Calendar, MapPin, ChevronRight, Trash2 } from 'lucide-react';
import TicketContainer from './UI/TicketContainer';
import PrimaryButton from './UI/PrimaryButton';
import { useUser } from '../contexts/UserContext';

const TicketCard = ({ itinerary, onViewRoute, onEdit, onRemove, isMine: isMineProp, onClick, onInfo }) => {
    const { user: currentUser } = useUser();
    const isMine = isMineProp || (currentUser && (
        itinerary.ownerEmail === currentUser.email ||
        itinerary.authorEmail === currentUser.email ||
        (itinerary.author && typeof itinerary.author === 'object' && itinerary.author.email === currentUser.email)
    ));
    const isHost = isMine; // In the new system, mine means I can edit/remove it.
    const { title, author, itineraryAuthor, description, createdAt, startDate, endDate } = itinerary;
    
    const formatDate = (dateStr) => {
        if (!dateStr) return null;
        const d = new Date(dateStr);
        return d.toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' });
    };

    const dateDisplay = (startDate && endDate) 
        ? `${formatDate(startDate)} - ${formatDate(endDate)}`
        : (createdAt ? formatDate(createdAt) : '');

    const topSection = (
        <div style={{ padding: '24px 24px 0 24px' }}>
            <div className="flex-between" style={{ marginBottom: '20px' }}>
                <div style={{ 
                    display: 'flex', 
                    alignItems: 'center', 
                    gap: '8px',
                    color: 'var(--primary-orange)',
                    fontSize: '11px',
                    fontWeight: 900,
                    letterSpacing: '1.5px'
                }}>
                    <Plane size={16} fill="var(--primary-orange)" /> TRIPGATHER AIR
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    {itinerary.stampImageUrl && (
                        <div style={{ 
                            background: 'rgba(74, 222, 128, 0.1)', 
                            padding: '6px 14px', 
                            borderRadius: '8px',
                            fontSize: '10px',
                            fontWeight: 900,
                            color: '#4ADE80',
                            letterSpacing: '0.5px',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '4px'
                        }}>
                            <span>STAMPED</span>
                        </div>
                    )}
                    <div style={{ 
                        background: 'rgba(99, 102, 241, 0.1)', 
                        padding: '6px 14px', 
                        borderRadius: '8px',
                        fontSize: '10px',
                        fontWeight: 900,
                        color: 'var(--secondary-purple)',
                        letterSpacing: '0.5px'
                    }}>FIRST CLASS</div>
                    {onRemove && (
                        <button 
                            onClick={(e) => {
                                e.stopPropagation();
                                if (window.confirm('이 여행을 제거하시겠습니까?')) {
                                    onRemove(itinerary.id);
                                }
                            }}
                            className="icon-circle"
                            style={{ width: '32px', height: '32px', background: 'rgba(0,0,0,0.05)', border: 'none' }}
                        >
                            <Trash2 size={14} color="var(--text-sub)" />
                        </button>
                    )}
                </div>
            </div>

            <div style={{ marginBottom: '8px' }}>
                <span className="label-orange">FLIGHT TITLE</span>
                <h3 className="heading-m" style={{ marginTop: '4px', lineHeight: 1.3 }}>
                    {title || itinerary.itineraryTitle}
                </h3>
            </div>
        </div>
    );

    const bottomSection = (
        <div className="flex-column" style={{ gap: '20px', paddingTop: '8px' }}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.5fr', gap: '24px' }}>
                <div>
                    <span className="label-muted">PASSENGER</span>
                    <p className="info-value" style={{ fontSize: '16px' }}>{author || itineraryAuthor}</p>
                </div>
                <div style={{ textAlign: 'right' }}>
                    <span className="label-muted">BOARDING DATE</span>
                    <p className="info-value" style={{ display: 'flex', alignItems: 'center', gap: '6px', justifyContent: 'flex-end', fontSize: '13px' }}>
                        <Calendar size={14} color="var(--primary-orange)" strokeWidth={2.5} /> {dateDisplay}
                    </p>
                </div>
            </div>

            <div style={{ padding: '16px', background: 'var(--bg-color)', borderRadius: '16px', border: '1px solid var(--border-color)' }}>
                <span className="label-muted">DESTINATION • LOG</span>
                <p className="text-s" style={{ fontWeight: 600, display: 'flex', gap: '8px', marginTop: '6px', color: 'var(--text-main)' }}>
                    <MapPin size={16} color="var(--secondary-blue)" strokeWidth={2.5} style={{ flexShrink: 0 }} />
                    {description || (itinerary.itinerary && itinerary.itinerary.description)}
                </p>
            </div>

            <div style={{ display: 'flex', gap: '12px', marginTop: '4px' }}>
                {isMine && (
                    <PrimaryButton 
                        variant="secondary"
                        onClick={(e) => {
                            e.stopPropagation();
                            onEdit && onEdit(itinerary);
                        }}
                        style={{ flex: 1, height: '52px', borderRadius: '14px', border: '1px solid var(--border-color)' }}
                    >
                        EDIT
                    </PrimaryButton>
                )}

                <PrimaryButton 
                    variant="primary"
                    onClick={(e) => {
                        e.stopPropagation();
                        onViewRoute && onViewRoute(itinerary);
                    }}
                    style={{ 
                        flex: isMine ? 1.5 : 2, 
                        height: '52px', 
                        borderRadius: '14px',
                        background: 'var(--primary-gradient)'
                    }}
                >
                    <>OPEN <ChevronRight size={18} style={{ marginLeft: '4px' }} /></>
                </PrimaryButton>
            </div>
        </div>
    );

    return (
        <TicketContainer 
            topSection={topSection} 
            bottomSection={bottomSection}
            onClick={onClick || (() => onViewRoute && onViewRoute(itinerary))}
            className="itinerary-ticket"
        />
    );
};

export default TicketCard;
