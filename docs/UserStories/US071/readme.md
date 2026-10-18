# US071 - Decommission an aircraft

## 1. Context

* As an Air Transport Company Collaborator, I want to retire an aircraft from my company’s fleet.
* This task is being developed for the first time in the sprint 2, and it has dependencies with the following requirements:
    * US070 - Add an Aircraft to an Air Transport Company
    * US080 - Create a Flight Plan
  
### 1.1 List of issues

**Main Issue:** Issue #52

**Analysis:** Issue#71

**Design and Unit Tests:** Issue #90

**Implement:** Issue #103

## 2. Requirements

**US071 -** As an Air Transport Company Collaborator, I want to retire an aircraft from my company’s fleet. It’s not possible to create flights for a retired aircraft, nor it is possible to retire an aircraft that has pending flights registered in the system.

### Acceptance Criteria:

- US071.1 The system must allow an aircraft to be marked as DECOMMISSIONED.
- US071.2 It must not be possible to retire an aircraft that has pending flights.
- US071.3 A retired aircraft must not be used to create new flight plans.
- US071.4 The aircraft must belong to the company performing the operation.
- US071.5 The system must validate that the aircraft exists before attempting to retire it.

### Dependencies/References:

Regarding this requirement, we understand that it relates to:

- **US055 - Create an Aircraft Model**: an aircraft must reference an existing model, which defines capacity and characteristics.
- **US060 - Register an Air Transport Company**: the aircraft is added to a specific company’s fleet.
- **Section 3.2**: defines aircraft attributes such as registration, model, and cabin configuration.

## 3. Analysis

The domain model must support lifecycle management of Aircraft entities, specifically their decommissioning.

![Excerpt of Air Transport Company, Aircraft and Flight in the Domain Model](excerpt_domainModel/excerpt_DecommissionAircraft.png)

| Attribute           | Description                                | Constraints             |
|---------------------|--------------------------------------------|-------------------------|
| Registration Number | Unique identifier of the aircraft          | Must be unique          |
| Operational Status  | Indicates if the aircraft is active or not | ACTIVE / DECOMMISSIONED |
| Company             | Owner company                              | Must exist              |

**Key business rules:**
- An aircraft cannot be retired if it has pending flights.
- A retired aircraft cannot be used to create new flights.
- Only aircraft belonging to the company can be retired.

**Relationship with the domain:**
The Aircraft aggregate is responsible for its lifecycle and state transitions. The AirTransportCompany provides the ownership context via CompanyId, but does not directly manage aircraft state. Flight validation is performed indirectly through company routes and associated flights.

## 4. Design

*In these sections, the team should present the solution design that was adopted to solve the requirement. This should include, at least, a diagram of the realization of the functionality (e.g., sequence diagram) and the specification of the main tests used to validate the functionality. A class diagram may be useful to describe the classes involved.*

### 4.1. Realization

The design of the solution for this requirement is based on the following class diagram:

![Decommission Aircraft Class Diagram](classDiagram/DecommissionAircraft_Diagram.png)

The following sequence diagram illustrates the realization of US071 — Decommission an Aircraft:

![Sequence Diagram - Decommission an aircraft](sequenceDiagram/DecommissionAircraft_SequenceDiagram.png)

#### Main classes and responsibilities (US071 - Decommission Aircraft)

- `Aircraft` → Aggregate Root responsible for enforcing aircraft lifecycle invariants and holding aircraft data such as registration number, seat configuration, operational status, country of registration, associated model, and pending/completed flights. It contains the business rule that prevents decommissioning when the aircraft has pending flights.
- `AircraftModel` → Aggregate Root responsible for validating model-related constraints, such as engine compatibility and maximum seat capacity. It is referenced by the Aircraft but not responsible for lifecycle operations.
- `AirTransportCompany` → Aggregate Root responsible for managing the company fleet and ensuring that aircraft belong to the correct company before allowing decommission operations.
- `AircraftRepository` → Persistence interface responsible for retrieving, validating existence, and persisting Aircraft entities, including updating operational status (ACTIVE → DECOMMISSIONED).
- `FlightRepository` → Persistence interface responsible for retrieving flights associated with a specific aircraft, in order to validate if there are pending flights before allowing decommission.
- `AirTransportCompanyRepository` → Persistence interface responsible for retrieving the current company context and validating aircraft ownership.
- `PersistenceContext` → Provides access to the repository factory used to obtain repository instances.
- `RepositoryFactory` → Factory responsible for creating repository implementations for Aircraft, Flight, and Company aggregates.
- `DecommissionAircraftController` → Application layer component responsible for orchestrating the use case, validating business rules (ownership, existence, pending flights), invoking the domain logic, and coordinating persistence operations.
- `DecommissionAircraftUI` → Console UI in `aisafe.app.backoffice.console` responsible for interacting with the collaborator, listing available aircraft, collecting user input, and delegating the decommission operation to the controller.
- `AppSettings` → Provides access to application configuration and initializes the repository factory used throughout the application layer.

