-- ===================================
-- Flyway Migration V1: 초기 스키마 생성
-- ===================================
-- 작성일: 2026-02-10
-- 설명: users, camping_items, customers, rentals 테이블 생성
-- ===================================

-- 1. users 테이블 생성
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'USER')),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP
);

-- users 테이블 인덱스
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

-- users 테이블 코멘트
COMMENT ON TABLE users IS '사용자 계정 테이블 (로그인용)';
COMMENT ON COLUMN users.username IS '로그인 아이디';
COMMENT ON COLUMN users.password IS '암호화된 비밀번호';
COMMENT ON COLUMN users.role IS '사용자 권한 (ADMIN: 관리자, USER: 일반사용자)';
COMMENT ON COLUMN users.enabled IS '계정 활성화 여부';

-- ===================================

-- 2. camping_items 테이블 생성
CREATE TABLE camping_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(30) NOT NULL CHECK (category IN (
        'TENT', 'SLEEPING_BAG', 'CAMP_STOVE', 'CAMPING_FURNITURE',
        'COOKING_GEAR', 'LIGHTING', 'RAIN_GEAR', 'COOLER'
    )),
    model VARCHAR(100),
    description VARCHAR(500),
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    base_daily_rate NUMERIC(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN (
        'AVAILABLE', 'RENTED', 'UNDER_REPAIR', 'OUT_OF_STOCK'
    )),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- camping_items 테이블 인덱스
CREATE INDEX idx_camping_items_category ON camping_items(category);
CREATE INDEX idx_camping_items_status ON camping_items(status);

-- camping_items 테이블 코멘트
COMMENT ON TABLE camping_items IS '캠핑 장비 테이블';
COMMENT ON COLUMN camping_items.category IS '장비 카테고리';
COMMENT ON COLUMN camping_items.base_daily_rate IS '기본 일일 대여료 (비성수기 기준)';
COMMENT ON COLUMN camping_items.status IS '장비 상태 (AVAILABLE: 대여가능, RENTED: 대여중, UNDER_REPAIR: 수리중, OUT_OF_STOCK: 재고없음)';

-- ===================================

-- 3. customers 테이블 생성
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    type VARCHAR(20) NOT NULL CHECK (type IN ('INDIVIDUAL', 'BUSINESS')),
    address VARCHAR(200),
    business_number VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- customers 테이블 인덱스
CREATE INDEX idx_customers_phone ON customers(phone);
CREATE INDEX idx_customers_type ON customers(type);

-- customers 테이블 코멘트
COMMENT ON TABLE customers IS '고객 정보 테이블';
COMMENT ON COLUMN customers.type IS '고객 유형 (INDIVIDUAL: 개인, BUSINESS: 기업)';
COMMENT ON COLUMN customers.business_number IS '사업자등록번호 (기업 고객만)';

-- ===================================

-- 4. rentals 테이블 생성
CREATE TABLE rentals (
    id BIGSERIAL PRIMARY KEY,
    machine_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    actual_return_date DATE,
    total_cost NUMERIC(12, 2) NOT NULL,
    deposit NUMERIC(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN (
        'PENDING', 'APPROVED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED',
        'EXTENSION_REQUESTED', 'OVERDUE', 'DEPOSIT_PENDING', 'RETURED_DAMAGED'
    )),
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 외래 키 제약조건
    CONSTRAINT fk_rentals_machine FOREIGN KEY (machine_id)
        REFERENCES camping_items(id) ON DELETE RESTRICT,
    CONSTRAINT fk_rentals_customer FOREIGN KEY (customer_id)
        REFERENCES customers(id) ON DELETE RESTRICT
);

-- rentals 테이블 인덱스
CREATE INDEX idx_rentals_machine_id ON rentals(machine_id);
CREATE INDEX idx_rentals_customer_id ON rentals(customer_id);
CREATE INDEX idx_rentals_status ON rentals(status);
CREATE INDEX idx_rentals_start_date ON rentals(start_date);
CREATE INDEX idx_rentals_end_date ON rentals(end_date);

-- rentals 테이블 코멘트
COMMENT ON TABLE rentals IS '캠핑 장비 대여 정보 테이블';
COMMENT ON COLUMN rentals.machine_id IS '대여 장비 ID (camping_items FK)';
COMMENT ON COLUMN rentals.customer_id IS '고객 ID (customers FK)';
COMMENT ON COLUMN rentals.status IS '대여 상태';
COMMENT ON COLUMN rentals.deposit IS '보증금';
COMMENT ON COLUMN rentals.actual_return_date IS '실제 반납일 (반납 전에는 NULL)';

-- ===================================
-- Migration 완료
-- ===================================
