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

  const withdrawAccount = async () => {
    setLoading(true);
    setError('');
    try {
      await UserRepository.withdrawAccount();
      if (setUser) setUser(null);
    } catch (err) {
      setError(err.message || 'Failed to withdraw account');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const fetchMyProfile = async () => {
    setLoading(true);
    setError('');
    try {
      const myProfile = await UserRepository.getMe();
      if (setUser) setUser(prev => ({ ...prev, ...myProfile }));
      return myProfile;
    } catch (err) {
      setError(err.message || 'Failed to fetch user profile');
    } finally {
      setLoading(false);
    }
  };

  return {
    user,
    loading,
    error,
    updateProfile,
    withdrawAccount,
    fetchMyProfile
  };
};
