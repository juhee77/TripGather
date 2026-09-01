-- V11: 모임 미션(호스트 출제) 및 크루 인증 테이블 생성
--
-- 미션은 여정(itinerary)이 아니라 모임(gathering)에 붙는다.
-- 장소 체크인은 route_point.is_completed 가 이미 담당하고 있고,
-- 미션은 "그 장소에서 무엇을 했는가"를 다루므로 축이 다르다.

CREATE TABLE gathering_mission (
    id BIGSERIAL PRIMARY KEY,
    gathering_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    reward_points INT NOT NULL DEFAULT 50,
    requires_photo BOOLEAN NOT NULL DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_by VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    FOREIGN KEY (gathering_id) REFERENCES gathering(id) ON DELETE CASCADE
);

CREATE INDEX idx_gathering_mission_gathering ON gathering_mission (gathering_id);

CREATE TABLE mission_completion (
    id BIGSERIAL PRIMARY KEY,
    mission_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    photo_url VARCHAR(500),
    memo TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    reviewed_at TIMESTAMP WITHOUT TIME ZONE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_by VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT uk_mission_completion_mission_user UNIQUE (mission_id, user_id),
    FOREIGN KEY (mission_id) REFERENCES gathering_mission(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_mission_completion_mission ON mission_completion (mission_id);
CREATE INDEX idx_mission_completion_user ON mission_completion (user_id);

-- 2026-04에 걷어낸 옛 미션 진행 트래킹 테이블 정리.
-- 대응하는 엔티티가 사라진 뒤에도 스키마에만 남아 매번 빈 테이블로 생성되고 있었다.
DROP TABLE IF EXISTS user_mission_step;
DROP TABLE IF EXISTS user_mission;
