import client from './client';

export const getUserBadges = async () => {
  const response = await client.get('/api/users/me/badges');
  return response.data;
};

export const getPointTransactions = async () => {
  const response = await client.get('/api/points/transactions');
  return response.data;
};

export const withdrawAccount = async () => {
  const response = await client.delete('/api/users/me');
  return response.data;
};
