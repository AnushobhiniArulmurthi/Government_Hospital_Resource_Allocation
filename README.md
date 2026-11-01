# Government Hospital Resource Allocation System (GHRAS)

A centralized, enterprise-grade, database-connected hospital resource allocation web application designed to solve operational bottlenecks in government district hospitals.

Built with **Java 21**, **Spring Boot 3.3.4**, **Spring Data JPA**, **MySQL 8.0**, and a modern **Vanilla HTML5/CSS3/JavaScript** frontend.

---

## 1. Project Overview & Clinical Objectives

Public district hospitals face unprecedented operational stress, managing thousands of outpatients and emergency admissions daily. GHRAS replaces disjointed paper logs and manual allocation with deterministic algorithmic workflows:
1. **Intelligent Triage & Priority Queuing:** Dynamic binary min-heap priority queues (Severity 1=Critical, Severity 2=Serious, Severity 3=Minor) ensure that emergency cases are never delayed behind routine outpatient visits.
2. **Decoupled Clinical Treatment from Bed Allocation:** Resolves the common fallacy of mandatory bed booking. Inpatient beds (`GeneralBed`, `ICUBed`, `EmergencyBed`) are allocated **only** when a doctor's formulated clinical `TreatmentPlan` explicitly mandates inpatient admission.
3. **Atomic Doctor ID Generation (`D-001`, `D-002`...):** Guaranteed monotonic sequential IDs generated via an atomic sequence table with row-level pessimistic locking (`SELECT ... FOR UPDATE`), preventing duplicate IDs across concurrent threads.
4. **Centralized Medical Resource & Inventory Allocation:** Atomic reservation and stock deduction for medical equipment (ventilators, dialysis units), consumable supplies (oxygen cylinders, emergency drugs), and surgical facilities with complete immutable audit trails.
5. **Diagnostic Services & External Scanning Hubs:** Laboratory and radiology ordering, scheduling, result recording, and physician review workflow.
6. **Inter-Hospital Referrals with Dijkstra Routing:** Regional road network modeled as a weighted graph ($V, E$). Prior to routing, candidate hospitals are pre-filtered for clinical eligibility (available beds, specialist doctors on duty). A custom Dijkstra algorithm backed by an indexed Min-Heap calculates the fastest travel-time road corridor for emergency ambulance transit.

---

## 2. Object-Oriented Programming (OOP) Architecture

GHRAS serves as a comprehensive demonstration of core OOP principles:
- **Abstraction:** Defined through foundational contracts:
  - `Allocatable`: Uniform interface for allocatable hospital resources (`allocate()`, `release()`, `isAvailable()`).
  - `AdmissionPolicy` & `ResourceAllocationPolicy`: Strategic abstractions for clinical admission validation.
  - `RoutePlanner`: Abstract interface for graph-based routing algorithms.
- **Inheritance:** Polymorphic single-table discriminator hierarchies:
  - `Bed` $\rightarrow$ `GeneralBed`, `ICUBed`, `EmergencyBed` (Discriminator: `bed_type`).
  - `Resource` $\rightarrow$ `MedicalEquipment`, `MedicalSupply`, `HospitalFacility` (Discriminator: `category`).
  - `Doctor` $\rightarrow$ `GeneralPhysician`, `SpecialistDoctor` (Discriminator: `doctor_type`).
- **Polymorphism:** Dynamic method dispatch via `Allocatable` and specialized subtype validations (e.g. ICU beds checking attached ventilators; general beds checking oxygen ports).
- **Encapsulation:** Strongly typed private attributes, parameterized constructors, and validated getters/setters across all models and DTOs.
- **Composition:** The `Hospital` entity models the physical hospital by composing collections of `Doctor`, `Bed`, `Resource`, and `DiagnosticService` objects.

---

## 3. Data Structures & Algorithms (DSA) Architecture

