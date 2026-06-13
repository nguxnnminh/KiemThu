-- ===================================================================
-- V7__seed_demo_data.sql
-- Smart Dental - Seed du lieu demo DAY DU theo dung quy trinh nghiep vu.
-- Phu MOI trang thai cua moi bang. TACH BIET hoan toan khoi du lieu
-- fixture cua test (BS0001/BN0001/DV001/CA00x/... giu nguyen, khong dung).
--
-- Nguyen tac:
--  - Moi FK tra cuu qua (SELECT id FROM ... WHERE code = ...).
--  - Ngay nghiep vu tinh quanh CURDATE() de queue/checkin/track co du lieu song.
--  - Tat ca nhan su/benh nhan/dich vu nghiep vu deu la BO MOI (BS0010+, BN0010+,
--    DV010+) => khong the va unique (doctor,shift,date) / (queue_date,number) /
--    (doctor,year,month) cua test.
--  - Tuong thich ca MySQL (dev) lan H2 MODE=MySQL (test): chi dung NOW(),
--    CURDATE(), TIMESTAMPADD(DAY, n, CURDATE()) (chuan JDBC, ca 2 deu ho tro),
--    YEAR(CURDATE())/MONTH(CURDATE()).
-- ===================================================================

-- ===================================================================
-- 1. DANH MUC MO RONG (phu ACTIVE + INACTIVE)
-- ===================================================================

-- 1a. Nhom dich vu moi
INSERT INTO service_categories (category_code, name, description, color_hex, status, created_at, updated_at) VALUES
('NH010', 'Chinh nha',        'Nieng rang, chinh nha tham my',        '#7c3aed', 'ACTIVE',   NOW(), NOW()),
('NH011', 'Phau thuat',       'Tieu phau, nho rang khon',             '#dc2626', 'ACTIVE',   NOW(), NOW()),
('NH012', 'Dich vu ngung',    'Nhom dich vu da ngung cung cap',       '#64748b', 'INACTIVE', NOW(), NOW());

-- 1b. Dich vu moi (gom 1 INACTIVE)
INSERT INTO services (service_code, name, category_id, unit, duration_minutes, description, status, created_at, updated_at) VALUES
('DV010', 'Nho rang khon',        (SELECT id FROM service_categories WHERE category_code='NH011'), 'RANG',       60, 'Tieu phau nho rang khon',            'ACTIVE',   NOW(), NOW()),
('DV011', 'Lay cao rang',         (SELECT id FROM service_categories WHERE category_code='NH001'), 'LAN',        30, 'Lay cao rang, danh bong',            'ACTIVE',   NOW(), NOW()),
('DV012', 'Tay trang rang',       (SELECT id FROM service_categories WHERE category_code='NH001'), 'LAN',        45, 'Tay trang rang tham my',             'ACTIVE',   NOW(), NOW()),
('DV013', 'Nieng rang mac cai',   (SELECT id FROM service_categories WHERE category_code='NH010'), 'LIEU_TRINH',90, 'Lieu trinh nieng rang mac cai',      'ACTIVE',   NOW(), NOW()),
('DV014', 'Chup phim X-quang',    (SELECT id FROM service_categories WHERE category_code='NH001'), 'PHIM',       15, 'Chup phim X-quang quanh chop',       'ACTIVE',   NOW(), NOW()),
('DV015', 'Dich vu da ngung',     (SELECT id FROM service_categories WHERE category_code='NH012'), 'LAN',        30, 'Dich vu khong con cung cap',         'INACTIVE', NOW(), NOW());

-- 1c. Gia dich vu: moi DV moi 1 gia ACTIVE; DV010 co lich su gia (1 cu INACTIVE + 1 moi ACTIVE)
INSERT INTO service_prices (service_id, price, effective_from, effective_to, reason, status, created_by, created_at, updated_at) VALUES
((SELECT id FROM services WHERE service_code='DV010'),  450000, '2024-01-01', '2025-12-31', 'Gia cu',                'EXPIRED',  'admin', NOW(), NOW()),
((SELECT id FROM services WHERE service_code='DV010'),  500000, '2026-01-01', NULL,         'Dieu chinh gia 2026',   'ACTIVE',   'admin', NOW(), NOW()),
((SELECT id FROM services WHERE service_code='DV011'),  150000, '2026-01-01', NULL,         'Gia khoi tao',          'ACTIVE',   'admin', NOW(), NOW()),
((SELECT id FROM services WHERE service_code='DV012'),  800000, '2026-01-01', NULL,         'Gia khoi tao',          'ACTIVE',   'admin', NOW(), NOW()),
((SELECT id FROM services WHERE service_code='DV013'), 2500000, '2026-01-01', NULL,         'Gia khoi tao',          'ACTIVE',   'admin', NOW(), NOW()),
((SELECT id FROM services WHERE service_code='DV014'),  120000, '2026-01-01', NULL,         'Gia khoi tao',          'ACTIVE',   'admin', NOW(), NOW());

-- 1d. Ngay nghi: them 1 da huy (INACTIVE) + 1 bao tri
INSERT INTO holidays (holiday_code, name, start_date, end_date, holiday_type, description, status, created_at, updated_at) VALUES
('NN010', 'Bao tri he thong ghe',  TIMESTAMPADD(DAY, 20, CURDATE()), TIMESTAMPADD(DAY, 20, CURDATE()), 'MAINTENANCE',   'Bao tri dinh ky ghe nha khoa',   'ACTIVE',   NOW(), NOW()),
('NN011', 'Nghi Tet (da huy)',     '2026-02-16',                          '2026-02-20',                          'CLINIC_CLOSED', 'Lich nghi da huy',               'INACTIVE', NOW(), NOW());

-- 1e. Phong + ghe: them 1 phong INACTIVE va 1 ghe INACTIVE
INSERT INTO rooms (room_code, name, description, status, created_at, updated_at) VALUES
('P010', 'Phong VIP',        'Phong dieu tri VIP tang 3',  'ACTIVE',   NOW(), NOW()),
('P011', 'Phong cu',         'Phong da ngung su dung',     'INACTIVE', NOW(), NOW());

INSERT INTO chairs (chair_code, name, room_id, description, status, created_at, updated_at) VALUES
('G010', 'Ghe VIP 1', (SELECT id FROM rooms WHERE room_code='P010'), 'Ghe nha khoa VIP', 'ACTIVE',   NOW(), NOW()),
('G011', 'Ghe cu',    (SELECT id FROM rooms WHERE room_code='P010'), 'Ghe da ngung',     'INACTIVE', NOW(), NOW());

