-- V16: 경유지(route_point) 사진·촬영시각 및 여정(itinerary) 타임랩스 GIF/MP4 URL 컬럼 추가 (#28)
--
-- 배경:
-- 여행 동선 타임랩스 스튜디오에서 업로드하거나 촬영한 사진을 각 경유지(route_point)에
-- 매칭해 두고, 완성된 보딩패스 타임랩스 GIF/MP4를 여정(itinerary)에 영구 보관한다.

ALTER TABLE route_point ADD COLUMN IF NOT EXISTS photo_url TEXT;
ALTER TABLE route_point ADD COLUMN IF NOT EXISTS taken_at TIMESTAMP;
ALTER TABLE itinerary ADD COLUMN IF NOT EXISTS timelapse_url TEXT;