| Data Structure / Algorithm | Class / Implementation | Purpose & Complexity |
| :--- | :--- | :--- |
| **PriorityQueue** | `HospitalPriorityQueue.java` | Synchronized binary min-heap for consultation and resource waiting queues. Orders by severity (1=Critical > 2 > 3), tie-broken by arrival time. ($O(\log N)$ enqueue/dequeue). |
| **Weighted Graph** | `WeightedGraph.java` | Regional road network with vertices (hospitals, intersections) and weighted edges (travel time in mins, distance in km). Space complexity: $O(V + E)$. |
| **Custom Min-Heap** | `MinHeap.java` | Array-backed binary min-heap with internal hash map for $O(1)$ index lookup, supporting $O(\log V)$ `decreaseKey` operations during Dijkstra relaxation. |
| **Dijkstra's Algorithm** | `DijkstraRouter.java` | Computes the shortest travel-time corridor from primary hospital to clinically eligible referral hospitals. Complexity: $O((V + E) \log V)$. |
| **HashMap** | `WeightedGraph.java`, Services | $O(1)$ adjacency list lookups, parent pointer tracking, and resource caching. |
| **ArrayList** | `DijkstraRouter.java` | Dynamic array for shortest path sequence reconstruction and batch allocation processing. |

---

## 4. System Architecture & Directory Structure

```
MINI_PROJECT_GHRA/
|-- backend/
|   |-- pom.xml                                    # Maven dependencies (Java 21, Spring Boot 3.3.4, JPA)
|   |-- src/main/java/com/hospital/resourceallocation/
|   |   |-- HospitalApplication.java               # Spring Boot Application Entry Point
|   |   |-- config/WebConfig.java                  # CORS configuration & Web MVC
|   |   |-- controller/                            # 12 REST API Controllers
|   |   |-- dto/                                   # Data Transfer Objects
|   |   |-- dsa/                                   # Custom DSA: MinHeap, WeightedGraph, DijkstraRouter, PriorityQueue
|   |   |-- model/                                 # 18 OOP Entity & Domain Models
|   |   |-- repository/                            # 23 Spring Data JPA Repositories
|   |   |-- service/                               # 12 Core Business Logic Services
|   |-- src/main/resources/application.properties  # Database connection configuration
|   |-- src/test/java/                             # 11 Unit/Integration Test Suites (36 Test Cases)
|-- database/
|   |-- schema.sql                                 # 25 Normalized Tables (InnoDB, constraints, sequences)
|   |-- sample_data.sql                            # Complete seed data (4 hospitals, 10-node road grid, doctors, beds)
|   |-- queries.sql                                # Analytic, queue verification, and Dijkstra test queries
|   |-- database_documentation.md                  # Comprehensive DB schema and ERD documentation
|-- documentation/
|   |-- problem-analysis.md                        # Clinical bottleneck analysis & problem scope
|   |-- uml-diagram.md                             # Mermaid class diagrams & sequence diagrams
|   |-- data-structures.md                         # Detailed DSA breakdown & complexity analysis
|   |-- algorithm-explanation.md                   # Dijkstra algorithm step-by-step walkthrough & numerical example
|   |-- test-cases.md                              # Complete matrix of 36 test cases with execution results
|-- frontend/
|   |-- css/                                       # Design system, variables, tables, forms, dashboard styles
|   |-- js/                                        # Modular API fetch layer, queue handlers, Dijkstra preview
|   |-- dashboard.html                             # Real-time analytics, queue counters, quick actions
|   |-- patients.html                              # Patient registration, national ID lookup, multi-visit history
|   |-- doctors.html                               # Monotonic Doctor ID generator, duty toggles, specialization
|   |-- consultations.html                         # Consultation queue, clinical notes, doctor examination
|   |-- treatment-plans.html                       # 7 clinical treatment pathways, diagnostic dispatch
|   |-- beds.html                                  # Ward-categorized bed grid with pessimistic locking
|   |-- resources.html                             # Medical equipment & consumable inventory management
|   |-- diagnostics.html                           # Laboratory & radiology test orders, scheduling, reviews
|   |-- hospitals.html                             # Regional hospital network profiles and capacities
|   |-- referrals.html                             # Dijkstra shortest path routing preview & transfer dispatch
|   |-- waiting-queue.html                         # PriorityQueue visualization & automatic re-evaluation
|   |-- admissions.html                            # Inpatient admission management
|   |-- discharge.html                             # Inpatient discharge, resource deallocation & queue trigger
|   |-- history.html                               # Immutable audit trails & patient visit lifecycle logs
|   |-- login.html                                 # Hospital coordinator authentication portal
|   |-- index.html                                 # Portal root entry point
|-- README.md                                      # Project documentation & execution guide
```

