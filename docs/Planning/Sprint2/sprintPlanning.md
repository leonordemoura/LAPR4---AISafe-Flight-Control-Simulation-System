# Sprint Planning – Sprint 2

## 1. General Information

**Sprint Planning Date:** 15-04-2026

**Sprint Duration:** 5 weeks

**Scrum Master:** Ângelo Martins

**Sprint Master:** Sofia Carvalho

### Development Team

| Name                | Mechanographic Number |
|---------------------|-----------------------|
| **Sofia Carvalho**  | 1240898               |
| **Francisco Sá**    | 1241341               |
| **Leonor De Moura** | 1240914               |
| **Mariana Moreira** | 1240905               |

---

# 2. Sprint Goal

> Update the base infrastructure and domain model of the AISafe system, and deliver the first functional increments across all participating course units: implement core backoffice management features (users, airports, aircraft models, companies and fleets), specify and validate the Flight DSL, and build the initial flight simulation engine with real-time safety violation detection.

---

# 3. User Stories 

| US    | Description                                    | Category   | UC    | Depends On          |
|-------|------------------------------------------------|------------|-------|---------------------|
| US030 | Authentication and Authorization               | Mandatory  | EAPLI | US003               |
| US031 | Register Users                                 | Mandatory  | EAPLI | US030               |
| US032 | Disable/Enable Users                           | Extra      | EAPLI | US031               |
| US033 | List users                                     | Extra      | EAPLI | US031               |
| US041 | Register Weather Data                          | Mandatory  | EAPLI | US030, US050        |
| US050 | Register an Air Control Area                   | Mandatory  | EAPLI | US030               |
| US052 | Create an Airport                              | Mandatory  | EAPLI | US050               |
| US055 | Create an Aircraft Model                       | Mandatory  | EAPLI | US056               |
| US056 | Create an Aircraft Engine Model                | Mandatory  | EAPLI | US030               |
| US057 | Add Engine Model to Aircraft Model             | Extra      | EAPLI | US055, US056        |
| US058 | Remove Engine Model from Aircraft Model        | Extra      | EAPLI | US057               |
| US060 | Register an Air Transport Company              | Mandatory  | EAPLI | US030               |
| US061 | Add a Customer's Collaborator                  | Mandatory  | EAPLI | US031, US060        |
| US062 | List Customer's Collaborators                  | Mandatory  | EAPLI | US061               |
| US063 | Edit Customer's Collaborator                   | Extra      | EAPLI | US061               |
| US064 | Disable Customer's Collaborator                | Extra      | EAPLI | US061               |
| US070 | Add Aircraft to Air Transport Company          | Mandatory  | EAPLI | US055, US060        |
| US071 | Decommission Aircraft                          | Mandatory  | EAPLI | US070               |
| US072 | List Company Fleet                             | Mandatory  | EAPLI | US070               |
| US081 | Create a Flight Plan from a File               | Mandatory  | LPROG | US083               |
| US083 | Flight DSL Specification and Validation        | Mandatory  | LPROG | US003               |
| US100 | Simulate Flights in a Given Area               | Mandatory  | SCOMP | US081, US052, US070 |
| US101 | Capture and Process Flight Movements           | Mandatory  | SCOMP | US100               |
| US102 | Detect Aircraft Safety Violations in Real Time | Mandatory  | SCOMP | US101               |
| US103 | Synchronize Flight Execution with a Time Step  | Mandatory  | SCOMP | US100, US101        |
| US109 | Generate and Store Final Simulation Report     | Mandatory  | SCOMP | US100, US101, US102 |

---

# 4. User Stories Breakdown into Tasks

## US030 – Authentication and Authorization

**Dependencies:** US003 (project structure must be in place to integrate the auth framework)

**Tasks**

- Implement user registration documentation
- Integrate authentication/authorization framework 
- Configure roles: Admin, Backoffice Operator, Air Transport Company Collaborator, Pilot, Flight Control Operator, Weather Person
- Protect all application entry points with authorization checks
- Write unit tests for auth logic

Responsible: All Team Members
 
---

## US031 – Register Users

**Dependencies:** US030 (authentication/authorization must be configured before users can be registered and assigned roles)

**Tasks**

- Implement user registration documentation
- Implement bootstrap process for initial user seeding
- Validate unique email per domain rules
- Write unit tests

