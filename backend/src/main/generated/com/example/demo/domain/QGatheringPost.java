package com.example.demo.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QGatheringPost is a Querydsl query type for GatheringPost
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGatheringPost extends EntityPathBase<GatheringPost> {

    private static final long serialVersionUID = -236910601L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QGatheringPost gatheringPost = new QGatheringPost("gatheringPost");

    public final QUser author;

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final QGathering gathering;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public QGatheringPost(String variable) {
        this(GatheringPost.class, forVariable(variable), INITS);
    }

    public QGatheringPost(Path<? extends GatheringPost> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QGatheringPost(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QGatheringPost(PathMetadata metadata, PathInits inits) {
        this(GatheringPost.class, metadata, inits);
    }

    public QGatheringPost(Class<? extends GatheringPost> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.author = inits.isInitialized("author") ? new QUser(forProperty("author")) : null;
        this.gathering = inits.isInitialized("gathering") ? new QGathering(forProperty("gathering"), inits.get("gathering")) : null;
    }

}

