package com.example.demo.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDirectMessage is a Querydsl query type for DirectMessage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDirectMessage extends EntityPathBase<DirectMessage> {

    private static final long serialVersionUID = 749180558L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDirectMessage directMessage = new QDirectMessage("directMessage");

    public final StringPath content = createString("content");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isRead = createBoolean("isRead");

    public final QUser receiver;

    public final QUser sender;

    public final DateTimePath<java.time.LocalDateTime> sentAt = createDateTime("sentAt", java.time.LocalDateTime.class);

    public QDirectMessage(String variable) {
        this(DirectMessage.class, forVariable(variable), INITS);
    }

    public QDirectMessage(Path<? extends DirectMessage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDirectMessage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDirectMessage(PathMetadata metadata, PathInits inits) {
        this(DirectMessage.class, metadata, inits);
    }

    public QDirectMessage(Class<? extends DirectMessage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.receiver = inits.isInitialized("receiver") ? new QUser(forProperty("receiver")) : null;
        this.sender = inits.isInitialized("sender") ? new QUser(forProperty("sender")) : null;
    }

}

