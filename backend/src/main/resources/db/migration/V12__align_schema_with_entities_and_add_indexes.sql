-- V12: 엔티티-스키마 정합성 복구 및 조회 인덱스 추가
--
-- 배경:
-- local 프로필은 ddl-auto=update 라 Hibernate 가 누락 컬럼을 자동 생성해 왔지만,
-- prod 프로필은 ddl-auto=validate 이므로 아래 컬럼이 없으면 애플리케이션이 기동하지 못한다.
-- (V1~V11 에 반영되지 않은 채 엔티티에만 추가된 필드들을 여기서 일괄 정렬한다)

-- 1) BaseEntity 감사 컬럼 누락분
ALTER TABLE users          ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE comment        ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE comment        ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE gathering      ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE gathering      ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE gathering_post ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE gathering_post ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE itinerary      ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE itinerary      ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- 2) trip 감사 컬럼 명명 정정 (created_date/updated_date -> BaseEntity 규약인 created_at/updated_at)
ALTER TABLE trip RENAME COLUMN created_date TO created_at;
ALTER TABLE trip RENAME COLUMN updated_date TO updated_at;

-- 3) itinerary: 소유/공개/기간 컬럼 누락분
ALTER TABLE itinerary ADD COLUMN IF NOT EXISTS owner_email   VARCHAR(255);
ALTER TABLE itinerary ADD COLUMN IF NOT EXISTS author_email  VARCHAR(255);
ALTER TABLE itinerary ADD COLUMN IF NOT EXISTS original_id   BIGINT;
ALTER TABLE itinerary ADD COLUMN IF NOT EXISTS public_status BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE itinerary ADD COLUMN IF NOT EXISTS start_date    DATE;
ALTER TABLE itinerary ADD COLUMN IF NOT EXISTS end_date      DATE;

-- 4) gathering: 여정 연결 및 기간 컬럼 누락분
ALTER TABLE gathering ADD COLUMN IF NOT EXISTS itinerary_id BIGINT;
ALTER TABLE gathering ADD COLUMN IF NOT EXISTS start_date   DATE;
ALTER TABLE gathering ADD COLUMN IF NOT EXISTS end_date     DATE;

-- 5) route_point: 시간대 표기 컬럼 누락분
ALTER TABLE route_point ADD COLUMN IF NOT EXISTS start_time VARCHAR(255);
ALTER TABLE route_point ADD COLUMN IF NOT EXISTS end_time   VARCHAR(255);

-- 6) 조회 경로 인덱스
--    리포지토리가 실제로 조회에 사용하는 외래키/필터 컬럼에만 최소한으로 추가한다.
CREATE INDEX IF NOT EXISTS idx_chat_message_gathering    ON chat_message (gathering_id);
CREATE INDEX IF NOT EXISTS idx_gathering_member_gathering ON gathering_member (gathering_id);
CREATE INDEX IF NOT EXISTS idx_gathering_member_user      ON gathering_member (user_id);
CREATE INDEX IF NOT EXISTS idx_gathering_host             ON gathering (host_id);
CREATE INDEX IF NOT EXISTS idx_gathering_like_gathering   ON gathering_like (gathering_id);
CREATE INDEX IF NOT EXISTS idx_gathering_like_user        ON gathering_like (user_id);
CREATE INDEX IF NOT EXISTS idx_gathering_post_gathering   ON gathering_post (gathering_id);
CREATE INDEX IF NOT EXISTS idx_comment_gathering          ON comment (gathering_id);
CREATE INDEX IF NOT EXISTS idx_route_point_itinerary      ON route_point (itinerary_id);
CREATE INDEX IF NOT EXISTS idx_itinerary_owner_email      ON itinerary (owner_email);
CREATE INDEX IF NOT EXISTS idx_itinerary_public           ON itinerary (public_status, deleted);
CREATE INDEX IF NOT EXISTS idx_trip_owner                 ON trip (owner_id);
CREATE INDEX IF NOT EXISTS idx_trip_review_trip           ON trip_review (trip_id);
CREATE INDEX IF NOT EXISTS idx_packing_item_trip          ON packing_item (trip_id);
CREATE INDEX IF NOT EXISTS idx_trip_expense_trip          ON trip_expense (trip_id);
CREATE INDEX IF NOT EXISTS idx_point_transactions_user    ON point_transactions (user_id);
CREATE INDEX IF NOT EXISTS idx_stamps_user                ON stamps (user_id);
CREATE INDEX IF NOT EXISTS idx_direct_message_sender      ON direct_message (sender_id);
CREATE INDEX IF NOT EXISTS idx_direct_message_receiver    ON direct_message (receiver_id);
