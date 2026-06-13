-- ===================================================================
-- V8__workflow_alignment.sql
-- Dong bo luong kham benh, ca lam viec va ho so benh an
-- ===================================================================

ALTER TABLE work_shifts
    ADD COLUMN day_type VARCHAR(20) NOT NULL DEFAULT 'ALL';

ALTER TABLE payroll_items
    ADD COLUMN treatment_session_id BIGINT NULL;

ALTER TABLE payroll_items
    MODIFY COLUMN doctor_shift_registration_id BIGINT NULL;

ALTER TABLE payroll_items
    ADD CONSTRAINT fk_payroll_items_session
        FOREIGN KEY (treatment_session_id) REFERENCES treatment_sessions (id);

CREATE INDEX idx_payroll_items_session ON payroll_items (treatment_session_id);
