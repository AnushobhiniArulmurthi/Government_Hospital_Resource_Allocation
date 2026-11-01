-- =====================================================================
-- GHRAS DATABASE MIGRATION SCRIPT
-- Adds Date of Birth, Dynamic Age, Patient Segmentation,
-- Doctor Demographics, Triage Fields, and Preferred Doctor References
-- =====================================================================

USE hospital_resource_db;

-- 1. Alter 'patients' Table
ALTER TABLE patients 
    ADD COLUMN date_of_birth DATE NULL AFTER full_name,
    ADD COLUMN email VARCHAR(100) NULL AFTER contact_number,
    ADD COLUMN patient_type VARCHAR(20) NOT NULL DEFAULT 'NEW' AFTER address,
    ADD COLUMN preferred_doctor_id VARCHAR(20) NULL AFTER patient_type;

-- Update existing 20 sample patients with calculated Date of Birth based on age
UPDATE patients SET date_of_birth = DATE_SUB(CURDATE(), INTERVAL age YEAR) WHERE date_of_birth IS NULL;
UPDATE patients SET patient_type = 'REGULAR', preferred_doctor_id = 'D-001' WHERE patient_id IN ('P-101', 'P-102', 'P-103', 'P-104', 'P-105');

-- Make date_of_birth NOT NULL after seeding
ALTER TABLE patients MODIFY date_of_birth DATE NOT NULL;

-- Add foreign key constraint for preferred doctor
ALTER TABLE patients 
    ADD CONSTRAINT fk_pat_pref_doc FOREIGN KEY (preferred_doctor_id) REFERENCES doctors(doctor_id) ON DELETE SET NULL;

-- 2. Alter 'doctors' Table
ALTER TABLE doctors
    ADD COLUMN date_of_birth DATE NULL AFTER doctor_name,
    ADD COLUMN gender VARCHAR(15) DEFAULT 'Male' AFTER date_of_birth,
    ADD COLUMN age INT DEFAULT 40 AFTER gender;

-- Seed existing doctors with realistic Date of Birth
UPDATE doctors SET date_of_birth = '1978-05-14', gender = 'Male', age = 48 WHERE doctor_id = 'D-001';
UPDATE doctors SET date_of_birth = '1982-08-22', gender = 'Female', age = 44 WHERE doctor_id = 'D-002';
UPDATE doctors SET date_of_birth = '1975-11-03', gender = 'Male', age = 51 WHERE doctor_id = 'D-003';
UPDATE doctors SET date_of_birth = '1985-02-18', gender = 'Female', age = 41 WHERE doctor_id = 'D-004';
UPDATE doctors SET date_of_birth = '1980-07-29', gender = 'Male', age = 46 WHERE doctor_id = 'D-005';
UPDATE doctors SET date_of_birth = '1988-09-12', gender = 'Female', age = 38 WHERE doctor_id = 'D-006';
UPDATE doctors SET date_of_birth = '1972-04-05', gender = 'Male', age = 54 WHERE doctor_id = 'D-007';
UPDATE doctors SET date_of_birth = '1984-12-30', gender = 'Male', age = 41 WHERE doctor_id = 'D-008';

-- Make date_of_birth NOT NULL
ALTER TABLE doctors MODIFY date_of_birth DATE NOT NULL;

-- 3. Alter 'patient_visits' Table
ALTER TABLE patient_visits
    MODIFY COLUMN severity_level VARCHAR(30) NOT NULL DEFAULT 'PENDING_TRIAGE',
    ADD COLUMN preferred_doctor_id VARCHAR(20) NULL AFTER assigned_doctor_id,
    ADD COLUMN alternative_doctor_offered BOOLEAN DEFAULT FALSE AFTER preferred_doctor_id,
    ADD COLUMN alternative_doctor_accepted BOOLEAN DEFAULT NULL AFTER alternative_doctor_offered,
    ADD COLUMN triage_notes TEXT DEFAULT NULL AFTER notes,
    ADD COLUMN triage_assessed_by VARCHAR(100) DEFAULT NULL AFTER triage_notes,
    ADD COLUMN triage_assessed_time DATETIME DEFAULT NULL AFTER triage_assessed_by;

-- Add foreign key constraint for preferred doctor on visit
ALTER TABLE patient_visits
    ADD CONSTRAINT fk_visit_pref_doc FOREIGN KEY (preferred_doctor_id) REFERENCES doctors(doctor_id) ON DELETE SET NULL;

-- 4. Alter 'diagnostic_orders' Table
ALTER TABLE diagnostic_orders
    ADD COLUMN reviewed_at DATETIME DEFAULT NULL AFTER doctor_review_notes,
    ADD COLUMN reviewed_by_doctor_id VARCHAR(20) DEFAULT NULL AFTER reviewed_at;

ALTER TABLE diagnostic_orders
    ADD CONSTRAINT fk_do_rev_doc FOREIGN KEY (reviewed_by_doctor_id) REFERENCES doctors(doctor_id) ON DELETE SET NULL;
