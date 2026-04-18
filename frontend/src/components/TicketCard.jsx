import React from 'react';
import { Plane, Calendar, MapPin, ChevronRight, CheckCircle } from 'lucide-react';
import TicketContainer from './UI/TicketContainer';
import PrimaryButton from './UI/PrimaryButton';
import { useUser } from '../contexts/UserContext';

const TicketCard = ({ itinerary, onViewRoute, onStartMission, onEdit }) => {
    const { user: currentUser } = useUser();
    const isHost = currentUser && (itinerary.author === currentUser.name || itinerary.itineraryAuthor === currentUser.name);
    const { title, author, itineraryAuthor, description, createdAt } = itinerary;
    const date = createdAt ? new Date(createdAt).toLocaleDateString() : '2026-04-06';
    const isMission = !!itinerary.steps;

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
                <div style={{ 
                    background: 'rgba(99, 102, 241, 0.1)', 
                    padding: '6px 14px', 
                    borderRadius: '8px',
                    fontSize: '10px',
                    fontWeight: 900,
                    color: 'var(--secondary-purple)',
                    letterSpacing: '0.5px'
                }}>FIRST CLASS</div>
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
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
                <div>
                    <span className="label-muted">PASSENGER</span>
                    <p className="info-value" style={{ fontSize: '16px' }}>{author || itineraryAuthor}</p>
                </div>
                <div style={{ textAlign: 'right' }}>
                    <span className="label-muted">BOARDING DATE</span>
                    <p className="info-value" style={{ display: 'flex', alignItems: 'center', gap: '6px', justifyContent: 'flex-end' }}>
                        <Calendar size={14} color="var(--primary-orange)" strokeWidth={2.5} /> {date}
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
                <PrimaryButton 
                    variant="secondary"
                    onClick={(e) => {
                        e.stopPropagation();
                        onViewRoute && onViewRoute(itinerary);
                    }}
                    style={{ flex: 1, height: '52px', borderRadius: '14px' }}
                >
                    INFO
                </PrimaryButton>
                {isHost && !isMission ? (
                    <PrimaryButton 
                        variant="primary"
                        onClick={(e) => {
                            e.stopPropagation();
                            onEdit && onEdit(itinerary);
                        }}
                        style={{ flex: 2, height: '52px', borderRadius: '14px' }}
                    >
                        EDIT JOURNEY
                    </PrimaryButton>
                ) : (
                    <PrimaryButton 
                        variant="primary"
                        onClick={async (e) => {
                            e.stopPropagation();
                            if (isMission) {
                                onViewRoute && onViewRoute(itinerary);
                                return;
                            }
                            onStartMission && await onStartMission(itinerary.id);
                        }}
                        style={{ 
                            flex: 2, 
                            height: '52px', 
                            borderRadius: '14px',
                            background: isMission ? 'var(--text-primary)' : 'var(--primary-gradient)'
                        }}
                    >
                        {isMission ? (
                            <><CheckCircle size={18} style={{ marginRight: '8px' }} /> CONTINUE</>
                        ) : (
                            <>BOARDING NOW <ChevronRight size={18} style={{ marginLeft: '4px' }} /></>
                        )}
                    </PrimaryButton>
                )}
            </div>
        </div>
    );

    return (
        <TicketContainer 
            topSection={topSection} 
            bottomSection={bottomSection}
            onClick={() => onViewRoute && onViewRoute(itinerary)}
            className="itinerary-ticket"
        />
    );
};

export default TicketCard;
