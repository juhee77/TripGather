-- V8: Trip-Itinerary 1:1 관계 전환 및 기존 데이터 마이그레이션

-- 1. trip 테이블에 itinerary_id 컬럼 및 외래키 제약조건 추가
ALTER TABLE trip ADD COLUMN itinerary_id BIGINT;
ALTER TABLE trip ADD CONSTRAINT fk_trip_itinerary FOREIGN KEY (itinerary_id) REFERENCES itinerary(id);

-- 2. 기존 trip_itinerary 관계 마이그레이션 (첫 번째 매핑 일정을 대표 일정으로 바인딩)
UPDATE trip SET itinerary_id = (
    SELECT itinerary_id 
    FROM trip_itinerary 
    WHERE trip_itinerary.trip_id = trip.id 
    ORDER BY id ASC 
    LIMIT 1
);

-- 3. 불필요해진 trip_itinerary 테이블 삭제
DROP TABLE trip_itinerary;