### 4.2. Acceptance Tests

Include here the main tests used to validate the functionality. Focus on how they relate to the acceptance criteria. May be automated or manual tests.

**Test 1:** Verifies that an `Aircraft` can be marked as DECOMMISSIONED

**Refers to Acceptance Criteria:** US071.1

```java
@Test
public void ensureAircraftIsMarkedAsDecommissioned() {
    // Arrange
    when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(activeAircraft));
    when(companyRepository.getCurrentCompany()).thenReturn(company);
    when(company.ownsAircraft(activeAircraft)).thenReturn(true);
    when(flightRepository.hasPendingFlights("CS-TUA")).thenReturn(false);

    // Act
    boolean result = controller.decommissionAircraft("CS-TUA");

    // Assert
    assertTrue(result);
    assertEquals(AircraftStatus.DECOMMISSIONED, activeAircraft.status());
    verify(aircraftRepository).save(activeAircraft);
}
```

---

**Test 2:** Verifies if it's possible to retire an aircraft that has pending flights.

**Refers to Acceptance Criteria:** US071.2

```java
@Test
public void ensureAircraftWithPendingFlightsCannotBeDecommissioned() {
    // Arrange
    when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(activeAircraft));
    when(companyRepository.getCurrentCompany()).thenReturn(company);
    when(company.ownsAircraft(activeAircraft)).thenReturn(true);
    when(flightRepository.hasPendingFlights("CS-TUA")).thenReturn(true);

    // Act & Assert
    assertThrows(IllegalStateException.class, () -> controller.decommissionAircraft("CS-TUA"));
    verify(aircraftRepository, never()).save(any(Aircraft.class));
}
```

---

**Test 3:** Verifies if it's possible to create new flight plans in a retired aircraft 

**Refers to Acceptance Criteria:** US071.3

```java
@Test
public void ensureDecommissionedAircraftCannotBeUsedToCreateFlightPlans() {
    // Arrange
    when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(decommissionedAircraft));

    // Act & Assert
    assertThrows(IllegalStateException.class, () -> flightPlanController.createFlightPlan(
            "CS-TUA", originAirport, destinationAirport, departureDate
    ));
}
```

---

**Test 4:** Verifies if the aircraft belongs to the company performing the operation

**Refers to Acceptance Criteria:** US071.4

```java
@Test
public void ensureAircraftMustBelongToCompanyToBeDecommissioned() {
    // Arrange
    when(aircraftRepository.findByRegistration("CS-TUA")).thenReturn(Optional.of(activeAircraft));
    when(companyRepository.getCurrentCompany()).thenReturn(company);
    when(company.ownsAircraft(activeAircraft)).thenReturn(false);

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> controller.decommissionAircraft("CS-TUA"));
    verify(aircraftRepository, never()).save(any(Aircraft.class));
}
```

---
**Test 5:** Verifies if the aircraft exists

**Refers to Acceptance Criteria:** US071.5

```java
@Test
public void ensureAircraftMustExistToBeDecommissioned() {
    // Arrange
    when(aircraftRepository.findByRegistration("CS-UNK")).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> controller.decommissionAircraft("CS-UNK"));
    verify(aircraftRepository, never()).save(any(Aircraft.class));
}
```

---
## 5. Implementation

The implementation follows the design presented in section 4 and applies Domain-Driven Design principles across all layers.

### 5.1. Evidence of implementation aligned with design

The following evidence shows that the implementation matches the planned architecture:

1. **Domain invariant enforced at state transition**
    - The `Aircraft` aggregate exposes a `decommission()` method that enforces the business rule at the domain level: it throws `IllegalStateException` if the aircraft is already decommissioned, ensuring the transition `ACTIVE → DECOMMISSIONED` is only performed once and is always consistent:
      ```java
      public void decommission() {
          if (this.status == AircraftStatus.DECOMMISSIONED) {
              throw new IllegalStateException("Aircraft is already decommissioned.");
          }
          this.status = AircraftStatus.DECOMMISSIONED;
      }
      ```
    - The `AircraftStatus` enum constrains the operational status to exactly two values (`ACTIVE`, `DECOMMISSIONED`), preventing invalid states from being represented in the domain.

