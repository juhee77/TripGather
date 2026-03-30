---
name: TripGatherUI
description: Guidelines and standards for working with TripGather's travel-themed UI system, including glassmorphism, ticket/passport components, and design tokens.
---

# TripGather UI Development Skill

This skill provides comprehensive guidelines for maintaining consistent UI/UX across the TripGather platform. Adhering to these standards ensures a premium, cohesive travel-themed experience.

## Core Design Principles

1.  **Travel Theme**: Everything should evoke the feeling of travel (Tickets, Passports, Stamps, Flight Paths).
2.  **Premium Glassmorphism**: Use `glass` and `glass-dark` classes for surfaces. Maintain a high `backdrop-filter: blur(12px)`.
3.  **Vibrant Accents**: Use `--primary-orange` for primary actions and `--secondary-purple` / `--secondary-blue` for supplementary accents.
4.  **Mobile-First**: The layout is optimized for a maximum width of `480px`.

## Standard Components

### 1. Card Container (`Card.jsx`)
Use for general content sections. It handles common glassmorphism, shadows, and hover animations.

- **Props**:
    - `glass`: Boolean (default `true`). Apply glassmorphism.
    - `padding`: Boolean (default `true`). Apply standard `24px` padding.
    - `animate`: Boolean (default `true`). Apply entry fade-in animation.

### 2. Ticket Layout (`TicketBase.jsx`)
Use for anything representing a voucher, flight ticket, or mission card.

- **Structure**:
    - `header`: React element for the top section (above the dashed line).
    - `footer`: React element for the action area (bottom gray background).
    - `children`: Main content between the header and footer.
- **Visual Features**: Includes decorative circular cutouts at the ends of the dashed separator.

### 3. Primary Button (`PrimaryButton.jsx`)
The standard button for all interactions.

- **Variants**:
    - `primary`: Orange gradient (Main actions).
    - `secondary`: Light gray (Secondary actions like "View Details").
    - `glass`: Transparent white border (Night mode or image overlays).
    - `outline`: Border-only (Low priority actions).

## Design Tokens & Utilities

Refer to `index.css` for these tokens:

- **Colors**: `--primary-orange`, `--secondary-purple`, `--text-muted`.
- **Spacing Icons**: Use `Lucide` icons with a standard size of `16px` - `24px`.
- **Layouts**:
    - `.flex-between`: Space-between alignment.
    - `.gap-md`: Consistent `16px` gap.
    - `.label-muted`: Standard tiny, bold, muted uppercase labels.

## When to use which Component

| Feature | Component |
| :--- | :--- |
| User Profile Info | `Card` |
| Itinerary / Mission | `TicketBase` |
| Navigation Items | `.icon-circle` |
| Main Action Buttons | `PrimaryButton (variant="primary")` |
