# US043 - Consult Weather Data

## 1. Context

* As a Weather Person, a Pilot, or a Flight Control operator, I want to consult weather data in the system in a given day and in a specific air control area.
* This task is being developed for the first time in sprint 3 and it has dependencies with the following requirements:
    * US041 - Register weather data
    * US042 - Import bulk weather data

### 1.1 List of issues

**Main Issue:** Issue #123

**Analysis:** Issue #137

**Design:** Issue #163

**Implement:** Issue #168

---

## 2. Requirements

**US043** As a Weather Person, a Pilot, or a Flight Control operator, I want to consult weather data in the system in a given day and in a specific air control area.

### Acceptance Criteria:

- **US043.1** The system must allow a Weather Person, Pilot, or Flight Control operator to consult weather data for a specific air control area.
- **US043.2** The system must allow consultation of weather data filtered by a specific day (date).
- **US043.3** The system must return all weather observations that match the selected air control area and time period.
- **US043.4** If no weather data exists for the given filters, the system must return an empty result without errors.
- **US043.5** The system must ensure that only valid and registered air control areas can be used as a filter.
- **US043.6** The system must return all relevant meteorological attributes (wind direction, wind speed, pressure, temperature, etc.).
- **US043.7** The system must provide consistent results for all authorized user roles.

### Dependencies/References:

Regarding this requirement, we understand that it relates to:

- **US041 - Register weather data**: weather data must exist in the system before consultation.
- **US042 - Import bulk weather data**: bulk-imported weather data must also be available for consultation.
- **US050 - Register an air control area**: consultation is always scoped to an existing air control area.
- **US100 / US110 / US113**: weather data is used by the flight simulation engine (especially wind influence).
- **Section 3.2**: defines meteorological attributes required for simulation (wind, pressure, temperature).

---

## 3. Analysis

The domain model for weather data must support efficient retrieval of meteorological observations based on spatial and temporal filters.

`WeatherData` is an independent aggregate that stores meteorological observations for a given air control area and time interval. Consultation does not modify the domain; it only queries existing persisted data.

### Key Business Rules:

- Weather data is always scoped to a valid **AirControlArea**.
- Queries must be filtered by:
    - Air control area
    - Date (or time interval)
- Only existing weather data can be returned.
- Missing data must return an empty result, not an error.
- All meteorological attributes must be included in the response.
- Results must be consistent across all authorized roles.

The relevant attributes returned per weather observation are:

| Attribute        | Description                                      | Constraints              |
|------------------|--------------------------------------------------|--------------------------|
| Area Code        | Identifier of the air control area               | Mandatory; must exist    |
| Observation Date | The date of the meteorological observation       | Mandatory                |
| Wind Direction   | Direction from which the wind is blowing         | Mandatory                |
| Wind Speed       | Speed of wind at observation time                | Mandatory                |
| Temperature      | Atmospheric temperature at observation time      | Mandatory                |
| Pressure         | Atmospheric pressure at observation time         | Mandatory                |
| Weather Sub-Area | Sub-area within the air control area             | Mandatory                |

---

## 4. Design

*In this section, the team presents the solution design adopted to solve the requirement. This includes a diagram of the realization of the functionality and the specification of the main tests used to validate the functionality.*

### 4.1. Realization

The design of the solution for this requirement is based on the following class diagram:

![Consult Weather Data - Class Diagram](classDiagram/weatherData_Diagram.png)

The following sequence diagram illustrates the realization of US043 — Consult Weather Data:

![Consult Weather Data - Sequence Diagram](sequenceDiagram/weatherData_SequenceDiagram.png)

### 4.2. Acceptance Tests

**Test 1:** Verifies that weather data consultation returns results for a valid area code and date.

**Refers to Acceptance Criteria:** US043.1, US043.2, US043.3

```java
@Test
void ensureWeatherDataIsReturnedForValidAreaCodeAndDate() {
    final var areaCode = new AreaCode("LPPR");
    final var date = LocalDate.of(2026, 6, 22);

    final var result = weatherDataRepository.findByAreaCodeAndDate(areaCode, date);

    assertNotNull(result);
    assertFalse(result.isEmpty());
}
```

**Test 2:** Verifies that an empty list is returned when no weather data exists for the given filters.

**Refers to Acceptance Criteria:** US043.4

