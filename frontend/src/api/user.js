import client from './client';

export const getUserBadges = async () => {
  const response = await client.get('/api/users/me/badges');
  return response.data;
};
