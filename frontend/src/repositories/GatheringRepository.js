import { authFetch, apiUrl } from '../api/client';

/**
 * GatheringRepository encapsulates data fetching logic.
 * It acts as the "Model" layer in MVVM.
 */
class GatheringRepository {
  async fetchAll(location = null) {
    const path = location && location !== '전체' 
      ? `/api/gatherings?location=${encodeURIComponent(location)}`
      : '/api/gatherings';
    
    const response = await fetch(apiUrl(path));
    if (!response.ok) throw new Error('Failed to fetch gatherings');
    return response.json();
  }

  async search(filters = {}) {
    const params = new URLSearchParams();
    if (filters.location && filters.location !== '전체') params.append('location', filters.location);
    if (filters.query) params.append('query', filters.query);
    if (filters.availableOnly) params.append('availableOnly', 'true');
    
    const queryString = params.toString();
    const path = `/api/gatherings/search${queryString ? `?${queryString}` : ''}`;
    
    const response = await fetch(apiUrl(path));
    if (!response.ok) throw new Error('Failed to search gatherings');
    return response.json();
  }

  async create(gatheringData) {
    const response = await authFetch('/api/gatherings', {
      method: 'POST',
      body: JSON.stringify(gatheringData)
    });
    if (!response.ok) throw new Error('Failed to create gathering');
    return response.json();
  }

  async join(gatheringId) {
    const response = await authFetch(`/api/gatherings/${gatheringId}/join`, {
      method: 'POST'
    });
    if (!response.ok) throw new Error('Failed to join gathering');
    return response.json();
  }

  async update(gatheringId, updateData) {
    const response = await authFetch(`/api/gatherings/${gatheringId}`, {
      method: 'PUT',
      body: JSON.stringify(updateData)
    });
    if (!response.ok) throw new Error('Failed to update gathering');
    return response.json();
  }

  async delete(gatheringId) {
    const response = await authFetch(`/api/gatherings/${gatheringId}`, {
      method: 'DELETE'
    });
    if (!response.ok) throw new Error('Failed to delete gathering');
  }

  async approveMember(gatheringId, userId) {
    const response = await authFetch(`/api/gatherings/${gatheringId}/members/${userId}/approve`, {
      method: 'POST'
    });
    if (!response.ok) throw new Error('Failed to approve member');
  }

  async rejectMember(gatheringId, userId) {
    const response = await authFetch(`/api/gatherings/${gatheringId}/members/${userId}/reject`, {
      method: 'POST'
    });
    if (!response.ok) throw new Error('Failed to reject member');
  }
}

export default new GatheringRepository();
