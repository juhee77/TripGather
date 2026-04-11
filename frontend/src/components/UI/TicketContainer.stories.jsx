import TicketContainer from './TicketContainer';

export default {
  title: 'UI/TicketContainer',
  component: TicketContainer,
};

export const Default = {
  args: {
    topSection: (
      <div style={{ padding: '20px', background: 'var(--primary-gradient)', color: 'white', borderTopLeftRadius: '16px', borderTopRightRadius: '16px' }}>
        <h3 style={{ margin: 0, fontWeight: 900 }}>SEOUL TO BUSAN</h3>
        <p style={{ margin: '8px 0 0 0', opacity: 0.8 }}>Flight TG-102</p>
      </div>
    ),
    bottomSection: (
      <div style={{ paddingTop: '20px' }}>
        <p style={{ margin: 0, color: 'var(--text-muted)' }}>Passenger</p>
        <h4 style={{ margin: '4px 0 16px 0' }}>Jihyun Lee</h4>
        <button className="primary-btn">View Details</button>
      </div>
    ),
  },
};
