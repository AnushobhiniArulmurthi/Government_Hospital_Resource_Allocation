# Database Documentation: Government Hospital Resource Allocation System (GHRAS)

## 1. Overview
The **Government Hospital Resource Allocation System (GHRAS)** database is designed in MySQL (InnoDB engine) adhering to 3NF relational normalization rules, complete foreign key integrity, and ACID transactional concurrency.

The database manages:
- Multi-hospital network (1 Primary Government Hospital + 3 Referral Hospitals).
- Road network weighted graph with travel-time weights.
- Doctors with specialized roles, duty statuses, and auto-generated sequence IDs (`D-001`, `D-002`, ...).
- Patients with multiple visits and full lifecycle tracking.
- Object-oriented Bed inheritance (General, ICU, Emergency).
- Object-oriented Resource inheritance (Medical Equipment, Medical Supplies, Hospital Facilities).
- Diagnostic Services & External Diagnostic Centres.
- Priority-based waiting queues (Doctor Consultation Queue & Resource Allocation Queue).
- Complete Audit, Allocation, and Discharge Histories.

---

## 2. Entity-Relationship & Table Schemas

### 2.1 Core Master Tables
| Table | Description | Primary Key | Key Foreign Keys |
|---|---|---|---|
| `hospitals` | Hospital identity, locations & base capacity | `hospital_id` | - |
| `hospital_routes` | Weighted road graph edges for Dijkstra | `route_id` | - |
| `doctor_id_sequence` | Atomic monotonic sequence counter | `id` | - |
| `doctors` | Doctor profiles, types, specializations & availability | `doctor_id` | `hospital_id` -> `hospitals.hospital_id` |
| `patients` | Patient identity records (1 record per citizen) | `patient_id` | - |
| `beds` | Bed instances by type & ward | `bed_id` | `hospital_id`, `current_patient_id`, `current_visit_id` |
| `resources` | Base resource inventory | `resource_id` | `hospital_id` |
| `medical_equipment` | Reusable medical equipment instances | `equipment_id` | `resource_id`, `hospital_id` |
| `medical_supplies` | Consumable inventory stocks | `supply_id` | `resource_id`, `hospital_id` |
| `hospital_facilities` | Operating theatres, ERs, labs, ambulances | `facility_id` | `resource_id`, `hospital_id` |
| `diagnostic_services` | In-house hospital diagnostic tests | `service_id` | `hospital_id` |
| `diagnostic_centres` | Out-of-hospital diagnostic scanning hubs | `centre_id` | - |

### 2.2 Operational & Transactional Tables
| Table | Description | Primary Key | Key Foreign Keys |
|---|---|---|---|
| `patient_visits` | Represents a single clinical visit episode | `visit_id` | `patient_id`, `assigned_doctor_id` |
| `consultations` | Doctor-patient consultation notes & vitals | `consultation_id` | `visit_id`, `patient_id`, `doctor_id` |
| `treatment_plans` | Doctor-directed treatment strategy | `treatment_plan_id` | `consultation_id`, `visit_id`, `doctor_id` |
| `diagnostic_orders` | Prescribed diagnostic tests & statuses | `order_id` | `visit_id`, `patient_id`, `doctor_id` |
| `patient_resource_requirements`| Mandatory/optional resource selections | `requirement_id` | `visit_id`, `patient_id` |
| `resource_allocations` | Active resource locking records | `allocation_id` | `visit_id`, `resource_id` |
| `doctor_assignments` | Consultation & inpatient doctor assignments | `assignment_id` | `doctor_id`, `visit_id` |
| `admissions` | Hospital inpatient admission records | `admission_id` | `visit_id`, `doctor_id`, `bed_id`, `hospital_id` |
| `referrals` | Referral transfer records based on Dijkstra | `referral_id` | `visit_id`, `from_hospital_id`, `to_hospital_id` |
| `consultation_waiting_queue`| Pre-consultation PriorityQueue snapshot | `queue_id` | `visit_id`, `patient_id` |
| `resource_waiting_queue` | Post-treatment plan PriorityQueue snapshot | `queue_id` | `visit_id`, `patient_id` |

### 2.3 History & Auditing Tables
| Table | Description | Primary Key |
|---|---|---|
| `resource_history` | Audit log of allocation, release & consumption | `history_id` |
| `discharge_history` | Discharge summary & released assets audit | `discharge_id` |

---

## 3. Concurrency & Safety Controls
1. **Row Locking (`SELECT ... FOR UPDATE`)**: Used when reserving beds, equipment, or supplies to prevent race conditions (two patients securing the same bed simultaneously).
2. **Doctor ID Generator**: Maintained in `doctor_id_sequence`. Incremented within a database transaction to guarantee no duplicates or re-use even after record deactivations.
3. **Foreign Key Cascades / Set Null**: Inpatient beds reference `current_patient_id` with `ON DELETE SET NULL`, preserving historical hospital records while ensuring clean deallocation.