-- 1f. Ca lam viec: them 1 ca INACTIVE (phu trang thai)
INSERT INTO work_shifts (shift_code, name, start_time, end_time, description, status, max_appointments, created_at, updated_at) VALUES
('CA010', 'Ca trua (ngung)', '12:00:00', '13:30:00', 'Ca trua da ngung su dung', 'INACTIVE', 5, NOW(), NOW());

-- ===================================================================
-- 2. NHAN SU + BENH NHAN + USERS MOI (phu moi trang thai)
-- ===================================================================

-- 2a. Nhan vien moi (bac si seed lam chu the nghiep vu, + cac trang thai)
INSERT INTO employees (employee_code, full_name, date_of_birth, gender, phone, email, address, position, specialty, qualification, workplace, hire_date, degree, status, created_at, updated_at) VALUES
('BS0010', 'Nguyen Van Seed',   '1986-03-12', 'MALE',   '0902000010', 'bs.seed@smartdental.vn',   'Ha Noi', 'DOCTOR',       'Nha khoa tong quat',  'Bac si CKI',        'Smart Dental', '2018-02-01', 'MASTER',     'ACTIVE',    NOW(), NOW()),
('BS0011', 'Tran Thi Mai',      '1990-07-22', 'FEMALE', '0902000011', 'bs.mai@smartdental.vn',    'Ha Noi', 'DOCTOR',       'Chinh nha',           'Tien si',           'Smart Dental', '2017-08-01', 'DOCTOR_PHD', 'ACTIVE',    NOW(), NOW()),
('BS0012', 'Le Van Tam',        '1992-11-05', 'MALE',   '0902000012', 'bs.tam@smartdental.vn',    'Ha Noi', 'DOCTOR',       'Phau thuat',          'Bac si nha khoa',   'Smart Dental', '2022-01-01', 'BACHELOR',   'SUSPENDED', NOW(), NOW()),
('LT0010', 'Pham Thi Thu',      '1996-09-09', 'FEMALE', '0902000013', 'lt.thu@smartdental.vn',    'Ha Noi', 'RECEPTIONIST', NULL,                  NULL,                'Smart Dental', '2021-06-01', NULL,         'ACTIVE',    NOW(), NOW()),
('LT0011', 'Vu Thi Nghi',       '1994-04-18', 'FEMALE', '0902000014', 'lt.nghi@smartdental.vn',   'Ha Noi', 'RECEPTIONIST', NULL,                  NULL,                'Smart Dental', '2020-03-01', NULL,         'INACTIVE',  NOW(), NOW());

-- 2b. Users moi: 1 gan bac si seed (dang nhap demo), 1 LOCKED (phu trang thai)
-- Hash mat khau tai dung tu V1: 'bsseed' dung hash cua 'doctor' (Doctor123),
-- 'ltthu' dung hash cua 'reception' (Reception123).
INSERT INTO users (user_code, username, password_hash, email, role, status, employee_id, patient_id, created_at, updated_at) VALUES
('ND0010', 'bsseed', '$2b$10$HQq4Xy6OhV3wo3.sheMf2OVJwcTTN5ebtBLJY7AbKuwUReW2Jz9dm', 'bs.seed@smartdental.vn', 'DOCTOR',       'ACTIVE', (SELECT id FROM employees WHERE employee_code='BS0010'), NULL, NOW(), NOW()),
('ND0011', 'ltthu',  '$2b$10$FMat6JdnhVujm09VJqNEjet3ZcBE5Zp/el80fMH2Ow3hnYAjyUMPe', 'lt.thu@smartdental.vn',  'RECEPTIONIST', 'LOCKED', (SELECT id FROM employees WHERE employee_code='LT0010'), NULL, NOW(), NOW());
UPDATE users SET locked_reason = 'Tam khoa do nghi phep dai ngay' WHERE username = 'ltthu';

-- 2c. Benh nhan moi (gom 1 INACTIVE)
INSERT INTO patients (patient_code, full_name, date_of_birth, gender, phone, email, address, status, created_at, updated_at) VALUES
('BN0010', 'Nguyen Thi Hoa',  '1995-02-10', 'FEMALE', '0912000010', 'hoa.nt@gmail.com',   'Ha Noi',     'ACTIVE',   NOW(), NOW()),
('BN0011', 'Tran Van Nam',    '1988-06-25', 'MALE',   '0912000011', 'nam.tv@gmail.com',   'Ha Noi',     'ACTIVE',   NOW(), NOW()),
('BN0012', 'Le Thi Lan',      '2000-12-01', 'FEMALE', '0912000012', 'lan.lt@gmail.com',   'Bac Ninh',   'ACTIVE',   NOW(), NOW()),
('BN0013', 'Pham Van Khang',  '1979-08-30', 'MALE',   '0912000013', 'khang.pv@gmail.com', 'Ha Noi',     'ACTIVE',   NOW(), NOW()),
('BN0014', 'Hoang Thi Yen',   '1993-03-14', 'FEMALE', '0912000014', 'yen.ht@gmail.com',   'Hung Yen',   'ACTIVE',   NOW(), NOW()),
('BN0015', 'Do Van Cuong',    '1985-10-19', 'MALE',   '0912000015', 'cuong.dv@gmail.com', 'Ha Noi',     'ACTIVE',   NOW(), NOW()),
('BN0016', 'Vu Thi Ngung',    '1991-01-08', 'FEMALE', '0912000016', 'ngung.vt@gmail.com', 'Ha Nam',     'INACTIVE', NOW(), NOW());

-- 2d. Ho so benh an cho cac benh nhan co nghiep vu (1:1 voi patient)
INSERT INTO medical_records (record_code, patient_id, note, status, created_at, updated_at) VALUES
('SD-MR0010', (SELECT id FROM patients WHERE patient_code='BN0010'), 'Di ung thuoc te lidocaine', 'ACTIVE', NOW(), NOW()),
('SD-MR0011', (SELECT id FROM patients WHERE patient_code='BN0011'), NULL,                         'ACTIVE', NOW(), NOW()),
('SD-MR0012', (SELECT id FROM patients WHERE patient_code='BN0012'), 'Tien su viem nha chu',       'ACTIVE', NOW(), NOW()),
('SD-MR0013', (SELECT id FROM patients WHERE patient_code='BN0013'), NULL,                         'ACTIVE', NOW(), NOW()),
('SD-MR0014', (SELECT id FROM patients WHERE patient_code='BN0014'), NULL,                         'ACTIVE', NOW(), NOW()),
('SD-MR0015', (SELECT id FROM patients WHERE patient_code='BN0015'), 'Cao huyet ap',               'ACTIVE', NOW(), NOW());

