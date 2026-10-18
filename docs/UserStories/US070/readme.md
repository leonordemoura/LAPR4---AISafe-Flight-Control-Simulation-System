# US070 - Add an aircraft to an air transport company

## 1. Context

* As an Air Transport Company Collaborator, I want to add an aircraft to my company’s fleet.
* This task is being developed for the first time in the sprint 2, and it has dependencies with the following requirements:
  *  US055 - Create an Aircraft Model
  *  US060 - Register an Air Transport Company
  * 
### 1.1 List of issues

**Main Issue:** Issue #47

**Analysis:** Issue #69

**Design and Unit Tests:** Issue #70

**Implement:** Issue #101

## 2. Requirements

**US070 -**  As an Air Transport Company Collaborator, I want to add an aircraft to my company’s fleet. The aircraft is of a given model, including the engines, and the number of seats of each type/class must be provided. Total number of seats cannot exceed the model’s capacity. An aircraft is identified by the aircraft registration number, that is unique. Also, an aircraft is registered in a country (it may not be the company’s home country). An aircraft also has an operational status.


### Acceptance Criteria:

- US070.1 The system must ensure that the aircraft registration number is unique within the system.
- US070.2 The aircraft must be associated with a valid aircraft model.
- US070.3 The total number of seats (sum of all classes) must not exceed the aircraft model’s capacity.
- US070.4 The aircraft must include the following attributes: registration number, country of registration, operational status, model and seat configuration.
- US070.5 All mandatory fields must be provided. The system must reject incomplete registrations.
- US070.6 The system must support the registration of aircraft through a bootstrap process. 

### Dependencies/References:

Regarding this requirement, we understand that it relates to:

- **US055 - Create an Aircraft Model**: an aircraft must reference an existing model, which defines capacity and characteristics.
- **US060 - Register an Air Transport Company**: the aircraft is added to a specific company’s fleet.
- **Section 3.2**: defines aircraft attributes such as registration, model, and cabin configuration.
- 
## 3. Analysis

The domain model for adding an Aircraft must include the following:

![Excerpt of Air Transport Company in the Domain Model](excerpt_domainModel/excerpt_airTransportCompany_Aircraft.png)

| Attribute              | Description                                              | Constraints                              |
|------------------------|----------------------------------------------------------|------------------------------------------|
| Registration Number    | Unique identifier of the aircraft                        | Must be unique                           |
| Model                  | Aircraft model                                           | Must exist                               |
| Company                | Owner company                                            | Must exist                               |
| Seat Configuration     | Number of seats per class                                | Total ≤ model capacity                   |
| Country                | Country of registration                                  | Must be valid                            |
| Operational Status     | Current state (active, inactive, etc.)                   | Must be valid                            |

**Key business rules:**
- Registration number must be unique.
- Total seats cannot exceed the model’s capacity.
- Aircraft must be associated with an existing model.
- Aircraft is always associated with one Air Transport Company.

**Relationship with the domain:**
The Aircraft aggregate represents the main entity responsible for aircraft data and validation within the system. The AirTransportCompany provides the ownership context through the CompanyId, which associates each aircraft to a specific company. Aircraft entities are always linked to one AirTransportCompany, but the relationship is maintained through identifiers rather than direct aggregation.

## 4. Design

*In these sections, the team should present the solution design that was adopted to solve the requirement. This should include, at least, a diagram of the realization of the functionality (e.g., sequence diagram) and the specification of the main tests used to validate the functionality. A class diagram may be useful to describe the classes involved.*

### 4.1. Realization

The design of the solution for this requirement is based on the following class diagram:

![Add Aircraft_to Fleet Aggregate Class Diagram](classDiagram/addAircraft_Diagram.png)

The following sequence diagram illustrates the realization of US070 — Add an aircraft to an air transport company:

![Sequence Diagram - Add an aircraft to an air transport company](sequenceDiagram/addAircraft_SequenceDiagram.png)


#### Main classes and responsibilities:

