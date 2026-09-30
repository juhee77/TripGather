-- V15: 회차별 스탬프
--
-- 배경:
-- 정기편(V14)은 규칙만 저장하고 회차는 조회 시 계산한다. 그런데 체크인은
-- "이 사람이 이 모임에 스탬프를 받은 적이 있는가" 만 보고 막고 있었다.
-- 그래서 매주 화요일 러닝크루에 첫 주 체크인을 하면 둘째 주부터는
-- "이미 체크인을 완료하여 보상을 받았습니다" 로 영원히 막혔다.
-- 정기편의 존재 이유인 "매주 나온다" 가 단 한 번만 기록됐다.
--
-- 회차 테이블을 따로 만들지 않고 스탬프에 회차 날짜를 새긴다.
-- 회차를 행으로 쌓으면 어디까지 만들어 둘지, 요일이 바뀌면 기존 회차를
-- 어떻게 할지 같은 상태 관리가 따라오는데, V14 가 그것을 피하려고
-- 회차를 계산으로 둔 선택과 결이 맞지 않는다.

ALTER TABLE stamps ADD COLUMN IF NOT EXISTS occurrence_date DATE;

-- 코스 완주 스탬프가 코스 ID 를 gathering_id 에 넣고 있었다.
-- 피드는 gathering_id 목록으로 "이미 참여한 모임" 을 칠하기 때문에,
-- 코스 12번을 완주하면 모임 12번이 참여한 것처럼 보였다. 대상을 분리한다.
ALTER TABLE stamps ADD COLUMN IF NOT EXISTS itinerary_id BIGINT;

-- 기존 행 보정: 아직 배포 전이라 실데이터는 없다.
-- 남아 있는 로컬 데이터의 gathering_id 는 모임/코스를 구분할 수 없으므로 그대로 둔다.
UPDATE stamps SET occurrence_date = CAST(completed_at AS DATE) WHERE occurrence_date IS NULL;

-- 중복 체크인 판정에 쓰는 조회 인덱스.
-- 유니크로 걸지 않는 이유: 한 모임에 미션이 여러 개면 같은 사람이 같은 날
-- 여러 장을 정상적으로 받는다. 회차 중복은 체크인 경로에서만 막아야 한다.
CREATE INDEX IF NOT EXISTS idx_stamps_user_gathering_occurrence
    ON stamps (user_id, gathering_id, occurrence_date);

CREATE INDEX IF NOT EXISTS idx_stamps_itinerary ON stamps (itinerary_id);
