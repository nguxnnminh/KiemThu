-- ===================================================================
-- V3__group2_schedule_management.sql
-- Smart Dental - Phan 2, Nhom 2: Quan ly lich kham
-- UC2.1 Ngay nghi, UC2.2 Ca lam viec, UC2.3 Phong & ghe,
-- UC2.4 Lich truc bac si, UC2.5 Dat lich kham, UC2.6 Quan ly benh nhan
-- ===================================================================

-- UC2.1: Ngay nghi / lich nghi cua phong kham
CREATE TABLE holidays (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    holiday_code    VARCHAR(20)  NOT NULL,
    name            VARCHAR(150) NOT NULL,
    start_date      DATE         NOT NULL,
    end_date        DATE         NOT NULL,
    holiday_type    VARCHAR(20)  NOT NULL,
    description     VARCHAR(255) NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,
    CONSTRAINT uq_holidays_code UNIQUE (holiday_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_holidays_dates ON holidays (start_date, end_date);
CREATE INDEX idx_holidays_status ON holidays (status);

-- UC2.2: Ca lam viec chuan cua phong kham
CREATE TABLE work_shifts (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    shift_code      VARCHAR(20)  NOT NULL,
    name            VARCHAR(100) NOT NULL,
    start_time      TIME         NOT NULL,
    end_time        TIME         NOT NULL,
    description     VARCHAR(255) NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,
    CONSTRAINT uq_work_shifts_code UNIQUE (shift_code),
    CONSTRAINT uq_work_shifts_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- UC2.3: Phong kham
CREATE TABLE rooms (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_code       VARCHAR(20)  NOT NULL,
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(255) NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,
    CONSTRAINT uq_rooms_code UNIQUE (room_code),
    CONSTRAINT uq_rooms_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- UC2.3: Ghe nha khoa thuoc phong
CREATE TABLE chairs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    chair_code      VARCHAR(20)  NOT NULL,
    name            VARCHAR(100) NOT NULL,
    room_id         BIGINT       NOT NULL,
    description     VARCHAR(255) NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,
    CONSTRAINT uq_chairs_code UNIQUE (chair_code),
    CONSTRAINT uq_chairs_room_name UNIQUE (room_id, name),
    CONSTRAINT fk_chairs_room FOREIGN KEY (room_id) REFERENCES rooms (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_chairs_status ON chairs (status);

-- UC2.4: Dang ky lich truc bac si theo ngay + ca
CREATE TABLE doctor_shift_registrations (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    registration_code VARCHAR(20) NOT NULL,
    doctor_id       BIGINT       NOT NULL,
    work_shift_id   BIGINT       NOT NULL,
    work_date       DATE         NOT NULL,
    room_id         BIGINT       NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'REGISTERED',
    note            VARCHAR(255) NULL,
    approved_by     VARCHAR(50)  NULL,
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,
    CONSTRAINT uq_doctor_shift_reg_code UNIQUE (registration_code),
    CONSTRAINT uq_doctor_shift_reg_unique UNIQUE (doctor_id, work_shift_id, work_date),
    CONSTRAINT fk_doctor_shift_reg_doctor FOREIGN KEY (doctor_id) REFERENCES employees (id),
    CONSTRAINT fk_doctor_shift_reg_shift FOREIGN KEY (work_shift_id) REFERENCES work_shifts (id),
    CONSTRAINT fk_doctor_shift_reg_room FOREIGN KEY (room_id) REFERENCES rooms (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_doctor_shift_reg_date ON doctor_shift_registrations (work_date);
CREATE INDEX idx_doctor_shift_reg_status ON doctor_shift_registrations (status);

-- UC2.5: Lich kham
CREATE TABLE appointments (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_code    VARCHAR(20)  NOT NULL,
    patient_id          BIGINT       NOT NULL,
    doctor_id           BIGINT       NULL,
    service_id          BIGINT       NULL,
    room_id             BIGINT       NULL,
    chair_id            BIGINT       NULL,
    appointment_date    DATE         NOT NULL,
    start_time          TIME         NOT NULL,
    end_time            TIME         NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    source              VARCHAR(20)  NOT NULL DEFAULT 'RECEPTIONIST',
    note                VARCHAR(255) NULL,
    cancel_reason       VARCHAR(255) NULL,
    created_at          DATETIME     NOT NULL,
    updated_at          DATETIME     NOT NULL,
    CONSTRAINT uq_appointments_code UNIQUE (appointment_code),
    CONSTRAINT fk_appointments_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_appointments_doctor FOREIGN KEY (doctor_id) REFERENCES employees (id),
    CONSTRAINT fk_appointments_service FOREIGN KEY (service_id) REFERENCES services (id),
    CONSTRAINT fk_appointments_room FOREIGN KEY (room_id) REFERENCES rooms (id),
    CONSTRAINT fk_appointments_chair FOREIGN KEY (chair_id) REFERENCES chairs (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_appointments_date ON appointments (appointment_date);
CREATE INDEX idx_appointments_status ON appointments (status);
CREATE INDEX idx_appointments_doctor ON appointments (doctor_id, appointment_date);
CREATE INDEX idx_appointments_patient ON appointments (patient_id);

-- UC2.5: Nhat ky thay doi trang thai lich kham
CREATE TABLE appointment_status_logs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id  BIGINT       NOT NULL,
    old_status      VARCHAR(20)  NULL,
    new_status      VARCHAR(20)  NOT NULL,
    changed_by      VARCHAR(50)  NULL,
    note            VARCHAR(255) NULL,
    created_at      DATETIME     NOT NULL,
    CONSTRAINT fk_appointment_status_logs_appt FOREIGN KEY (appointment_id) REFERENCES appointments (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_appointment_status_logs_appt ON appointment_status_logs (appointment_id);

-- ===================================================================
-- Sequence cho cac ma moi
-- ===================================================================
INSERT INTO code_sequences (sequence_key, current_value, updated_at) VALUES
('HOLIDAY', 0, NOW()),
('WORK_SHIFT', 0, NOW()),
('ROOM', 0, NOW()),
('CHAIR', 0, NOW()),
('DOCTOR_SHIFT', 0, NOW()),
('APPOINTMENT', 0, NOW());

-- ===================================================================
-- Seed: Ca lam viec chuan
-- ===================================================================
INSERT INTO work_shifts (shift_code, name, start_time, end_time, description, status, created_at, updated_at) VALUES
('CA001', 'Ca sang', '08:00:00', '12:00:00', 'Ca lam viec buoi sang', 'ACTIVE', NOW(), NOW()),
('CA002', 'Ca chieu', '13:30:00', '17:30:00', 'Ca lam viec buoi chieu', 'ACTIVE', NOW(), NOW()),
('CA003', 'Ca toi', '18:00:00', '21:00:00', 'Ca lam viec buoi toi', 'ACTIVE', NOW(), NOW());

UPDATE code_sequences SET current_value = 3, updated_at = NOW() WHERE sequence_key = 'WORK_SHIFT';

-- ===================================================================
-- Seed: Phong va ghe
-- ===================================================================
INSERT INTO rooms (room_code, name, description, status, created_at, updated_at) VALUES
('P001', 'Phong kham 1', 'Phong kham tong quat tang 1', 'ACTIVE', NOW(), NOW()),
('P002', 'Phong kham 2', 'Phong dieu tri tang 2', 'ACTIVE', NOW(), NOW());

UPDATE code_sequences SET current_value = 2, updated_at = NOW() WHERE sequence_key = 'ROOM';

INSERT INTO chairs (chair_code, name, room_id, description, status, created_at, updated_at) VALUES
('G001', 'Ghe 1', (SELECT id FROM rooms WHERE room_code = 'P001'), 'Ghe nha khoa so 1', 'ACTIVE', NOW(), NOW()),
('G002', 'Ghe 2', (SELECT id FROM rooms WHERE room_code = 'P001'), 'Ghe nha khoa so 2', 'ACTIVE', NOW(), NOW()),
('G003', 'Ghe 1', (SELECT id FROM rooms WHERE room_code = 'P002'), 'Ghe nha khoa so 1', 'ACTIVE', NOW(), NOW());

UPDATE code_sequences SET current_value = 3, updated_at = NOW() WHERE sequence_key = 'CHAIR';

-- ===================================================================
-- Seed: Ngay nghi mau
-- ===================================================================
INSERT INTO holidays (holiday_code, name, start_date, end_date, holiday_type, description, status, created_at, updated_at) VALUES
('NN001', 'Nghi le Quoc Khanh', '2026-09-02', '2026-09-02', 'HOLIDAY', 'Nghi le Quoc khanh 2/9', 'ACTIVE', NOW(), NOW());

UPDATE code_sequences SET current_value = 1, updated_at = NOW() WHERE sequence_key = 'HOLIDAY';