- `Aircraft` → Aggregate Root responsible for enforcing aircraft invariants and holding aircraft data such as registration number, seat configuration, operational status, country of registration and associated model.
- `AircraftModel` → Aggregate Root responsible for validating model-related constraints, namely engine compatibility and maximum seat capacity.
- `AirTransportCompany` → Aggregate Root responsible for managing the company fleet and associating aircraft with the company.
- `AircraftRepository` → Persistence interface responsible for verifying aircraft registration uniqueness and persisting aircraft entities.
- `AircraftModelRepository` → Persistence interface responsible for retrieving aircraft models by identifier.
- `AirTransportCompanyRepository` → Persistence interface responsible for retrieving the current company and persisting fleet updates.
- `PersistenceContext` → Provides access to the repository factory used to obtain repository instances.
- `RepositoryFactory` → Factory responsible for creating repository implementations.
- `AddAircraftController` → Application layer component responsible for orchestrating the use case, validating business rules through the domain model and coordinating persistence operations.
- `AddAircraftUI` → Console UI in `aisafe.app.backoffice.console` responsible for interacting with the collaborator, collecting input data and delegating the operation to the controller.
- `AppSettings` → Provides access to application configuration and repository factory initialization.

### 4.2. Acceptance Tests

Include here the main tests used to validate the functionality. Focus on how they relate to the acceptance criteria. May be automated or manual tests.

---

**Test 1:** Verifies that an aircraft is successfully created with valid data.

**Refers to Acceptance Criteria:** US070.1, US070.2, US070.3, US070.4, US070.5

```java
@Test
void ensureAircraftIsCreatedSuccessfully() {

    final var aircraft = new Aircraft(
            AircraftRegistration.valueOf("CS-TUA"),
            validAircraftModel(),
            List.of(CurrentEngine.valueOf("CFM56-7B")),
            new CabinConfiguration(150, 20, 10),
            new RegistrationYear(2010),
            new Country("Portugal", 620),
            AircraftStatus.ACTIVE,
            validCompany()
    );

    assertNotNull(aircraft);
}
```

---

**Test 2:** Verifies that all mandatory fields are required.

**Refers to Acceptance Criteria:** US070.4, US070.5

```java
@Test
void ensureMandatoryFieldsCannotBeNull() {

    assertThrows(IllegalArgumentException.class, () -> {
        new Aircraft(null, null, null, null, null, null, null, null);
    });
}
```

---

**Test 3:** Verifies that total seats cannot exceed the model's capacity.

**Refers to Acceptance Criteria:** US070.3

```java
@Test
void ensureSeatsExceedingModelCapacityIsRejected() {

    assertThrows(IllegalArgumentException.class, () -> {
        new Aircraft(
                AircraftRegistration.valueOf("CS-TUA"),
                validAircraftModel(),                 // max 200 seats
                List.of(CurrentEngine.valueOf("CFM56-7B")),
                new CabinConfiguration(180, 20, 10),  // 210 seats — exceeds 200
                new RegistrationYear(2010),
                new Country("Portugal", 620),
                AircraftStatus.ACTIVE,
                validCompany()
        );
    });
}
```

---

**Test 4:** Verifies that a null aircraft model is rejected.

**Refers to Acceptance Criteria:** US070.2, US070.5

```java
@Test
void ensureNullAircraftModelIsRejected() {

    assertThrows(IllegalArgumentException.class, () -> {
        new Aircraft(
                AircraftRegistration.valueOf("CS-TUA"),
                null,
                List.of(CurrentEngine.valueOf("CFM56-7B")),
                new CabinConfiguration(150, 20, 10),
                new RegistrationYear(2010),
                new Country("Portugal", 620),
                AircraftStatus.ACTIVE,
                validCompany()
        );
    });
}
```

---

**Test 5:** Verifies that an aircraft must have at least one engine.

**Refers to Acceptance Criteria:** US070.4, US070.5

```java
@Test
void ensureEmptyEngineListIsRejected() {

    assertThrows(IllegalArgumentException.class, () -> {
        new Aircraft(
                AircraftRegistration.valueOf("CS-TUA"),
                validAircraftModel(),
                Collections.emptyList(),
                new CabinConfiguration(150, 20, 10),
                new RegistrationYear(2010),
                new Country("Portugal", 620),
                AircraftStatus.ACTIVE,
                validCompany()
        );
    });
}
```

---

**Test 6:** Verifies that aircraft registration through bootstrap uses the same domain validations.

**Refers to Acceptance Criteria:** US070.6

