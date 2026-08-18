-- V10: Trip Expense (여행 지출 및 정산) 테이블 생성

CREATE TABLE trip_expense (
    id BIGSERIAL PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    payer_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    category VARCHAR(50) DEFAULT '기타',
    expense_date TIMESTAMP,
    memo TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    FOREIGN KEY (trip_id) REFERENCES trip(id) ON DELETE CASCADE,
    FOREIGN KEY (payer_id) REFERENCES users(id) ON DELETE CASCADE
);
