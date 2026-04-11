package com.example.demo.repository;

import com.example.demo.domain.Gathering;
import java.util.List;

public interface GatheringRepositoryCustom {
    List<Gathering> searchGatherings(String query, String category, String location, Boolean availableOnly);
}