Responsible: Leonor de Moura
 
---

## US032 – Disable/Enable Users

**Dependencies:** US031 (users must exist before they can be disabled/enabled)

**Tasks**

- Implement user disablement/enablement documentation
- Implement disable/enable user use case
- Ensure disabled users cannot authenticate
- Write unit tests

Responsible: To be assigned
 
---

## US033 – List Users

**Dependencies:** US031 (users must exist to be listed)

**Tasks**

- Implement documentation
- Implement list users use case including status display
- Write unit tests

Responsible: To be assigned
 
---

## US050 – Register an Air Control Area

**Dependencies:** US030 (authorization required), US003 (project structure)

**Tasks**

- Implement air control area registration documentation
- Implement air control area registration use case
- Validate unique area code and geographic boundaries
- Implement bootstrap process
- Write unit tests

Responsible: Francisco Sá
 
---

## US052 – Create an Airport

**Dependencies:** US050 (an air control area must exist to associate the airport with)

**Tasks**

- Implement documentation
- Implement airport creation use case
- Validate unique IATA/ICAO codes and coordinates
- Implement bootstrap process
- Write unit tests

Responsible: Leonor de Moura
 
---

## US055 – Create an Aircraft Model

**Dependencies:** US056 (at least one engine model must exist to associate with the aircraft model)

**Tasks**

- Implement documentation
- Implement aircraft model creation use case
- Validate unique model name + manufacturer combination
- Associate at least one certified engine model
- Implement bootstrap process
- Write unit tests

Responsible: Leonor de Moura
 
---

## US056 – Create an Aircraft Engine Model

**Dependencies:** US030 (authorization required)

**Tasks**

- Implement documentation
- Implement engine model creation use case
- Validate unique name + manufacturer combination
- Implement bootstrap process
- Write unit tests

Responsible: Sofia Carvalho
 
---

## US057 – Add Engine Model to Aircraft Model

**Dependencies:** US055 (aircraft model must exist), US056 (engine model must exist)

**Tasks**

- Implement documentation
- Implement add engine model to aircraft model use case
- Validate engine type compatibility
- Prevent duplicate engine model associations
- Write unit tests

Responsible: To be assigned
 
---

## US058 – Remove Engine Model from Aircraft Model

**Dependencies:** US057 (engine must have been added before it can be removed)

**Tasks**

- Implement documentation
- Implement remove engine model use case
- Block removal if aircraft instances are using the engine model
- Enforce minimum of one certified engine model
- Write unit tests

Responsible: To be assigned
 
---

## US060 – Register an Air Transport Company

**Dependencies:** US030 (authorization required)

**Tasks**

- Implement documentation
- Implement air transport company registration use case
- Validate unique company name, IATA (2 letters) and ICAO (2–3 letters) codes
- Implement bootstrap process
- Write unit tests

Responsible: Sofia Carvalho
 
---

## US061 – Add a Customer's Collaborator

**Dependencies:** US031 (collaborator is also a system user, so user registration must be available), US060 (the customer company must exist)

**Tasks**

- Implement documentation
- Implement add collaborator use case
- Create corresponding system user for the collaborator
- Implement bootstrap process
- Write unit tests

Responsible: Sofia Carvalho
 
---

## US062 – List Customer's Collaborators

**Dependencies:** US061 (collaborators must exist to be listed)

**Tasks**

- Implement documentation
- Implement list collaborators use case (exclude disabled)
- Write unit tests

Responsible: Francisco Sá
 
---

## US063 – Edit Customer's Collaborator

**Dependencies:** US061 (collaborator must exist)

**Tasks**

- Implement documentation
- Implement edit collaborator use case (email and phone number only)
- Write unit tests

Responsible: To be assigned
 
---

## US064 – Disable Customer's Collaborator

**Dependencies:** US061 (collaborator must exist)

**Tasks**

- Implement documentation
- Implement disable collaborator use case
- Ensure disabled collaborator cannot access the system
- Write unit tests

Responsible: To be assigned
 
---

## US070 – Add Aircraft to Air Transport Company

**Dependencies:** US055 (aircraft model must exist), US060 (company must exist)

**Tasks**

- Implement documentation
- Implement add aircraft use case
- Validate unique registration number
- Validate seat count does not exceed model capacity
- Write unit tests