```java
@Test
void ensureBootstrapAircraftRegistrationIsSupported() {

    assertDoesNotThrow(() -> {
        new Aircraft(
                AircraftRegistration.valueOf("CS-TUB"),
                validAircraftModel(),
                List.of(CurrentEngine.valueOf("CFM56-7B")),
                new CabinConfiguration(150, 20, 10),
                new RegistrationYear(2015),
                new Country("Portugal", 620),
                AircraftStatus.ACTIVE,
                validCompany()
        );
    });
}
```

---

## 5. Implementation

The implementation follows the design presented in section 4 and applies Domain-Driven Design principles across all layers.

### 5.1. Evidence of implementation aligned with design

The following evidence shows that the implementation matches the planned architecture:

1. **Domain invariants enforced at construction**
   - `Aircraft` validates that all mandatory attributes are non-null in the constructor, throwing `IllegalArgumentException` if any is missing (US070.4, US070.5).
   - It additionally ensures that:
     - The engine list is not empty — `Aircraft` must have at least one engine (US070.4, US070.5)
     - The total seats from `CabinConfiguration` do not exceed the model's `maximumSeats()` (US070.3)
   - `CabinConfiguration` enforces that no seat count is negative and that the total is at least one, preventing invalid configurations from being constructed.
   - `AircraftRegistration` normalises and validates the registration string (trim + uppercase), rejecting null or empty values via `Preconditions.nonEmpty`.
   - `RegistrationYear` validates that the year is between 1900 and the current year, using `Preconditions.ensure`.
   - `CurrentEngine` rejects null or blank engine model identifiers at factory-method level (`valueOf`).

2. **Value objects for all aircraft attributes**
   - All aircraft attributes are encapsulated in dedicated value objects:
     - `AircraftRegistration` — unique identifier, case-insensitive, stored normalised
     - `CabinConfiguration` — seat counts per class (economy, business, first class), with `totalSeats()` helper
     - `RegistrationYear` — validated year with `age()` helper derived from current year
     - `CurrentEngine` — references an engine model by identifier
     - `AircraftStatus` — enum (`ACTIVE`, `DECOMMISSIONED`)
     - `Country` — country name and numeric code
     - `FlightCrewSize` — positive crew size, validated at construction
   - This guarantees type safety, encapsulated validation logic, and immutability-like behaviour within the aggregate.

3. **Uniqueness enforced at two levels**
   - Repository level: `AddAircraftController` calls `aircraftRepo.existsByRegistration(registration)` before instantiation, rejecting duplicates early.
   - Persistence level: `AircraftRegistration` is mapped with `unique = true` on the `registration` column in the `@Table` definition of `Aircraft`:
```java
     @AttributeOverride(name = "value", column = @Column(name = "registration", nullable = false, unique = true))
```
- Both `JpaAircraftRepository` and `InMemoryAircraftRepository` implement `existsByRegistration` and `findByRegistration`, using case-insensitive matching in JPA (`lower(e.registration.value) = lower(:registration)`).

4. **Mandatory association with Aircraft Model and Company**
    - `AddAircraftController.addAircraft(...)` resolves the `AircraftModel` via `AircraftModelRepository.ofIdentity(new AircraftModelId(...))` and the `AirTransportCompany` via `AirTransportCompanyRepository.ofIdentity(companyId)`, throwing `IllegalArgumentException` if either is not found (US070.2).
    - The `Aircraft` entity maps both associations as mandatory (`optional = false`) JPA relationships:
```java
     @ManyToOne(optional = false)
     @JoinColumn(name = "company_id", nullable = false)
     private AirTransportCompany company;
```

5. **Role-based access control**
    - `AddAircraftController` enforces role-based access by calling:
```java
     authz.ensureAuthenticatedUserHasAnyOf(AisafeRoles.BACKOFFICE_OPERATOR);
```
     before executing any registration logic, ensuring that only authorised users can add aircraft.

6. **Bootstrap support**
    - Aircraft can be registered during system startup through the bootstrap process, which reuses the same domain constructor and repository validations as the interactive UI flow, satisfying US070.6.

### 5.2. Relevant artifacts and configuration files

The main files required to understand and execute this implementation are:

- **Domain**
    - `aisafe.core/src/main/java/aiSafe/aircraft/domain/Aircraft.java`
    - `aisafe.core/src/main/java/aiSafe/aircraft/domain/AircraftRegistration.java`
    - `aisafe.core/src/main/java/aiSafe/aircraft/domain/AircraftStatus.java`
    - `aisafe.core/src/main/java/aiSafe/aircraft/domain/CabinConfiguration.java`
    - `aisafe.core/src/main/java/aiSafe/aircraft/domain/CurrentEngine.java`
    - `aisafe.core/src/main/java/aiSafe/aircraft/domain/RegistrationYear.java`
    - `aisafe.core/src/main/java/aiSafe/aircraft/domain/FlightCrewSize.java`
    - `aisafe.core/src/main/java/aiSafe/aircraft/domain/CompanyID.java`
- **Repository Interface**
    - `aisafe.core/src/main/java/aiSafe/aircraft/repositories/AircraftRepository.java`
- **Application**
    - `aisafe.core/src/main/java/aiSafe/aircraft/application/AddAircraftController.java`
- **Presentation**
    - `aisafe.app.backoffice.console/src/main/java/aiSafe/app/backoffice/console/presentation/aircraft/AddAircraftUI.java`
    - `aisafe.app.backoffice.console/src/main/java/aiSafe/app/backoffice/console/presentation/aircraft/AddAircraftAction.java`
    - `aisafe.app.backoffice.console/src/main/java/aiSafe/app/backoffice/console/presentation/MainMenu.java`
- **Persistence**
    - `aisafe.persistence.impl/src/main/java/aiSafe/persistence/impl/jpa/JpaAircraftRepository.java`
    - `aisafe.persistence.impl/src/main/java/aiSafe/persistence/impl/inmemory/InMemoryAircraftRepository.java`
    - `aisafe.persistence.impl/src/main/resources/META-INF/persistence.xml`
- **Bootstrap**
    - `aisafe.bootstrappers/src/main/java/aiSafe/infrastructure/bootstrap/MasterDataBootstrapper.java`
    - `aisafe.bootstrappers/src/main/java/aiSafe/infrastructure/bootstrap/AircraftBootstrapper.java`

### 5.3. Major commits (summary)

The following commits are the major milestones related to this requirement:

