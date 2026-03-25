import { useState, useCallback, useEffect } from 'react';
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

  const createItinerary = async (itineraryData) => {
    await ItineraryRepository.create(itineraryData);
    await fetchItineraries();
  };

  const updateItinerary = async (id, updateData) => {
    await ItineraryRepository.update(id, updateData);
    await fetchItineraries();
  };

  const deleteItinerary = async (id) => {
    await ItineraryRepository.delete(id);
    setItineraries(prev => prev.filter(i => i.id !== id));
  };

  const refreshItineraries = () => {
    return fetchItineraries();
  };

  return {
    itineraries,
    isLoading,
    error,
    actions: {
      createItinerary,
      updateItinerary,
      deleteItinerary,
      refreshItineraries
    }
  };
};
