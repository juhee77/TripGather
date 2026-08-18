package com.example.demo.repository;

import com.example.demo.domain.Gathering;
import java.util.List;

public interface GatheringRepositoryCustom {
    default List<Gathering> searchGatherings(String query, String category, String location, Boolean availableOnly) {
        return searchGatherings(query, category, location, availableOnly, "LATEST");
    }
    List<Gathering> searchGatherings(String query, String category, String location, Boolean availableOnly, String sortBy);
}