-- ===================================================================
-- 3. LICH TRUC BAC SI (phu REGISTERED / APPROVED / REJECTED / CANCELLED)
--    Dung BS0010/BS0011 + ca CA001/CA002/CA003 + ngay quanh hom nay.
--    APPROVED moi co room+chair (dung cho lich kham).
-- ===================================================================
INSERT INTO doctor_shift_registrations (registration_code, doctor_id, work_shift_id, work_date, room_id, chair_id, status, note, approved_by, created_at, updated_at) VALUES
('SD-DSR0010', (SELECT id FROM employees WHERE employee_code='BS0010'), (SELECT id FROM work_shifts WHERE shift_code='CA001'), CURDATE(),                        (SELECT id FROM rooms WHERE room_code='P001'), (SELECT id FROM chairs WHERE chair_code='G001'), 'APPROVED',   'Truc ca sang hom nay',     'manager', NOW(), NOW()),
('SD-DSR0011', (SELECT id FROM employees WHERE employee_code='BS0011'), (SELECT id FROM work_shifts WHERE shift_code='CA002'), CURDATE(),                        (SELECT id FROM rooms WHERE room_code='P002'), (SELECT id FROM chairs WHERE chair_code='G003'), 'APPROVED',   'Truc ca chieu hom nay',    'manager', NOW(), NOW()),
('SD-DSR0012', (SELECT id FROM employees WHERE employee_code='BS0010'), (SELECT id FROM work_shifts WHERE shift_code='CA001'), TIMESTAMPADD(DAY, 1, CURDATE()),  (SELECT id FROM rooms WHERE room_code='P001'), (SELECT id FROM chairs WHERE chair_code='G002'), 'APPROVED',   'Truc ca sang ngay mai',    'manager', NOW(), NOW()),
('SD-DSR0013', (SELECT id FROM employees WHERE employee_code='BS0010'), (SELECT id FROM work_shifts WHERE shift_code='CA002'), TIMESTAMPADD(DAY, 2, CURDATE()),  (SELECT id FROM rooms WHERE room_code='P010'), (SELECT id FROM chairs WHERE chair_code='G010'), 'REGISTERED', 'Cho duyet',                NULL,      NOW(), NOW()),
('SD-DSR0014', (SELECT id FROM employees WHERE employee_code='BS0011'), (SELECT id FROM work_shifts WHERE shift_code='CA001'), TIMESTAMPADD(DAY, 3, CURDATE()),  NULL,                                          NULL,                                            'REGISTERED', 'Cho duyet phong',          NULL,      NOW(), NOW()),
('SD-DSR0015', (SELECT id FROM employees WHERE employee_code='BS0010'), (SELECT id FROM work_shifts WHERE shift_code='CA003'), TIMESTAMPADD(DAY, 4, CURDATE()),  NULL,                                          NULL,                                            'REJECTED',   'Trung lich da co bac si',  'manager', NOW(), NOW()),
('SD-DSR0016', (SELECT id FROM employees WHERE employee_code='BS0011'), (SELECT id FROM work_shifts WHERE shift_code='CA002'), TIMESTAMPADD(DAY, 5, CURDATE()),  (SELECT id FROM rooms WHERE room_code='P001'), (SELECT id FROM chairs WHERE chair_code='G002'), 'CANCELLED',  'Bac si xin nghi',          'manager', NOW(), NOW());