2. **Pending flight validation before decommission**
    - `DecommissionAircraftController` calls `flightRepository.findByAircraftRegistration(registration)` and then checks if any of the returned flights is active or pending via `Flight::isActiveOrPending`. If so, an `IllegalStateException` is thrown and the aircraft is never modified, satisfying US071.2:
      ```java
      final boolean hasPendingFlights = flights.stream()
              .anyMatch(Flight::isActiveOrPending);
 
      if (hasPendingFlights) {
          throw new IllegalStateException("Aircraft has pending or active flights.");
      }
      ```
    - `FlightRepository` exposes `findByAircraftRegistration(AircraftRegistration registration)`, implemented in both `JpaFlightRepository` and `InMemoryFlightRepository`.

3. **Existence validation before any operation**
    - `DecommissionAircraftController` calls `aircraftRepository.findByRegistration(...)`, throwing `IllegalArgumentException` if the aircraft is not found, satisfying US071.5. No further operations are attempted on an unknown aircraft:
      ```java
      final Aircraft aircraft = aircraftRepository
              .findByRegistration(String.valueOf(registration))
              .orElseThrow(() ->
                      new IllegalArgumentException(
                              "No aircraft found with registration: " + registrationValue));
      ```

4. **Persistence of the updated status**
    - After a successful `decommission()` call on the domain entity, the controller immediately calls `aircraftRepository.save(aircraft)`, persisting the updated `DECOMMISSIONED` status. Both `JpaAircraftRepository` and `InMemoryAircraftRepository` support save/update operations on existing entities.

5. **Guard against creating flight plans for decommissioned aircraft**
    - The `Aircraft` aggregate exposes `status()`, which allows the flight plan creation controller (US080) to validate the aircraft status before proceeding. If the aircraft is `DECOMMISSIONED`, the operation is rejected, satisfying US071.3.

6. **Ownership validation**
    - Ownership validation (US071.4) is enforced at the presentation layer: `DecommissionAircraftUI` only presents aircraft belonging to the current company's context, so the controller does not independently re-validate company ownership.

7. **Error handling and user feedback**
    - `DecommissionAircraftUI` distinguishes between `IllegalArgumentException` (e.g. aircraft not found) and `IllegalStateException` (e.g. pending flights, already decommissioned), displaying targeted messages to the collaborator without exposing internal stack traces. Unexpected errors are logged via SLF4J and a generic message is shown.

### 5.2. Relevant artifacts and configuration files

The main files required to understand and execute this implementation are:

- **Domain**
    - `aisafe.core/src/main/java/aiSafe/aircraft/domain/Aircraft.java`
    - `aisafe.core/src/main/java/aiSafe/aircraft/domain/AircraftStatus.java`
- **Repository Interfaces**
    - `aisafe.core/src/main/java/aiSafe/aircraft/repositories/AircraftRepository.java`
    - `aisafe.core/src/main/java/aiSafe/flight/repositories/FlightRepository.java`
- **Application**
    - `aisafe.core/src/main/java/aiSafe/aircraft/application/DecommissionAircraftController.java`
- **Presentation**
    - `aisafe.app.backoffice.console/src/main/java/aiSafe/app/backoffice/console/presentation/aircraft/DecommissionAircraftUI.java`
    - `aisafe.app.backoffice.console/src/main/java/aiSafe/app/backoffice/console/presentation/aircraft/DecommissionAircraftAction.java`
- **Persistence**
    - `aisafe.persistence.impl/src/main/java/aiSafe/persistence/impl/jpa/JpaAircraftRepository.java`
    - `aisafe.persistence.impl/src/main/java/aiSafe/persistence/impl/jpa/JpaFlightRepository.java`
    - `aisafe.persistence.impl/src/main/java/aiSafe/persistence/impl/inmemory/InMemoryAircraftRepository.java`
    - `aisafe.persistence.impl/src/main/java/aiSafe/persistence/impl/inmemory/InMemoryFlightRepository.java`

### 5.3. Major commits (summary)

The following commits are the major milestones related to this requirement:

