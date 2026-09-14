-- V13: 경로 지점별 메모
--
-- "그 장소에서 무엇을 할지/무엇을 챙길지" 같은 자유 메모를 일정 항목마다 남길 수 있게 한다.
-- label 은 장소명이라 짧게 유지해야 해서 별도 컬럼으로 분리한다.

ALTER TABLE route_point ADD COLUMN IF NOT EXISTS memo VARCHAR(500);