Responsible: Mariana Moreira
 
---

## US071 – Decommission Aircraft

**Dependencies:** US070 (aircraft must have been added before it can be decommissioned)

**Tasks**

- Implement documentation
- Implement decommission aircraft use case
- Block decommissioning if pending flights exist
- Write unit tests

Responsible: Mariana Moreira
 
---

## US072 – List Company Fleet

**Dependencies:** US070 (aircraft must have been added to the fleet)

**Tasks**

- Implement documentation
- Implement list fleet use case
- Write unit tests

Responsible: Mariana Moreira
 
---

## US041 – Register Weather Data

**Dependencies:** US030 (authorization required), US050 (air control area must exist to associate weather data with)

**Tasks**

- Implement documentation
- Implement register weather data use case for a specific air control area
- Validate data format and area association
- Write unit tests

Responsible: Francisco Sá

---

## US081 – Create a Flight Plan from a File

**Dependencies:** US083 (DSL grammar and validation must be implemented before a file can be parsed and imported)

**Tasks**

- Implement file ingestion and format detection
- Invoke lexical, syntactic and semantic validation pipeline (from US083)
- Map validated DSL constructs to domain flight plan objects
- Report meaningful errors for invalid files
- Write unit tests

Responsible: Mariana and Sofia
 
---

## US083 – Flight DSL Specification and Validation

**Dependencies:** US003 (project structure must support ANTLR integration)

**Tasks**

- Document informal lexical and syntactic specification of the DSL
- Define formal ANTLR grammar (lexer and parser rules)
- Implement lexical and syntactic validation
- Implement parse tree traversal (at least one visitor and one listener)
- Build internal representation (AST or domain objects)
- Implement semantic validation rules
- Produce clear error messages with line/column information
- Write unit tests

Responsible: Leonor and Francisco
 
---

## US100 – Simulate Flights in a Given Area

**Dependencies:** US081 (flight plans must be importable), US052 (airports must exist), US070 (aircraft must exist in fleets)

**Tasks**

- Implement simulation entry point in C
- Fork a child process per flight
- Set up pipes between main process and flight processes
- Validate simulation parameters (time range, area, thresholds)
- Write unit tests

Responsible: Leonor de Moura
 
---

## US101 – Capture and Process Flight Movements

**Dependencies:** US100 (simulation infrastructure must be in place)

**Tasks**

- Implement position update sending from each flight process via pipe
- Implement main process tracking of aircraft positions over time
- Store past positions for future safety analysis
- Write unit tests

Responsible: Francisco Sá
 
---

## US102 – Detect Aircraft Safety Violations in Real Time

**Dependencies:** US101 (aircraft positions must be tracked before violations can be detected)

**Tasks**

- Implement continuous proximity monitoring
- Implement SIGUSR1 signal dispatch on violation detection
- Implement signal handler in flight processes
- Implement early termination logic when violations exceed threshold
- Write unit tests

Responsible: Sofia Carvalho
 
---

## US103 – Synchronize Flight Execution with a Time Step

**Dependencies:** US100 (simulation processes must be running), US101 (movement capture must be implemented)

**Tasks**

- Implement step-by-step simulation progression
- Ensure each flight process sends position updates at defined time intervals
- Implement main process barrier to wait for all updates before advancing
- Write unit tests

Responsible: Mariana Moreira
 
---

## US109 – Generate and Store Final Simulation Report

**Dependencies:** US100 (simulation processes must be running), US101 (movement capture must be implemented), US102 (safety violation detection must be implemented), US103 (time step synchronization must be implemented)

**Tasks**

- Implement documentation
- Implement report generation logic to summarize flight paths, violations, and outcomes
- Write unit tests

Responsible: All team members
 
---

# 5. Definition of Done (DoD)

A User Story is considered completed when:

- Code is implemented
- Code compiles successfully
- Unit tests are implemented and peer reviewed
- Build passes in the CI pipeline
- Documentation is updated
- Commit is pushed to the repository

---

# 6. Identified Risks

At this point of the sprint, we haven't identified any risks.

---

# 7. Additional Notes

- The user stories that were not assigned to a specific team member will be distributed later on, specifically the extra user stories that will be distributed after the mandatory ones are completed. The distribution will be done based on the progress of each team member and their workload.