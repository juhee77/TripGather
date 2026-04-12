import { useState, useCallback, useEffect, useMemo } from 'react';
import UserMissionRepository from '../repositories/UserMissionRepository';
import { useAuth } from '../contexts/AuthContext';

export const useMissionsViewModel = () => {
  const { isAuthenticated } = useAuth();
  const [activeMissions, setActiveMissions] = useState([]);
  const [myStamps, setMyStamps] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchMissions = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await UserMissionRepository.getMyMissions();
      setActiveMissions(data || []);
    } catch (err) {
      console.error(err);
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const fetchStamps = useCallback(async () => {
    try {
      const data = await UserMissionRepository.getMyStamps();
      setMyStamps(data || []);
    } catch (err) {
      console.error(err);
    }
  }, []);

  useEffect(() => {
    if (isAuthenticated) {
      fetchMissions();
      fetchStamps();
    } else {
      setActiveMissions([]);
      setMyStamps([]);
    }
  }, [fetchMissions, fetchStamps, isAuthenticated]);

  const startMission = useCallback(async (itineraryId) => {
    await UserMissionRepository.startMission(itineraryId);
    await fetchMissions();
  }, [fetchMissions]);

  const completeMission = useCallback(async (itineraryId) => {
    await UserMissionRepository.completeMission(itineraryId);
    await fetchMissions();
    await fetchStamps();
  }, [fetchMissions, fetchStamps]);

  const completeStep = useCallback(async (missionId, stepId, memo, photoUrl) => {
    await UserMissionRepository.completeStep(missionId, stepId, memo, photoUrl);
    await fetchMissions();
    await fetchStamps(); 
  }, [fetchMissions, fetchStamps]);

  const actions = useMemo(() => ({
    fetchMissions,
    fetchStamps,
    startMission,
    completeMission,
    completeStep
  }), [fetchMissions, fetchStamps, startMission, completeMission, completeStep]);

  return {
    activeMissions,
    myStamps,
    isLoading,
    error,
    actions
  };
};
