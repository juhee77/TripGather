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
        BooleanBuilder builder = buildConditions(gathering, query, category, location, availableOnly);

        return queryFactory.selectFrom(gathering)
                .where(builder)
                .orderBy(resolveOrder(gathering, sortBy), gathering.createdAt.desc(), gathering.id.desc())
                .fetch();
    }

    @Override
    public List<Gathering> searchGatherings(String query, String category, String location, Boolean availableOnly,
                                            String sortBy, int page, int size) {
        QGathering gathering = QGathering.gathering;
        BooleanBuilder builder = buildConditions(gathering, query, category, location, availableOnly);

        return queryFactory.selectFrom(gathering)
                .where(builder)
                .orderBy(resolveOrder(gathering, sortBy), gathering.createdAt.desc(), gathering.id.desc())
                .offset((long) page * size)
                // 다음 페이지 존재 여부를 별도 COUNT 없이 판단하기 위해 한 건 더 읽는다.
                .limit(size + 1L)
                .fetch();
    }

    private BooleanBuilder buildConditions(QGathering gathering, String query, String category,
                                           String location, Boolean availableOnly) {
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

        return builder;
    }

    private com.querydsl.core.types.OrderSpecifier<?> resolveOrder(QGathering gathering, String sortBy) {
        if ("LIKES".equalsIgnoreCase(sortBy) || "POPULAR".equalsIgnoreCase(sortBy)) {
            return gathering.likeCount.desc();
        }
        if ("MEMBERS".equalsIgnoreCase(sortBy)) {
            return gathering.currentJoining.desc();
        }
        return gathering.createdAt.desc();
    }
}