```java
@Test
void ensureEmptyListIsReturnedWhenNoDataExistsForGivenFilters() {
    final var areaCode = new AreaCode("LPPR");
    final var date = LocalDate.of(2000, 1, 1);

    final var result = weatherDataRepository.findByAreaCodeAndDate(areaCode, date);

    assertNotNull(result);
    assertTrue(result.isEmpty());
}
```

**Test 3:** Verifies that area code cannot be null.

**Refers to Acceptance Criteria:** US043.5

```java
@Test(expected = IllegalArgumentException.class)
void ensureAreaCodeCannotBeNull() {
    new AreaCode(null);
}
```

**Test 4:** Verifies that area code cannot be blank.

**Refers to Acceptance Criteria:** US043.5

```java
@Test(expected = IllegalArgumentException.class)
void ensureAreaCodeCannotBeBlank() {
    new AreaCode("");
}
```

**Test 5:** Verifies that all meteorological attributes are present in the returned weather data.

**Refers to Acceptance Criteria:** US043.6

```java
@Test
void ensureReturnedWeatherDataContainsAllMeteorologicalAttributes() {
    final var areaCode = new AreaCode("LPPR");
    final var date = LocalDate.of(2026, 6, 22);

    final var result = weatherDataRepository.findByAreaCodeAndDate(areaCode, date);

    assertFalse(result.isEmpty());
    final WeatherData data = result.get(0);
    assertNotNull(data.windDirection());
    assertNotNull(data.windSpeed());
    assertNotNull(data.temperature());
    assertNotNull(data.pressure());
    assertNotNull(data.weatherSubArea());
    assertNotNull(data.observationDate());
}
```

---

## 5. Implementation

The implementation follows the design presented in section 4 and applies Domain-Driven Design principles across the domain, application, presentation and persistence layers.

### 5.1. Evidence of implementation aligned with design

The following evidence shows that the implementation matches the planned architecture and satisfies the acceptance criteria:

1. **Query scoped to a valid AirControlArea**
    - `AreaCode` is implemented as a value object that rejects null or blank values, ensuring only valid area codes are accepted (**US043.5**).

2. **Filtering by area code and date**
    - `ConsultWeatherDataController` delegates to the repository method `findByAreaCodeAndDate(AreaCode, LocalDate)`, which applies both filters atomically (**US043.1**, **US043.2**, **US043.3**).

3. **Empty result for missing data**
    - The repository returns an empty list (not null, not an exception) when no data matches the given filters (**US043.4**).

4. **All meteorological attributes returned**
    - `WeatherData` exposes `windDirection()`, `windSpeed()`, `temperature()`, `pressure()`, `weatherSubArea()`, and `observationDate()` (**US043.6**).

5. **Role-based access control**
    - The controller calls `authorizationGuardService.ensureAuthenticatedUserWithRole(...)` before executing the query, allowing Weather Persons, Pilots, and Flight Control operators (**US043.7**).

6. **Repository abstraction with pluggable persistence**
    - The `WeatherDataRepository` interface lives in the core module, with concrete implementations provided in:
        - In-memory: `InMemoryWeatherDataRepository`
        - JPA: `JpaWeatherDataRepository`

### 5.2. Relevant artifacts and configuration files

The main files required to understand and execute this implementation are:

- **Domain**
    - `aisafe.core/src/main/java/aiSafe/weatherdata/domain/WeatherData.java`
    - `aisafe.core/src/main/java/aiSafe/weatherdata/domain/AreaCode.java`
    - `aisafe.core/src/main/java/aiSafe/weatherdata/domain/WindDirection.java`
    - `aisafe.core/src/main/java/aiSafe/weatherdata/domain/WindSpeed.java`
- **Repository contract (core)**
    - `aisafe.core/src/main/java/aiSafe/weatherdata/repositories/WeatherDataRepository.java`
- **Application**
    - `aisafe.core/src/main/java/aiSafe/weatherdata/application/ConsultWeatherDataController.java`
- **Presentation**
    - `aisafe.app.backoffice.console/src/main/java/aiSafe/app/backoffice/console/presentation/weatherdata/ConsultWeatherDataAction.java`
    - `aisafe.app.backoffice.console/src/main/java/aiSafe/app/backoffice/console/presentation/weatherdata/ConsultWeatherDataUI.java`
    - `aisafe.app.backoffice.console/src/main/java/aiSafe/app/backoffice/console/presentation/MainMenu.java`
