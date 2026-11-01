-- =====================================================================
-- GOVERNMENT HOSPITAL RESOURCE ALLOCATION SYSTEM (GHRAS)
-- Sample Data Population Script
-- =====================================================================

USE hospital_resource_db;
SET FOREIGN_KEY_CHECKS = 0;

-- Clean existing data
TRUNCATE TABLE discharge_history;
TRUNCATE TABLE resource_history;
TRUNCATE TABLE hospital_routes;
TRUNCATE TABLE resource_waiting_queue;
TRUNCATE TABLE consultation_waiting_queue;
TRUNCATE TABLE referrals;
TRUNCATE TABLE admissions;
TRUNCATE TABLE doctor_assignments;
TRUNCATE TABLE resource_allocations;
TRUNCATE TABLE patient_resource_requirements;
TRUNCATE TABLE diagnostic_orders;
TRUNCATE TABLE treatment_plans;
TRUNCATE TABLE consultations;
TRUNCATE TABLE diagnostic_centres;
TRUNCATE TABLE diagnostic_services;
TRUNCATE TABLE hospital_facilities;
TRUNCATE TABLE medical_supplies;
TRUNCATE TABLE medical_equipment;
TRUNCATE TABLE resources;
TRUNCATE TABLE beds;
TRUNCATE TABLE patient_visits;
TRUNCATE TABLE patients;
TRUNCATE TABLE doctors;
TRUNCATE TABLE doctor_id_sequence;
TRUNCATE TABLE hospitals;

-- 1. HOSPITALS (1 Primary + 3 Referral Hospitals)
INSERT INTO hospitals (hospital_id, hospital_name, location_name, is_primary, contact_number, address, total_beds, latitude, longitude) VALUES
('H-001', 'Nalam District Government Headquarters Hospital', 'Nalam Central', TRUE, '+91-4362-230001', 'Hospital Road, Nalam Central, Nalam District', 60, 10.7870, 79.1378),
('H-002', 'Anna Memorial Super Specialty Hospital', 'Medical Square', FALSE, '+91-4362-245102', 'Kamarajar Salai, Medical Square, Nalam', 50, 10.8120, 79.1620),
('H-003', 'Thiruvarur Government Medical College Hospital', 'East Junction', FALSE, '+91-4366-221303', 'College Road, East Junction, Thiruvarur', 45, 10.7712, 79.2310),
('H-004', 'Kaveri Regional Government Hospital', 'South Hub', FALSE, '+91-4362-259404', 'Bypass Ring Road, South Hub, Nalam', 40, 10.7420, 79.1150);

-- 2. DOCTOR ID SEQUENCE (Initialized at 8 because 8 sample doctors exist)
INSERT INTO doctor_id_sequence (id, current_val) VALUES (1, 8);

-- 3. DOCTORS (Initial 8 Doctors, including General Physicians and Specialists)
INSERT INTO doctors (doctor_id, doctor_name, specialization, qualification, contact_number, email, duty_status, availability_status, hospital_id, doctor_type, date_of_joining, max_inpatient_capacity, current_inpatient_count, is_active) VALUES
('D-001', 'Dr. Ramesh Kumar', 'General Medicine', 'MBBS, MD (General Medicine)', '+91-9840112233', 'ramesh.k@nalamgh.gov.in', 'ON_DUTY', 'AVAILABLE', 'H-001', 'GENERAL_PHYSICIAN', '2019-03-15', 8, 1, TRUE),
('D-002', 'Dr. Priya Sundaram', 'Cardiology', 'MBBS, MD, DM (Cardiology)', '+91-9840223344', 'priya.s@nalamgh.gov.in', 'ON_DUTY', 'AVAILABLE', 'H-001', 'SPECIALIST', '2020-07-01', 6, 2, TRUE),
('D-003', 'Dr. Anand Raman', 'Pulmonology', 'MBBS, DTCD, DNB (Respiratory)', '+91-9840334455', 'anand.r@nalamgh.gov.in', 'ON_DUTY', 'AVAILABLE', 'H-001', 'SPECIALIST', '2021-01-10', 6, 0, TRUE),
('D-004', 'Dr. Meenakshi Iyer', 'Emergency & Critical Care', 'MBBS, MEM (Emergency Med)', '+91-9840445566', 'meenakshi.i@nalamgh.gov.in', 'ON_DUTY', 'AVAILABLE', 'H-001', 'SPECIALIST', '2018-11-20', 10, 2, TRUE),
('D-005', 'Dr. Senthil Nathan', 'Orthopedics', 'MBBS, MS (Orthopedics)', '+91-9840556677', 'senthil.n@nalamgh.gov.in', 'ON_DUTY', 'OFF_DUTY', 'H-001', 'SPECIALIST', '2022-04-18', 6, 0, TRUE),
('D-006', 'Dr. Kavitha Selvam', 'Pediatrics', 'MBBS, DCH, MD (Pediatrics)', '+91-9840667788', 'kavitha.s@nalamgh.gov.in', 'ON_DUTY', 'AVAILABLE', 'H-001', 'GENERAL_PHYSICIAN', '2023-02-14', 8, 0, TRUE),
('D-007', 'Dr. Rajesh Balaji', 'Neurology', 'MBBS, MD, DM (Neurology)', '+91-9840778899', 'rajesh.b@annaspec.gov.in', 'ON_DUTY', 'AVAILABLE', 'H-002', 'SPECIALIST', '2020-09-01', 5, 1, TRUE),
('D-008', 'Dr. Shalini George', 'Nephrology', 'MBBS, MD, DM (Nephrology)', '+91-9840889900', 'shalini.g@thiruvarurmc.gov.in', 'ON_DUTY', 'AVAILABLE', 'H-003', 'SPECIALIST', '2021-05-11', 5, 0, TRUE);

