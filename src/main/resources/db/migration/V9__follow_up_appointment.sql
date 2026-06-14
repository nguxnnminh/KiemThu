-- ===================================================================
-- V9__follow_up_appointment.sql
-- Lich hen kham lai (tai kham): bac si chon ngay kham lai khi hoan tat phien kham.
-- Luu ngay kham lai vao ho so phien kham va lien ket toi lich hen tai kham duoc tao tu dong.
-- ===================================================================

ALTER TABLE treatment_sessions
    ADD COLUMN follow_up_date DATE NULL;

ALTER TABLE treatment_sessions
    ADD COLUMN follow_up_appointment_id BIGINT NULL;

ALTER TABLE treatment_sessions
    ADD CONSTRAINT fk_treatment_sessions_follow_up
        FOREIGN KEY (follow_up_appointment_id) REFERENCES appointments (id);