-- ===================================================================
-- 4. LICH KHAM (phu PENDING/CONFIRMED/CHECKED_IN/IN_PROGRESS/COMPLETED/
--    CANCELLED/NO_SHOW). Cac lich CHECKED_IN/IN_PROGRESS/COMPLETED gan
--    checkin + session o buoc 5-6.
-- ===================================================================
INSERT INTO appointments (appointment_code, patient_id, doctor_id, service_id, room_id, chair_id, work_shift_id, doctor_shift_registration_id, appointment_date, start_time, end_time, status, source, note, cancel_reason, created_at, updated_at) VALUES
('SD-A0010', (SELECT id FROM patients WHERE patient_code='BN0010'), (SELECT id FROM employees WHERE employee_code='BS0010'), (SELECT id FROM services WHERE service_code='DV001'), NULL,                                          NULL,                                            (SELECT id FROM work_shifts WHERE shift_code='CA001'), NULL,                                                                  CURDATE(),                        '08:00:00', '08:30:00', 'PENDING',     'PATIENT_ONLINE', 'Dat online cho xac nhan',  NULL,                        NOW(), NOW()),
('SD-A0011', (SELECT id FROM patients WHERE patient_code='BN0011'), (SELECT id FROM employees WHERE employee_code='BS0010'), (SELECT id FROM services WHERE service_code='DV011'), (SELECT id FROM rooms WHERE room_code='P001'), (SELECT id FROM chairs WHERE chair_code='G001'), (SELECT id FROM work_shifts WHERE shift_code='CA001'), (SELECT id FROM doctor_shift_registrations WHERE registration_code='SD-DSR0010'), CURDATE(),                        '08:30:00', '09:00:00', 'CONFIRMED',   'RECEPTIONIST',   'Da xac nhan dien thoai',   NULL,                        NOW(), NOW()),
('SD-A0012', (SELECT id FROM patients WHERE patient_code='BN0012'), (SELECT id FROM employees WHERE employee_code='BS0010'), (SELECT id FROM services WHERE service_code='DV014'), (SELECT id FROM rooms WHERE room_code='P001'), (SELECT id FROM chairs WHERE chair_code='G001'), (SELECT id FROM work_shifts WHERE shift_code='CA001'), (SELECT id FROM doctor_shift_registrations WHERE registration_code='SD-DSR0010'), CURDATE(),                        '09:00:00', '09:30:00', 'CHECKED_IN',  'RECEPTIONIST',   'Benh nhan da den',         NULL,                        NOW(), NOW()),
('SD-A0013', (SELECT id FROM patients WHERE patient_code='BN0013'), (SELECT id FROM employees WHERE employee_code='BS0010'), (SELECT id FROM services WHERE service_code='DV001'), (SELECT id FROM rooms WHERE room_code='P001'), (SELECT id FROM chairs WHERE chair_code='G001'), (SELECT id FROM work_shifts WHERE shift_code='CA001'), (SELECT id FROM doctor_shift_registrations WHERE registration_code='SD-DSR0010'), CURDATE(),                        '09:30:00', '10:00:00', 'CHECKED_IN',  'RECEPTIONIST',   'Da goi vao phong',         NULL,                        NOW(), NOW()),
('SD-A0014', (SELECT id FROM patients WHERE patient_code='BN0014'), (SELECT id FROM employees WHERE employee_code='BS0010'), (SELECT id FROM services WHERE service_code='DV010'), (SELECT id FROM rooms WHERE room_code='P001'), (SELECT id FROM chairs WHERE chair_code='G001'), (SELECT id FROM work_shifts WHERE shift_code='CA001'), (SELECT id FROM doctor_shift_registrations WHERE registration_code='SD-DSR0010'), CURDATE(),                        '10:00:00', '11:00:00', 'IN_PROGRESS', 'RECEPTIONIST',   'Dang dieu tri',            NULL,                        NOW(), NOW()),
('SD-A0015', (SELECT id FROM patients WHERE patient_code='BN0015'), (SELECT id FROM employees WHERE employee_code='BS0010'), (SELECT id FROM services WHERE service_code='DV011'), (SELECT id FROM rooms WHERE room_code='P001'), (SELECT id FROM chairs WHERE chair_code='G001'), (SELECT id FROM work_shifts WHERE shift_code='CA001'), (SELECT id FROM doctor_shift_registrations WHERE registration_code='SD-DSR0010'), CURDATE(),                        '07:30:00', '08:00:00', 'COMPLETED',   'RECEPTIONIST',   'Da hoan tat',              NULL,                        NOW(), NOW()),
('SD-A0016', (SELECT id FROM patients WHERE patient_code='BN0010'), (SELECT id FROM employees WHERE employee_code='BS0010'), (SELECT id FROM services WHERE service_code='DV012'), (SELECT id FROM rooms WHERE room_code='P001'), (SELECT id FROM chairs WHERE chair_code='G001'), (SELECT id FROM work_shifts WHERE shift_code='CA001'), NULL,                                                                  TIMESTAMPADD(DAY, -3, CURDATE()), '08:00:00', '08:45:00', 'COMPLETED',   'RECEPTIONIST',   'Tay trang rang',           NULL,                        NOW(), NOW()),
('SD-A0017', (SELECT id FROM patients WHERE patient_code='BN0011'), (SELECT id FROM employees WHERE employee_code='BS0010'), (SELECT id FROM services WHERE service_code='DV010'), (SELECT id FROM rooms WHERE room_code='P001'), (SELECT id FROM chairs WHERE chair_code='G001'), (SELECT id FROM work_shifts WHERE shift_code='CA001'), NULL,                                                                  TIMESTAMPADD(DAY, -5, CURDATE()), '09:00:00', '10:00:00', 'COMPLETED',   'RECEPTIONIST',   'Nho rang khon',            NULL,                        NOW(), NOW()),
('SD-A0018', (SELECT id FROM patients WHERE patient_code='BN0012'), (SELECT id FROM employees WHERE employee_code='BS0011'), (SELECT id FROM services WHERE service_code='DV012'), (SELECT id FROM rooms WHERE room_code='P002'), (SELECT id FROM chairs WHERE chair_code='G003'), (SELECT id FROM work_shifts WHERE shift_code='CA002'), NULL,                                                                  TIMESTAMPADD(DAY, -7, CURDATE()), '14:00:00', '14:45:00', 'COMPLETED',   'RECEPTIONIST',   'Hoa don da huy',           NULL,                        NOW(), NOW()),
('SD-A0019', (SELECT id FROM patients WHERE patient_code='BN0013'), (SELECT id FROM employees WHERE employee_code='BS0010'), (SELECT id FROM services WHERE service_code='DV001'), NULL,                                          NULL,                                            (SELECT id FROM work_shifts WHERE shift_code='CA001'), NULL,                                                                  TIMESTAMPADD(DAY, 1, CURDATE()),  '08:00:00', '08:30:00', 'CANCELLED',   'PATIENT_ONLINE', NULL,                       'Benh nhan ban dot xuat',    NOW(), NOW()),
('SD-A0020', (SELECT id FROM patients WHERE patient_code='BN0014'), (SELECT id FROM employees WHERE employee_code='BS0010'), (SELECT id FROM services WHERE service_code='DV001'), (SELECT id FROM rooms WHERE room_code='P001'), (SELECT id FROM chairs WHERE chair_code='G001'), (SELECT id FROM work_shifts WHERE shift_code='CA001'), NULL,                                                                  TIMESTAMPADD(DAY, -2, CURDATE()), '08:00:00', '08:30:00', 'NO_SHOW',     'RECEPTIONIST',   'Benh nhan khong den',      NULL,                        NOW(), NOW());

INSERT INTO appointment_status_logs (appointment_id, old_status, new_status, changed_by, note, created_at) VALUES
((SELECT id FROM appointments WHERE appointment_code='SD-A0011'), 'PENDING',     'CONFIRMED',   'reception', 'Xac nhan lich',  NOW()),
((SELECT id FROM appointments WHERE appointment_code='SD-A0014'), 'CHECKED_IN',  'IN_PROGRESS', 'bsseed',    'Bat dau kham',   NOW()),
((SELECT id FROM appointments WHERE appointment_code='SD-A0015'), 'IN_PROGRESS', 'COMPLETED',   'bsseed',    'Hoan tat kham',  NOW()),
((SELECT id FROM appointments WHERE appointment_code='SD-A0019'), 'PENDING',     'CANCELLED',   'reception', 'Huy theo yeu cau', NOW()),
((SELECT id FROM appointments WHERE appointment_code='SD-A0020'), 'CONFIRMED',   'NO_SHOW',     'reception', 'Khong den',      NOW());