-- 4. BEDS (General, ICU, Emergency Beds across Hospitals)
INSERT INTO beds (bed_id, bed_number, bed_type, hospital_id, status, floor_ward, daily_charge) VALUES
-- H-001 Beds
('BED-101', 'GEN-01', 'GENERAL', 'H-001', 'AVAILABLE', 'Ward A - Ground Floor', 0.00),
('BED-102', 'GEN-02', 'GENERAL', 'H-001', 'AVAILABLE', 'Ward A - Ground Floor', 0.00),
('BED-103', 'GEN-03', 'GENERAL', 'H-001', 'OCCUPIED',  'Ward A - Ground Floor', 0.00),
('BED-104', 'GEN-04', 'GENERAL', 'H-001', 'AVAILABLE', 'Ward B - 1st Floor', 0.00),
('BED-105', 'ICU-01', 'ICU',     'H-001', 'AVAILABLE', 'ICU Ward - 2nd Floor', 0.00),
('BED-106', 'ICU-02', 'ICU',     'H-001', 'OCCUPIED',  'ICU Ward - 2nd Floor', 0.00),
('BED-107', 'EMG-01', 'EMERGENCY', 'H-001', 'AVAILABLE', 'Triage & Trauma Center', 0.00),
('BED-108', 'EMG-02', 'EMERGENCY', 'H-001', 'AVAILABLE', 'Triage & Trauma Center', 0.00),
-- H-002 Beds (Referral Hospital)
('BED-201', 'ICU-201', 'ICU',     'H-002', 'AVAILABLE', 'Super Specialty ICU Block', 0.00),
('BED-202', 'ICU-202', 'ICU',     'H-002', 'AVAILABLE', 'Neuro ICU Block', 0.00),
('BED-203', 'GEN-201', 'GENERAL', 'H-002', 'AVAILABLE', 'General Ward Floor 1', 0.00),
-- H-003 Beds (Referral Hospital)
('BED-301', 'ICU-301', 'ICU',     'H-003', 'AVAILABLE', 'Medical College ICU Floor 2', 0.00),
('BED-302', 'GEN-301', 'GENERAL', 'H-003', 'AVAILABLE', 'General Ward Floor 2', 0.00),
-- H-004 Beds (Referral Hospital)
('BED-401', 'EMG-401', 'EMERGENCY', 'H-004', 'AVAILABLE', 'South Trauma Block', 0.00),
('BED-402', 'GEN-401', 'GENERAL', 'H-004', 'AVAILABLE', 'General Ward 1', 0.00);

