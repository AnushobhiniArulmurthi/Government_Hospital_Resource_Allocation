-- =====================================================================
-- GOVERNMENT HOSPITAL RESOURCE ALLOCATION SYSTEM (GHRAS)
-- Common Analytical, Transactional and Diagnostic Queries
-- =====================================================================

USE hospital_resource_db;

-- 1. DASHBOARD: Patient Statistics Summary
SELECT 
    COUNT(DISTINCT p.patient_id) AS total_registered_patients,
    SUM(CASE WHEN v.severity_level = 'Critical' THEN 1 ELSE 0 END) AS critical_patients,
    SUM(CASE WHEN v.severity_level = 'Serious' THEN 1 ELSE 0 END) AS serious_patients,
    SUM(CASE WHEN v.severity_level = 'Minor' THEN 1 ELSE 0 END) AS minor_patients,
    (SELECT COUNT(*) FROM consultation_waiting_queue WHERE status = 'WAITING') AS waiting_for_consultation,
    SUM(CASE WHEN v.visit_status IN ('IN_CONSULTATION', 'TREATMENT_PLANNED') THEN 1 ELSE 0 END) AS under_treatment,
    (SELECT COUNT(*) FROM admissions WHERE status = 'ACTIVE') AS admitted_patients,
    SUM(CASE WHEN v.visit_status = 'DISCHARGED' OR v.visit_status = 'COMPLETED' THEN 1 ELSE 0 END) AS discharged_patients
FROM patients p
LEFT JOIN patient_visits v ON p.patient_id = v.patient_id;

-- 2. DASHBOARD: Doctor Statistics Summary
SELECT 
    COUNT(*) AS total_doctors,
    SUM(CASE WHEN availability_status = 'AVAILABLE' AND duty_status = 'ON_DUTY' THEN 1 ELSE 0 END) AS available_doctors,
    SUM(CASE WHEN availability_status = 'IN_CONSULTATION' THEN 1 ELSE 0 END) AS consulting_doctors,
    SUM(CASE WHEN current_inpatient_count > 0 THEN 1 ELSE 0 END) AS assigned_to_inpatients,
    SUM(CASE WHEN duty_status IN ('OFF_DUTY', 'ON_LEAVE') THEN 1 ELSE 0 END) AS off_duty_or_leave
FROM doctors
WHERE is_active = TRUE;

-- 3. DASHBOARD: Bed Statistics Summary
SELECT 
    COUNT(*) AS total_beds,
    SUM(CASE WHEN status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_beds,
    SUM(CASE WHEN status = 'OCCUPIED' THEN 1 ELSE 0 END) AS occupied_beds,
    SUM(CASE WHEN bed_type = 'GENERAL' AND status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_general_beds,
    SUM(CASE WHEN bed_type = 'ICU' AND status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_icu_beds,
    SUM(CASE WHEN bed_type = 'EMERGENCY' AND status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_emergency_beds
FROM beds
WHERE hospital_id = 'H-001';

-- 4. DASHBOARD: Referral Statistics Summary
SELECT 
    COUNT(*) AS total_referrals,
    SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) AS pending_referrals,
    SUM(CASE WHEN status IN ('CONFIRMED', 'COMPLETED') THEN 1 ELSE 0 END) AS completed_referrals
FROM referrals;

-- 5. ATOMIC DOCTOR ID GENERATION (Safe concurrency)
-- In transactional Java service:
-- START TRANSACTION;
-- SELECT current_val FROM doctor_id_sequence WHERE id = 1 FOR UPDATE;
-- UPDATE doctor_id_sequence SET current_val = current_val + 1 WHERE id = 1;
-- COMMIT;

-- 6. CONSULTATION QUEUE RANKING (Severity-First, Arrival-Time Second)
SELECT 
    q.queue_id,
    q.visit_id,
    p.full_name AS patient_name,
    p.age,
    p.gender,
    q.severity_level,
    q.required_specialization,
    q.arrival_time,
    q.priority_score
FROM consultation_waiting_queue q
JOIN patients p ON q.patient_id = p.patient_id
WHERE q.status = 'WAITING'
ORDER BY q.priority_score ASC, q.arrival_time ASC;

-- 7. RESOURCE ALLOCATION QUEUE (Severity-First, Arrival-Time Second)
SELECT 
    rq.queue_id,
    rq.visit_id,
    p.full_name AS patient_name,
    rq.severity_level,
    rq.required_bed_type,
    rq.required_specialization,
    rq.required_resources,
    rq.arrival_time,
    rq.priority_score
FROM resource_waiting_queue rq
JOIN patients p ON rq.patient_id = p.patient_id
WHERE rq.status = 'WAITING'
ORDER BY rq.priority_score ASC, rq.arrival_time ASC;

-- 8. COMPLETE PATIENT HISTORY (All visits, consultations, treatments, admissions)
SELECT 
    p.patient_id,
    p.full_name,
    v.visit_id,
    v.arrival_time,
    v.severity_level,
    v.patient_category,
    v.visit_status,
    d.doctor_name AS consulting_doctor,
    c.symptoms,
    c.doctor_observations,
    c.prescription,
    tp.treatment_category,
    tp.doctor_notes,
    adm.admission_id,
    adm.bed_id,
    adm.admission_date,
    adm.discharge_date
FROM patients p
JOIN patient_visits v ON p.patient_id = v.patient_id
LEFT JOIN consultations c ON v.visit_id = c.visit_id
LEFT JOIN doctors d ON c.doctor_id = d.doctor_id
LEFT JOIN treatment_plans tp ON c.consultation_id = tp.consultation_id
LEFT JOIN admissions adm ON v.visit_id = adm.visit_id
ORDER BY p.patient_id, v.arrival_time DESC;

-- 9. FIND ELIGIBLE REFERRAL HOSPITALS (Checking bed + doctor + equipment availability)
SELECT 
    h.hospital_id,
    h.hospital_name,
    h.location_name,
    COUNT(DISTINCT b.bed_id) AS available_matching_beds,
    COUNT(DISTINCT doc.doctor_id) AS available_matching_doctors
FROM hospitals h
LEFT JOIN beds b ON h.hospital_id = b.hospital_id AND b.bed_type = 'ICU' AND b.status = 'AVAILABLE'
LEFT JOIN doctors doc ON h.hospital_id = doc.hospital_id AND doc.specialization = 'Neurology' AND doc.availability_status = 'AVAILABLE'
WHERE h.is_primary = FALSE
GROUP BY h.hospital_id, h.hospital_name, h.location_name;
