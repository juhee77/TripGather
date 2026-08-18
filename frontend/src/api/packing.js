import client from './client';

export const getPackingItems = async (tripId) => {
  const response = await client.get(`/api/trips/${tripId}/packing`);
  return response.data;
};

export const initDefaultPackingItems = async (tripId) => {
  const response = await client.post(`/api/trips/${tripId}/packing/init`);
  return response.data;
};

export const addPackingItem = async (tripId, name, category) => {
  const response = await client.post(`/api/trips/${tripId}/packing`, { name, category });
  return response.data;
};

export const togglePackingItem = async (tripId, itemId) => {
  const response = await client.patch(`/api/trips/${tripId}/packing/${itemId}/toggle`);
  return response.data;
};

export const deletePackingItem = async (tripId, itemId) => {
  const response = await client.delete(`/api/trips/${tripId}/packing/${itemId}`);
  return response.data;
};

export const getPackingProgress = async (tripId) => {
  const response = await client.get(`/api/trips/${tripId}/packing/progress`);
  return response.data;
};