-- 5. RESOURCES (Medical Equipment, Supplies, Facilities)
INSERT INTO resources (resource_id, resource_name, category, hospital_id, total_quantity, available_quantity, allocated_quantity, is_reusable, status) VALUES
-- Equipment for H-001
('RES-EQ-01', 'Oxygen Cylinder', 'EQUIPMENT', 'H-001', 30, 26, 4, TRUE, 'AVAILABLE'),
('RES-EQ-02', 'Ventilator', 'EQUIPMENT', 'H-001', 10, 8, 2, TRUE, 'AVAILABLE'),
('RES-EQ-03', 'ECG Machine', 'EQUIPMENT', 'H-001', 8, 7, 1, TRUE, 'AVAILABLE'),
('RES-EQ-04', 'Patient Monitor', 'EQUIPMENT', 'H-001', 25, 21, 4, TRUE, 'AVAILABLE'),
('RES-EQ-05', 'Wheelchair', 'EQUIPMENT', 'H-001', 20, 18, 2, TRUE, 'AVAILABLE'),
('RES-EQ-06', 'Stretcher', 'EQUIPMENT', 'H-001', 15, 13, 2, TRUE, 'AVAILABLE'),
('RES-EQ-07', 'Infusion Pump', 'EQUIPMENT', 'H-001', 18, 16, 2, TRUE, 'AVAILABLE'),
-- Supplies for H-001 (Consumables)
('RES-SP-01', 'First Aid Kits', 'SUPPLY', 'H-001', 150, 135, 15, FALSE, 'AVAILABLE'),
('RES-SP-02', 'IV Fluid Bottles', 'SUPPLY', 'H-001', 300, 260, 40, FALSE, 'AVAILABLE'),
('RES-SP-03', 'Syringes', 'SUPPLY', 'H-001', 1000, 850, 150, FALSE, 'AVAILABLE'),
('RES-SP-04', 'Bandages', 'SUPPLY', 'H-001', 500, 420, 80, FALSE, 'AVAILABLE'),
('RES-SP-05', 'Basic Medicines', 'SUPPLY', 'H-001', 800, 710, 90, FALSE, 'AVAILABLE'),
-- Facilities for H-001
('RES-FC-01', 'Emergency Room', 'FACILITY', 'H-001', 3, 2, 1, TRUE, 'AVAILABLE'),
('RES-FC-02', 'Operation Theatre', 'FACILITY', 'H-001', 4, 3, 1, TRUE, 'AVAILABLE'),
('RES-FC-03', 'Laboratory', 'FACILITY', 'H-001', 2, 2, 0, TRUE, 'AVAILABLE'),
('RES-FC-04', 'Pharmacy', 'FACILITY', 'H-001', 2, 2, 0, TRUE, 'AVAILABLE'),
('RES-FC-05', 'Ambulance', 'FACILITY', 'H-001', 5, 4, 1, TRUE, 'AVAILABLE'),
-- Resources for H-002 (Super Specialty)
('RES-EQ-201', 'Ventilator', 'EQUIPMENT', 'H-002', 15, 12, 3, TRUE, 'AVAILABLE'),
('RES-EQ-202', 'ECG Machine', 'EQUIPMENT', 'H-002', 10, 9, 1, TRUE, 'AVAILABLE'),
('RES-FC-201', 'Operation Theatre', 'FACILITY', 'H-002', 6, 5, 1, TRUE, 'AVAILABLE'),
-- Resources for H-003 (Medical College)
('RES-EQ-301', 'Ventilator', 'EQUIPMENT', 'H-003', 12, 10, 2, TRUE, 'AVAILABLE'),
('RES-EQ-302', 'Oxygen Cylinder', 'EQUIPMENT', 'H-003', 40, 35, 5, TRUE, 'AVAILABLE'),
-- Resources for H-004
('RES-EQ-401', 'Oxygen Cylinder', 'EQUIPMENT', 'H-004', 25, 20, 5, TRUE, 'AVAILABLE');

-- 6. MEDICAL EQUIPMENT ITEMS
INSERT INTO medical_equipment (equipment_id, resource_id, equipment_type, serial_number, condition_status, is_allocated, hospital_id) VALUES
('EQ-1001', 'RES-EQ-01', 'Oxygen Cylinder', 'O2-CYL-001', 'OPERATIONAL', FALSE, 'H-001'),
('EQ-1002', 'RES-EQ-01', 'Oxygen Cylinder', 'O2-CYL-002', 'OPERATIONAL', FALSE, 'H-001'),
('EQ-1003', 'RES-EQ-02', 'Ventilator',      'VENT-001',   'OPERATIONAL', FALSE, 'H-001'),
('EQ-1004', 'RES-EQ-02', 'Ventilator',      'VENT-002',   'OPERATIONAL', FALSE, 'H-001'),
('EQ-1005', 'RES-EQ-03', 'ECG Machine',     'ECG-001',    'OPERATIONAL', FALSE, 'H-001'),
('EQ-1006', 'RES-EQ-04', 'Patient Monitor', 'PMON-001',   'OPERATIONAL', FALSE, 'H-001'),
('EQ-1007', 'RES-EQ-05', 'Wheelchair',      'WCH-001',    'OPERATIONAL', FALSE, 'H-001'),
('EQ-1008', 'RES-EQ-06', 'Stretcher',       'STR-001',    'OPERATIONAL', FALSE, 'H-001'),
('EQ-1009', 'RES-EQ-07', 'Infusion Pump',   'INFP-001',   'OPERATIONAL', FALSE, 'H-001'),
('EQ-2001', 'RES-EQ-201', 'Ventilator',     'VENT-201',   'OPERATIONAL', FALSE, 'H-002');

-- 7. MEDICAL SUPPLIES ITEMS
INSERT INTO medical_supplies (supply_id, resource_id, supply_name, unit_type, unit_stock, min_reorder_level, expiry_date, hospital_id) VALUES
('SUP-101', 'RES-SP-01', 'First Aid Kits', 'Kit', 135, 20, '2027-12-31', 'H-001'),
('SUP-102', 'RES-SP-02', 'IV Fluid Bottles', 'Bottle', 260, 50, '2027-06-30', 'H-001'),
('SUP-103', 'RES-SP-03', 'Syringes', 'Pack of 10', 850, 100, '2028-01-15', 'H-001'),
('SUP-104', 'RES-SP-04', 'Bandages', 'Roll', 420, 50, '2028-05-30', 'H-001'),
('SUP-105', 'RES-SP-05', 'Basic Medicines', 'Box', 710, 100, '2027-09-30', 'H-001');

