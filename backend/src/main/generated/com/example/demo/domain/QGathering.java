package com.example.demo.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGathering is a Querydsl query type for Gathering
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGathering extends EntityPathBase<Gathering> {

    private static final long serialVersionUID = 1861168823L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGathering gathering = new QGathering("gathering");

    public final StringPath bgImageUrl = createString("bgImageUrl");

    public final StringPath category = createString("category");

    public final ListPath<Comment, QComment> comments = this.<Comment, QComment>createList("comments", Comment.class, QComment.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> currentJoining = createNumber("currentJoining", Integer.class);

    public final StringPath dates = createString("dates");

    public final QUser host;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Double> lat = createNumber("lat", Double.class);

    public final NumberPath<Double> lng = createNumber("lng", Double.class);

    public final StringPath location = createString("location");

    public final NumberPath<Integer> maxJoining = createNumber("maxJoining", Integer.class);

    public final ListPath<GatheringMember, QGatheringMember> members = this.<GatheringMember, QGatheringMember>createList("members", GatheringMember.class, QGatheringMember.class, PathInits.DIRECT2);

    public final StringPath title = createString("title");

    public QGathering(String variable) {
        this(Gathering.class, forVariable(variable), INITS);
    }

    public QGathering(Path<? extends Gathering> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGathering(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGathering(PathMetadata metadata, PathInits inits) {
        this(Gathering.class, metadata, inits);
    }

    public QGathering(Class<? extends Gathering> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.host = inits.isInitialized("host") ? new QUser(forProperty("host")) : null;
    }

}

