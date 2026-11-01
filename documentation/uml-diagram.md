# Unified Modeling Language (UML) Diagrams: GHRAS

---

## 1. Object-Oriented Architecture Overview

The **Government Hospital Resource Allocation System (GHRAS)** is designed strictly adhering to the fundamental principles of Object-Oriented Programming (OOP):
- **Abstraction:** Defined through core business interfaces such as `Allocatable`, `AdmissionPolicy`, `ResourceAllocationPolicy`, and `RoutePlanner`.
- **Inheritance:** Polymorphic class hierarchies for **Beds** (`Bed` $\rightarrow$ `GeneralBed`, `ICUBed`, `EmergencyBed`), **Resources** (`Resource` $\rightarrow$ `MedicalEquipment`, `MedicalSupply`, `HospitalFacility`), and **Doctors** (`Doctor` $\rightarrow$ `GeneralPhysician`, `SpecialistDoctor`).
- **Polymorphism:** Runtime dynamic method dispatch through the `Allocatable` interface (`allocate()`, `release()`, `isAvailable()`, `getResourceIdentifier()`), and custom validation routines across different bed and resource subtypes.
- **Encapsulation:** Strongly typed private fields with validated public accessors and mutators across all domain models and DTOs.
- **Composition:** The `Hospital` entity models the physical hospital by composing collections of `Doctor`, `Bed`, `Resource`, and `DiagnosticService` instances.

---

## 2. Core Class Diagram (Mermaid)

```mermaid
classDiagram
    class Allocatable {
        <<interface>>
        +allocate(String patientId, String visitId) boolean
        +release() boolean
        +isAvailable() boolean
        +getResourceIdentifier() String
    }

    class Bed {
        <<abstract>>
        -String bedId
        -String bedNumber
        -String wardNumber
        -String bedType
        -String status
        -String hospitalId
        -String currentPatientId
        -String currentVisitId
        -LocalDateTime allocatedTime
        +allocate(String patientId, String visitId) boolean
        +release() boolean
        +isAvailable() boolean
        +getBedId() String
    }
    Allocatable <|.. Bed

    class GeneralBed {
        -boolean oxygenPortAvailable
        -boolean ivStandAvailable
        +allocate(String patientId, String visitId) boolean
    }
    class ICUBed {
        -boolean ventilatorAttached
        -boolean cardiacMonitorAttached
        -boolean suctionUnitAvailable
        +allocate(String patientId, String visitId) boolean
    }
    class EmergencyBed {
        -boolean traumaKitAvailable
        -boolean defibrillatorNearby
        +allocate(String patientId, String visitId) boolean
    }
    Bed <|-- GeneralBed
    Bed <|-- ICUBed
    Bed <|-- EmergencyBed

    class Resource {
        <<abstract>>
        -String resourceId
        -String resourceName
        -String category
        -String hospitalId
        -String status
        -int totalQuantity
        -int availableQuantity
        +allocate(String patientId, String visitId) boolean
        +release() boolean
        +isAvailable() boolean
        +deductQuantity(int qty) boolean
        +replenishQuantity(int qty) void
    }
    Allocatable <|.. Resource

    class MedicalEquipment {
        -String serialNumber
        -String modelNumber
        -String maintenanceSchedule
        +allocate(String patientId, String visitId) boolean
    }
    class MedicalSupply {
        -String batchNumber
        -LocalDate expiryDate
        -String unitOfMeasure
        -int reorderLevel
        +isExpired() boolean
    }
    class HospitalFacility {
        -String roomNumber
        -String floorLevel
        -int operationalCapacity
    }
    Resource <|-- MedicalEquipment
    Resource <|-- MedicalSupply
    Resource <|-- HospitalFacility

    class Doctor {
        <<abstract>>
        -String doctorId
        -String fullName
        -String specialization
        -String doctorType
        -String department
        -boolean onDuty
        -boolean isAvailable
        -int maxDailyPatients
        -int currentActivePatients
        +allocate(String patientId, String visitId) boolean
        +release() boolean
        +isAvailable() boolean
    }
    Allocatable <|.. Doctor

    class GeneralPhysician {
        -String opdClinicRoom
        -boolean acceptsPediatric
    }
    class SpecialistDoctor {
        -String subSpecialtyCertification
        -boolean otPrivileges
    }
    Doctor <|-- GeneralPhysician
    Doctor <|-- SpecialistDoctor

    class Hospital {
        -String hospitalId
        -String hospitalName
        -String locationName
        -int totalBeds
        -boolean isPrimary
        -List~Doctor~ doctors
        -List~Bed~ beds
        -List~Resource~ resources
    }
    Hospital *-- Doctor
    Hospital *-- Bed
    Hospital *-- Resource

    class Patient {
        -String patientId
        -String nationalId
        -String fullName
        -LocalDate dateOfBirth
        -String gender
        -String bloodGroup
        -String contactNumber
    }

    class PatientVisit {
        -String visitId
        -String patientId
        -String triageCategory
        -int triageSeverity
        -String status
        -LocalDateTime registrationTime
    }
    Patient "1" *-- "*" PatientVisit

    class Consultation {
        -String consultationId
        -String visitId
        -String doctorId
        -String clinicalNotes
        -String prescription
        -String status
    }
    PatientVisit "1" *-- "1" Consultation

    class TreatmentPlan {
        -String planId
        -String visitId
        -String doctorId
        -String treatmentPathway
        -boolean requiresAdmission
        -String requiredBedType
    }
    PatientVisit "1" *-- "1" TreatmentPlan

    class Admission {
        -String admissionId
        -String visitId
        -String bedId
        -String status
        -LocalDateTime admissionTime
        -LocalDateTime dischargeTime
    }
    PatientVisit "1" *-- "0..1" Admission
```