-- ===================================================================
-- 5. CHECK-IN / HANG DOI (phu WAITING/CALLED/IN_EXAM/DONE/CANCELLED)
--    queue_number dai cao (8000+) -> khong trung test fixture.
--    queue_date = ngay cua appointment tuong ung.
-- ===================================================================
INSERT INTO visit_checkins (appointment_id, patient_id, receptionist_id, checkin_time, queue_number, queue_date, arrival_status, initial_symptoms, note, status, cancel_reason, created_at, updated_at) VALUES
-- WAITING: SD-A0012 (CHECKED_IN, dang cho goi)
((SELECT id FROM appointments WHERE appointment_code='SD-A0012'), (SELECT id FROM patients WHERE patient_code='BN0012'), (SELECT id FROM employees WHERE employee_code='LT0010'), NOW(),                          8001, CURDATE(),                        'ON_TIME', 'Dau rang ham tren',        NULL, 'WAITING',   NULL,                NOW(), NOW()),
-- CALLED: SD-A0013 (da goi vao)
((SELECT id FROM appointments WHERE appointment_code='SD-A0013'), (SELECT id FROM patients WHERE patient_code='BN0013'), (SELECT id FROM employees WHERE employee_code='LT0010'), NOW(),                          8002, CURDATE(),                        'LATE',    'Kham dinh ky',             NULL, 'CALLED',    NULL,                NOW(), NOW()),
-- IN_EXAM: SD-A0014 (dang kham) -> session OPEN
((SELECT id FROM appointments WHERE appointment_code='SD-A0014'), (SELECT id FROM patients WHERE patient_code='BN0014'), (SELECT id FROM employees WHERE employee_code='LT0010'), NOW(),                          8003, CURDATE(),                        'ON_TIME', 'Sung loi rang khon',       NULL, 'IN_EXAM',   NULL,                NOW(), NOW()),
-- DONE: SD-A0015 (hom nay, da hoan tat)
((SELECT id FROM appointments WHERE appointment_code='SD-A0015'), (SELECT id FROM patients WHERE patient_code='BN0015'), (SELECT id FROM employees WHERE employee_code='LT0010'), NOW(),                          8004, CURDATE(),                        'ON_TIME', 'Lay cao rang',             NULL, 'DONE',      NULL,                NOW(), NOW()),
-- DONE: SD-A0016 (-3 ngay)
((SELECT id FROM appointments WHERE appointment_code='SD-A0016'), (SELECT id FROM patients WHERE patient_code='BN0010'), (SELECT id FROM employees WHERE employee_code='LT0010'), TIMESTAMPADD(DAY, -3, NOW()),   8001, TIMESTAMPADD(DAY, -3, CURDATE()), 'ON_TIME', 'Tay trang',                NULL, 'DONE',      NULL,                NOW(), NOW()),
-- DONE: SD-A0017 (-5 ngay)
((SELECT id FROM appointments WHERE appointment_code='SD-A0017'), (SELECT id FROM patients WHERE patient_code='BN0011'), (SELECT id FROM employees WHERE employee_code='LT0010'), TIMESTAMPADD(DAY, -5, NOW()),   8001, TIMESTAMPADD(DAY, -5, CURDATE()), 'ON_TIME', 'Nho rang khon',            NULL, 'DONE',      NULL,                NOW(), NOW()),
-- DONE: SD-A0018 (-7 ngay)
((SELECT id FROM appointments WHERE appointment_code='SD-A0018'), (SELECT id FROM patients WHERE patient_code='BN0012'), (SELECT id FROM employees WHERE employee_code='LT0010'), TIMESTAMPADD(DAY, -7, NOW()),   8001, TIMESTAMPADD(DAY, -7, CURDATE()), 'ON_TIME', 'Tay trang rang',           NULL, 'DONE',      NULL,                NOW(), NOW()),
-- CANCELLED: 1 checkin bi huy (gan lai SD-A0020 da NO_SHOW -> minh hoa huy checkin)
((SELECT id FROM appointments WHERE appointment_code='SD-A0020'), (SELECT id FROM patients WHERE patient_code='BN0014'), (SELECT id FROM employees WHERE employee_code='LT0010'), TIMESTAMPADD(DAY, -2, NOW()),   8001, TIMESTAMPADD(DAY, -2, CURDATE()), 'ON_TIME', NULL,                       NULL, 'CANCELLED', 'Benh nhan bo ve',   NOW(), NOW());

-- ===================================================================
-- 6. PHIEN KHAM (phu OPEN/COMPLETED/SUSPENDED)
--    SD-A0014 -> OPEN ; SD-A0015..0018 -> COMPLETED ; 1 SUSPENDED
-- ===================================================================
INSERT INTO treatment_sessions (session_code, appointment_id, medical_record_id, patient_id, doctor_id, checkin_id, examination_date, symptom, diagnosis, treatment_plan, doctor_note, status, difficult_coefficient, created_at, updated_at) VALUES
('SD-S0014', (SELECT id FROM appointments WHERE appointment_code='SD-A0014'), (SELECT id FROM medical_records WHERE record_code='SD-MR0014'), (SELECT id FROM patients WHERE patient_code='BN0014'), (SELECT id FROM employees WHERE employee_code='BS0010'), (SELECT id FROM visit_checkins WHERE appointment_id=(SELECT id FROM appointments WHERE appointment_code='SD-A0014')), CURDATE(),                        'Sung loi quanh rang khon',  'Viem loi trum rang khon 38', 'Theo doi, hen tieu phau',          'Dang dieu tri',          'OPEN',      NULL, NOW(), NOW()),
('SD-S0015', (SELECT id FROM appointments WHERE appointment_code='SD-A0015'), (SELECT id FROM medical_records WHERE record_code='SD-MR0015'), (SELECT id FROM patients WHERE patient_code='BN0015'), (SELECT id FROM employees WHERE employee_code='BS0010'), (SELECT id FROM visit_checkins WHERE appointment_id=(SELECT id FROM appointments WHERE appointment_code='SD-A0015')), CURDATE(),                        'Cao rang nhieu',            'Viem nuou do cao rang',      'Lay cao rang, danh bong',          'Hoan tat',               'COMPLETED', NULL, NOW(), NOW()),
('SD-S0016', (SELECT id FROM appointments WHERE appointment_code='SD-A0016'), (SELECT id FROM medical_records WHERE record_code='SD-MR0010'), (SELECT id FROM patients WHERE patient_code='BN0010'), (SELECT id FROM employees WHERE employee_code='BS0010'), (SELECT id FROM visit_checkins WHERE appointment_id=(SELECT id FROM appointments WHERE appointment_code='SD-A0016')), TIMESTAMPADD(DAY, -3, CURDATE()), 'Rang xin mau',              'Nhiem mau ngoai sinh',       'Tay trang rang tham my',           'Ket qua tot',            'COMPLETED', NULL, NOW(), NOW()),
('SD-S0017', (SELECT id FROM appointments WHERE appointment_code='SD-A0017'), (SELECT id FROM medical_records WHERE record_code='SD-MR0011'), (SELECT id FROM patients WHERE patient_code='BN0011'), (SELECT id FROM employees WHERE employee_code='BS0010'), (SELECT id FROM visit_checkins WHERE appointment_id=(SELECT id FROM appointments WHERE appointment_code='SD-A0017')), TIMESTAMPADD(DAY, -5, CURDATE()), 'Dau rang khon moc lech',    'Rang khon 48 moc lech',      'Tieu phau nho rang khon',          'Ca phuc tap nhieu chan',  'COMPLETED', 0.30, NOW(), NOW()),
('SD-S0018', (SELECT id FROM appointments WHERE appointment_code='SD-A0018'), (SELECT id FROM medical_records WHERE record_code='SD-MR0012'), (SELECT id FROM patients WHERE patient_code='BN0012'), (SELECT id FROM employees WHERE employee_code='BS0011'), (SELECT id FROM visit_checkins WHERE appointment_id=(SELECT id FROM appointments WHERE appointment_code='SD-A0018')), TIMESTAMPADD(DAY, -7, CURDATE()), 'Rang xin mau',              'Nhiem mau',                  'Tay trang',                        'Hoa don sau do bi huy',  'COMPLETED', NULL, NOW(), NOW());

