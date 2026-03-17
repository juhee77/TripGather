# TripGather Project Roadmap - TODO List

## 1. Chat & Community Features
- [ ] **Group Chat Rooms**: Implement real-time group chat for each gathering/moim.
- [ ] **Chat Service**: Backend WebSocket support (Spring Message + STOMP).
- [ ] **Chat UI**: Interactive chat window with message history and participant list.
- [ ] **Direct Messaging**: 1:1 chat between users.

## 2. Gathering (Moim) Enhancements
- [ ] **Gathering CRUD Refactoring**: Align with the "Full CRUD" policy (Daily schedules, Participant management).
- [ ] **Member Approval System**: Host can approve/reject join requests.
- [ ] **Gathering Tags & Search**: Filter by categories and location.

## 3. Infrastructure & Security
- [ ] **Notification System**: SSE or Push notifications for chat and gathering updates.
- [ ] **Media Upload**: Profile and gathering image uploads using S3 or local storage.

## 4. Completed Tasks
- [x] JWT Authentication & OAuth2 Integration
- [x] Itinerary Management (Daily CRUD)
- [x] Backend Integration Test (Given-When-Then)