- **Persistence**
    - `aisafe.persistence.impl/src/main/java/aiSafe/persistence/impl/inmemory/InMemoryWeatherDataRepository.java`
    - `aisafe.persistence.impl/src/main/java/aiSafe/persistence/impl/jpa/JpaWeatherDataRepository.java`
    - `aisafe.core/src/main/java/aiSafe/infrastructure/persistence/RepositoryFactory.java`
    - `aisafe.persistence.impl/src/main/resources/META-INF/persistence.xml`

### 5.3. Major commits (summary)

The following commits are the major milestones related to this requirement:

- `8d976b2` - #137 EAPLI: added the analysis for US043 and the excerpt of the domain model
- `9ab1c81` - #163 EAPLI: added class diagram and sequence diagram for US043
- `6d60698` - #163 EAPLI: Updated readMe with Acceptance Tests
- `afdd6be` - #168 EAPLI: Add WeatherData repository interface and implementations for US043
- `7657e9f` - #168 EAPLI: Add ConsultWeatherDataController with role-based authorization
- `8c24a4b` - #168 EAPLI: Add ConsultWeatherDataUI, action and menu entry for weather data consultation
- `7d35a40` - #168 EAPLI: implement ConsultWeatherDataService
- `7ded2ef` - #168 EAPLI: unit tests for controller and service

---

## 6. Integration/Demonstration

### 6.1. Architectural Integration

This functionality integrates with the system following the persistence architecture defined for the project. The `WeatherDataRepository` interface is defined in the core module and resolved at runtime via `PersistenceContext`, which delegates to the active `RepositoryFactory` implementation:

- **`persistence.impl.inmemory`** — used for testing and early development, backed by `InMemoryWeatherDataRepository`.
- **`persistence.impl.jpa`** — used in production, backed by `JpaWeatherDataRepository`, with ORM mapping registered in `META-INF/persistence.xml`.

This ensures the domain and application layers are decoupled from persistence technology.

### 6.2. Integration Points

| Layer                  | Component                                                | Role                                           |
|------------------------|----------------------------------------------------------|------------------------------------------------|
| Domain                 | `WeatherData`, `AreaCode`, `WindDirection`, `WindSpeed`  | Aggregate and value objects                    |
| Domain                 | `WeatherDataRepository`                                  | Repository interface                           |
| Application            | `ConsultWeatherDataController`                           | Orchestrates query and enforces access control |
| Persistence (JPA)      | `JpaWeatherDataRepository`                               | Queries relational DB via JPA                  |
| Persistence (InMemory) | `InMemoryWeatherDataRepository`                          | Used in tests and early development            |
| Infrastructure         | `RepositoryFactory` / `PersistenceContext`               | Resolves the active repository implementation  |
| Presentation           | `ConsultWeatherDataUI` / `ConsultWeatherDataAction`      | Console entry point                            |

### 6.3. How to Demonstrate

1. Start the Backoffice console application.
2. Log in with a user holding the `WEATHER_PERSON`, `PILOT`, or `FLIGHT_CONTROL_OPERATOR` role.
3. Navigate to `Operations > Consult Weather Data`.
4. Provide a valid air control area code and a date.
5. Confirm that all matching weather observations are listed with their meteorological attributes.
6. Repeat the query with a date that has no data and confirm the system returns an empty result without errors.

---

## 7. Observations

### 7.1. Architectural Decisions

This use case is read-only — it introduces no side effects on the domain. The consultation query is entirely delegated to the repository, keeping the application layer thin. The same `WeatherData` aggregate used for registration (US041) and bulk import (US042) is reused here without modification, ensuring consistency across use cases.

The persistence layer follows the pattern defined in the project:

![Persistence Layer Pattern](../persistenceDiagram/persistenceDiagram.png)

### 7.2. Third-party Works

- **EAPLI Framework** — provides the base infrastructure for `AggregateRoot`, `ValueObject`, `Repository`, `AuthzRegistry`, and console UI rendering used throughout this implementation.
- **Jakarta Persistence (JPA) / EclipseLink** — used for ORM mapping of the `WeatherData` entity and JPQL-based filtering by area code and date.