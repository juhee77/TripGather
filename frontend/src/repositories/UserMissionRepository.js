import { authFetch } from '../api/client';

class UserMissionRepository {
  async startMission(itineraryId) {
    const response = await authFetch(`/api/missions/start/${itineraryId}`, {
      method: 'POST'
    });
    if (!response.ok) throw new Error('Failed to start mission');
    return response.json();
  }

  async completeMission(itineraryId) {
    const response = await authFetch(`/api/missions/complete/${itineraryId}`, {
      method: 'POST'
    });
    if (!response.ok) throw new Error('Failed to complete mission');
    return response.json();
  }

  async getMyMissions() {
    const response = await authFetch('/api/missions/me');
    if (!response.ok) throw new Error('Failed to fetch my missions');
    return response.json();
  }

  async getMyStamps() {
    const response = await authFetch('/api/missions/me/stamps');
    if (!response.ok) throw new Error('Failed to fetch stamps');
    return response.json();
  }

  async completeStep(missionId, stepId, memo, photoUrl) {
    const response = await authFetch(`/api/missions/${missionId}/steps/${stepId}/complete`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ memo, photoUrl })
    });
    if (!response.ok) throw new Error('Failed to complete mission step');
    return response.json();
  }
}

export default new UserMissionRepository();