-- 8. HOSPITAL FACILITIES
INSERT INTO hospital_facilities (facility_id, resource_id, facility_name, facility_type, is_operational, hospital_id) VALUES
('FAC-101', 'RES-FC-01', 'Emergency Room A', 'EMERGENCY_ROOM', TRUE, 'H-001'),
('FAC-102', 'RES-FC-02', 'Operation Theatre 1', 'OPERATION_THEATRE', TRUE, 'H-001'),
('FAC-103', 'RES-FC-03', 'Central Clinical Lab', 'LABORATORY', TRUE, 'H-001'),
('FAC-104', 'RES-FC-04', 'Hospital Pharmacy Unit', 'PHARMACY', TRUE, 'H-001'),
('FAC-105', 'RES-FC-05', 'Advanced Life Ambulance 108-A', 'AMBULANCE', TRUE, 'H-001');

-- 9. DIAGNOSTIC SERVICES (Internal Hospital Services)
INSERT INTO diagnostic_services (service_id, service_code, service_name, test_type, hospital_id, cost, turnaround_hours, is_available) VALUES
('DS-001', 'DS-XRAY-01', 'Digital Chest X-Ray', 'X_RAY', 'H-001', 150.00, 1, TRUE),
('DS-002', 'DS-USG-01',  'Abdominal Ultrasound Scan', 'ULTRASOUND', 'H-001', 300.00, 2, TRUE),
('DS-003', 'DS-BLD-01',  'Complete Blood Count (CBC) & Panel', 'BLOOD_TEST', 'H-001', 80.00, 1, TRUE),
('DS-004', 'DS-LAB-01',  'Comprehensive Metabolic Profile', 'LAB_TEST', 'H-001', 120.00, 2, TRUE),
('DS-005', 'DS-CT-01',   'High-Resolution CT Scan (128 Slice)', 'CT_SCAN', 'H-002', 1200.00, 3, TRUE),
('DS-006', 'DS-MRI-01',  'Advanced 3.0 Tesla MRI Scan', 'MRI_SCAN', 'H-002', 2500.00, 4, TRUE),
('DS-007', 'DS-CT-02',   'Emergency Head CT Scan', 'CT_SCAN', 'H-003', 1100.00, 2, TRUE);

-- 10. EXTERNAL DIAGNOSTIC CENTRES
INSERT INTO diagnostic_centres (centre_id, centre_name, location_name, contact_number, address, available_services, is_active) VALUES
('DC-001', 'MedScan Advanced Diagnostics', 'West Ward', '+91-4362-281001', '12 Nehru Road, West Ward, Nalam', 'CT Scan, MRI Scan, Ultrasound, Digital X-Ray, Blood Test', TRUE),
('DC-002', 'Apex Imaging & Clinical Pathology', 'North Bypass', '+91-4362-282002', '45 North Ring Road, North Bypass, Nalam', 'MRI Scan, CT Scan, PET Scan, Pathology Lab Tests', TRUE),
('DC-003', 'City Health Diagnostic Centre', 'Green Valley', '+91-4362-283003', '88 Main Street, Green Valley, Nalam', 'Ultrasound, X-Ray, Blood Test, Urine Analysis', TRUE);