-- ===================================================================
-- 7. SO DO RANG (phu nhieu ToothStatus) + lich su rang cho BN0011
--    (phien SD-S0017 nho rang khon 48).
-- ===================================================================
INSERT INTO dental_tooth_status (medical_record_id, patient_id, tooth_number, tooth_name, status, diagnosis, note, last_updated_session_id, created_at, updated_at) VALUES
((SELECT id FROM medical_records WHERE record_code='SD-MR0011'), (SELECT id FROM patients WHERE patient_code='BN0011'), 48, 'Rang khon ham duoi phai', 'EXTRACTED', 'Da nho rang khon moc lech', 'Hau phau on dinh', (SELECT id FROM treatment_sessions WHERE session_code='SD-S0017'), NOW(), NOW()),
((SELECT id FROM medical_records WHERE record_code='SD-MR0011'), (SELECT id FROM patients WHERE patient_code='BN0011'), 16, 'Rang ham tren phai',      'CARIES',    'Sau rang mat nhai',         NULL,                (SELECT id FROM treatment_sessions WHERE session_code='SD-S0017'), NOW(), NOW()),
((SELECT id FROM medical_records WHERE record_code='SD-MR0011'), (SELECT id FROM patients WHERE patient_code='BN0011'), 26, 'Rang ham tren trai',      'FILLED',    'Da han composite',          NULL,                (SELECT id FROM treatment_sessions WHERE session_code='SD-S0017'), NOW(), NOW()),
((SELECT id FROM medical_records WHERE record_code='SD-MR0012'), (SELECT id FROM patients WHERE patient_code='BN0012'), 36, 'Rang ham duoi trai',      'ROOT_CANAL','Da dieu tri tuy',           NULL,                (SELECT id FROM treatment_sessions WHERE session_code='SD-S0018'), NOW(), NOW()),
((SELECT id FROM medical_records WHERE record_code='SD-MR0012'), (SELECT id FROM patients WHERE patient_code='BN0012'), 11, 'Rang cua giua tren phai', 'CROWNED',   'Boc rang su',               NULL,                (SELECT id FROM treatment_sessions WHERE session_code='SD-S0018'), NOW(), NOW()),
((SELECT id FROM medical_records WHERE record_code='SD-MR0010'), (SELECT id FROM patients WHERE patient_code='BN0010'), 24, 'Rang ham nho tren trai',  'MISSING',   'Mat rang chua phuc hinh',   NULL,                NULL,                                                              NOW(), NOW());

INSERT INTO tooth_treatment_histories (medical_record_id, patient_id, treatment_session_id, tooth_number, old_status, new_status, diagnosis, note, updated_by, updated_at) VALUES
((SELECT id FROM medical_records WHERE record_code='SD-MR0011'), (SELECT id FROM patients WHERE patient_code='BN0011'), (SELECT id FROM treatment_sessions WHERE session_code='SD-S0017'), 48, 'NORMAL', 'EXTRACTED', 'Nho rang khon moc lech', 'Tieu phau thanh cong', 'bsseed', NOW()),
((SELECT id FROM medical_records WHERE record_code='SD-MR0012'), (SELECT id FROM patients WHERE patient_code='BN0012'), (SELECT id FROM treatment_sessions WHERE session_code='SD-S0018'), 36, 'CARIES', 'ROOT_CANAL', 'Dieu tri tuy rang 36',   'Hoan tat noi nha',     'bsseed', NOW());

-- ===================================================================
-- 8. DICH VU DIEU TRI da dang ky theo phien (phu ACTIVE/INVOICED/CANCELLED)
--    unit_price lay tu service_prices ACTIVE; total = unit_price*qty - discount.
--    Phien OPEN (SD-S0014) -> ACTIVE ; phien COMPLETED da lap HD -> INVOICED.
-- ===================================================================
-- SD-S0014 (OPEN): 1 ACTIVE (DV010 nho rang khon) + 1 CANCELLED (DV014)
INSERT INTO registered_services (treatment_session_id, patient_id, service_id, service_price_id, service_name_snapshot, unit_snapshot, quantity, unit_price, discount_amount, total_amount, tooth_number, status, note, created_at, updated_at) VALUES
((SELECT id FROM treatment_sessions WHERE session_code='SD-S0014'), (SELECT id FROM patients WHERE patient_code='BN0014'), (SELECT id FROM services WHERE service_code='DV010'), (SELECT id FROM service_prices WHERE service_id=(SELECT id FROM services WHERE service_code='DV010') AND status='ACTIVE'), 'Nho rang khon', 'RANG', 1, 500000, 0,     500000, 38, 'ACTIVE',    NULL,              NOW(), NOW()),
((SELECT id FROM treatment_sessions WHERE session_code='SD-S0014'), (SELECT id FROM patients WHERE patient_code='BN0014'), (SELECT id FROM services WHERE service_code='DV014'), (SELECT id FROM service_prices WHERE service_id=(SELECT id FROM services WHERE service_code='DV014') AND status='ACTIVE'), 'Chup phim X-quang', 'PHIM', 1, 120000, 0, 120000, 38, 'CANCELLED', 'Huy do khong can',NOW(), NOW()),
-- SD-S0015 (COMPLETED, PAID): DV011 lay cao rang 150k -> INVOICED
((SELECT id FROM treatment_sessions WHERE session_code='SD-S0015'), (SELECT id FROM patients WHERE patient_code='BN0015'), (SELECT id FROM services WHERE service_code='DV011'), (SELECT id FROM service_prices WHERE service_id=(SELECT id FROM services WHERE service_code='DV011') AND status='ACTIVE'), 'Lay cao rang', 'LAN', 1, 150000, 0, 150000, NULL, 'INVOICED', NULL, NOW(), NOW()),
-- SD-S0016 (COMPLETED, PARTIAL): DV012 tay trang 800k -> INVOICED
((SELECT id FROM treatment_sessions WHERE session_code='SD-S0016'), (SELECT id FROM patients WHERE patient_code='BN0010'), (SELECT id FROM services WHERE service_code='DV012'), (SELECT id FROM service_prices WHERE service_id=(SELECT id FROM services WHERE service_code='DV012') AND status='ACTIVE'), 'Tay trang rang', 'LAN', 1, 800000, 0, 800000, NULL, 'INVOICED', NULL, NOW(), NOW()),
-- SD-S0017 (COMPLETED, UNPAID): DV010 nho rang khon 500k -> INVOICED
((SELECT id FROM treatment_sessions WHERE session_code='SD-S0017'), (SELECT id FROM patients WHERE patient_code='BN0011'), (SELECT id FROM services WHERE service_code='DV010'), (SELECT id FROM service_prices WHERE service_id=(SELECT id FROM services WHERE service_code='DV010') AND status='ACTIVE'), 'Nho rang khon', 'RANG', 1, 500000, 0, 500000, 48, 'INVOICED', NULL, NOW(), NOW()),
-- SD-S0018 (COMPLETED, HD CANCELLED): DV012 tay trang 800k -> INVOICED
((SELECT id FROM treatment_sessions WHERE session_code='SD-S0018'), (SELECT id FROM patients WHERE patient_code='BN0012'), (SELECT id FROM services WHERE service_code='DV012'), (SELECT id FROM service_prices WHERE service_id=(SELECT id FROM services WHERE service_code='DV012') AND status='ACTIVE'), 'Tay trang rang', 'LAN', 1, 800000, 0, 800000, NULL, 'INVOICED', NULL, NOW(), NOW());