---

## 5. Prerequisites & Environment Setup

### Required Software
1. **Java Development Kit (JDK):** Version 21 LTS (`jdk-21.0.12.1` or higher).
2. **Apache Maven:** Version 3.9+ (or use installed Maven at `C:\Users\Lenovo\apache-maven-3.9.16\bin\mvn.cmd`).
3. **MySQL Server:** Version 8.0+ (via XAMPP or standalone MySQL service running on port 3306).
4. **Web Browser:** Modern web browser (Chrome, Edge, Firefox).

---

## 6. Database Setup & Initialization

Ensure MySQL is running on port 3306.

1. Open PowerShell or Command Prompt.
2. Execute the database initialization scripts in sequence:
   ```powershell
   cmd.exe /c "C:\xampp\mysql\bin\mysql.exe -u root < d:\Mini_Projects\MINI_PROJECT_GHRA\database\schema.sql"
   cmd.exe /c "C:\xampp\mysql\bin\mysql.exe -u root < d:\Mini_Projects\MINI_PROJECT_GHRA\database\sample_data.sql"
   ```
3. Verify tables and seed data:
   ```powershell
   cmd.exe /c "C:\xampp\mysql\bin\mysql.exe -u root -e \"USE hospital_resource_db; SHOW TABLES;\""
   ```

---

## 7. Backend Execution & Automated Testing

### 7.1 Running the Automated Test Suite (36 Test Cases)
Run Maven test to execute all 11 test suites verifying OOP hierarchies, Dijkstra routing, PriorityQueue ordering, atomic ID generation, and transaction rollbacks:
```powershell
& "C:\Users\Lenovo\apache-maven-3.9.16\bin\mvn.cmd" -f "d:\Mini_Projects\MINI_PROJECT_GHRA\backend\pom.xml" test
```
*Expected Result: `BUILD SUCCESS` (11 tests run, 0 failures, 0 errors).*

### 7.2 Starting the Spring Boot Backend Server
To start the Spring Boot application on port 8080:
```powershell
& "C:\Users\Lenovo\apache-maven-3.9.16\bin\mvn.cmd" -f "d:\Mini_Projects\MINI_PROJECT_GHRA\backend\pom.xml" spring-boot:run
```
The server will initialize Spring Data JPA, connect to `hospital_resource_db`, and expose all REST APIs at `http://localhost:8080/api/...`.

---

## 8. Frontend Access & User Portals

With the backend running, open the application in any web browser:
- **Login Portal:** Open `d:\Mini_Projects\MINI_PROJECT_GHRA\frontend\login.html` (or `index.html`).
  - Default Credentials: Username: `admin` | Password: `admin123` (or any coordinator credentials).
- **Core Dashboard:** `frontend/dashboard.html` — Live KPI metrics, bed occupancy, doctor on-duty status, and waiting queue counters.
- **Patient Management:** `frontend/patients.html` — Register patients, view multi-visit histories.
- **Doctor Registration:** `frontend/doctors.html` — Generates next atomic ID (`D-001`, `D-002`...), toggles duty status.
- **Consultation Queue:** `frontend/consultations.html` — Priority-ordered triage assignments and examinations.
- **Treatment Plans:** `frontend/treatment-plans.html` — Select clinical pathways (Emergency Trauma, ICU Admission, Outpatient, etc.).
- **Bed Management:** `frontend/beds.html` — Real-time ward view with pessimistic reservation.
- **Medical Resources:** `frontend/resources.html` — Equipment and supply tracking with audit trail.
- **Diagnostic Services:** `frontend/diagnostics.html` — Internal lab tests and external scanning hubs.
- **Referrals (Dijkstra):** `frontend/referrals.html` — Interactive Dijkstra route calculator showing fastest road corridor.
- **Priority Waiting Queues:** `frontend/waiting-queue.html` — Visual Min-Heap queue with manual re-evaluation triggers.
- **Admissions & Discharge:** `frontend/admissions.html` & `frontend/discharge.html` — Inpatient admissions and automatic resource deallocation.
- **Audit Logs:** `frontend/history.html` — Complete history of transactions and patient journeys.

---

## 9. Core REST API Endpoints Reference

