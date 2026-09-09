import { useState, useCallback, useEffect, useMemo, useRef } from 'react';
import GatheringRepository from '../repositories/GatheringRepository';

const PAGE_SIZE = 20;

export const useGatheringsViewModel = () => {
  const [gatherings, setGatherings] = useState([]);
  const [selectedRegion, setSelectedRegion] = useState('전체');
  const [searchQuery, setSearchQuery] = useState('');
  const [availableOnly, setAvailableOnly] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [error, setError] = useState(null);
  // 현재 로드된 마지막 페이지 번호. 리렌더를 유발할 필요가 없어 ref 로 둔다.
  const pageRef = useRef(0);

  const fetchGatherings = useCallback(async (filters = {}) => {
    setIsLoading(true);
    setError(null);
    pageRef.current = 0;
    try {
      const data = await GatheringRepository.search(filters, 0, PAGE_SIZE);
      setGatherings(data || []);
      // 응답 건수가 요청한 size 와 같으면 다음 페이지가 있을 수 있다고 본다.
      setHasMore((data || []).length >= PAGE_SIZE);
    } catch (err) {
      console.error(err);
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  }, []);

  // 현재 필터를 유지한 채 다음 페이지를 이어붙인다.
  const loadMoreGatherings = useCallback(async () => {
    if (isLoading || isLoadingMore || !hasMore) return;
    const nextPage = pageRef.current + 1;
    setIsLoadingMore(true);
    try {
      const data = await GatheringRepository.search(
        { location: selectedRegion, query: searchQuery, availableOnly },
        nextPage,
        PAGE_SIZE
      );
      if (data && data.length > 0) {
        setGatherings(prev => [...prev, ...data]);
        pageRef.current = nextPage;
      }
      setHasMore((data || []).length >= PAGE_SIZE);
    } catch (err) {
      console.error('Failed to load more gatherings:', err);
    } finally {
      setIsLoadingMore(false);
    }
  }, [selectedRegion, searchQuery, availableOnly, isLoading, isLoadingMore, hasMore]);

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

  const likeGathering = useCallback(async (gatheringId) => {
    try {
      await GatheringRepository.like(gatheringId);
      // Update local state for immediate feedback
      setGatherings(prev => prev.map(g => 
        g.id === gatheringId 
          ? { ...g, likedByCurrentUser: !g.likedByCurrentUser, likeCount: g.likedByCurrentUser ? (g.likeCount || 0) - 1 : (g.likeCount || 0) + 1 } 
          : g
      ));
    } catch (err) {
      console.error("Like failed", err);
    }
  }, []);

  const actions = useMemo(() => ({
    handleRegionChange,
    handleSearchQueryChange,
    handleAvailableOnlyChange,
    createGathering,
    deleteGathering,
    refreshGatherings,
    likeGathering,
    loadMoreGatherings
  }), [handleRegionChange, handleSearchQueryChange, handleAvailableOnlyChange, createGathering, deleteGathering, refreshGatherings, likeGathering, loadMoreGatherings]);

  return {
    gatherings,
    selectedRegion,
    searchQuery,
    availableOnly,
    isLoading,
    isLoadingMore,
    hasMore,
    error,
    actions
  };
};
