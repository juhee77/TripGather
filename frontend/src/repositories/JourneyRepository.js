import { authFetch } from '../api/client';

class JourneyRepository {
  async fetchMine() {
    const res = await authFetch('/api/journeys/me/itineraries');
    if (!res.ok) throw new Error('Failed to fetch my journeys');
    return res.json();
  }

  async add(itineraryId) {
    const res = await authFetch(`/api/journeys/me/itineraries/${itineraryId}`, {
      method: 'POST',
    });
    if (!res.ok) throw new Error('Failed to add journey');
    return res.json();
  }

  async remove(itineraryId) {
    const res = await authFetch(`/api/journeys/me/itineraries/${itineraryId}`, {
      method: 'DELETE',
    });
    if (!res.ok) throw new Error('Failed to remove journey');
  }
}

export default new JourneyRepository();

