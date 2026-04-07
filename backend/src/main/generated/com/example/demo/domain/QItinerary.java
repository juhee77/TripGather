package com.example.demo.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QItinerary is a Querydsl query type for Itinerary
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QItinerary extends EntityPathBase<Itinerary> {

    private static final long serialVersionUID = 294046651L;

    public static final QItinerary itinerary = new QItinerary("itinerary");

    public final StringPath author = createString("author");

    public final StringPath bgImageUrl = createString("bgImageUrl");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath dates = createString("dates");

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath location = createString("location");

    public final ListPath<RoutePoint, QRoutePoint> routePoints = this.<RoutePoint, QRoutePoint>createList("routePoints", RoutePoint.class, QRoutePoint.class, PathInits.DIRECT2);

    public final StringPath stampImageUrl = createString("stampImageUrl");

    public final StringPath title = createString("title");

    public QItinerary(String variable) {
        super(Itinerary.class, forVariable(variable));
    }

    public QItinerary(Path<? extends Itinerary> path) {
        super(path.getType(), path.getMetadata());
    }

    public QItinerary(PathMetadata metadata) {
        super(Itinerary.class, metadata);
    }

}

