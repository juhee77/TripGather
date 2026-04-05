# TripGather UI Component Usage Examples

This guide demonstrates practical code implementations using the TripGather UI component library.

## Base Card Implementation

For a standard content container like a profile or gathering summary:

```jsx
import Card from './components/UI/Card';

const GatheringSummary = ({ title, host }) => {
  return (
    <Card className="flex-column gap-sm">
      <h3 className="heading-m">{title}</h3>
      <div className="flex-between">
        <span className="label-muted">Host</span>
        <span className="text-s text-bold">{host}</span>
      </div>
    </Card>
  );
};
```

## Detailed Ticket Implementation

For sophisticated multi-section designs like an itinerary or travel mission:

```jsx
import TicketBase from './components/UI/TicketBase';
import PrimaryButton from './components/UI/PrimaryButton';
import { Calendar } from 'lucide-react';

const TravelTicket = ({ itinerary }) => {
  const header = (
    <div className="flex-column gap-xs">
      <h3 className="heading-m">{itinerary.title}</h3>
      <span className="label-muted">Priority: High</span>
    </div>
  );

  const footer = (
    <>
      <PrimaryButton variant="secondary" style={{ flex: 1 }}>Details</PrimaryButton>
      <PrimaryButton variant="primary" style={{ flex: 1.5 }}>Join Mission</PrimaryButton>
    </>
  );

  return (
    <TicketBase header={header} footer={footer}>
      <div style={{ padding: '24px 0' }} className="flex-between">
        <div>
          <span className="label-muted">Departure</span>
          <span className="info-value">{itinerary.startLocation}</span>
        </div>
        <Calendar size={18} color="var(--primary-orange)" />
      </div>
    </TicketBase>
  );
};
```

## Button Variants Comparison

Always match the button variant to the hierarchy of the action:

```jsx
{/* Primary Action (Submit, Confirm, Join) */}
<PrimaryButton variant="primary">Confirm Booking</PrimaryButton>

{/* Secondary Action (Cancel, Later, Details) */}
<PrimaryButton variant="secondary">Cancel</PrimaryButton>

{/* Overlay Action (On top of images or in dark mode) */}
<PrimaryButton variant="glass">View Image</PrimaryButton>
```