- `ddeb5a1` — add sequence diagram for US071 (#90)
- `736fd6f` — add class diagram for US071 (#90)
- `c99a5be` — add acceptance tests to US071 README (#90)
- `0f26b68` — add Flight aggregate domain classes and fix AircraftRegistration (#103)
- `e7dd9f1` — add Flight persistence layer (#103)
- `16980d6` — add Decommission Aircraft use case (#103)

---

## 6. Integration/Demonstration

### 6.1. Architectural Integration

This functionality integrates with the system following the same persistence architecture defined for US070. The `AircraftRepository` and `FlightRepository` interfaces are defined in the core domain module and resolved at runtime via `PersistenceContext`:

- **`persistence.impl.inmemory`** — used for testing, backed by `InMemoryAircraftRepository` and `InMemoryFlightRepository`, using Java streams to check for pending/active flights and update aircraft status.
- **`persistence.impl.jpa`** — used in production, backed by `JpaAircraftRepository` and `JpaFlightRepository`, using JPQL to query flights by aircraft registration and persist the updated status via JPA merge.

The decommission use case crosses two aggregate boundaries (`Aircraft` and `Flight`) exclusively through their repository interfaces, preserving aggregate isolation — the `Flight` aggregate is never modified, only queried.

### 6.2. Integration Points

| Layer                    | Component                                                | Role                                                                       |
|--------------------------|----------------------------------------------------------|----------------------------------------------------------------------------|
| Domain                   | `Aircraft`, `AircraftRepository`                         | Aggregate root holding lifecycle state and its persistence interface       |
| Domain                   | `Flight`, `FlightRepository`                             | Queried via `findByAircraftRegistration` to detect pending/active flights  |
| Application              | `DecommissionAircraftController`                         | Orchestrates existence check, pending flight check, domain call, and save  |
| Persistence (JPA)        | `JpaAircraftRepository`, `JpaFlightRepository`           | Persist and query entities in the relational database via JPA/Hibernate    |
| Persistence (InMemory)   | `InMemoryAircraftRepository`, `InMemoryFlightRepository` | Used in tests and bootstrap dry-runs                                       |
| Infrastructure           | `PersistenceContext`                                     | Resolves repository instances injected into the controller                 |
| Presentation             | `DecommissionAircraftAction`                             | Menu action that delegates execution to `DecommissionAircraftUI`           |
| Presentation             | `DecommissionAircraftUI`                                 | Console UI — reads registration, confirms intent, calls controller         |

### 6.3. How to Demonstrate

1. Start the application using the Backoffice console module.
2. Log in with a user holding the `BACKOFFICE_OPERATOR` role.
3. Navigate to `Operations > Fleet Management > Decommission Aircraft`.
4. The UI (`DecommissionAircraftUI`) prompts the collaborator for the aircraft registration number and asks for confirmation (`yes/no`).
5. If confirmed, the controller validates: existence → no pending/active flights → calls `aircraft.decommission()` → persists via `save`.
6. On success, the system prints `Aircraft successfully decommissioned.`
7. Business rule violations produce targeted messages:
    - Aircraft not found → `Error: No aircraft found with registration: <reg>`
    - Pending or active flights → `Unable to decommission: Aircraft has pending or active flights.`
    - Already decommissioned → `Unable to decommission: Aircraft is already decommissioned.`
8. To verify US071.3, attempt to create a new flight plan (US080) using the decommissioned aircraft — the system should reject the operation.

---

## 7. Observations

### 7.1. Architectural Decisions

The decommission operation was deliberately kept as a state transition on the `Aircraft` aggregate rather than a delete operation. This preserves historical traceability — decommissioned aircraft remain queryable for reporting and audit purposes — and avoids cascading referential integrity issues with past flights that reference the aircraft.

The pending flight check is performed at the controller level rather than inside the `Aircraft` domain method, because querying flights requires crossing an aggregate boundary. The controller calls `flightRepository.findByAircraftRegistration(registration)` and evaluates `Flight::isActiveOrPending` via a stream before invoking `aircraft.decommission()`. The domain method only enforces the invariant that an already-decommissioned aircraft cannot be decommissioned again. This separation keeps the domain pure (no repository dependencies) while still enforcing all business rules before any state change is persisted.

An alternative considered was to place all validation inside the `Aircraft` aggregate by injecting a `FlightRepository` into the domain method. This was rejected because it would couple the domain to the persistence interface, violating DDD aggregate isolation principles. The controller-level orchestration pattern used across this project (also seen in US070) was preferred for consistency and testability.

It is also worth noting that ownership validation (US071.4) is handled implicitly at the presentation layer — `DecommissionAircraftUI` only presents aircraft belonging to the current company's context. A future improvement could be to add an explicit ownership check at the controller level for robustness against non-UI entry points (e.g. a future REST API).

### 7.2. Third-party Works

- **EAPLI Framework** — provides the base infrastructure for `AggregateRoot`, `DomainRepository`, `InMemoryDomainRepository`, `JpaAutoTxRepository`, `AbstractUI`, `Console`, and `Action` used throughout this implementation.
- **Jakarta Persistence (JPA) / Hibernate** — used for ORM mapping of the `Aircraft` entity and JPQL-based querying of flights by aircraft registration.
