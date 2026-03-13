# TripGather Future Plans

**Base date:** 2025-03-14  
**Goal:** Extend the current MVP with README features and scalability.  
**Note:** Use the **ui-ux-designer** subagent (`.cursor/agents/ui-ux-designer.md`) when designing or reviewing screens, flows, and visual consistency.

---

## 1. Short-term (high priority)

### 1.1 Map–backend integration
- [ ] **Populate map markers from API**  
  - In `MapPage`, call `GET /api/gatherings` (or only gatherings with lat/lng) and render markers.
  - On marker click: open gathering detail or wire "참여하기" to `POST /api/gatherings/{id}/join`.
- [ ] **Show itinerary routes on map**  
  - Display itineraries that have `RoutePoint`s as polyline + markers (dedicated API or reuse `/api/itineraries`).

### 1.2 User / auth foundation
- [ ] **User entity and basic API**  
  - Add User domain with minimal fields (id, name, bio, profileImageUrl, etc.).
  - Connect `/api/users/me` to DB or session (or migrate single mock user to a DB row).
- [ ] **Link gatherings/itineraries/comments to user**  
  - Align Gathering.host, Itinerary.author, Comment.author with User FK or identifier (incremental migration if needed).

### 1.3 My page features
- [ ] **My hosted / joined gatherings**  
  - Backend: `GET /api/gatherings?hostId=...` or `GET /api/users/me/gatherings`.
  - My page: show these lists (currently only Home "내 모임" + localStorage).
- [ ] **My itineraries**  
  - Backend: `GET /api/itineraries?authorId=...` or `GET /api/users/me/itineraries`; show on My page.
- [ ] **Profile edit**  
  - Name, bio, profile image update API and UI (after User domain is in place).

### 1.4 UI/UX (designer subagent)
- [ ] **Design review**  
  - Invoke **ui-ux-designer** for: Home tabs, FeedCard/GatheringDetailModal, MapPage bottom sheet, BottomNav, My page layout.
- [ ] **Design tokens**  
  - Document or centralize colors, spacing, typography (e.g. CSS variables in `index.css`); ensure loading/empty/error states exist for main flows.

---

## 2. Mid-term (core feature expansion)

### 2.1 Auth / security
- [ ] **Login and signup**  
  - Email/password or one social login for v1.
  - Spring Security + JWT (or session).
- [ ] **Authorization**  
  - Only author can create/update/delete gatherings and itineraries; minimal rules for comment deletion, etc.

### 2.2 Gathering improvements
- [ ] **Categories**  
  - Define categories (e.g. travel, food, sports, hobby, local) as enum or code table; add filters/tabs in UI.
- [ ] **Update / delete gathering**  
  - `PATCH` / `DELETE` `/api/gatherings/{id}` and corresponding modals/buttons in frontend.
- [ ] **Leave gathering**  
  - e.g. `POST /api/gatherings/{id}/leave` to decrease currentJoining and update participation.

### 2.3 Itinerary improvements
- [ ] **Create / edit RoutePoints**  
  - In create/edit itinerary flow: pick places on map → save as ordered RoutePoints.
  - Add PUT or PATCH in ItineraryController with route point list.
- [ ] **Itinerary detail map**  
  - In RouteDetailModal (or detail page): show route polyline and markers.

### 2.4 Chat
- [ ] **Per-gathering chat room**  
  - WebSocket (e.g. STOMP) or server push for simple room-based chat.
  - Chat list (ChatPage): list rooms for "my joined gatherings".

---

## 3. Long-term (scale and operations)

### 3.1 Location and search
- [ ] **Nearby gatherings**  
  - Radius search by current lat/lng (PostGIS or distance query when on PostgreSQL).
- [ ] **Region filter**  
  - e.g. "강남구 ▾" to filter gatherings/itineraries by district/area (address or coordinates).

### 3.2 Trust / manners
- [ ] **User manner score and badges**  
  - As in README: simple profile trust and badges; v1 can be participation count or simple review stats.

### 3.3 Operations and deployment
- [ ] **Environment separation**  
  - API base URL via env (e.g. `VITE_API_URL`); secure CORS and DB credentials for prod.
- [ ] **Tests**  
  - Keep and extend existing integration tests; add controller/service tests when auth and permissions are added.

### 3.4 DB and media (optional)
- [ ] **PostGIS**  
  - Consider PostgreSQL + PostGIS for richer location queries.
- [ ] **Image storage**  
  - Policy for gathering bgImageUrl etc. (e.g. S3 or local storage).

---

## 4. Suggested order of work

1. **Map–gathering integration** (1.1) → immediate user impact.
2. **User domain + My page data** (1.2, 1.3) → "my gatherings / my itineraries" backed by server.
3. **UI/UX pass** (1.4) → consistency and usability before feature growth.
4. **Login / signup** (2.1) → anchor all features to "who".
5. **Gathering / itinerary CRUD and leave** (2.2, 2.3) → complete daily usage flows.
6. **Chat** (2.4) → close the "갈 사람~" flow.
7. **Location search, region filter, manner/badges** (3.x) → differentiation and operations.

---

Update this document as the project progresses (add, complete, or reorder items).