-- 11. ROAD NETWORK (10 Connected Locations - Weighted Graph Edges for Dijkstra's Algorithm)
INSERT INTO hospital_routes (from_location, to_location, distance_km, travel_time_minutes, road_condition) VALUES
-- Direct and bidirectional routes among 10 locations in Nalam District
('Nalam Central', 'South Hub', 4.5, 8, 'EXCELLENT'),
('South Hub', 'Nalam Central', 4.5, 8, 'EXCELLENT'),
('Nalam Central', 'Medical Square', 6.0, 12, 'GOOD'),
('Medical Square', 'Nalam Central', 6.0, 12, 'GOOD'),
('Nalam Central', 'West Ward', 3.0, 6, 'GOOD'),
('West Ward', 'Nalam Central', 3.0, 6, 'GOOD'),
('West Ward', 'North Bypass', 5.0, 9, 'GOOD'),
('North Bypass', 'West Ward', 5.0, 9, 'GOOD'),
('North Bypass', 'Green Valley', 4.2, 7, 'EXCELLENT'),
('Green Valley', 'North Bypass', 4.2, 7, 'EXCELLENT'),
('Green Valley', 'Medical Square', 3.5, 6, 'GOOD'),
('Medical Square', 'Green Valley', 3.5, 6, 'GOOD'),
('Medical Square', 'East Junction', 8.0, 14, 'HIGHWAY'),
('East Junction', 'Medical Square', 8.0, 14, 'HIGHWAY'),
('South Hub', 'Port Road', 5.5, 10, 'AVERAGE'),
('Port Road', 'South Hub', 5.5, 10, 'AVERAGE'),
('Port Road', 'Valley Station', 4.0, 7, 'GOOD'),
('Valley Station', 'Port Road', 4.0, 7, 'GOOD'),
('Valley Station', 'Cross Junction', 3.8, 6, 'EXCELLENT'),
('Cross Junction', 'Valley Station', 3.8, 6, 'EXCELLENT'),
('Cross Junction', 'East Junction', 6.5, 11, 'GOOD'),
('East Junction', 'Cross Junction', 6.5, 11, 'GOOD'),
('Nalam Central', 'Green Valley', 7.5, 15, 'CONGESTED'),
('Green Valley', 'Nalam Central', 7.5, 15, 'CONGESTED'),
('South Hub', 'Cross Junction', 7.0, 13, 'GOOD'),
('Cross Junction', 'South Hub', 7.0, 13, 'GOOD');

-- 12. SAMPLE PATIENTS & VISITS (20 Synthetic Patient Records across All Workflows)
INSERT INTO patients (patient_id, full_name, age, gender, contact_number, address) VALUES
('P-1001', 'Murugan Selvam', 45, 'Male', '+91-9789012345', '14 North Car St, Nalam'),
('P-1002', 'Lakshmi Narayanan', 62, 'Female', '+91-9789023456', '27 West Main St, Nalam'),
('P-1003', 'Karthik Raja', 28, 'Male', '+91-9789034567', '5 South Colony, Nalam'),
('P-1004', 'Ananya Krishnan', 34, 'Female', '+91-9789045678', '9 Gandhi Nagar, Nalam'),
('P-1005', 'Gopalakrishnan V', 71, 'Male', '+91-9789056789', '102 Temple View Road, Nalam'),
('P-1006', 'Deepa Jayakumar', 23, 'Female', '+91-9789067890', '18 Kamarajar St, Nalam'),
('P-1007', 'Balaji Subramanian', 50, 'Male', '+91-9789078901', '33 New Bus Stand Rd, Nalam'),
('P-1008', 'Revathi Chandran', 41, 'Female', '+91-9789089012', '76 VOC Nagar, Nalam'),
('P-1009', 'Saravanan Muthu', 38, 'Male', '+91-9789090123', '4 Anna Salai, Nalam'),
('P-1010', 'Bhavani Shankar', 55, 'Female', '+91-9789101234', '11 Nehru Street, Nalam'),
('P-1011', 'Vigneshwaran P', 19, 'Male', '+91-9789112345', '60 Periyar St, Nalam'),
('P-1012', 'Meena Kumari', 30, 'Female', '+91-9789123456', '23 Bypass Rd, Nalam'),
('P-1013', 'Arunachalam S', 67, 'Male', '+91-9789134567', '8 Railway Feeder Rd, Nalam'),
('P-1014', 'Sumathi Kannan', 48, 'Female', '+91-9789145678', '15 Thillai Nagar, Nalam'),
('P-1015', 'Naveen Kumar', 26, 'Male', '+91-9789156789', '34 Market Rd, Nalam'),
('P-1016', 'Radhika Vijay', 39, 'Female', '+91-9789167890', '52 Hospital Colony, Nalam'),
('P-1017', 'Thirunavukkarasu M', 60, 'Male', '+91-9789178901', '7 Bazaar St, Nalam'),
('P-1018', 'Gayathri Sundar', 31, 'Female', '+91-9789189012', '99 Bharathi Nagar, Nalam'),
('P-1019', 'Ramanathan C', 74, 'Male', '+91-9789190123', '12 Sannathi St, Nalam'),
('P-1020', 'Janani Ramesh', 8, 'Female', '+91-9789201234', '42 School Rd, Nalam');

-- 13. PATIENT VISITS (Multiple visit IDs with diverse statuses & categories)
INSERT INTO patient_visits (visit_id, patient_id, severity_level, patient_category, visit_status, arrival_time, assigned_doctor_id, assigned_bed_id, notes) VALUES
('VIS-2001', 'P-1001', 'Minor', 'Outpatient', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 4 HOUR), 'D-001', NULL, 'Mild fever and sore throat. Outpatient consultation complete.'),
('VIS-2002', 'P-1002', 'Critical', 'Emergency', 'ADMITTED', DATE_SUB(NOW(), INTERVAL 3 HOUR), 'D-002', 'BED-106', 'Acute coronary syndrome symptoms. Immediate ICU admission.'),
('VIS-2003', 'P-1003', 'Serious', 'Diagnostic-Based', 'IN_CONSULTATION', DATE_SUB(NOW(), INTERVAL 2 HOUR), 'D-003', NULL, 'Persistent cough and hemoptysis. Chest X-Ray and CT requested.'),
('VIS-2004', 'P-1004', 'Minor', 'Outpatient', 'REGISTERED', DATE_SUB(NOW(), INTERVAL 90 MINUTE), NULL, NULL, 'Skin allergy and pruritus. Waiting for general physician.'),
('VIS-2005', 'P-1005', 'Critical', 'Inpatient', 'ADMITTED', DATE_SUB(NOW(), INTERVAL 5 HOUR), 'D-004', 'BED-103', 'Severe respiratory distress with hypoxia. General ward oxygen support.'),
('VIS-2006', 'P-1006', 'Minor', 'Outpatient', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 1 DAY), 'D-006', NULL, 'Routine pediatric follow-up with mother.'),
('VIS-2007', 'P-1007', 'Serious', 'Inpatient', 'TREATMENT_PLANNED', DATE_SUB(NOW(), INTERVAL 80 MINUTE), 'D-001', NULL, 'Uncontrolled diabetic ketoacidosis. General bed recommended.'),
('VIS-2008', 'P-1008', 'Critical', 'Emergency', 'REFERRED', DATE_SUB(NOW(), INTERVAL 2 HOUR), 'D-004', NULL, 'Severe intracranial hemorrhage. Primary hospital lacks MRI/Neuro. Referred to H-002.'),
('VIS-2009', 'P-1009', 'Minor', 'Outpatient', 'REGISTERED', DATE_SUB(NOW(), INTERVAL 45 MINUTE), NULL, NULL, 'Sprain in left ankle after badminton match.'),
('VIS-2010', 'P-1010', 'Serious', 'Diagnostic-Based', 'IN_CONSULTATION', DATE_SUB(NOW(), INTERVAL 35 MINUTE), 'D-001', NULL, 'Severe abdominal colic pain. Prescribed abdominal ultrasound.'),
('VIS-2011', 'P-1011', 'Minor', 'Outpatient', 'REGISTERED', DATE_SUB(NOW(), INTERVAL 30 MINUTE), NULL, NULL, 'Headache and fatigue.'),
('VIS-2012', 'P-1012', 'Serious', 'Inpatient', 'REGISTERED', DATE_SUB(NOW(), INTERVAL 25 MINUTE), NULL, NULL, 'High grade fever with low platelet count. Possible Dengue.'),
('VIS-2013', 'P-1013', 'Critical', 'Emergency', 'REGISTERED', DATE_SUB(NOW(), INTERVAL 15 MINUTE), NULL, NULL, 'Severe chest heaviness, diaphoresis. Critical priority triage.'),
('VIS-2014', 'P-1014', 'Minor', 'Outpatient', 'COMPLETED', DATE_SUB(NOW(), INTERVAL 6 HOUR), 'D-001', NULL, 'Dyspepsia and acid reflux. Medications prescribed.'),
('VIS-2015', 'P-1015', 'Serious', 'Emergency', 'REGISTERED', DATE_SUB(NOW(), INTERVAL 10 MINUTE), NULL, NULL, 'Laceration wound on forearm with active bleeding.'),
('VIS-2016', 'P-1016', 'Minor', 'Outpatient', 'REGISTERED', DATE_SUB(NOW(), INTERVAL 8 MINUTE), NULL, NULL, 'Joint pain during morning hours.'),
('VIS-2017', 'P-1017', 'Critical', 'Emergency', 'REGISTERED', DATE_SUB(NOW(), INTERVAL 5 MINUTE), NULL, NULL, 'Hypotensive shock. Needs immediate triage and resuscitation.'),
('VIS-2018', 'P-1018', 'Minor', 'Outpatient', 'REGISTERED', DATE_SUB(NOW(), INTERVAL 3 MINUTE), NULL, NULL, 'Seasonal rhinitis.'),
('VIS-2019', 'P-1019', 'Serious', 'Inpatient', 'REGISTERED', DATE_SUB(NOW(), INTERVAL 2 MINUTE), NULL, NULL, 'Chronic renal failure follow-up with fluid overload.'),
('VIS-2020', 'P-1020', 'Minor', 'Outpatient', 'REGISTERED', DATE_SUB(NOW(), INTERVAL 1 MINUTE), NULL, NULL, 'Fever in 8-year child.');

