import client from './client';

export const getItinerary = async (id) => {
  const response = await client.get(`/api/itineraries/${id}`);
  return response.data;
};

export const toggleRoutePoint = async (itineraryId, pointId) => {
  const response = await client.patch(`/api/itineraries/${itineraryId}/points/${pointId}/toggle`);
  return response.data;
};