-- ===================================================================
-- 9. HOA DON (phu UNPAID/PARTIAL/PAID/CANCELLED). Bat bien:
--    final = total - discount ; remaining = final - paid.
-- ===================================================================
INSERT INTO invoices (invoice_code, patient_id, treatment_session_id, total_amount, discount_amount, discount_type, discount_note, final_amount, paid_amount, remaining_amount, status, created_by, created_at, updated_at) VALUES
-- PAID: SD-S0015, total 150k, thu du
('SD-INV0015', (SELECT id FROM patients WHERE patient_code='BN0015'), (SELECT id FROM treatment_sessions WHERE session_code='SD-S0015'), 150000, 0,      'NONE',    NULL,                       150000, 150000, 0,      'PAID',      'reception', NOW(), NOW()),
-- PARTIAL: SD-S0016, total 800k, giam 100k (AMOUNT), final 700k, thu 300k, con 400k
('SD-INV0016', (SELECT id FROM patients WHERE patient_code='BN0010'), (SELECT id FROM treatment_sessions WHERE session_code='SD-S0016'), 800000, 100000, 'AMOUNT',  'Khach quen giam 100k',     700000, 300000, 400000, 'PARTIAL',   'reception', NOW(), NOW()),
-- UNPAID: SD-S0017, total 500k, chua thu
('SD-INV0017', (SELECT id FROM patients WHERE patient_code='BN0011'), (SELECT id FROM treatment_sessions WHERE session_code='SD-S0017'), 500000, 0,      'NONE',    NULL,                       500000, 0,      500000, 'UNPAID',    'reception', NOW(), NOW()),
-- CANCELLED: SD-S0018, total 800k, giam 10% (PERCENT) = 80k, final 720k, da huy
('SD-INV0018', (SELECT id FROM patients WHERE patient_code='BN0012'), (SELECT id FROM treatment_sessions WHERE session_code='SD-S0018'), 800000, 80000,  'PERCENT', 'Giam 10% uu dai',          720000, 0,      720000, 'CANCELLED', 'reception', NOW(), NOW());

-- ===================================================================
-- 10. THANH TOAN (phu PAYMENT/REFUND ; method CASH/BANK_TRANSFER/CARD ;
--     status SUCCESS/REFUNDED).
-- ===================================================================
INSERT INTO payments (invoice_id, amount, payment_method, payment_type, collected_by, paid_at, note, status, created_at, updated_at) VALUES
-- SD-INV0015 PAID: 1 PAYMENT tien mat (SUCCESS) + 1 REFUND minh hoa hoan tien (REFUNDED)
((SELECT id FROM invoices WHERE invoice_code='SD-INV0015'), 150000, 'CASH',          'PAYMENT', 'reception', NOW(),                          'Thu du tien mat',        'SUCCESS',  NOW(), NOW()),
((SELECT id FROM invoices WHERE invoice_code='SD-INV0015'), 50000,  'CASH',          'REFUND',  'reception', NOW(),                          'Hoan 1 phan do nham',    'REFUNDED', NOW(), NOW()),
-- SD-INV0016 PARTIAL: tra lam 2 lan -> chuyen khoan 200k + the 100k = 300k da thu (SUCCESS)
((SELECT id FROM invoices WHERE invoice_code='SD-INV0016'), 200000, 'BANK_TRANSFER', 'PAYMENT', 'reception', TIMESTAMPADD(DAY, -3, NOW()),   'Tra truoc 200k chuyen khoan', 'SUCCESS', NOW(), NOW()),
((SELECT id FROM invoices WHERE invoice_code='SD-INV0016'), 100000, 'CARD',          'PAYMENT', 'reception', TIMESTAMPADD(DAY, -2, NOW()),   'Tra them 100k bang the',      'SUCCESS', NOW(), NOW());

