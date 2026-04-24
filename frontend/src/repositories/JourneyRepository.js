import { authFetch } from '../api/client';

class JourneyRepository {
  async fetchMine(email) {
    if (!email) return [];
    const res = await authFetch(`/api/my-trips?email=${encodeURIComponent(email)}`);
    if (!res.ok) throw new Error('Failed to fetch my journeys');
    return res.json();
  }

  async add(itineraryId, email) {
    if (!email) throw new Error('User email required');
    const res = await authFetch(`/api/my-trips/clone?originalId=${itineraryId}&email=${encodeURIComponent(email)}`, {
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

  async toggleShare(itineraryId, email, isPublic) {
    const res = await authFetch(`/api/my-trips/${itineraryId}/share?email=${encodeURIComponent(email)}&isPublic=${isPublic}`, {
      method: 'PATCH',
    });
    if (!res.ok) throw new Error('Failed to toggle share');
    return res.json();
  }
}

export default new JourneyRepository();

