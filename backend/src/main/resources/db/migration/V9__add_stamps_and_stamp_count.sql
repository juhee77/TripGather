-- V9: stamps 테이블 생성 및 users 테이블 stamps_count 컬럼 추가

-- 1. users 테이블에 stamps_count 컬럼 추가
ALTER TABLE users ADD COLUMN stamps_count INT DEFAULT 0 NOT NULL;

-- 2. stamps 테이블 생성
CREATE TABLE stamps (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    gathering_id BIGINT,
    title VARCHAR(255) NOT NULL,
    stamp_image_url TEXT,
    completed_at TIMESTAMP WITHOUT TIME ZONE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_by VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
