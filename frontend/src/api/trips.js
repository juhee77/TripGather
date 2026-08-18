import client from './client';

export const addTripExpense = async (expenseData) => {
  const response = await client.post('/api/trips/expenses', expenseData);
  return response.data;
};

export const getTripExpenses = async (tripId) => {
  const response = await client.get(`/api/trips/${tripId}/expenses`);
  return response.data;
};

export const deleteTripExpense = async (expenseId) => {
  const response = await client.delete(`/api/trips/expenses/${expenseId}`);
  return response.data;
};

export const getTripSettlement = async (tripId, memberCount = 1) => {
  const response = await client.get(`/api/trips/${tripId}/settlement`, {
    params: { memberCount }
  });
  return response.data;
};
