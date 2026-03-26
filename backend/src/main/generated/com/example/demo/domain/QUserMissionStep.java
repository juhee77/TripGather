package com.example.demo.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserMissionStep is a Querydsl query type for UserMissionStep
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserMissionStep extends EntityPathBase<UserMissionStep> {

    private static final long serialVersionUID = 20745309L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserMissionStep userMissionStep = new QUserMissionStep("userMissionStep");

    public final DateTimePath<java.time.LocalDateTime> completedAt = createDateTime("completedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isCompleted = createBoolean("isCompleted");

    public final StringPath memo = createString("memo");

    public final StringPath photoUrl = createString("photoUrl");

    public final QRoutePoint routePoint;

    public final QUserMission userMission;

    public QUserMissionStep(String variable) {
        this(UserMissionStep.class, forVariable(variable), INITS);
    }

    public QUserMissionStep(Path<? extends UserMissionStep> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserMissionStep(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserMissionStep(PathMetadata metadata, PathInits inits) {
        this(UserMissionStep.class, metadata, inits);
    }

    public QUserMissionStep(Class<? extends UserMissionStep> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.routePoint = inits.isInitialized("routePoint") ? new QRoutePoint(forProperty("routePoint"), inits.get("routePoint")) : null;
        this.userMission = inits.isInitialized("userMission") ? new QUserMission(forProperty("userMission"), inits.get("userMission")) : null;
    }

}