-- ===================================================================
-- 11. HE SO CA PHUC TAP (phu PENDING/APPROVED/REJECTED).
--     Chi tren phien COMPLETED cua dung bac si so huu, coefficient 0.1-0.5.
-- ===================================================================
INSERT INTO complex_case_coefficients (coefficient_code, treatment_session_id, doctor_id, coefficient, reason, status, approved_by, reject_reason, proposed_by, created_at, updated_at) VALUES
-- PENDING: SD-S0015 (BS0010)
('SD-CCC0010', (SELECT id FROM treatment_sessions WHERE session_code='SD-S0015'), (SELECT id FROM employees WHERE employee_code='BS0010'), 0.20, 'Benh nhan kho hop tac, cao rang nhieu', 'PENDING',  NULL,      NULL,                         'bsseed', NOW(), NOW()),
-- APPROVED: SD-S0017 (BS0010) - nho rang khon phuc tap
('SD-CCC0011', (SELECT id FROM treatment_sessions WHERE session_code='SD-S0017'), (SELECT id FROM employees WHERE employee_code='BS0010'), 0.30, 'Rang khon moc lech nhieu chan rang',    'APPROVED', 'manager', NULL,                         'bsseed', NOW(), NOW()),
-- REJECTED: SD-S0016 (BS0010)
('SD-CCC0012', (SELECT id FROM treatment_sessions WHERE session_code='SD-S0016'), (SELECT id FROM employees WHERE employee_code='BS0010'), 0.40, 'De xuat he so cao',                     'REJECTED', 'manager', 'Ca khong du phuc tap, tu choi', 'bsseed', NOW(), NOW());

-- ===================================================================
-- 12. PHIEU LUONG (phu DRAFT/PENDING/APPROVED/CANCELLED).
--     Dung BS0010/BS0011, ky luong nam/thang KHONG trung test (BS0001 only).
--     APPROVED/PENDING co payroll_items snapshot tham chieu DSR APPROVED.
-- ===================================================================
INSERT INTO payroll_slips (slip_code, doctor_id, payroll_year, payroll_month, total_salary, status, note, created_by, approved_by, created_at, updated_at) VALUES
-- APPROVED: BS0010, thang truoc
('SD-PL0010', (SELECT id FROM employees WHERE employee_code='BS0010'), YEAR(TIMESTAMPADD(MONTH, -1, CURDATE())), MONTH(TIMESTAMPADD(MONTH, -1, CURDATE())), 960000, 'APPROVED', 'Da duyet chi luong', 'manager', 'manager', NOW(), NOW()),
-- PENDING: BS0011, thang truoc
('SD-PL0011', (SELECT id FROM employees WHERE employee_code='BS0011'), YEAR(TIMESTAMPADD(MONTH, -1, CURDATE())), MONTH(TIMESTAMPADD(MONTH, -1, CURDATE())), 480000, 'PENDING',  'Cho quan ly duyet',  'manager', NULL,      NOW(), NOW()),
-- DRAFT: BS0010, thang nay
('SD-PL0012', (SELECT id FROM employees WHERE employee_code='BS0010'), YEAR(CURDATE()), MONTH(CURDATE()), 0, 'DRAFT', 'Ban nhap dang tinh', 'manager', NULL, NOW(), NOW()),
-- CANCELLED: BS0011, thang nay
('SD-PL0013', (SELECT id FROM employees WHERE employee_code='BS0011'), YEAR(CURDATE()), MONTH(CURDATE()), 0, 'CANCELLED', 'Huy do tinh sai', 'manager', NULL, NOW(), NOW());

-- payroll_items cho slip APPROVED (SD-PL0010): 2 ca (DSR0010 + DSR0012) = 480k*2 = 960k
INSERT INTO payroll_items (payroll_slip_id, doctor_shift_registration_id, work_date, shift_name_snapshot, total_hours, shift_coefficient_snapshot, patient_coefficient_snapshot, converted_hours, degree_coefficient_snapshot, hourly_rate_snapshot, amount, created_at, updated_at) VALUES
((SELECT id FROM payroll_slips WHERE slip_code='SD-PL0010'), (SELECT id FROM doctor_shift_registrations WHERE registration_code='SD-DSR0010'), CURDATE(),                       'Ca sang', 4.00, 1.00, 0.00, 4.00, 1.20, 100000, 480000, NOW(), NOW()),
((SELECT id FROM payroll_slips WHERE slip_code='SD-PL0010'), (SELECT id FROM doctor_shift_registrations WHERE registration_code='SD-DSR0012'), TIMESTAMPADD(DAY, 1, CURDATE()), 'Ca sang', 4.00, 1.00, 0.00, 4.00, 1.20, 100000, 480000, NOW(), NOW());

-- payroll_items cho slip PENDING (SD-PL0011): 1 ca (DSR0011) = 4*1.1*100000 = 440k... dung 480k cho khop total -> 4*1.2*100000
INSERT INTO payroll_items (payroll_slip_id, doctor_shift_registration_id, work_date, shift_name_snapshot, total_hours, shift_coefficient_snapshot, patient_coefficient_snapshot, converted_hours, degree_coefficient_snapshot, hourly_rate_snapshot, amount, created_at, updated_at) VALUES
((SELECT id FROM payroll_slips WHERE slip_code='SD-PL0011'), (SELECT id FROM doctor_shift_registrations WHERE registration_code='SD-DSR0011'), CURDATE(), 'Ca chieu', 4.00, 1.00, 0.00, 4.00, 1.20, 100000, 480000, NOW(), NOW());

-- ===================================================================
-- 13. CAP NHAT code_sequences de ma auto-generate sau nay khong trung seed.
--     Dat gia tri du cao cho cac prefix bi seed dung.
-- ===================================================================
UPDATE code_sequences SET current_value = GREATEST(current_value, 20), updated_at = NOW() WHERE sequence_key = 'SERVICE';
UPDATE code_sequences SET current_value = GREATEST(current_value, 12), updated_at = NOW() WHERE sequence_key = 'SERVICE_CATEGORY';
UPDATE code_sequences SET current_value = GREATEST(current_value, 11), updated_at = NOW() WHERE sequence_key = 'HOLIDAY';
UPDATE code_sequences SET current_value = GREATEST(current_value, 11), updated_at = NOW() WHERE sequence_key = 'ROOM';
UPDATE code_sequences SET current_value = GREATEST(current_value, 11), updated_at = NOW() WHERE sequence_key = 'CHAIR';
UPDATE code_sequences SET current_value = GREATEST(current_value, 10), updated_at = NOW() WHERE sequence_key = 'WORK_SHIFT';
UPDATE code_sequences SET current_value = GREATEST(current_value, 11), updated_at = NOW() WHERE sequence_key = 'USER';
UPDATE code_sequences SET current_value = GREATEST(current_value, 12), updated_at = NOW() WHERE sequence_key = 'DOCTOR';
UPDATE code_sequences SET current_value = GREATEST(current_value, 11), updated_at = NOW() WHERE sequence_key = 'RECEPTIONIST';
UPDATE code_sequences SET current_value = GREATEST(current_value, 16), updated_at = NOW() WHERE sequence_key = 'PATIENT';
