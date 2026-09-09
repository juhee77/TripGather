package com.example.demo.repository;

import com.example.demo.domain.Gathering;
import java.util.List;

public interface GatheringRepositoryCustom {
    default List<Gathering> searchGatherings(String query, String category, String location, Boolean availableOnly) {
        return searchGatherings(query, category, location, availableOnly, "LATEST");
    }
    List<Gathering> searchGatherings(String query, String category, String location, Boolean availableOnly, String sortBy);

    /**
     * 페이지 단위 조회. size+1 건을 읽어와 다음 페이지 존재 여부를 별도 COUNT 쿼리 없이 판단한다.
     * 호출부(Service)가 size+1 건 중 마지막 한 건을 잘라내고 hasNext 를 계산한다.
     */
    List<Gathering> searchGatherings(String query, String category, String location, Boolean availableOnly,
                                     String sortBy, int page, int size);
}
