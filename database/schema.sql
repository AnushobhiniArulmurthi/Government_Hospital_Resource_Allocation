-- =====================================================================
-- GOVERNMENT HOSPITAL RESOURCE ALLOCATION SYSTEM (GHRAS)
-- Database: hospital_resource_db
-- Dialect: MySQL 5.5+ / 8.0+ / MariaDB
-- =====================================================================

CREATE DATABASE IF NOT EXISTS hospital_resource_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hospital_resource_db;

-- Drop foreign key checks during initialization
SET FOREIGN_KEY_CHECKS = 0;

-- 1. HOSPITALS (Primary Hospital + Referral Hospitals)
DROP TABLE IF EXISTS hospitals;
CREATE TABLE hospitals (
    hospital_id VARCHAR(20) PRIMARY KEY,
    hospital_name VARCHAR(150) NOT NULL,
    location_name VARCHAR(100) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    contact_number VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    total_beds INT NOT NULL DEFAULT 0,
    latitude DECIMAL(10, 6) DEFAULT 0.0,
    longitude DECIMAL(10, 6) DEFAULT 0.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. DOCTOR ID SEQUENCE (Atomic Database Sequence for D-001, D-002...)
DROP TABLE IF EXISTS doctor_id_sequence;
CREATE TABLE doctor_id_sequence (
    id INT PRIMARY KEY,
    current_val BIGINT NOT NULL
) ENGINE=InnoDB;
INSERT INTO doctor_id_sequence (id, current_val) VALUES (1, 0)
ON DUPLICATE KEY UPDATE id=id;

-- 3. DOCTORS
DROP TABLE IF EXISTS doctors;
CREATE TABLE doctors (
    doctor_id VARCHAR(20) PRIMARY KEY,
    doctor_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(15) NOT NULL DEFAULT 'Male',
    age INT NOT NULL,
    specialization VARCHAR(80) NOT NULL,
    qualification VARCHAR(100) NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    duty_status VARCHAR(20) NOT NULL DEFAULT 'ON_DUTY', -- ON_DUTY, OFF_DUTY, ON_LEAVE
    availability_status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE, IN_CONSULTATION, ASSIGNED_TO_INPATIENT, OFF_DUTY, ON_LEAVE
    hospital_id VARCHAR(20) NOT NULL,
    doctor_type VARCHAR(30) NOT NULL DEFAULT 'SPECIALIST', -- GENERAL_PHYSICIAN, SPECIALIST
    date_of_joining DATE NOT NULL,
    max_inpatient_capacity INT NOT NULL DEFAULT 5,
    current_inpatient_count INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_doctor_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4. PATIENTS
DROP TABLE IF EXISTS patients;
CREATE TABLE patients (
    patient_id VARCHAR(20) PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(15) NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    email VARCHAR(100) DEFAULT NULL,
    address VARCHAR(255) NOT NULL,
    patient_type VARCHAR(20) NOT NULL DEFAULT 'NEW', -- NEW, REGULAR
    preferred_doctor_id VARCHAR(20) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pat_pref_doc FOREIGN KEY (preferred_doctor_id) REFERENCES doctors(doctor_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 5. PATIENT VISITS (Supports Multiple Visits per Patient)
DROP TABLE IF EXISTS patient_visits;
CREATE TABLE patient_visits (
    visit_id VARCHAR(20) PRIMARY KEY,
    patient_id VARCHAR(20) NOT NULL,
    severity_level VARCHAR(30) NOT NULL DEFAULT 'PENDING_TRIAGE', -- PENDING_TRIAGE, Critical, Serious, Minor
    patient_category VARCHAR(30) NOT NULL DEFAULT 'Outpatient', -- Outpatient, Diagnostic-Based, Inpatient, Emergency
    visit_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_TRIAGE', -- PENDING_TRIAGE, TRIAGED, IN_CONSULTATION, TREATMENT_PLANNED, ADMITTED, REFERRED, NO_DOCTOR_AVAILABLE, DISCHARGED, COMPLETED
    arrival_time DATETIME NOT NULL,
    assigned_doctor_id VARCHAR(20) DEFAULT NULL,
    preferred_doctor_id VARCHAR(20) DEFAULT NULL,
    alternative_doctor_offered BOOLEAN DEFAULT FALSE,
    alternative_doctor_accepted BOOLEAN DEFAULT NULL,
    assigned_bed_id VARCHAR(20) DEFAULT NULL,
    discharge_time DATETIME DEFAULT NULL,
    notes TEXT DEFAULT NULL,
    triage_notes TEXT DEFAULT NULL,
    triage_assessed_by VARCHAR(100) DEFAULT NULL,
    triage_assessed_time DATETIME DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_visit_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_visit_doctor FOREIGN KEY (assigned_doctor_id) REFERENCES doctors(doctor_id) ON DELETE SET NULL,
    CONSTRAINT fk_visit_pref_doc FOREIGN KEY (preferred_doctor_id) REFERENCES doctors(doctor_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 6. BEDS (General, ICU, Emergency)
DROP TABLE IF EXISTS beds;
CREATE TABLE beds (
    bed_id VARCHAR(20) PRIMARY KEY,
    bed_number VARCHAR(20) NOT NULL,
    bed_type VARCHAR(30) NOT NULL, -- GENERAL, ICU, EMERGENCY
    hospital_id VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE, OCCUPIED, MAINTENANCE
    current_patient_id VARCHAR(20) DEFAULT NULL,
    current_visit_id VARCHAR(20) DEFAULT NULL,
    floor_ward VARCHAR(50) NOT NULL,
    daily_charge DECIMAL(10, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bed_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE,
    CONSTRAINT fk_bed_patient FOREIGN KEY (current_patient_id) REFERENCES patients(patient_id) ON DELETE SET NULL,
    CONSTRAINT fk_bed_visit FOREIGN KEY (current_visit_id) REFERENCES patient_visits(visit_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 7. RESOURCES (Medical Equipment, Medical Supplies, Facilities)
DROP TABLE IF EXISTS resources;
CREATE TABLE resources (
    resource_id VARCHAR(20) PRIMARY KEY,
    resource_name VARCHAR(100) NOT NULL,
    category VARCHAR(30) NOT NULL, -- EQUIPMENT, SUPPLY, FACILITY
    hospital_id VARCHAR(20) NOT NULL,
    total_quantity INT NOT NULL DEFAULT 0,
    available_quantity INT NOT NULL DEFAULT 0,
    allocated_quantity INT NOT NULL DEFAULT 0,
    is_reusable BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE, LOW_STOCK, DEPLETED, MAINTENANCE
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_res_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 8. MEDICAL EQUIPMENT (Sub-class of Resource)
DROP TABLE IF EXISTS medical_equipment;
CREATE TABLE medical_equipment (
    equipment_id VARCHAR(20) PRIMARY KEY,
    resource_id VARCHAR(20) NOT NULL,
    equipment_type VARCHAR(50) NOT NULL, -- Oxygen Cylinder, Ventilator, ECG Machine, Patient Monitor, Wheelchair, Stretcher, Infusion Pump
    serial_number VARCHAR(50) UNIQUE NOT NULL,
    condition_status VARCHAR(20) NOT NULL DEFAULT 'OPERATIONAL', -- OPERATIONAL, UNDER_MAINTENANCE, FAULTY
    is_allocated BOOLEAN NOT NULL DEFAULT FALSE,
    hospital_id VARCHAR(20) NOT NULL,
    CONSTRAINT fk_me_res FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE,
    CONSTRAINT fk_me_hosp FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 9. MEDICAL SUPPLIES (Sub-class of Resource - Consumable)
DROP TABLE IF EXISTS medical_supplies;
CREATE TABLE medical_supplies (
    supply_id VARCHAR(20) PRIMARY KEY,
    resource_id VARCHAR(20) NOT NULL,
    supply_name VARCHAR(80) NOT NULL, -- First Aid Kits, IV Fluid Bottles, Syringes, Bandages, Basic Medicines
    unit_type VARCHAR(30) NOT NULL DEFAULT 'Units',
    unit_stock INT NOT NULL DEFAULT 0,
    min_reorder_level INT NOT NULL DEFAULT 10,
    expiry_date DATE DEFAULT NULL,
    hospital_id VARCHAR(20) NOT NULL,
    CONSTRAINT fk_ms_res FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE,
    CONSTRAINT fk_ms_hosp FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 10. HOSPITAL FACILITIES (Sub-class of Resource)
DROP TABLE IF EXISTS hospital_facilities;
CREATE TABLE hospital_facilities (
    facility_id VARCHAR(20) PRIMARY KEY,
    resource_id VARCHAR(20) NOT NULL,
    facility_name VARCHAR(80) NOT NULL, -- Emergency Room, Operation Theatre, Laboratory, Pharmacy, Ambulance
    facility_type VARCHAR(40) NOT NULL,
    is_operational BOOLEAN NOT NULL DEFAULT TRUE,
    hospital_id VARCHAR(20) NOT NULL,
    CONSTRAINT fk_hf_res FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE,
    CONSTRAINT fk_hf_hosp FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 11. DIAGNOSTIC SERVICES (Internal in hospital)
DROP TABLE IF EXISTS diagnostic_services;
CREATE TABLE diagnostic_services (
    service_id VARCHAR(20) PRIMARY KEY,
    service_code VARCHAR(30) UNIQUE NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    test_type VARCHAR(40) NOT NULL, -- X_RAY, CT_SCAN, MRI_SCAN, ULTRASOUND, BLOOD_TEST, LAB_TEST
    hospital_id VARCHAR(20) NOT NULL,
    cost DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    turnaround_hours INT NOT NULL DEFAULT 2,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_ds_hosp FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 12. EXTERNAL DIAGNOSTIC CENTRES
DROP TABLE IF EXISTS diagnostic_centres;
CREATE TABLE diagnostic_centres (
    centre_id VARCHAR(20) PRIMARY KEY,
    centre_name VARCHAR(150) NOT NULL,
    location_name VARCHAR(100) NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    available_services TEXT NOT NULL, -- Comma-separated or JSON list of services
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 13. CONSULTATIONS
DROP TABLE IF EXISTS consultations;
CREATE TABLE consultations (
    consultation_id VARCHAR(20) PRIMARY KEY,
    visit_id VARCHAR(20) NOT NULL,
    patient_id VARCHAR(20) NOT NULL,
    doctor_id VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED
    symptoms TEXT DEFAULT NULL,
    doctor_observations TEXT DEFAULT NULL,
    prescription TEXT DEFAULT NULL,
    started_at DATETIME DEFAULT NULL,
    completed_at DATETIME DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cons_visit FOREIGN KEY (visit_id) REFERENCES patient_visits(visit_id) ON DELETE CASCADE,
    CONSTRAINT fk_cons_pat FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_cons_doc FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 14. TREATMENT PLANS
DROP TABLE IF EXISTS treatment_plans;
CREATE TABLE treatment_plans (
    treatment_plan_id VARCHAR(20) PRIMARY KEY,
    consultation_id VARCHAR(20) NOT NULL,
    visit_id VARCHAR(20) NOT NULL,
    patient_id VARCHAR(20) NOT NULL,
    doctor_id VARCHAR(20) NOT NULL,
    treatment_category VARCHAR(40) NOT NULL, -- CONSULTATION_ONLY, MEDICATION_BASIC, DIAGNOSTIC_REQUIRED, ADMISSION_REQUIRED, EMERGENCY_TREATMENT, REFERRAL_REQUIRED, FOLLOW_UP
    doctor_notes TEXT DEFAULT NULL,
    required_bed_type VARCHAR(30) DEFAULT NULL, -- GENERAL, ICU, EMERGENCY, NO_BED
    required_doctor_specialization VARCHAR(80) DEFAULT NULL,
    required_resources TEXT DEFAULT NULL, -- JSON or comma-separated list of resources requested
    diagnostic_orders_summary TEXT DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED', -- CREATED, IN_PROGRESS, COMPLETED, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tp_cons FOREIGN KEY (consultation_id) REFERENCES consultations(consultation_id) ON DELETE CASCADE,
    CONSTRAINT fk_tp_visit FOREIGN KEY (visit_id) REFERENCES patient_visits(visit_id) ON DELETE CASCADE,
    CONSTRAINT fk_tp_doc FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 15. DIAGNOSTIC ORDERS
DROP TABLE IF EXISTS diagnostic_orders;
CREATE TABLE diagnostic_orders (
    order_id VARCHAR(20) PRIMARY KEY,
    visit_id VARCHAR(20) NOT NULL,
    patient_id VARCHAR(20) NOT NULL,
    doctor_id VARCHAR(20) NOT NULL,
    test_type VARCHAR(50) NOT NULL, -- X_RAY, CT_SCAN, MRI_SCAN, ULTRASOUND, BLOOD_TEST, LAB_TEST
    priority VARCHAR(20) NOT NULL DEFAULT 'MINOR', -- CRITICAL, SERIOUS, MINOR
    status VARCHAR(30) NOT NULL DEFAULT 'PRESCRIBED', -- PRESCRIBED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    diagnostic_centre_id VARCHAR(20) DEFAULT NULL, -- NULL if internal, or external centre_id
    prescribed_date DATETIME NOT NULL,
    scheduled_date DATETIME DEFAULT NULL,
    completed_date DATETIME DEFAULT NULL,
    results_summary TEXT DEFAULT NULL,
    doctor_review_notes TEXT DEFAULT NULL,
    reviewed_at DATETIME DEFAULT NULL,
    reviewed_by_doctor_id VARCHAR(20) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_do_visit FOREIGN KEY (visit_id) REFERENCES patient_visits(visit_id) ON DELETE CASCADE,
    CONSTRAINT fk_do_pat FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_do_doc FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 16. PATIENT RESOURCE REQUIREMENTS
DROP TABLE IF EXISTS patient_resource_requirements;
CREATE TABLE patient_resource_requirements (
    requirement_id VARCHAR(20) PRIMARY KEY,
    visit_id VARCHAR(20) NOT NULL,
    patient_id VARCHAR(20) NOT NULL,
    resource_category VARCHAR(30) NOT NULL,
    resource_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    is_mandatory BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, ALLOCATED, REFERRED, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prr_visit FOREIGN KEY (visit_id) REFERENCES patient_visits(visit_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 17. RESOURCE ALLOCATIONS (Active tracking)
DROP TABLE IF EXISTS resource_allocations;
CREATE TABLE resource_allocations (
    allocation_id VARCHAR(20) PRIMARY KEY,
    visit_id VARCHAR(20) NOT NULL,
    patient_id VARCHAR(20) NOT NULL,
    resource_id VARCHAR(20) NOT NULL,
    resource_name VARCHAR(100) NOT NULL,
    resource_type VARCHAR(30) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    allocated_at DATETIME NOT NULL,
    released_at DATETIME DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, RELEASED
    CONSTRAINT fk_ra_visit FOREIGN KEY (visit_id) REFERENCES patient_visits(visit_id) ON DELETE CASCADE,
    CONSTRAINT fk_ra_res FOREIGN KEY (resource_id) REFERENCES resources(resource_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 18. DOCTOR ASSIGNMENTS (Inpatient / Consultation tracking)
DROP TABLE IF EXISTS doctor_assignments;
CREATE TABLE doctor_assignments (
    assignment_id VARCHAR(20) PRIMARY KEY,
    doctor_id VARCHAR(20) NOT NULL,
    patient_id VARCHAR(20) NOT NULL,
    visit_id VARCHAR(20) NOT NULL,
    assignment_type VARCHAR(30) NOT NULL, -- CONSULTATION, INPATIENT_CARE
    start_time DATETIME NOT NULL,
    end_time DATETIME DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, COMPLETED, CANCELLED
    CONSTRAINT fk_da_doc FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE,
    CONSTRAINT fk_da_vis FOREIGN KEY (visit_id) REFERENCES patient_visits(visit_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 19. ADMISSIONS
DROP TABLE IF EXISTS admissions;
CREATE TABLE admissions (
    admission_id VARCHAR(20) PRIMARY KEY,
    visit_id VARCHAR(20) NOT NULL,
    patient_id VARCHAR(20) NOT NULL,
    doctor_id VARCHAR(20) NOT NULL,
    hospital_id VARCHAR(20) NOT NULL,
    bed_id VARCHAR(20) DEFAULT NULL,
    admission_date DATETIME NOT NULL,
    discharge_date DATETIME DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, DISCHARGED, TRANSFERRED
    admission_notes TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_adm_vis FOREIGN KEY (visit_id) REFERENCES patient_visits(visit_id) ON DELETE CASCADE,
    CONSTRAINT fk_adm_doc FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE,
    CONSTRAINT fk_adm_hosp FOREIGN KEY (hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE,
    CONSTRAINT fk_adm_bed FOREIGN KEY (bed_id) REFERENCES beds(bed_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 20. REFERRALS (Using Dijkstra's algorithm to find best hospital)
DROP TABLE IF EXISTS referrals;
CREATE TABLE referrals (
    referral_id VARCHAR(20) PRIMARY KEY,
    visit_id VARCHAR(20) NOT NULL,
    patient_id VARCHAR(20) NOT NULL,
    from_hospital_id VARCHAR(20) NOT NULL,
    to_hospital_id VARCHAR(20) NOT NULL,
    reason TEXT NOT NULL,
    travel_time_minutes INT NOT NULL DEFAULT 0,
    route_path VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED', -- PENDING, CONFIRMED, COMPLETED, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ref_vis FOREIGN KEY (visit_id) REFERENCES patient_visits(visit_id) ON DELETE CASCADE,
    CONSTRAINT fk_ref_from FOREIGN KEY (from_hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE,
    CONSTRAINT fk_ref_to FOREIGN KEY (to_hospital_id) REFERENCES hospitals(hospital_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 21. CONSULTATION WAITING QUEUE
DROP TABLE IF EXISTS consultation_waiting_queue;
CREATE TABLE consultation_waiting_queue (
    queue_id VARCHAR(20) PRIMARY KEY,
    visit_id VARCHAR(20) NOT NULL,
    patient_id VARCHAR(20) NOT NULL,
    severity_level VARCHAR(20) NOT NULL, -- Critical, Serious, Minor
    required_specialization VARCHAR(80) NOT NULL,
    arrival_time DATETIME NOT NULL,
    priority_score INT NOT NULL, -- 1=Critical, 2=Serious, 3=Minor
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING', -- WAITING, ASSIGNED, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cwq_vis FOREIGN KEY (visit_id) REFERENCES patient_visits(visit_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 22. RESOURCE WAITING QUEUE
DROP TABLE IF EXISTS resource_waiting_queue;
CREATE TABLE resource_waiting_queue (
    queue_id VARCHAR(20) PRIMARY KEY,
    visit_id VARCHAR(20) NOT NULL,
    patient_id VARCHAR(20) NOT NULL,
    severity_level VARCHAR(20) NOT NULL,
    required_bed_type VARCHAR(30) DEFAULT NULL,
    required_specialization VARCHAR(80) DEFAULT NULL,
    required_resources TEXT DEFAULT NULL,
    arrival_time DATETIME NOT NULL,
    priority_score INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING', -- WAITING, ALLOCATED, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rwq_vis FOREIGN KEY (visit_id) REFERENCES patient_visits(visit_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 23. HOSPITAL ROUTES (Weighted Graph Edges)
DROP TABLE IF EXISTS hospital_routes;
CREATE TABLE hospital_routes (
    route_id INT AUTO_INCREMENT PRIMARY KEY,
    from_location VARCHAR(100) NOT NULL,
    to_location VARCHAR(100) NOT NULL,
    distance_km DECIMAL(6, 2) NOT NULL,
    travel_time_minutes INT NOT NULL,
    road_condition VARCHAR(50) DEFAULT 'GOOD'
) ENGINE=InnoDB;

-- 24. RESOURCE HISTORY
DROP TABLE IF EXISTS resource_history;
CREATE TABLE resource_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    resource_id VARCHAR(20) NOT NULL,
    resource_name VARCHAR(100) NOT NULL,
    action_type VARCHAR(30) NOT NULL, -- ALLOCATED, RELEASED, CONSUMED, RESTOCKED
    quantity INT NOT NULL,
    patient_id VARCHAR(20) DEFAULT NULL,
    visit_id VARCHAR(20) DEFAULT NULL,
    timestamp DATETIME NOT NULL,
    remarks VARCHAR(255) DEFAULT NULL
) ENGINE=InnoDB;

-- 25. DISCHARGE HISTORY
DROP TABLE IF EXISTS discharge_history;
CREATE TABLE discharge_history (
    discharge_id VARCHAR(20) PRIMARY KEY,
    admission_id VARCHAR(20) NOT NULL,
    visit_id VARCHAR(20) NOT NULL,
    patient_id VARCHAR(20) NOT NULL,
    discharge_date DATETIME NOT NULL,
    discharge_summary TEXT NOT NULL,
    resources_released TEXT DEFAULT NULL,
    follow_up_instructions TEXT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Restore foreign key checks
SET FOREIGN_KEY_CHECKS = 1;