-- 14. SAMPLE CONSULTATIONS
INSERT INTO consultations (consultation_id, visit_id, patient_id, doctor_id, status, symptoms, doctor_observations, prescription, started_at, completed_at) VALUES
('CON-3001', 'VIS-2001', 'P-1001', 'D-001', 'COMPLETED', 'Fever 101F, throat irritation for 2 days', 'Pharyngeal congestion present, chest clear', 'Paracetamol 650mg TDS x 3d, Azithromycin 500mg OD x 3d', DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 220 MINUTE)),
('CON-3002', 'VIS-2002', 'P-1002', 'D-002', 'COMPLETED', 'Severe retrosternal chest pain radiating to left arm', 'BP 160/100, ST elevation on Lead II, III, aVF', 'Aspirin 300mg stat, Clopidogrel 300mg, Atorvastatin 80mg, transfer to ICU', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 160 MINUTE)),
('CON-3003', 'VIS-2003', 'P-1003', 'D-003', 'IN_PROGRESS', 'Cough with blood streaks, weight loss', 'Crepitations in right upper lobe, SpO2 94%', 'Order Digital Chest X-Ray and Sputum AFB test', DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL),
('CON-3004', 'VIS-2007', 'P-1007', 'D-001', 'COMPLETED', 'Polyuria, extreme thirst, random blood sugar 420 mg/dl', 'Dehydration grade II, Kussmaul breathing', 'IV Normal Saline 1000ml, Regular Insulin sliding scale, admit General Bed', DATE_SUB(NOW(), INTERVAL 80 MINUTE), DATE_SUB(NOW(), INTERVAL 60 MINUTE)),
('CON-3005', 'VIS-2008', 'P-1008', 'D-004', 'COMPLETED', 'Sudden loss of consciousness, right hemiplegia', 'GCS 8/15, anisocoria. Immediate neurosurgery / CT/MRI required', 'Stabilize airway, refer immediately to Anna Memorial Specialty Hospital', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 105 MINUTE));

