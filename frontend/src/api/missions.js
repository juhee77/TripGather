import client from './client';

const base = (gatheringId) => `/api/gatherings/${gatheringId}/missions`;

/* 크루 */

export const getMissions = async (gatheringId) => {
  const response = await client.get(base(gatheringId));
  return response.data;
};

export const getMissionProgress = async (gatheringId) => {
  const response = await client.get(`${base(gatheringId)}/progress`);
  return response.data;
};

export const submitMission = async (gatheringId, missionId, { photoUrl, memo } = {}) => {
  const response = await client.post(`${base(gatheringId)}/${missionId}/submit`, { photoUrl, memo });
  return response.data;
};

/* 호스트 */

export const createMission = async (gatheringId, mission) => {
  const response = await client.post(base(gatheringId), mission);
  return response.data;
};

export const updateMission = async (gatheringId, missionId, mission) => {
  const response = await client.put(`${base(gatheringId)}/${missionId}`, mission);
  return response.data;
};

export const deleteMission = async (gatheringId, missionId) => {
  const response = await client.delete(`${base(gatheringId)}/${missionId}`);
  return response.data;
};

export const getPendingCompletions = async (gatheringId) => {
  const response = await client.get(`${base(gatheringId)}/completions/pending`);
  return response.data;
};

export const approveCompletion = async (gatheringId, completionId) => {
  const response = await client.patch(`${base(gatheringId)}/completions/${completionId}/approve`);
  return response.data;
};

export const rejectCompletion = async (gatheringId, completionId) => {
  const response = await client.patch(`${base(gatheringId)}/completions/${completionId}/reject`);
  return response.data;
};
