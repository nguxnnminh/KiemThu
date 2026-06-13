-- ===================================================================
-- V1__init_core.sql
-- Smart Dental - Phan 0: Bang nen mong (employees, users, patients,
-- audit_logs, code_sequences) + seed du lieu demo
-- ===================================================================

CREATE TABLE employees (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_code   VARCHAR(20)  NOT NULL,
    full_name       VARCHAR(150) NOT NULL,
    date_of_birth   DATE         NULL,
    gender          VARCHAR(20)  NULL,
    phone           VARCHAR(15)  NULL,
    email           VARCHAR(150) NULL,
    address         VARCHAR(255) NULL,
    position        VARCHAR(20)  NOT NULL,
    specialty       VARCHAR(150) NULL,
    qualification   VARCHAR(150) NULL,
    workplace       VARCHAR(150) NULL,
    hire_date       DATE         NULL,
    degree          VARCHAR(20)  NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,
    CONSTRAINT uq_employees_code UNIQUE (employee_code),
    CONSTRAINT uq_employees_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_employees_position ON employees (position);
CREATE INDEX idx_employees_status ON employees (status);
CREATE INDEX idx_employees_full_name ON employees (full_name);

CREATE TABLE patients (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_code    VARCHAR(20)  NOT NULL,
    full_name       VARCHAR(150) NOT NULL,
    date_of_birth   DATE         NULL,
    gender          VARCHAR(20)  NULL,
    phone           VARCHAR(15)  NULL,
    email           VARCHAR(150) NULL,
    address         VARCHAR(255) NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,
    CONSTRAINT uq_patients_code UNIQUE (patient_code),
    CONSTRAINT uq_patients_phone UNIQUE (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_patients_full_name ON patients (full_name);

CREATE TABLE users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_code       VARCHAR(20)  NOT NULL,
    username        VARCHAR(50)  NOT NULL,
    password_hash   VARCHAR(100) NOT NULL,
    email           VARCHAR(150) NOT NULL,
    role            VARCHAR(20)  NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    employee_id     BIGINT       NULL,
    patient_id      BIGINT       NULL,
    locked_reason   VARCHAR(255) NULL,
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,
    CONSTRAINT uq_users_code UNIQUE (user_code),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT fk_users_employee FOREIGN KEY (employee_id) REFERENCES employees (id),
    CONSTRAINT fk_users_patient FOREIGN KEY (patient_id) REFERENCES patients (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_users_status ON users (status);

CREATE TABLE audit_logs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)  NULL,
    action          VARCHAR(50)  NOT NULL,
    entity_name     VARCHAR(100) NOT NULL,
    entity_id       VARCHAR(50)  NULL,
    description     VARCHAR(500) NULL,
    ip_address      VARCHAR(50)  NULL,
    created_at      DATETIME     NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_audit_logs_entity ON audit_logs (entity_name, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);

CREATE TABLE code_sequences (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    sequence_key    VARCHAR(50)  NOT NULL,
    current_value   BIGINT       NOT NULL DEFAULT 0,
    updated_at      DATETIME     NOT NULL,
    CONSTRAINT uq_code_sequences_key UNIQUE (sequence_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===================================================================
-- Seed: Employees mau
-- ===================================================================
INSERT INTO employees (employee_code, full_name, date_of_birth, gender, phone, email, address, position, specialty, qualification, workplace, hire_date, degree, status, created_at, updated_at)
VALUES
('QL0001', 'Nguyen Van Quan Ly', '1985-01-15', 'MALE', '0901000001', 'manager@smartdental.vn', 'Ha Noi', 'MANAGER', NULL, NULL, 'Smart Dental', '2020-01-01', NULL, 'ACTIVE', NOW(), NOW()),
('LT0001', 'Tran Thi Le Tan', '1995-05-20', 'FEMALE', '0901000002', 'reception@smartdental.vn', 'Ha Noi', 'RECEPTIONIST', NULL, NULL, 'Smart Dental', '2021-03-01', NULL, 'ACTIVE', NOW(), NOW()),
('BS0001', 'Le Van Bac Si', '1988-09-10', 'MALE', '0901000003', 'doctor@smartdental.vn', 'Ha Noi', 'DOCTOR', 'Nha khoa tong quat', 'Bac si nha khoa', 'Smart Dental', '2019-06-01', 'MASTER', 'ACTIVE', NOW(), NOW());

-- ===================================================================
-- Seed: Users demo
-- Mat khau goc (BCrypt da ma hoa):
--   admin     / Admin123
--   manager   / Manager123
--   reception / Reception123
--   doctor    / Doctor123
--   patient   / Patient123
-- ===================================================================
INSERT INTO users (user_code, username, password_hash, email, role, status, employee_id, patient_id, created_at, updated_at)
VALUES
('ND0001', 'admin', '$2b$10$mLRz7Ddt15wY6Fey3wQ2M.UcHRbtjNY9RQbg8Y.OLYH0DIL2pMlRG', 'admin@smartdental.vn', 'ADMIN', 'ACTIVE', NULL, NULL, NOW(), NOW()),
('ND0002', 'manager', '$2b$10$va5sDMXjf4jJaiyGgUDG1O2UEyYo4yq5pivAP3EPmJjffy/Zikm/S', 'manager@smartdental.vn', 'MANAGER', 'ACTIVE', (SELECT id FROM employees WHERE employee_code = 'QL0001'), NULL, NOW(), NOW()),
('ND0003', 'reception', '$2b$10$FMat6JdnhVujm09VJqNEjet3ZcBE5Zp/el80fMH2Ow3hnYAjyUMPe', 'reception@smartdental.vn', 'RECEPTIONIST', 'ACTIVE', (SELECT id FROM employees WHERE employee_code = 'LT0001'), NULL, NOW(), NOW()),
('ND0004', 'doctor', '$2b$10$HQq4Xy6OhV3wo3.sheMf2OVJwcTTN5ebtBLJY7AbKuwUReW2Jz9dm', 'doctor@smartdental.vn', 'DOCTOR', 'ACTIVE', (SELECT id FROM employees WHERE employee_code = 'BS0001'), NULL, NOW(), NOW());

-- Patient demo
INSERT INTO patients (patient_code, full_name, date_of_birth, gender, phone, email, address, status, created_at, updated_at)
VALUES
('BN0001', 'Pham Thi Benh Nhan', '1999-12-01', 'FEMALE', '0909000001', 'patient@smartdental.vn', 'Ha Noi', 'ACTIVE', NOW(), NOW());

INSERT INTO users (user_code, username, password_hash, email, role, status, employee_id, patient_id, created_at, updated_at)
VALUES
('ND0005', 'patient', '$2b$10$NNRcT6LyB4eYE81L92Xsh.FB9h8KPPzUcYOlzc1WrvACoc//tFd4e', 'patient@smartdental.vn', 'PATIENT', 'ACTIVE', NULL, (SELECT id FROM patients WHERE patient_code = 'BN0001'), NOW(), NOW());

-- ===================================================================
-- Seed: Code sequences
-- ===================================================================
INSERT INTO code_sequences (sequence_key, current_value, updated_at) VALUES
('USER', 5, NOW()),
('DOCTOR', 1, NOW()),
('RECEPTIONIST', 1, NOW()),
('MANAGER', 1, NOW()),
('PATIENT', 1, NOW());
