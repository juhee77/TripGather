-- Phase 1 & 2 Table Baseline

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    provider VARCHAR(20) DEFAULT 'local',
    provider_id VARCHAR(255),
    role VARCHAR(20) DEFAULT 'ROLE_USER',
    bio TEXT,
    profile_image_url VARCHAR(255),
    points INTEGER DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE gathering (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    host_id BIGINT REFERENCES users(id),
    location VARCHAR(255) NOT NULL,
    lat DOUBLE PRECISION,
    lng DOUBLE PRECISION,
    category VARCHAR(100),
    dates VARCHAR(255),
    current_joining INTEGER DEFAULT 1,
    max_joining INTEGER DEFAULT 10,
    bg_image_url TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE comment (
    id BIGSERIAL PRIMARY KEY,
    gathering_id BIGINT REFERENCES gathering(id) ON DELETE CASCADE,
    author VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE gathering_member (
    id BIGSERIAL PRIMARY KEY,
    gathering_id BIGINT REFERENCES gathering(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20),
    requested_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE chat_message (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    sender_id BIGINT REFERENCES users(id),
    gathering_id BIGINT REFERENCES gathering(id) ON DELETE CASCADE,
    sent_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE direct_message (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT REFERENCES users(id),
    receiver_id BIGINT REFERENCES users(id),
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE itinerary (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255),
    author VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE route_point (
    id BIGSERIAL PRIMARY KEY,
    itinerary_id BIGINT REFERENCES itinerary(id) ON DELETE CASCADE,
    day_number INTEGER DEFAULT 1,
    day_label VARCHAR(100),
    sequence_order INTEGER,
    label VARCHAR(255),
    lat DOUBLE PRECISION,
    lng DOUBLE PRECISION
);

CREATE TABLE user_mission (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    itinerary_id BIGINT REFERENCES itinerary(id),
    status VARCHAR(20),
    started_at TIMESTAMP WITHOUT TIME ZONE,
    completed_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE TABLE user_mission_step (
    id BIGSERIAL PRIMARY KEY,
    user_mission_id BIGINT REFERENCES user_mission(id) ON DELETE CASCADE,
    route_point_id BIGINT REFERENCES route_point(id) ON DELETE CASCADE,
    is_completed BOOLEAN DEFAULT FALSE,
    memo TEXT,
    photo_url VARCHAR(255),
    completed_at TIMESTAMP WITHOUT TIME ZONE
);
