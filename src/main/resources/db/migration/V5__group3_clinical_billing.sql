-- ===================================================================
-- V5__group3_clinical_billing.sql
-- Smart Dental - Phan 3, Nhom 3: Tiep don va kham benh
-- UC3.1 Check-in, UC3.2 Kham benh & ho so benh an, UC3.3 So do rang FDI 32,
-- UC3.4 Dang ky dich vu dieu tri, UC3.5 Thanh toan, UC3.6 Thong ke doanh thu
-- ===================================================================

-- UC3.1: Tiep don / check-in benh nhan
CREATE TABLE visit_checkins (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id      BIGINT       NOT NULL,
    patient_id          BIGINT       NOT NULL,
    receptionist_id     BIGINT       NULL,
    checkin_time        DATETIME     NOT NULL,
    queue_number        INT          NOT NULL,
    queue_date          DATE         NOT NULL,
    arrival_status      VARCHAR(20)  NOT NULL,
    initial_symptoms    VARCHAR(500) NULL,
    note                VARCHAR(255) NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'WAITING',
    cancel_reason       VARCHAR(255) NULL,
    created_at          DATETIME     NOT NULL,
    updated_at          DATETIME     NOT NULL,
    CONSTRAINT fk_visit_checkins_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (id),
    CONSTRAINT fk_visit_checkins_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_visit_checkins_receptionist FOREIGN KEY (receptionist_id) REFERENCES employees (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_visit_checkins_status ON visit_checkins (status);
CREATE INDEX idx_visit_checkins_queue ON visit_checkins (queue_date, queue_number);
CREATE UNIQUE INDEX uq_visit_checkins_queue_date_number ON visit_checkins (queue_date, queue_number);

-- UC3.2: Ho so benh an chinh cua benh nhan
CREATE TABLE medical_records (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_code     VARCHAR(20)  NOT NULL,
    patient_id      BIGINT       NOT NULL,
    note            VARCHAR(500) NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,
    CONSTRAINT uq_medical_records_code UNIQUE (record_code),
    CONSTRAINT uq_medical_records_patient UNIQUE (patient_id),
    CONSTRAINT fk_medical_records_patient FOREIGN KEY (patient_id) REFERENCES patients (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- UC3.2: Phien kham benh
CREATE TABLE treatment_sessions (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_code            VARCHAR(20)  NOT NULL,
    appointment_id          BIGINT       NOT NULL,
    medical_record_id       BIGINT       NOT NULL,
    patient_id              BIGINT       NOT NULL,
    doctor_id               BIGINT       NOT NULL,
    checkin_id              BIGINT       NOT NULL,
    examination_date        DATE         NOT NULL,
    symptom                 VARCHAR(1000) NULL,
    diagnosis               VARCHAR(1000) NULL,
    treatment_plan          VARCHAR(1000) NULL,
    doctor_note             VARCHAR(1000) NULL,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
    difficult_coefficient   DECIMAL(5,2) NULL,
    created_at              DATETIME     NOT NULL,
    updated_at              DATETIME     NOT NULL,
    CONSTRAINT uq_treatment_sessions_code UNIQUE (session_code),
    CONSTRAINT uq_treatment_sessions_appointment UNIQUE (appointment_id),
    CONSTRAINT fk_treatment_sessions_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (id),
    CONSTRAINT fk_treatment_sessions_medical_record FOREIGN KEY (medical_record_id) REFERENCES medical_records (id),
    CONSTRAINT fk_treatment_sessions_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_treatment_sessions_doctor FOREIGN KEY (doctor_id) REFERENCES employees (id),
    CONSTRAINT fk_treatment_sessions_checkin FOREIGN KEY (checkin_id) REFERENCES visit_checkins (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_treatment_sessions_status ON treatment_sessions (status);
CREATE INDEX idx_treatment_sessions_doctor ON treatment_sessions (doctor_id);
CREATE INDEX idx_treatment_sessions_patient ON treatment_sessions (patient_id);

-- UC3.3: Trang thai hien hanh cua tung rang theo benh nhan
CREATE TABLE dental_tooth_status (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    medical_record_id           BIGINT       NOT NULL,
    patient_id                  BIGINT       NOT NULL,
    tooth_number                INT          NOT NULL,
    tooth_name                  VARCHAR(50)  NULL,
    status                      VARCHAR(20)  NOT NULL DEFAULT 'NORMAL',
    diagnosis                   VARCHAR(255) NULL,
    note                        VARCHAR(255) NULL,
    last_updated_session_id     BIGINT       NULL,
    created_at                  DATETIME     NOT NULL,
    updated_at                  DATETIME     NOT NULL,
    CONSTRAINT uq_dental_tooth_status_patient_tooth UNIQUE (patient_id, tooth_number),
    CONSTRAINT fk_dental_tooth_status_medical_record FOREIGN KEY (medical_record_id) REFERENCES medical_records (id),
    CONSTRAINT fk_dental_tooth_status_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_dental_tooth_status_session FOREIGN KEY (last_updated_session_id) REFERENCES treatment_sessions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- UC3.3: Lich su thay doi trang thai rang (khong xoa)
CREATE TABLE tooth_treatment_histories (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    medical_record_id   BIGINT       NOT NULL,
    patient_id          BIGINT       NOT NULL,
    treatment_session_id BIGINT      NOT NULL,
    tooth_number        INT          NOT NULL,
    old_status          VARCHAR(20)  NULL,
    new_status          VARCHAR(20)  NOT NULL,
    diagnosis           VARCHAR(255) NULL,
    note                VARCHAR(255) NULL,
    updated_by          VARCHAR(50)  NULL,
    updated_at          DATETIME     NOT NULL,
    CONSTRAINT fk_tooth_history_medical_record FOREIGN KEY (medical_record_id) REFERENCES medical_records (id),
    CONSTRAINT fk_tooth_history_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_tooth_history_session FOREIGN KEY (treatment_session_id) REFERENCES treatment_sessions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_tooth_history_patient_tooth ON tooth_treatment_histories (patient_id, tooth_number);

-- UC3.4: Dich vu dieu tri da dang ky cho mot phien kham
CREATE TABLE registered_services (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    treatment_session_id    BIGINT       NOT NULL,
    patient_id              BIGINT       NOT NULL,
    service_id              BIGINT       NOT NULL,
    service_price_id        BIGINT       NOT NULL,
    service_name_snapshot   VARCHAR(150) NOT NULL,
    unit_snapshot           VARCHAR(20)  NOT NULL,
    quantity                INT          NOT NULL DEFAULT 1,
    unit_price              DECIMAL(14,2) NOT NULL,
    discount_amount         DECIMAL(14,2) NOT NULL DEFAULT 0,
    total_amount            DECIMAL(14,2) NOT NULL,
    tooth_number            INT          NULL,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    note                    VARCHAR(255) NULL,
    created_at              DATETIME     NOT NULL,
    updated_at              DATETIME     NOT NULL,
    CONSTRAINT fk_registered_services_session FOREIGN KEY (treatment_session_id) REFERENCES treatment_sessions (id),
    CONSTRAINT fk_registered_services_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_registered_services_service FOREIGN KEY (service_id) REFERENCES services (id),
    CONSTRAINT fk_registered_services_price FOREIGN KEY (service_price_id) REFERENCES service_prices (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_registered_services_session ON registered_services (treatment_session_id);
CREATE INDEX idx_registered_services_status ON registered_services (status);

-- UC3.5: Hoa don
CREATE TABLE invoices (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_code            VARCHAR(20)  NOT NULL,
    patient_id              BIGINT       NOT NULL,
    treatment_session_id    BIGINT       NOT NULL,
    total_amount            DECIMAL(14,2) NOT NULL,
    discount_amount         DECIMAL(14,2) NOT NULL DEFAULT 0,
    discount_type           VARCHAR(20)  NOT NULL DEFAULT 'NONE',
    discount_note           VARCHAR(255) NULL,
    final_amount            DECIMAL(14,2) NOT NULL,
    paid_amount             DECIMAL(14,2) NOT NULL DEFAULT 0,
    remaining_amount        DECIMAL(14,2) NOT NULL,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'UNPAID',
    created_by              VARCHAR(50)  NULL,
    created_at              DATETIME     NOT NULL,
    updated_at              DATETIME     NOT NULL,
    CONSTRAINT uq_invoices_code UNIQUE (invoice_code),
    CONSTRAINT uq_invoices_session UNIQUE (treatment_session_id),
    CONSTRAINT fk_invoices_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_invoices_session FOREIGN KEY (treatment_session_id) REFERENCES treatment_sessions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_invoices_status ON invoices (status);
CREATE INDEX idx_invoices_created_at ON invoices (created_at);

-- UC3.5: Lich su thu/hoan tien cua hoa don
CREATE TABLE payments (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id          BIGINT       NOT NULL,
    amount              DECIMAL(14,2) NOT NULL,
    payment_method      VARCHAR(20)  NOT NULL,
    payment_type        VARCHAR(20)  NOT NULL DEFAULT 'PAYMENT',
    collected_by        VARCHAR(50)  NULL,
    paid_at             DATETIME     NOT NULL,
    note                VARCHAR(255) NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'SUCCESS',
    created_at          DATETIME     NOT NULL,
    updated_at          DATETIME     NOT NULL,
    CONSTRAINT fk_payments_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_payments_invoice ON payments (invoice_id);
CREATE INDEX idx_payments_paid_at ON payments (paid_at);
