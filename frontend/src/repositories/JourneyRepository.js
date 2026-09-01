import { authFetch } from '../api/client';

/**
 * 내 여정 API.
 * 대상 사용자는 서버가 JWT 인증 주체에서 판별하므로 email 을 보내지 않는다.
 */
class JourneyRepository {
  async fetchMine() {
    const res = await authFetch('/api/my-trips');
    if (!res.ok) throw new Error('Failed to fetch my journeys');
    return res.json();
  }

  async add(itineraryId) {
    const res = await authFetch(`/api/my-trips/clone?originalId=${itineraryId}`, {
      method: 'POST',
    });
    if (!res.ok) throw new Error('Failed to add journey');
    return res.json();
  }

  async remove(itineraryId) {
    // Normal itinerary deletion if the user owns it
    const res = await authFetch(`/api/itineraries/${itineraryId}`, {
      method: 'DELETE',
    });
    if (!res.ok) throw new Error('Failed to remove journey');
  }

  async toggleShare(itineraryId, isPublic) {
    const res = await authFetch(`/api/my-trips/${itineraryId}/share?isPublic=${isPublic}`, {
      method: 'PATCH',
    });
    if (!res.ok) throw new Error('Failed to toggle share');
    return res.json();
  }
}

export default new JourneyRepository();