-- 15. SAMPLE TREATMENT PLANS
INSERT INTO treatment_plans (treatment_plan_id, consultation_id, visit_id, patient_id, doctor_id, treatment_category, doctor_notes, required_bed_type, required_doctor_specialization, required_resources, diagnostic_orders_summary, status) VALUES
('TP-4001', 'CON-3001', 'VIS-2001', 'P-1001', 'D-001', 'CONSULTATION_ONLY', 'Patient stabilized on oral medication. Rest advised for 3 days.', 'NO_BED', 'General Medicine', 'Basic Medicines', 'None', 'COMPLETED'),
('TP-4002', 'CON-3002', 'VIS-2002', 'P-1002', 'D-002', 'HOSPITAL_ADMISSION_REQUIRED', 'Acute inferior wall myocardial infarction. Continuous cardiac telemetry and ICU bed mandated.', 'ICU', 'Cardiology', 'ECG Machine, Patient Monitor, Oxygen Cylinder, IV Fluid Bottles', 'ECG, Cardiac Troponin I', 'IN_PROGRESS'),
('TP-4003', 'CON-3003', 'VIS-2003', 'P-1003', 'D-003', 'DIAGNOSTIC_TESTS_REQUIRED', 'Prescribed Chest X-Ray. Re-evaluate treatment plan after radiological report.', 'NO_BED', 'Pulmonology', 'Digital Chest X-Ray', 'DS-XRAY-01', 'IN_PROGRESS'),
('TP-4004', 'CON-3004', 'VIS-2007', 'P-1007', 'D-001', 'HOSPITAL_ADMISSION_REQUIRED', 'Severe DKA needing monitored IV rehydration and insulin protocol.', 'GENERAL', 'General Medicine', 'IV Fluid Bottles, Syringes, Basic Medicines', 'Blood Sugar, Electrolytes', 'CREATED'),
('TP-4005', 'CON-3005', 'VIS-2008', 'P-1008', 'D-004', 'REFERRAL_REQUIRED', 'Acute intracranial bleed requires emergent neurosurgical care and 3T MRI at H-002.', 'ICU', 'Neurology', 'Ambulance, Oxygen Cylinder, Ventilator', 'Emergency Head CT Scan', 'COMPLETED');

-- 16. SAMPLE DIAGNOSTIC ORDERS
INSERT INTO diagnostic_orders (order_id, visit_id, patient_id, doctor_id, test_type, priority, status, diagnostic_centre_id, prescribed_date, scheduled_date, completed_date, results_summary, doctor_review_notes) VALUES
('DO-5001', 'VIS-2003', 'P-1003', 'D-003', 'X_RAY', 'SERIOUS', 'COMPLETED', NULL, DATE_SUB(NOW(), INTERVAL 110 MINUTE), DATE_SUB(NOW(), INTERVAL 90 MINUTE), DATE_SUB(NOW(), INTERVAL 45 MINUTE), 'Patchy opacity in right apical lobe, suggestive of consolidation/Kochs.', 'Report reviewed. Start anti-tubercular therapy workup and sputum test.'),
('DO-5002', 'VIS-2010', 'P-1010', 'D-001', 'ULTRASOUND', 'SERIOUS', 'SCHEDULED', 'DC-001', DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_ADD(NOW(), INTERVAL 1 HOUR), NULL, NULL, NULL),
('DO-5003', 'VIS-2008', 'P-1008', 'D-004', 'CT_SCAN', 'CRITICAL', 'COMPLETED', 'DC-002', DATE_SUB(NOW(), INTERVAL 100 MINUTE), DATE_SUB(NOW(), INTERVAL 80 MINUTE), DATE_SUB(NOW(), INTERVAL 50 MINUTE), 'Hyperdense lesion in left basal ganglia measuring 32x24mm with midline shift.', 'Acute hemorrhage. Urgent neurosurgical evacuation recommended.');

-- 17. SAMPLE ADMISSIONS
INSERT INTO admissions (admission_id, visit_id, patient_id, doctor_id, hospital_id, bed_id, admission_date, status, admission_notes) VALUES
('ADM-6001', 'VIS-2002', 'P-1002', 'D-002', 'H-001', 'BED-106', DATE_SUB(NOW(), INTERVAL 150 MINUTE), 'ACTIVE', 'Admitted to ICU-02 for acute STEMI stabilization.'),
('ADM-6002', 'VIS-2005', 'P-1005', 'D-004', 'H-001', 'BED-103', DATE_SUB(NOW(), INTERVAL 4 HOUR), 'ACTIVE', 'Admitted to GEN-03 for severe COPD exacerbation with oxygen therapy.');

