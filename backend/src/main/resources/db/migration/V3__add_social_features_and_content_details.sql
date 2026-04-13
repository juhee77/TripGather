-- V3: Add social features (likes, posts), gathering status, and itinerary details

-- 1. Update existing tables
ALTER TABLE gathering ADD COLUMN status VARCHAR(20) DEFAULT 'OPEN';
ALTER TABLE gathering ADD COLUMN like_count INTEGER DEFAULT 0;

ALTER TABLE itinerary ADD COLUMN location VARCHAR(255);
ALTER TABLE itinerary ADD COLUMN dates VARCHAR(255);
ALTER TABLE itinerary ADD COLUMN bg_image_url TEXT;
ALTER TABLE itinerary ADD COLUMN stamp_image_url TEXT;

-- 2. Create new tables for social features and points
CREATE TABLE gathering_like (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    gathering_id BIGINT REFERENCES gathering(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    UNIQUE (user_id, gathering_id)
);

CREATE TABLE gathering_post (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    gathering_id BIGINT REFERENCES gathering(id) ON DELETE CASCADE,
    content TEXT,
    image_url VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE point_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount INTEGER NOT NULL,
    transaction_type VARCHAR(20) NOT NULL, -- EARN, USE
    description VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE
);
