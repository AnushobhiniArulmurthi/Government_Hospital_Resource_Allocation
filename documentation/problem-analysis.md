# Problem Analysis: Government Hospital Resource Allocation System (GHRAS)

---

## 1. Executive Summary & Operational Context

Government hospitals in developing public healthcare ecosystems bear an immense patient load, often catering to thousands of outpatients and hundreds of emergency admissions daily. Unlike private healthcare facilities governed by upfront billing and appointment caps, public district hospitals operate under an open-access mandate where refusal of emergency care is legally and ethically unacceptable.

However, existing operational workflows in government district hospitals remain heavily reliant on paper registers, fragmented department logs, and subjective manual allocation. This systemic friction results in severe operational failures:
1. **Critical Triage Bottlenecks:** Triage priority (such as severe chest pain or trauma) is often lost in chaotic outpatient registration queues where stable patients with mild symptoms wait alongside deteriorating patients.
2. **Subjective Doctor Assignment:** Doctor consultations are assigned without systematic balancing of clinical load or specialized requirements, leading to idle doctors in one department while others face extreme backlogs.
3. **Inefficient Bed Allocation:** Patients are frequently allocated beds regardless of clinical necessity, leading to artificial saturation of General, ICU, and Emergency beds, while patients in genuine need of intensive care are turned away.
4. **Supply Stockouts & Unaudited Resource Use:** Critical medical equipment (ventilators, defibrillators, dialysis machines) and life-saving consumables (oxygen cylinders, emergency drugs) lack centralized atomic reservation, leading to double-allocation and untracked depletion.
5. **Chaotic Inter-Hospital Referrals:** When a district hospital exhausts its ICU beds or specialist surgical capacity, patients are transferred arbitrarily without real-time knowledge of receiving hospital capacities or road transit times, resulting in preventable in-transit fatalities.

