-- ===================================================================
-- V6__group4_payroll.sql
-- Smart Dental - Phan 4, Nhom 4: Tinh luong bac si
-- UC4.1 Muc tien/gio, UC4.2 He so ca, UC4.3 He so ca phuc tap,
-- UC4.4 Phieu luong thang, UC4.5-4.7 Bao cao luong
-- ===================================================================

-- UC4.1: Muc tien co ban cho mot gio lam viec
CREATE TABLE doctor_hourly_rates (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    rate_code       VARCHAR(20)  NOT NULL,
    hourly_rate     DECIMAL(14,2) NOT NULL,
    effective_from  DATE         NOT NULL,
    effective_to    DATE         NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    note            VARCHAR(255) NULL,
    created_by      VARCHAR(50)  NULL,
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,
    CONSTRAINT uq_doctor_hourly_rates_code UNIQUE (rate_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_doctor_hourly_rates_status ON doctor_hourly_rates (status);

-- UC4.2: He so ca lam viec
CREATE TABLE shift_coefficients (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    coefficient_code VARCHAR(20) NOT NULL,
    work_shift_id   BIGINT       NOT NULL,
    coefficient     DECIMAL(5,2) NOT NULL,
    effective_from  DATE         NOT NULL,
    effective_to    DATE         NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    note            VARCHAR(255) NULL,
    created_by      VARCHAR(50)  NULL,
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,
    CONSTRAINT uq_shift_coefficients_code UNIQUE (coefficient_code),
    CONSTRAINT fk_shift_coefficients_work_shift FOREIGN KEY (work_shift_id) REFERENCES work_shifts (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_shift_coefficients_status ON shift_coefficients (status);
CREATE INDEX idx_shift_coefficients_shift ON shift_coefficients (work_shift_id);

-- UC4.3: He so ca kham phuc tap de xuat theo phien dieu tri
CREATE TABLE complex_case_coefficients (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    coefficient_code    VARCHAR(20)  NOT NULL,
    treatment_session_id BIGINT      NOT NULL,
    doctor_id           BIGINT       NOT NULL,
    coefficient         DECIMAL(5,2) NOT NULL,
    reason              VARCHAR(500) NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    approved_by         VARCHAR(50)  NULL,
    reject_reason       VARCHAR(255) NULL,
    proposed_by         VARCHAR(50)  NULL,
    created_at          DATETIME     NOT NULL,
    updated_at          DATETIME     NOT NULL,
    CONSTRAINT uq_complex_case_coefficients_code UNIQUE (coefficient_code),
    CONSTRAINT fk_complex_case_coefficients_session FOREIGN KEY (treatment_session_id) REFERENCES treatment_sessions (id),
    CONSTRAINT fk_complex_case_coefficients_doctor FOREIGN KEY (doctor_id) REFERENCES employees (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_complex_case_coefficients_status ON complex_case_coefficients (status);
CREATE INDEX idx_complex_case_coefficients_doctor ON complex_case_coefficients (doctor_id);
CREATE INDEX idx_complex_case_coefficients_session ON complex_case_coefficients (treatment_session_id);

-- UC4.4: Phieu luong bac si theo thang
CREATE TABLE payroll_slips (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    slip_code       VARCHAR(20)  NOT NULL,
    doctor_id       BIGINT       NOT NULL,
    payroll_year    INT          NOT NULL,
    payroll_month   INT          NOT NULL,
    total_salary    DECIMAL(16,2) NOT NULL DEFAULT 0,
    status          VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    note            VARCHAR(255) NULL,
    created_by      VARCHAR(50)  NULL,
    approved_by     VARCHAR(50)  NULL,
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,
    CONSTRAINT uq_payroll_slips_code UNIQUE (slip_code),
    CONSTRAINT uq_payroll_slips_doctor_period UNIQUE (doctor_id, payroll_year, payroll_month),
    CONSTRAINT fk_payroll_slips_doctor FOREIGN KEY (doctor_id) REFERENCES employees (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_payroll_slips_status ON payroll_slips (status);
CREATE INDEX idx_payroll_slips_period ON payroll_slips (payroll_year, payroll_month);

-- UC4.4: Chi tiet luong theo tung ca truc trong phieu luong (snapshot)
CREATE TABLE payroll_items (
    id                              BIGINT AUTO_INCREMENT PRIMARY KEY,
    payroll_slip_id                 BIGINT       NOT NULL,
    doctor_shift_registration_id    BIGINT       NOT NULL,
    work_date                       DATE         NOT NULL,
    shift_name_snapshot             VARCHAR(100) NOT NULL,
    total_hours                     DECIMAL(6,2) NOT NULL,
    shift_coefficient_snapshot      DECIMAL(5,2) NOT NULL,
    patient_coefficient_snapshot    DECIMAL(5,2) NOT NULL DEFAULT 0,
    converted_hours                 DECIMAL(8,2) NOT NULL,
    degree_coefficient_snapshot     DECIMAL(5,2) NOT NULL,
    hourly_rate_snapshot            DECIMAL(14,2) NOT NULL,
    amount                          DECIMAL(16,2) NOT NULL,
    created_at                      DATETIME     NOT NULL,
    updated_at                      DATETIME     NOT NULL,
    CONSTRAINT fk_payroll_items_slip FOREIGN KEY (payroll_slip_id) REFERENCES payroll_slips (id),
    CONSTRAINT fk_payroll_items_shift_reg FOREIGN KEY (doctor_shift_registration_id) REFERENCES doctor_shift_registrations (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_payroll_items_slip ON payroll_items (payroll_slip_id);

-- ===================================================================
-- Seed: Muc tien/gio mac dinh dang ap dung
-- ===================================================================
INSERT INTO doctor_hourly_rates (rate_code, hourly_rate, effective_from, effective_to, status, note, created_by, created_at, updated_at) VALUES
('TG0001', 100000.00, '2026-01-01', NULL, 'ACTIVE', 'Muc tien luong co ban mac dinh', 'system', NOW(), NOW());

-- ===================================================================
-- Seed: He so ca lam viec mac dinh cho cac ca hien co
-- ===================================================================
INSERT INTO shift_coefficients (coefficient_code, work_shift_id, coefficient, effective_from, effective_to, status, note, created_by, created_at, updated_at)
SELECT 'HSC0001', id, 1.0, '2026-01-01', NULL, 'ACTIVE', 'He so mac dinh ca sang', 'system', NOW(), NOW() FROM work_shifts WHERE name = 'Ca sang';

INSERT INTO shift_coefficients (coefficient_code, work_shift_id, coefficient, effective_from, effective_to, status, note, created_by, created_at, updated_at)
SELECT 'HSC0002', id, 1.1, '2026-01-01', NULL, 'ACTIVE', 'He so mac dinh ca chieu', 'system', NOW(), NOW() FROM work_shifts WHERE name = 'Ca chieu';

INSERT INTO shift_coefficients (coefficient_code, work_shift_id, coefficient, effective_from, effective_to, status, note, created_by, created_at, updated_at)
SELECT 'HSC0003', id, 1.2, '2026-01-01', NULL, 'ACTIVE', 'He so mac dinh ca toi', 'system', NOW(), NOW() FROM work_shifts WHERE name = 'Ca toi';

-- ===================================================================
-- Seed: Cap nhat code_sequences cho cac prefix moi
-- ===================================================================
INSERT INTO code_sequences (sequence_key, current_value, updated_at)
SELECT 'HOURLY_RATE', 1, NOW() WHERE NOT EXISTS (SELECT 1 FROM code_sequences WHERE sequence_key = 'HOURLY_RATE');

INSERT INTO code_sequences (sequence_key, current_value, updated_at)
SELECT 'SHIFT_COEFFICIENT', (SELECT COUNT(*) FROM shift_coefficients), NOW()
WHERE NOT EXISTS (SELECT 1 FROM code_sequences WHERE sequence_key = 'SHIFT_COEFFICIENT');

INSERT INTO code_sequences (sequence_key, current_value, updated_at)
SELECT 'COMPLEX_CASE_COEFFICIENT', 0, NOW() WHERE NOT EXISTS (SELECT 1 FROM code_sequences WHERE sequence_key = 'COMPLEX_CASE_COEFFICIENT');

INSERT INTO code_sequences (sequence_key, current_value, updated_at)
SELECT 'PAYROLL_SLIP', 0, NOW() WHERE NOT EXISTS (SELECT 1 FROM code_sequences WHERE sequence_key = 'PAYROLL_SLIP');