- `78f463c` - add sequence diagram for US070 (#70)
- `1e721c5` - add class diagram for US070 (#70)
- `87842ee` - add aircraft domain classes (`Aircraft`, `AircraftRegistration`, `CabinConfiguration`, `CurrentEngine`, `AircraftStatus`) (#101)
- `419aee9` - add aircraft creation flow (`AddAircraftController`, `AddAircraftUI`, `AddAircraftAction`) (#101)
- `0db40ef` - validate that total cabin seats do not exceed aircraft model maximum capacity (#101)
- `1eeb822` - added unit tests for aircraft domain classes (#70)
---

## 6. Integration/Demonstration

### 6.1. Architectural Integration

This functionality integrates with the system following the persistence architecture defined for the project. The `AircraftRepository` interface is defined in the core domain module (`aiSafe.aircraft.repositories`) and resolved at runtime via `PersistenceContext`, which delegates to the active `RepositoryFactory` implementation:

- **`persistence.impl.inmemory`** — used for testing and early development, backed by `InMemoryAircraftRepository`, which uses reflection to assign surrogate Long identities and Java streams for all query operations.
- **`persistence.impl.jpa`** — used in production, backed by `JpaAircraftRepository`, which extends `JpaAutoTxRepository` and uses JPQL predicates for all filtering operations (by company, model, maker, capacity, and age).

This design ensures the domain and application layers are fully decoupled from persistence technology, with switching between implementations requiring only a configuration change.

### 6.2. Integration Points

| Layer                  | Component                                              | Role                                                             |
|------------------------|--------------------------------------------------------|------------------------------------------------------------------|
| Domain                 | `Aircraft`, `AircraftRepository`                       | Aggregate root and repository interface                          |
| Domain                 | `AircraftModel`, `AircraftModelRepository`             | Provides model validation and seat capacity constraints          |
| Domain                 | `AirTransportCompany`, `AirTransportCompanyRepository` | Provides company context and enforces fleet association          |
| Application            | `AddAircraftController`                                | Orchestrates the use case, enforces authz and business rules     |
| Persistence (JPA)      | `JpaAircraftRepository`                                | Persists aircraft to relational DB via JPA/Hibernate             |
| Persistence (InMemory) | `InMemoryAircraftRepository`                           | Used in tests and bootstrap dry-runs                             |
| Infrastructure         | `PersistenceContext`                                   | Resolves the active `RepositoryFactory` at runtime               |
| Bootstrap              | `MasterDataBootstrapper`, `AircraftBootstrapper`       | Seeds aircraft at system startup                                 |
| Presentation           | `AddAircraftAction`                                    | Menu action that delegates execution to `AddAircraftUI`          |
| Presentation           | `AddAircraftUI`                                        | Console UI — collects input, validates locally, calls controller |

### 6.3. How to Demonstrate

1. Start the application using the Backoffice console module.
2. Log in with a user holding the `BACKOFFICE_OPERATOR` role.
3. Navigate to `Operations > Fleet Management > Add Aircraft to the Fleet`.
4. The UI (`AddAircraftUI`) guides the collaborator through the following prompts in sequence:
    - **Aircraft Registration Number** — must be unique; the UI immediately rejects duplicates before proceeding.
    - **Aircraft Model ID** — chosen from the list of available models displayed with their ID, name, and maximum seat capacity.
    - **Engine Model ID(s)** — one or more identifiers, comma-separated; at least one is mandatory.
    - **Number of First Class seats**, **Business Class seats**, and **Economy Class seats** — the UI pre-validates that the total does not exceed the selected model's capacity before calling the controller.
    - **Registration Year** — must be between 1900 and the current year.
    - **Country of Registration** — country name followed by the numeric code.
    - **Operational Status** — chosen by number from the list of available statuses (`ACTIVE`, `DECOMMISSIONED`).
    - **Company ID** — chosen from the list of available air transport companies.
5. On success, the system prints `Aircraft successfully added to the fleet.` Business rule violations and unexpected errors are caught and reported to the collaborator without exposing internal stack traces.

---

## 7. Observations

### 7.1. Architectural Decisions

The project follows a layered, DDD-based architecture where the repository contract (`XRepository`) is always defined in the core domain module, while concrete implementations (`InMemoryXRepository`, `JpaXRepository`) live in separate modules resolved at runtime by `RepositoryFactory`. This ensures:

- The domain model has **zero dependency** on persistence technology.
- Switching between InMemory and JPA requires only a configuration change, not a code change.

The UI (`AddAircraftUI`) performs a lightweight pre-validation of the seat total before delegating to the controller, providing immediate feedback to the collaborator. However, the definitive validation is always re-enforced at the domain level inside the `Aircraft` constructor and at the controller level, ensuring that business rules cannot be bypassed regardless of the entry point (UI, bootstrap, or future REST API).

The relationship between `Aircraft` and `AirTransportCompany` is maintained via a direct `@ManyToOne` JPA association rather than through a value-object identifier, which simplifies querying (e.g. `findByCompany`) while still respecting the aggregate boundary — `AirTransportCompany` is never modified through the `Aircraft` aggregate. All cross-aggregate references in queries are resolved by the controller before passing them into the domain.

The `AircraftRepository` also exposes several additional query methods beyond US070 (`findByCompanyAndModel`, `findByCompanyAndMaker`, `findByCompanyAndMinCapacity`, `findByCompanyAndMaxCapacity`, `findByCompanyAndMaxAge`) to support future fleet management use cases (e.g. US072), following the principle of defining the full repository contract upfront.

The persistence layer followed the pattern represented below:

![Persistence Layer Pattern](../persistenceDiagram/persistenceDiagram.png)

### 7.2. Third-party Works

- **EAPLI Framework** — provides the base infrastructure for `AggregateRoot`, `ValueObject`, `DomainRepository`, `InMemoryDomainRepository`, `JpaAutoTxRepository`, `AuthzRegistry`, and `Preconditions` used throughout this implementation.
- **Jakarta Persistence (JPA) / Hibernate** — used for ORM mapping of the `Aircraft` entity, its embedded value objects (`AircraftRegistration`, `CabinConfiguration`, `RegistrationYear`, `Country`), and the `aircraft_engines` element collection table.
