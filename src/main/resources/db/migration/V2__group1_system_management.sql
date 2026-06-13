-- ===================================================================
-- V2__group1_system_management.sql
-- Smart Dental - Phan 1, Nhom 1: Quan ly he thong
-- Bo sung danh muc dich vu (UC1.3) va bang gia dich vu (UC1.4)
-- ===================================================================

CREATE TABLE service_categories (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_code   VARCHAR(20)  NOT NULL,
    name            VARCHAR(150) NOT NULL,
    description     VARCHAR(255) NULL,
    color_hex       VARCHAR(10)  NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,
    CONSTRAINT uq_service_categories_code UNIQUE (category_code),
    CONSTRAINT uq_service_categories_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE services (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_code        VARCHAR(20)  NOT NULL,
    name                VARCHAR(150) NOT NULL,
    category_id         BIGINT       NOT NULL,
    unit                VARCHAR(20)  NOT NULL,
    duration_minutes    INT          NOT NULL,
    description         VARCHAR(255) NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at          DATETIME     NOT NULL,
    updated_at          DATETIME     NOT NULL,
    CONSTRAINT uq_services_code UNIQUE (service_code),
    CONSTRAINT uq_services_category_name UNIQUE (category_id, name),
    CONSTRAINT fk_services_category FOREIGN KEY (category_id) REFERENCES service_categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_services_status ON services (status);

CREATE TABLE service_prices (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_id      BIGINT       NOT NULL,
    price           DECIMAL(14,2) NOT NULL,
    effective_from  DATE         NOT NULL,
    effective_to    DATE         NULL,
    reason          VARCHAR(255) NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_by      VARCHAR(50)  NULL,
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,
    CONSTRAINT fk_service_prices_service FOREIGN KEY (service_id) REFERENCES services (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_service_prices_service ON service_prices (service_id);
CREATE INDEX idx_service_prices_status ON service_prices (status);

-- ===================================================================
-- Sequence cho ma dich vu / nhom dich vu
-- ===================================================================
INSERT INTO code_sequences (sequence_key, current_value, updated_at) VALUES
('SERVICE', 0, NOW()),
('SERVICE_CATEGORY', 0, NOW());

-- ===================================================================
-- Seed: mot vai nhom dich vu va dich vu mau de UC1.4 co du lieu thao tac
-- ===================================================================
INSERT INTO service_categories (category_code, name, description, color_hex, status, created_at, updated_at) VALUES
('NH001', 'Kham va tu van', 'Cac dich vu kham, tu van tong quat', '#2563eb', 'ACTIVE', NOW(), NOW()),
('NH002', 'Dieu tri rang', 'Cac dich vu dieu tri, han, nho rang', '#16a34a', 'ACTIVE', NOW(), NOW());

UPDATE code_sequences SET current_value = 2, updated_at = NOW() WHERE sequence_key = 'SERVICE_CATEGORY';

INSERT INTO services (service_code, name, category_id, unit, duration_minutes, description, status, created_at, updated_at) VALUES
('DV001', 'Kham tong quat', (SELECT id FROM service_categories WHERE category_code = 'NH001'), 'LAN', 30, 'Kham va tu van tong quat ban dau', 'ACTIVE', NOW(), NOW()),
('DV002', 'Han rang sau', (SELECT id FROM service_categories WHERE category_code = 'NH002'), 'RANG', 45, 'Han tham my rang sau', 'ACTIVE', NOW(), NOW());

UPDATE code_sequences SET current_value = 2, updated_at = NOW() WHERE sequence_key = 'SERVICE';

INSERT INTO service_prices (service_id, price, effective_from, effective_to, reason, status, created_by, created_at, updated_at) VALUES
((SELECT id FROM services WHERE service_code = 'DV001'), 100000, '2024-01-01', NULL, 'Gia khoi tao he thong', 'ACTIVE', 'admin', NOW(), NOW()),
((SELECT id FROM services WHERE service_code = 'DV002'), 350000, '2024-01-01', NULL, 'Gia khoi tao he thong', 'ACTIVE', 'admin', NOW(), NOW());
