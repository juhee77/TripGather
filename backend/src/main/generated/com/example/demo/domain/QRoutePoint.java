package com.example.demo.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRoutePoint is a Querydsl query type for RoutePoint
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRoutePoint extends EntityPathBase<RoutePoint> {

    private static final long serialVersionUID = -357821417L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRoutePoint routePoint = new QRoutePoint("routePoint");

    public final StringPath dayLabel = createString("dayLabel");

    public final NumberPath<Integer> dayNumber = createNumber("dayNumber", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QItinerary itinerary;

    public final StringPath label = createString("label");

    public final NumberPath<Double> lat = createNumber("lat", Double.class);

    public final NumberPath<Double> lng = createNumber("lng", Double.class);

    public final NumberPath<Integer> sequenceOrder = createNumber("sequenceOrder", Integer.class);

    public QRoutePoint(String variable) {
        this(RoutePoint.class, forVariable(variable), INITS);
    }

    public QRoutePoint(Path<? extends RoutePoint> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRoutePoint(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRoutePoint(PathMetadata metadata, PathInits inits) {
        this(RoutePoint.class, metadata, inits);
    }

    public QRoutePoint(Class<? extends RoutePoint> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.itinerary = inits.isInitialized("itinerary") ? new QItinerary(forProperty("itinerary")) : null;
    }

}