---

## 3. Sequence Diagram 1: Patient Triage & Consultation Lifecycle

```mermaid
sequenceDiagram
    autonumber
    actor Nurse as Triage Nurse
    actor Doctor as Attending Doctor
    participant PS as PatientService
    participant QS as WaitingQueueService
    participant CS as ConsultationService
    participant DB as MySQL Database

    Nurse->>PS: registerPatientVisit(PatientDTO)
    PS->>DB: INSERT INTO patients (or fetch existing by nationalId)
    PS->>DB: INSERT INTO patient_visits (status: 'TRIAGED')
    PS->>QS: enqueueConsultation(visitId, triageSeverity)
    QS->>DB: INSERT INTO consultation_waiting_queue (Min-Heap priority)
    Note over QS: Highest priority: Severity 1 > 2 > 3 (Tie-breaker: arrival timestamp)
    
    QS->>CS: checkAvailableDoctors()
    alt Doctor Available on Duty
        CS->>DB: SELECT * FROM doctors WHERE is_available=1 FOR UPDATE
        CS->>DB: INSERT INTO consultations (status: 'IN_PROGRESS')
        CS->>DB: UPDATE doctors SET is_available=0
        Doctor->>CS: completeConsultation(notes, diagnosis, prescription)
        CS->>DB: UPDATE consultations SET status='COMPLETED'
        CS->>DB: UPDATE doctors SET is_available=1
        CS->>QS: triggerProcessConsultationQueue()
    else All Doctors Busy
        Note over QS: Patient waits at top of PriorityQueue
    end
```

---

## 4. Sequence Diagram 2: Inpatient Admission, Bed Allocation & Discharge

```mermaid
sequenceDiagram
    autonumber
    actor Doctor as Attending Doctor
    participant TPS as TreatmentPlanService
    participant ADS as AdmissionDischargeService
    participant BS as BedService
    participant RS as ResourceService
    participant QS as WaitingQueueService
    participant DB as MySQL Database

    Doctor->>TPS: formulateTreatmentPlan(requiresAdmission: TRUE, bedType: 'ICU')
    TPS->>DB: INSERT INTO treatment_plans
    TPS->>ADS: admitPatient(visitId, bedType: 'ICU')
    
    ADS->>BS: allocateBed(hospitalId: 'H-001', bedType: 'ICU', visitId)
    BS->>DB: SELECT * FROM beds WHERE bed_type='ICU' AND status='AVAILABLE' FOR UPDATE
    alt ICU Bed Available
        BS->>DB: UPDATE beds SET status='OCCUPIED', current_visit_id=visitId
        ADS->>DB: INSERT INTO admissions (status: 'ADMITTED')
        ADS->>DB: UPDATE patient_visits SET status='ADMITTED'
    else No ICU Beds Available
        ADS->>QS: enqueueResource(visitId, 'ICU_BED', severity)
        QS->>DB: INSERT INTO resource_waiting_queue
    end

    Note over ADS: Patient undergoes clinical inpatient care...

    Doctor->>ADS: dischargePatient(visitId, dischargeSummary)
    ADS->>BS: releaseBed(bedId)
    BS->>DB: UPDATE beds SET status='AVAILABLE', current_visit_id=NULL
    ADS->>RS: releaseEquipmentAndDeductSupplies(visitId)
    ADS->>DB: INSERT INTO discharge_history
    ADS->>DB: UPDATE admissions SET status='DISCHARGED'
    ADS->>QS: processResourceQueue()
    Note over QS: Deallocated ICU Bed immediately pops next waiting patient!
```

---

## 5. Sequence Diagram 3: Inter-Hospital Referral with Dijkstra Routing

```mermaid
sequenceDiagram
    autonumber
    actor Coordinator as Hospital Coordinator
    participant RefS as ReferralService
    participant Router as DijkstraRouter
    participant Graph as WeightedGraph
    participant DB as MySQL Database

    Coordinator->>RefS: findFastestReferral(visitId, requiredBedType: 'ICU', specialization: 'CARDIOLOGIST')
    RefS->>DB: Query candidate referral hospitals (H-002, H-003, H-004)
    RefS->>RefS: Filter hospitals by Clinical Eligibility:
    Note over RefS: Must have available ICU bed AND on-duty Cardiologist
    
    RefS->>Router: findShortestRoute(sourceNode: 'NODE_H001', eligibleHospitalNodes)
    Router->>Graph: Query adjacency edges (distanceKm, travelTimeMinutes)
    Note over Router: Run Dijkstra with custom MinHeap keyed on travel time
    Router-->>RefS: RouteResult(Destination: H-003, TravelTime: 22 mins, RoutePath)
    
    Coordinator->>RefS: processReferral(visitId, toHospitalId: 'H-003', reason)
    RefS->>DB: INSERT INTO referrals (status: 'ACCEPTED', travel_time: 22)
    RefS->>DB: UPDATE patient_visits SET status='REFERRED'
    RefS-->>Coordinator: Referral confirmed & Ambulance road corridor dispatched
```
