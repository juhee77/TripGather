import { useState, useCallback, useEffect, useMemo } from 'react';
import ItineraryRepository from '../repositories/ItineraryRepository';

export const useItinerariesViewModel = () => {
  const [itineraries, setItineraries] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchItineraries = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await ItineraryRepository.fetchAll();
      setItineraries(data);
    } catch (err) {
      console.error(err);
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchItineraries();
  }, [fetchItineraries]);

  const createItinerary = useCallback(async (itineraryData) => {
    await ItineraryRepository.create(itineraryData);
    await fetchItineraries();
  }, [fetchItineraries]);

  const updateItinerary = useCallback(async (id, updateData) => {
    await ItineraryRepository.update(id, updateData);
    await fetchItineraries();
  }, [fetchItineraries]);

  const deleteItinerary = useCallback(async (id) => {
    await ItineraryRepository.delete(id);
    setItineraries(prev => prev.filter(i => i.id !== id));
  }, []);

  const refreshItineraries = useCallback(() => {
    return fetchItineraries();
  }, [fetchItineraries]);

  const actions = useMemo(() => ({
    createItinerary,
    updateItinerary,
    deleteItinerary,
    refreshItineraries
  }), [createItinerary, updateItinerary, deleteItinerary, refreshItineraries]);

  return {
    itineraries,
    isLoading,
    error,
    actions
  };
};
