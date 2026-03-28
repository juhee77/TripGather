import { authFetch, apiUrl } from '../api/client';

class ItineraryRepository {
  async fetchAll() {
    const response = await fetch(apiUrl('/api/itineraries'));
    if (!response.ok) throw new Error('Failed to fetch itineraries');
    return response.json();
  }

  async getById(id) {
    const response = await fetch(apiUrl(`/api/itineraries/${id}`));
    if (!response.ok) throw new Error('Failed to fetch itinerary');
    return response.json();
  }

  async create(itineraryData) {
    const response = await authFetch('/api/itineraries', {
      method: 'POST',
      body: JSON.stringify(itineraryData)
    });
    if (!response.ok) throw new Error('Failed to create itinerary');
    return response.json();
  }

  async update(id, updateData) {
    const response = await authFetch(`/api/itineraries/${id}`, {
      method: 'PATCH',
      body: JSON.stringify(updateData)
    });
    if (!response.ok) throw new Error('Failed to update itinerary');
    return response.json();
  }

  async delete(id) {
    const response = await authFetch(`/api/itineraries/${id}`, {
      method: 'DELETE'
    });
    if (!response.ok) throw new Error('Failed to delete itinerary');
  }
}

export default new ItineraryRepository();
