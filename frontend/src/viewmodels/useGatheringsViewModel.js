import { useState, useCallback, useEffect } from 'react';
import GatheringRepository from '../repositories/GatheringRepository';

/**
 * useGatheringsViewModel acts as the "ViewModel" in MVVM.
 * It manages the view state and handles business interactions via the Model.
 */
export const useGatheringsViewModel = () => {
  const [gatherings, setGatherings] = useState([]);
  const [selectedRegion, setSelectedRegion] = useState('전체');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchGatherings = useCallback(async (region = '전체') => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await GatheringRepository.fetchAll(region);
      setGatherings(data);
    } catch (err) {
      console.error(err);
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  // Fetch automatically initially or when region changes
  useEffect(() => {
    fetchGatherings(selectedRegion);
  }, [selectedRegion, fetchGatherings]);

  const handleRegionChange = (newRegion) => {
    setSelectedRegion(newRegion);
  };

  const createGathering = async (gatheringData) => {
    await GatheringRepository.create(gatheringData);
    await fetchGatherings(selectedRegion); // Refresh list
  };

  const deleteGathering = async (gatheringId) => {
    await GatheringRepository.delete(gatheringId);
    setGatherings(prev => prev.filter(g => g.id !== gatheringId));
  };

  const refreshGatherings = () => {
    return fetchGatherings(selectedRegion);
  };

  return {
    gatherings,
    selectedRegion,
    isLoading,
    error,
    actions: {
      handleRegionChange,
      createGathering,
      deleteGathering,
      refreshGatherings
    }
  };
};