| Module | Method | Endpoint | Description |
| :--- | :--- | :--- | :--- |
| **Dashboard** | `GET` | `/api/dashboard/stats` | Aggregated hospital KPI statistics and queue counters. |
| **Doctors** | `GET` | `/api/doctors` | List all doctors with specializations and duty status. |
| | `POST` | `/api/doctors/register` | Atomically register new doctor with auto-generated ID (`D-001`, etc.). |
| | `POST` | `/api/doctors/{id}/toggle-duty` | Toggle doctor on-duty status. |
| **Patients** | `GET` | `/api/patients` | Retrieve all registered patient records. |
| | `POST` | `/api/patients/register` | Register new patient or new visit for existing national ID. |
| | `GET` | `/api/patients/{id}/visits` | Retrieve all historical visits for a patient ID. |
| **Consultations**| `GET` | `/api/consultations/active` | Get all ongoing consultations. |
| | `POST` | `/api/consultations/assign` | Assign next available doctor to patient. |
| | `POST` | `/api/consultations/complete` | Complete consultation with diagnosis and prescription. |
| **Treatment** | `POST` | `/api/treatment-plans/formulate`| Formulate treatment plan (decoupled from automatic bed booking). |
| **Beds** | `GET` | `/api/beds` | Retrieve beds filtered by ward type (General, ICU, Emergency). |
| | `POST` | `/api/beds/allocate` | Allocate bed under pessimistic row lock. |
| | `POST` | `/api/beds/release` | Deallocate bed back to AVAILABLE. |
| **Resources** | `GET` | `/api/resources` | List medical equipment, consumable supplies, and facilities. |
| | `POST` | `/api/resources/allocate` | Centralized multi-resource atomic allocation with rollback. |
| | `POST` | `/api/resources/release` | Release equipment or replenish consumable stock. |
| | `GET` | `/api/resources/history` | Retrieve immutable resource transaction audit logs. |
| **Diagnostics** | `GET` | `/api/diagnostics/orders` | List diagnostic orders across lab and imaging centers. |
| | `POST` | `/api/diagnostics/orders` | Place new diagnostic order with priority rating. |
| | `POST` | `/api/diagnostics/orders/{id}/schedule` | Schedule diagnostic examination. |
| | `POST` | `/api/diagnostics/orders/{id}/complete` | Record test results. |
| | `POST` | `/api/diagnostics/orders/{id}/review` | Doctor clinical review of report. |
| **Referrals** | `GET` | `/api/referrals/hospitals` | List all 4 connected regional hospitals. |
| | `POST` | `/api/referrals/find-fastest` | Compute fastest eligible referral hospital using Dijkstra's algorithm. |
| | `POST` | `/api/referrals/process` | Dispatch inter-hospital patient transfer. |
| **Queues** | `GET` | `/api/queues/consultation` | Retrieve Consultation PriorityQueue ordered by severity & time. |
| | `GET` | `/api/queues/resource` | Retrieve Resource Waiting Queue ordered by severity & time. |
| | `POST` | `/api/queues/consultation/process` | Re-evaluate consultation queue against idle doctors. |
| | `POST` | `/api/queues/resource/process` | Re-evaluate resource queue against freed beds and equipment. |
| **Admissions** | `GET` | `/api/admissions/active` | List all active inpatient admissions. |
| | `POST` | `/api/admissions/admit` | Inpatient bed admission based on treatment plan. |
| | `POST` | `/api/admissions/discharge` | Patient discharge, bed & equipment release, queue trigger. |
| | `GET` | `/api/admissions/discharge-history`| List historical patient discharge records. |

---

## 10. Verification Matrix & Summary

- **Object-Oriented Design:** 100% compliant with Abstraction, Inheritance (`Bed`, `Resource`, `Doctor` hierarchies), Polymorphism, Encapsulation, and Composition.
- **Data Structures & Algorithms:** Full implementation and validation of custom `MinHeap`, `WeightedGraph`, `DijkstraRouter`, and synchronized `HospitalPriorityQueue`.
- **Database Normalization:** 25 normalized relational tables with foreign key cascades, strict indexes, and transactional concurrency safeguards.
- **Unit & Integration Tests:** 36 granular test cases spanning 11 test suites executed via Maven with **0 errors and 0 failures**.