The **Government Hospital Resource Allocation System (GHRAS)** addresses these systemic failures through a computer science-driven, centralized resource allocation platform integrating modern Software Engineering patterns (Object-Oriented Programming, Polymorphic Hierarchies, Abstraction) and optimized Data Structures and Algorithms (Priority Queues, HashMaps, Custom Weighted Graphs, and Dijkstra's Shortest Path Algorithm).

---

## 2. Key Problem Domains & Structural Inefficiencies

```
+--------------------------------------------------------------------------------------------------+
|                           CURRENT PUBLIC HEALTHCARE SYSTEM INEFFICIENCIES                       |
+--------------------------------------------------------------------------------------------------+
| 1. Registration & Triage Chaos      --> First-Come-First-Served (FCFS) ignores clinical urgency.  |
| 2. Subjective Doctor Allocation     --> No dynamic matching of doctor status & patient condition. |
| 3. Mandatory Bed Misconception      --> Bed booked for outpatients who only need oral prescription.|
| 4. Inventory Concurrency Glitches   --> Simultaneous booking of single ventilator or oxygen unit. |
| 5. Ad-Hoc Referral Transfers        --> Transfers dispatched without verifying destination status. |
| 6. Unconnected Road Corridors       --> No algorithmic computation of fastest emergency transit.   |
+--------------------------------------------------------------------------------------------------+
```

### 2.1 Triage & Waiting Queue Mismanagement
* **Deficiency:** Standard registration desks operate under First-Come-First-Served (FCFS). A patient presenting with acute ST-segment elevation myocardial infarction (STEMI) or head trauma may queue behind multiple patients seeking routine prescription renewals for mild seasonal ailments.
* **Algorithmic Solution:** GHRAS introduces a **Deterministic 3-Tier Priority Queue** backed by a binary Min-Heap. Patients are triaged into:
  - **Severity 1 (Critical):** Immediate resuscitation / emergency intervention required.
  - **Severity 2 (Serious):** Urgent clinical assessment required; high risk of deterioration.
  - **Severity 3 (Minor):** Stable routine outpatient checkup.
  The queue comparator strictly prioritizes Severity 1 over Severity 2 and 3, using the patient registration timestamp solely as a secondary tie-breaker.

### 2.2 Decoupling Clinical Treatment from Automatic Bed Allocation
* **Deficiency:** A common misconception in legacy hospital management software is allocating an inpatient bed upon admission of every registered patient. This depletes the hospital's fixed bed capacity within hours of daily opening.
* **Algorithmic Solution:** GHRAS enforces an OOP **Clinical Admission Policy (`AdmissionPolicy`)**. A patient is registered, triaged, and assigned to a doctor for clinical examination (`Consultation`). An inpatient bed (`GeneralBed`, `ICUBed`, or `EmergencyBed`) is **only** allocated if the doctor's formulated `TreatmentPlan` explicitly specifies an inpatient pathway requiring bed admission. If the patient is prescribed outpatient medication or diagnostic testing, the consultation completes and resources are saved for genuinely ill patients.

### 2.3 Resource Race Conditions & Concurrency Hazards
* **Deficiency:** In high-concurrency environments with multiple duty coordinators, two doctors may simultaneously attempt to allocate the last available mechanical ventilator or ICU bed.
* **Algorithmic Solution:** GHRAS employs **Pessimistic Locking (`SELECT ... FOR UPDATE`)** and Spring Data JPA transaction boundaries (`@Transactional`). In addition, doctor identifiers are minted monotonically using an atomic sequence table (`doctor_id_sequence`) under row-level exclusion to guarantee zero duplicate keys across distributed threads.

### 2.4 Inter-Hospital Network Referrals & Ambulance Routing
* **Deficiency:** District hospitals have finite ICU beds and specialist doctors. When a regional hospital (such as Nalam District GH) reaches resource saturation, doctors must refer patients to neighboring hospitals. Referring patients without clinical verification or optimal routing causes ambulances to arrive at hospitals that also lack ICU beds or specialist surgeons.
* **Algorithmic Solution:** GHRAS represents the regional hospital network as a **Directed Weighted Graph $G = (V, E)$**, where vertices $V$ represent regional hospital nodes and road intersections, and edges $E$ represent road corridors weighted by actual travel time (minutes) and distance (km). Prior to route computation, the system pre-filters candidate hospitals for **Clinical Eligibility** (verifying that the target hospital possesses available beds of the required category, an on-duty specialist of the required discipline, and required equipment). A custom **Dijkstra Algorithm** backed by an indexed **Min-Heap** then computes the fastest transit corridor from the originating hospital.

---

## 3. System Scope & Stakeholders

| Stakeholder Role | Responsibilities in GHRAS | Key Interfaces / Modules |
| :--- | :--- | :--- |
| **Triage Nurse / Receptionist** | Patient registration, national ID lookup, vital signs recording, triage severity categorization (1, 2, 3). | Patient Registration, Triage Desk |
| **Attending Doctor / Specialist** | Clinical examination, diagnosis formulation, treatment plan selection, prescribing tests, ordering admission or discharge. | Consultation Queue, Treatment Planner |
| **Hospital Operations Coordinator** | Real-time monitoring of bed capacity, medical inventory, oxygen supply, and waiting queue re-evaluation. | Live Dashboard, Beds & Inventory Grid |
| **Diagnostic Lab Technician** | Scheduling laboratory tests, digital imaging (X-Ray, CT, MRI), entering diagnostic findings. | Diagnostic Services & Hubs |
| **Referral & Dispatch Officer** | Managing inter-hospital transfers, running Dijkstra route calculator, ambulance dispatch. | Referral Management Module |
| **Medical Superintendent** | System-wide oversight, clinical audit trails, resource utilization analytics, disaster triage management. | History Logs, Analytical Dashboard |

---

## 4. Architectural Non-Functional Requirements

1. **ACID Transaction Guarantees:** All multi-step allocations (deducting inventory, assigning beds, updating patient visit status) must succeed or fail as an atomic transaction. Partial allocations are strictly rolled back.
2. **Deterministic Priority Queue Invariant:** A Severity 1 patient arriving at time $t_2$ must preemptively be served before a Severity 2 patient who arrived at time $t_1$ ($t_1 < t_2$).
3. **Pessimistic Concurrency Control:** Bed and doctor states transition under strict row-level database locks to prevent dirty reads and double allocations.
4. **Sub-Second Algorithmic Response Time:** Dijkstra route computation across the hospital road network and PriorityQueue extract operations must complete within $< 10$ milliseconds.
5. **Audit Trail Immutability:** All resource movements (allocation, consumption, release, disposal) are logged to an append-only `resource_history` table for complete regulatory compliance.
