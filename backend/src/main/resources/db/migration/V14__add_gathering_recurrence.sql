-- V14: 정기 모임(정기편) 지원
--
-- 기존 모임은 startDate/endDate 한 쌍만 가져 "매주 화요일 한강 러닝" 같은
-- 반복되는 동네 활동을 표현할 수 없었다. 매 회차마다 새 모임을 만들고
-- 크루를 새로 모아 다시 승인해야 했다.
--
-- 회차 자체는 테이블에 쌓지 않고 규칙만 저장한 뒤 조회 시 계산한다.
-- 회차를 미리 생성해 두면 어디까지 만들어 둘지, 규칙이 바뀌면 기존 회차를
-- 어떻게 할지 같은 상태 관리 문제가 따라온다.

ALTER TABLE gathering ADD COLUMN IF NOT EXISTS recurrence_rule VARCHAR(20) NOT NULL DEFAULT 'NONE';
ALTER TABLE gathering ADD COLUMN IF NOT EXISTS recurrence_day_of_week VARCHAR(10);
ALTER TABLE gathering ADD COLUMN IF NOT EXISTS recurrence_until DATE;

-- 정기편만 따로 훑는 조회를 대비한 부분 인덱스 대용 (H2 호환을 위해 일반 인덱스)
CREATE INDEX IF NOT EXISTS idx_gathering_recurrence ON gathering (recurrence_rule);
