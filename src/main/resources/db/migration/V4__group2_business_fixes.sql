-- ===================================================================
-- V4__group2_business_fixes.sql
-- Smart Dental - Va loi nghiep vu Phan 2 truoc khi sang Phan 3
-- - Lich truc bac si (UC2.3) bo sung ghe truc va so luong lich kham toi da
-- - Lich kham (UC2.4) ke thua phong/ghe tu lich truc CONFIRMED (APPROVED)
-- ===================================================================

-- UC2.2: Gioi han so lich kham toi da cho moi ca lam viec
ALTER TABLE work_shifts
    ADD COLUMN max_appointments INT NOT NULL DEFAULT 10;

-- UC2.3: Lich truc bac si gan voi ghe cu the (ngoai phong)
ALTER TABLE doctor_shift_registrations
    ADD COLUMN chair_id BIGINT NULL;

ALTER TABLE doctor_shift_registrations
    ADD CONSTRAINT fk_doctor_shift_reg_chair FOREIGN KEY (chair_id) REFERENCES chairs (id);

-- UC2.4: Lich kham gan voi ca lam viec va lich truc da duoc duyet
ALTER TABLE appointments
    ADD COLUMN work_shift_id BIGINT NULL;

ALTER TABLE appointments
    ADD COLUMN doctor_shift_registration_id BIGINT NULL;

ALTER TABLE appointments
    ADD CONSTRAINT fk_appointments_work_shift FOREIGN KEY (work_shift_id) REFERENCES work_shifts (id);

ALTER TABLE appointments
    ADD CONSTRAINT fk_appointments_doctor_shift_reg FOREIGN KEY (doctor_shift_registration_id) REFERENCES doctor_shift_registrations (id);

CREATE INDEX idx_appointments_doctor_shift_reg ON appointments (doctor_shift_registration_id);
