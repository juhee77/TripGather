// @vitest-environment jsdom
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { renderHook, act, waitFor } from '@testing-library/react';

import GatheringRepository from '../repositories/GatheringRepository';
import { useGatheringsViewModel } from '../viewmodels/useGatheringsViewModel';

/**
 * 라운지 피드 페이지네이션.
 *
 * 서버는 한 번에 20건씩 내려주고, 응답 건수가 요청한 size 보다 적으면 마지막 페이지다.
 * 이 규칙을 잘못 다루면 "더 보기"가 영영 사라지지 않거나, 필터를 바꿨는데
 * 이전 결과가 남아 섞이는 문제가 생긴다.
 */

const PAGE_SIZE = 20;
const rows = (from, count) =>
  Array.from({ length: count }, (_, i) => ({ id: from + i, title: `모임 ${from + i}` }));

describe('useGatheringsViewModel — 페이지네이션', () => {
  let search;

  beforeEach(() => {
    search = vi.spyOn(GatheringRepository, 'search');
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('첫 페이지를 0번부터 기본 크기로 요청한다', async () => {
    search.mockResolvedValue(rows(1, 3));

    const { result } = renderHook(() => useGatheringsViewModel());

    await waitFor(() => expect(result.current.gatherings).toHaveLength(3));
    expect(search).toHaveBeenCalledWith(expect.any(Object), 0, PAGE_SIZE);
  });

  it('응답이 한 페이지를 채우지 못하면 더 불러올 것이 없다고 본다', async () => {
    search.mockResolvedValue(rows(1, 3));

    const { result } = renderHook(() => useGatheringsViewModel());

    await waitFor(() => expect(result.current.gatherings).toHaveLength(3));
    expect(result.current.hasMore).toBe(false);
  });

  it('응답이 한 페이지를 꽉 채우면 다음 페이지가 있을 수 있다고 본다', async () => {
    search.mockResolvedValue(rows(1, PAGE_SIZE));

    const { result } = renderHook(() => useGatheringsViewModel());

    await waitFor(() => expect(result.current.gatherings).toHaveLength(PAGE_SIZE));
    expect(result.current.hasMore).toBe(true);
  });

  it('더 보기는 다음 페이지를 요청해 기존 목록 뒤에 이어 붙인다', async () => {
    search.mockResolvedValueOnce(rows(1, PAGE_SIZE));
    const { result } = renderHook(() => useGatheringsViewModel());
    await waitFor(() => expect(result.current.gatherings).toHaveLength(PAGE_SIZE));

    search.mockResolvedValueOnce(rows(PAGE_SIZE + 1, 5));
    await act(async () => {
      await result.current.actions.loadMoreGatherings();
    });

    expect(search).toHaveBeenLastCalledWith(expect.any(Object), 1, PAGE_SIZE);
    expect(result.current.gatherings).toHaveLength(PAGE_SIZE + 5);
    // 앞쪽은 그대로 두고 뒤에 붙어야 한다 (교체가 아니다)
    expect(result.current.gatherings[0].id).toBe(1);
    expect(result.current.gatherings.at(-1).id).toBe(PAGE_SIZE + 5);
    expect(result.current.hasMore).toBe(false);
  });

  it('더 볼 것이 없으면 더 보기를 눌러도 요청하지 않는다', async () => {
    search.mockResolvedValue(rows(1, 3));
    const { result } = renderHook(() => useGatheringsViewModel());
    await waitFor(() => expect(result.current.hasMore).toBe(false));

    const callsBefore = search.mock.calls.length;
    await act(async () => {
      await result.current.actions.loadMoreGatherings();
    });

    expect(search.mock.calls.length).toBe(callsBefore);
  });

  it('지역을 바꾸면 목록을 이어 붙이지 않고 첫 페이지부터 다시 받는다', async () => {
    search.mockResolvedValue(rows(1, PAGE_SIZE));
    const { result } = renderHook(() => useGatheringsViewModel());
    await waitFor(() => expect(result.current.gatherings).toHaveLength(PAGE_SIZE));

    search.mockResolvedValue(rows(100, 2));
    await act(async () => {
      result.current.actions.handleRegionChange('강남구');
    });

    await waitFor(() => expect(result.current.gatherings).toHaveLength(2));
    // 이전 지역 결과가 남아 섞이면 안 된다
    expect(result.current.gatherings[0].id).toBe(100);
    expect(search).toHaveBeenLastCalledWith(
      expect.objectContaining({ location: '강남구' }), 0, PAGE_SIZE
    );
  });

  it('조회에 실패해도 목록을 비우지 않고 오류만 남긴다', async () => {
    vi.spyOn(console, 'error').mockImplementation(() => {});
    search.mockRejectedValue(new Error('network down'));

    const { result } = renderHook(() => useGatheringsViewModel());

    await waitFor(() => expect(result.current.error).toBe('network down'));
    expect(result.current.gatherings).toEqual([]);
    expect(result.current.isLoading).toBe(false);
  });
});
