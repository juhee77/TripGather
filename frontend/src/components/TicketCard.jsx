import React from 'react';
import { Plane, Calendar, MapPin } from 'lucide-react';
import TicketBase from './UI/TicketBase';
import PrimaryButton from './UI/PrimaryButton';

const TicketCard = ({ itinerary, onViewRoute, onStartMission }) => {
    const { title, author, description, createdAt } = itinerary;
    const date = createdAt ? new Date(createdAt).toLocaleDateString() : '2026-03-12';

    const header = (
        <div className="flex-column gap-md">
            <div className="flex-between">
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

            <h3 className="heading-m" style={{ color: 'var(--text-primary)' }}>
                {title}
            </h3>
        </div>
    );

    const handleView = (e) => {
        if (e) e.stopPropagation();
        if (onViewRoute) onViewRoute(itinerary);
    };

    const footer = (
        <>
            <PrimaryButton 
                variant="secondary"
                onClick={handleView}
                style={{ flex: 1, height: '48px', fontSize: '14px' }}
            >
                상세 보기
            </PrimaryButton>
            <PrimaryButton 
                variant="primary"
                onClick={async (e) => {
                    if (e) e.stopPropagation();
                    if (itinerary.steps) {
                        handleView();
                        return;
                    }
                    if (onStartMission) {
                        await onStartMission(itinerary.id);
                    }
                }}
                style={{ flex: 1.5, height: '48px', fontSize: '14px', whiteSpace: 'nowrap' }}
            >
                {itinerary.steps ? '미션 계속하기' : '챌린지 참여하기'}
            </PrimaryButton>
        </>
    );

    return (
        <div className="animate-fade" style={{ marginBottom: '20px' }}>
            <TicketBase header={header} footer={footer}>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px', padding: '24px 0' }}>
                    <div>
                        <span className="label-muted">Passenger</span>
                        <p className="info-value">{author}</p>
                    </div>
                    <div>
                        <span className="label-muted">Date of Flight</span>
                        <p className="info-value" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <Calendar size={14} color="var(--primary-orange)" strokeWidth={2.5} /> {date}
                        </p>
                    </div>
                </div>

                <div style={{ paddingBottom: '24px', borderTop: '1px solid var(--bg-color)', paddingTop: '20px' }}>
                    <span className="label-muted">Flight Path</span>
                    <p className="text-s" style={{ fontWeight: 600, display: 'flex', gap: '8px' }}>
                        <MapPin size={16} color="var(--secondary-purple)" strokeWidth={2.5} style={{ flexShrink: 0 }} />
                        {description}
                    </p>
                </div>
            </TicketBase>
        </div>
    );
};

export default TicketCard;
