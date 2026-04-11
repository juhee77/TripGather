package com.example.demo.repository;

import com.example.demo.domain.Gathering;
import com.example.demo.domain.QGathering;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GatheringRepositoryCustomImpl implements GatheringRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Gathering> searchGatherings(String query, String category, String location, Boolean availableOnly) {
        QGathering gathering = QGathering.gathering;
        BooleanBuilder builder = new BooleanBuilder();

        if (query != null && !query.isEmpty()) {
            builder.and(gathering.title.containsIgnoreCase(query)
                    .or(gathering.location.containsIgnoreCase(query)));
        }

        if (category != null && !category.isEmpty()) {
            builder.and(gathering.category.eq(category));
        }

        if (location != null && !location.isEmpty()) {
            builder.and(gathering.location.containsIgnoreCase(location));
        }

        if (Boolean.TRUE.equals(availableOnly)) {
            builder.and(gathering.currentJoining.lt(gathering.maxJoining));
        }

        return queryFactory.selectFrom(gathering)
                .where(builder)
                .orderBy(gathering.createdAt.desc())
                .fetch();
    }
}
