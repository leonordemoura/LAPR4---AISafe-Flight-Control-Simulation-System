# Sprint Planning – Sprint 3

## 1. General Information

**Sprint Planning Date:** 17-05-2026  
**Sprint Duration:** 4 weeks  
**Scrum Master:** Ângelo Martins
**Sprint Master:** Leonor De Moura

### Development Team

| Name                | Mechanographic Number |
|---------------------|-----------------------|
| Sofia Carvalho      | 1240898               |
| Francisco Sá        | 1241341               |
| Leonor De Moura     | 1240914               |
| Mariana Moreira     | 1240905               |

---

## 2. Sprint Goal

Deliver core system functionalities for flight operations, weather integration, remote access, simulation infrastructure, and domain-specific validation mechanisms, ensuring system scalability and distributed execution support.

---

## 3. User Stories

| US      | Description                                              |
|---------|----------------------------------------------------------|
| US042   | Import bulk weather data                                 |
| US043   | Consult weather data                                     |
| US075   | Add a pilot                                              |
| US076   | List pilot roster                                        |
| US077   | Remove a pilot (deactivate)                              |
| US073   | Create a flight route                                    |
| US080   | Create a flight plan                                     |
| US082   | Insert weather data in a flight                          |
| US085   | Test/validate flight plan (C DSL validation)             |
| US121   | Create flight plan from file (DSL-based import)          |
| US120   | Flight DSL specification and validation                  |
| US78    | Air Transport Company collaborator remote access         |
| US86    | Pilot remote access                                      |
| US44    | Weather person remote access                             |
| US90    | External logging of remote accesses                      |
| US91    | Remote access log visualization                          |
| US105   | Initialize hybrid simulation environment (shared memory) |
| US106   | Function-specific threads in parent process              |
| US107   | Notify report thread via condition variables             |
| US108   | Step-by-step simulation synchronization                  |
| US109   | Generate and store final simulation report               |

---

## 4. User Stories Breakdown into Tasks

---

### US042 – Import Bulk Weather Data

- Analyze external weather data formats and providers
- Implement CSV import functionality
- Design extensible ingestion architecture for multiple providers
- Store normalized weather data in the system

**Responsible:** Mariana

---

### US043 – Consult Weather Data

- Implement query system for weather by date and air control area
- Integrate weather filtering and retrieval services

**Responsible:** Mariana

---

### US075 – Add a Pilot

- Create pilot entity and validations
- Ensure pilot certification for aircraft models
- Implement pilot creation service

**Responsible:** Leonor

---

### US076 – List Pilot Roster

- Implement retrieval of company pilots
- Provide filtered roster view

**Responsible:** Leonor

---

### US077 – Remove (Deactivate) Pilot

- Implement pilot deactivation logic
- Ensure pilots with assigned flight plans cannot be deactivated
- Enforce business rules and constraints

**Responsible:** Leonor

---

### US073 – Create Flight Route

- Implement route entity between airports
- Enforce route naming rules (2 letters + up to 4 numbers)
- Ensure route uniqueness validation

**Responsible:** Sofia

---

### US080 – Create Flight Plan

- Implement flight plan creation workflow
- Associate aircraft, pilot, departure time, and fuel
- Set initial status to "draft"
- Enforce multi-step validation process

**Responsible:** Francisco

---

### US082 – Insert Weather Data in Flight Plan

- Allow weather attachment to flight plans
- Invalidate previous test results when weather changes

**Responsible:** Mariana

---

### US085 – Test/Validate Flight Plan (C DSL)

- Validate flight plan using DSL rules
- Implement C-based validation component
- Ensure correctness of test execution pipeline

**Responsible:** All team members

---

### US121 – Create Flight Plan from File

- Implement file import mechanism for flight plans
- Validate file format using DSL (US120)
- Handle multiple file formats
- Provide meaningful error messages

**Responsible:** Francisco e Leonor

---

### US120 – Flight DSL Specification and Validation

- Define lexical and syntactic specification
- Implement ANTLR grammar
- Implement AST / domain model generation
- Perform semantic validation
- Provide error handling and reporting

**Responsible:** Sofia e Mariana

---

### US78 – Air Transport Company Remote Access

- Implement TCP-based client-server communication
- Ensure no direct database access from client
- Enforce authentication and authorization
- Expose all collaborator functionalities remotely

**Responsible:** Sofia

---

### US86 – Pilot Remote Access

- Implement TCP client for pilot operations
- Secure authentication and authorization
- Restrict operations via network interface

**Responsible:** Francisco

---

### US44 – Weather Person Remote Access

- Implement TCP client for weather data upload
- Enforce secure access and authentication
- Restrict database access outside server layer

**Responsible:** Francisco

---

### US90 – External Logging of Remote Accesses

- Implement UDP-based log transmission
- Log login/logout/disconnect events
- Include timestamp, username, IP, port, service ID
- Integrate with remote logging server

**Responsible:** Mariana

---

### US91 – Remote Access Log Visualization

- Implement HTTP server for log visualization
- Create live-updating dashboard (AJAX)
- Display active users and recent events

**Responsible:** Leonor

---

### US105 – Hybrid Simulation Environment Initialization

- Implement multi-threaded parent process (C)
- Spawn child processes for flights
- Configure shared memory segment
- Use semaphores for synchronization

**Responsible:** Leonor

---

### US106 – Simulation Threads in Parent Process

- Implement safety violation detection thread
- Implement report generation thread
- Add additional required threads
- Use mutexes and condition variables

**Responsible:** Francisco

---

### US107 – Condition Variable Notification System

- Detect safety violations in shared memory
- Signal report thread using condition variables
- Ensure thread-safe synchronization

**Responsible:** Sofia

---

### US108 – Simulation Step Synchronization

- Implement semaphore-based step control
- Ensure synchronized progression of simulation steps

**Responsible:** Mariana

---

### US109 – Final Simulation Report Generation

- Aggregate simulation results
- Include flight statuses and safety violations
- Export report to file
- Indicate final validation result (pass/fail)

**Responsible:** All team members

---

## 5. Definition of Done (DoD)

A User Story is considered complete when:

- Code is implemented and compiles successfully
- Unit tests are implemented and reviewed
- CI pipeline builds successfully
- Documentation is updated
- Code is peer-reviewed
- Changes are merged into the main branch

---

## 6. Identified Risks

- High complexity of concurrent simulation (threads, semaphores, shared memory)
- Integration complexity between DSL, simulation, and remote access systems
- Potential delays due to C-based components (testing and debugging complexity)
- Risk of integration issues between distributed TCP/UDP services