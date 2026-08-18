import client from './client';

export const getTripReviews = async (tripId, category) => {
  const response = await client.get(`/api/trips/${tripId}/reviews`, {
    params: { category }
  });
  return response.data;
};

export const createTripReview = async (tripId, reviewData) => {
  const response = await client.post(`/api/trips/${tripId}/reviews`, reviewData);
  return response.data;
};

export const deleteTripReview = async (tripId, reviewId) => {
  const response = await client.delete(`/api/trips/${tripId}/reviews/${reviewId}`);
  return response.data;
};

export const getTripReviewSummary = async (tripId) => {
  const response = await client.get(`/api/trips/${tripId}/reviews/summary`);
  return response.data;
};