-- 18. SAMPLE ACTIVE RESOURCE ALLOCATIONS
INSERT INTO resource_allocations (allocation_id, visit_id, patient_id, resource_id, resource_name, resource_type, quantity, allocated_at, status) VALUES
('ALL-7001', 'VIS-2002', 'P-1002', 'RES-EQ-04', 'Patient Monitor', 'EQUIPMENT', 1, DATE_SUB(NOW(), INTERVAL 150 MINUTE), 'ACTIVE'),
('ALL-7002', 'VIS-2002', 'P-1002', 'RES-EQ-01', 'Oxygen Cylinder', 'EQUIPMENT', 1, DATE_SUB(NOW(), INTERVAL 150 MINUTE), 'ACTIVE'),
('ALL-7003', 'VIS-2005', 'P-1005', 'RES-EQ-01', 'Oxygen Cylinder', 'EQUIPMENT', 1, DATE_SUB(NOW(), INTERVAL 4 HOUR), 'ACTIVE');

-- 19. SAMPLE REFERRALS
INSERT INTO referrals (referral_id, visit_id, patient_id, from_hospital_id, to_hospital_id, reason, travel_time_minutes, route_path, status) VALUES
('REF-8001', 'VIS-2008', 'P-1008', 'H-001', 'H-002', 'Acute intracerebral bleed requiring Neurosurgical ICU and immediate craniotomy (unavailable at primary center). Fastest route selected via Dijkstra algorithm.', 12, 'Nalam Central -> Medical Square (Anna Memorial Super Specialty Hospital)', 'CONFIRMED');

-- 20. SAMPLE CONSULTATION WAITING QUEUE
INSERT INTO consultation_waiting_queue (queue_id, visit_id, patient_id, severity_level, required_specialization, arrival_time, priority_score, status) VALUES
('CWQ-9001', 'VIS-2013', 'P-1013', 'Critical', 'Emergency & Critical Care', DATE_SUB(NOW(), INTERVAL 15 MINUTE), 1, 'WAITING'),
('CWQ-9002', 'VIS-2017', 'P-1017', 'Critical', 'Emergency & Critical Care', DATE_SUB(NOW(), INTERVAL 5 MINUTE), 1, 'WAITING'),
('CWQ-9003', 'VIS-2015', 'P-1015', 'Serious', 'Emergency & Critical Care', DATE_SUB(NOW(), INTERVAL 10 MINUTE), 2, 'WAITING'),
('CWQ-9004', 'VIS-2012', 'P-1012', 'Serious', 'General Medicine', DATE_SUB(NOW(), INTERVAL 25 MINUTE), 2, 'WAITING'),
('CWQ-9005', 'VIS-2004', 'P-1004', 'Minor', 'General Medicine', DATE_SUB(NOW(), INTERVAL 90 MINUTE), 3, 'WAITING'),
('CWQ-9006', 'VIS-2009', 'P-1009', 'Minor', 'Orthopedics', DATE_SUB(NOW(), INTERVAL 45 MINUTE), 3, 'WAITING'),
('CWQ-9007', 'VIS-2011', 'P-1011', 'Minor', 'General Medicine', DATE_SUB(NOW(), INTERVAL 30 MINUTE), 3, 'WAITING');

-- 21. SAMPLE RESOURCE WAITING QUEUE
INSERT INTO resource_waiting_queue (queue_id, visit_id, patient_id, severity_level, required_bed_type, required_specialization, required_resources, arrival_time, priority_score, status) VALUES
('RWQ-9101', 'VIS-2007', 'P-1007', 'Serious', 'GENERAL', 'General Medicine', 'IV Fluid Bottles, Syringes, Basic Medicines', DATE_SUB(NOW(), INTERVAL 60 MINUTE), 2, 'WAITING');

-- 22. SAMPLE RESOURCE HISTORY
INSERT INTO resource_history (resource_id, resource_name, action_type, quantity, patient_id, visit_id, timestamp, remarks) VALUES
('RES-EQ-01', 'Oxygen Cylinder', 'ALLOCATED', 1, 'P-1002', 'VIS-2002', DATE_SUB(NOW(), INTERVAL 150 MINUTE), 'Assigned to acute STEMI patient in ICU-02'),
('RES-EQ-04', 'Patient Monitor', 'ALLOCATED', 1, 'P-1002', 'VIS-2002', DATE_SUB(NOW(), INTERVAL 150 MINUTE), 'Assigned for cardiac vitals telemetry'),
('RES-EQ-01', 'Oxygen Cylinder', 'ALLOCATED', 1, 'P-1005', 'VIS-2005', DATE_SUB(NOW(), INTERVAL 4 HOUR), 'Assigned to COPD exacerbation patient'),
('RES-SP-02', 'IV Fluid Bottles', 'CONSUMED', 2, 'P-1002', 'VIS-2002', DATE_SUB(NOW(), INTERVAL 140 MINUTE), 'Administered 5% Dextrose Normal Saline');

SET FOREIGN_KEY_CHECKS = 1;
