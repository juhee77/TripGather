import { useState, useCallback, useEffect } from 'react';
import UserMissionRepository from '../repositories/UserMissionRepository';

export const useMissionsViewModel = () => {
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

  const startMission = async (itineraryId) => {
    await UserMissionRepository.startMission(itineraryId);
    await fetchMissions();
  };

  const completeMission = async (itineraryId) => {
    await UserMissionRepository.completeMission(itineraryId);
    await fetchMissions();
    await fetchStamps();
  };

  const completeStep = async (missionId, stepId, memo, photoUrl) => {
    await UserMissionRepository.completeStep(missionId, stepId, memo, photoUrl);
    await fetchMissions();
    await fetchStamps(); // Stamp might be new
  };

  return {
    activeMissions,
    myStamps,
    isLoading,
    error,
    actions: {
      fetchMissions,
      fetchStamps,
      startMission,
      completeMission,
      completeStep
    }
  };
};
