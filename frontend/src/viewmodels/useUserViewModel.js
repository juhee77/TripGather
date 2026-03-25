import { useState } from 'react';
import UserRepository from '../repositories/UserRepository';
import { useAuth } from '../contexts/AuthContext';

export const useUserViewModel = () => {
  const { user, setUser } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const updateProfile = async (id, updateData) => {
    setLoading(true);
    setError('');
    try {
      const updatedUser = await UserRepository.updateProfile(id, updateData);
      if (setUser) setUser(prev => ({ ...prev, ...updatedUser }));
      return updatedUser;
    } catch (err) {
      setError(err.message || 'Failed to update profile');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return {
    user,
    loading,
    error,
    updateProfile
  };
};
