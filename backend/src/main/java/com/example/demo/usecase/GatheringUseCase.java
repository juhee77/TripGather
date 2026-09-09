package com.example.demo.usecase;

import com.example.demo.domain.Gathering;
import java.util.List;

public interface GatheringUseCase {

    /** 피드 한 페이지의 기본 크기 */
    int DEFAULT_PAGE_SIZE = 20;
    /** 클라이언트가 요청할 수 있는 페이지 최대 크기 */
    int MAX_PAGE_SIZE = 50;

    List<Gathering> getAllGatherings(String location);
    List<Gathering> getPopularGatherings();
    List<Gathering> getUserLikedGatherings(String email);
    default List<Gathering> searchGatherings(String query, String category, String location, Boolean availableOnly) {
        return searchGatherings(query, category, location, availableOnly, "LATEST");
    }
    List<Gathering> searchGatherings(String query, String category, String location, Boolean availableOnly, String sortBy);

    /**
     * 페이지 단위 모임 검색.
     *
     * @param page 0 부터 시작하는 페이지 번호
     * @param size 페이지 크기. {@link #MAX_PAGE_SIZE} 로 제한된다.
     * @return size+1 건 이하. 반환 건수가 size 를 초과하면(=size+1건) 다음 페이지가 있다는 뜻이며,
     *         호출부(Controller)가 마지막 한 건을 잘라내고 이를 hasNext 판단에 사용한다.
     */
    List<Gathering> searchGatherings(String query, String category, String location, Boolean availableOnly,
                                     String sortBy, int page, int size);
    Gathering createGathering(Gathering gathering);
    Gathering updateGathering(Long id, Gathering updateData);
    void deleteGathering(Long id);
    List<Gathering> getHostedGatherings();
    Gathering getGathering(Long id);
    void likeGathering(Long id);
    boolean isLikedByUser(Long gatheringId, String email);
}
