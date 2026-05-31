-- V7: Trip Hub 테이블 생성

CREATE TABLE trip (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    destination VARCHAR(255),
    country VARCHAR(10),
    start_date DATE,
    end_date DATE,
    bg_image_url TEXT,
    status VARCHAR(20) DEFAULT 'PLANNING',
    owner_id BIGINT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_by VARCHAR(255),
    created_date TIMESTAMP,
    updated_date TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE trip_itinerary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    itinerary_id BIGINT NOT NULL,
    display_order INT DEFAULT 0,
    FOREIGN KEY (trip_id) REFERENCES trip(id),
    FOREIGN KEY (itinerary_id) REFERENCES itinerary(id),
    UNIQUE (trip_id, itinerary_id)
);

CREATE TABLE packing_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(50) DEFAULT '기타',
    checked BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (trip_id) REFERENCES trip(id)
);

CREATE TABLE trip_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    rating INT DEFAULT 5,
    image_urls TEXT,
    category VARCHAR(50) DEFAULT '관광지',
    created_at TIMESTAMP,
    FOREIGN KEY (trip_id) REFERENCES trip(id),
    FOREIGN KEY (author_id) REFERENCES users(id)
);
