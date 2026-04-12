import { useState, useCallback, useEffect, useMemo } from 'react';
import GatheringRepository from '../repositories/GatheringRepository';

export const useGatheringsViewModel = () => {
  const [gatherings, setGatherings] = useState([]);
  const [selectedRegion, setSelectedRegion] = useState('전체');
  const [searchQuery, setSearchQuery] = useState('');
  const [availableOnly, setAvailableOnly] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchGatherings = useCallback(async (filters = {}) => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await GatheringRepository.search(filters);
      setGatherings(data || []);
    } catch (err) {
      console.error(err);
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    // debounce would be ideal, but direct call for now
    const timer = setTimeout(() => {
      fetchGatherings({ location: selectedRegion, query: searchQuery, availableOnly });
    }, 300);
    return () => clearTimeout(timer);
  }, [selectedRegion, searchQuery, availableOnly, fetchGatherings]);

  const handleRegionChange = useCallback((newRegion) => {
    setSelectedRegion(newRegion);
  }, []);

  const handleSearchQueryChange = useCallback((query) => {
    setSearchQuery(query);
  }, []);

  const handleAvailableOnlyChange = useCallback((available) => {
    setAvailableOnly(available);
  }, []);

  const createGathering = useCallback(async (gatheringData) => {
    await GatheringRepository.create(gatheringData);
    await fetchGatherings({ location: selectedRegion, query: searchQuery, availableOnly });
  }, [selectedRegion, searchQuery, availableOnly, fetchGatherings]);

  const deleteGathering = useCallback(async (gatheringId) => {
    await GatheringRepository.delete(gatheringId);
    setGatherings(prev => prev.filter(g => g.id !== gatheringId));
  }, []);

  const refreshGatherings = useCallback(() => {
    return fetchGatherings({ location: selectedRegion, query: searchQuery, availableOnly });
  }, [selectedRegion, searchQuery, availableOnly, fetchGatherings]);

  const actions = useMemo(() => ({
    handleRegionChange,
    handleSearchQueryChange,
    handleAvailableOnlyChange,
    createGathering,
    deleteGathering,
    refreshGatherings
  }), [handleRegionChange, handleSearchQueryChange, handleAvailableOnlyChange, createGathering, deleteGathering, refreshGatherings]);

  return {
    gatherings,
    selectedRegion,
    searchQuery,
    availableOnly,
    isLoading,
    error,
    actions
  };
};
