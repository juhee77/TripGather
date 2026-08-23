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
    public List<Gathering> searchGatherings(String query, String category, String location, Boolean availableOnly, String sortBy) {
        QGathering gathering = QGathering.gathering;
        BooleanBuilder builder = new BooleanBuilder();

        if (query != null && !query.trim().isEmpty()) {
            String trimmedQuery = query.trim();
            builder.and(gathering.title.containsIgnoreCase(trimmedQuery)
                    .or(gathering.location.containsIgnoreCase(trimmedQuery)));
        }

        if (category != null && !category.trim().isEmpty()) {
            builder.and(gathering.category.eq(category.trim()));
        }

        if (location != null && !location.trim().isEmpty()) {
            builder.and(gathering.location.containsIgnoreCase(location.trim()));
        }

        if (Boolean.TRUE.equals(availableOnly)) {
            builder.and(gathering.status.eq(com.example.demo.domain.GatheringStatus.OPEN));
        }

        com.querydsl.core.types.OrderSpecifier<?> orderSpecifier = gathering.createdAt.desc();
        if ("LIKES".equalsIgnoreCase(sortBy) || "POPULAR".equalsIgnoreCase(sortBy)) {
            orderSpecifier = gathering.likeCount.desc();
        } else if ("MEMBERS".equalsIgnoreCase(sortBy)) {
            orderSpecifier = gathering.currentJoining.desc();
        }

        return queryFactory.selectFrom(gathering)
                .where(builder)
                .orderBy(orderSpecifier, gathering.createdAt.desc())
                .fetch();
    }
}
