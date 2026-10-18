# US081 – Create a Flight Plan from a File

### *Sprint 2*

---

## 1. Context

**User Story 081:**

*As a Pilot, I want to create a flight plan from a file, so that formally defined plans can be imported without retyping.*

The system validates the file format before use. **Core Flight DSL** validation is provided by **US083**.

### Dependencies

| Story     | Role                                                                 |
|-----------|----------------------------------------------------------------------|
| **US083** | Grammar, lexer/parser, semantic rules, `FlightDslValidator`          |
| **US030** | Authentication (`PILOT` role required)                               |

---

## 2. Implementation

## 2.1 Execution Flow

1. User provides:
    - Flight designator (airline + number + suffix)
    - File path

2. System:
    - Reads file (size + UTF-8 validation)
    - Detects format
    - Validates DSL (US083)
    - Retrieves Flight from repository
    - Maps DSL → FlightPlan
    - Validates route compatibility
    - Persists Flight

3. Output:
    - Success → FlightPlan stored
    - Failure → list of errors

### Entry point

```java
ImportFlightPlanFromFileController controller = new ImportFlightPlanFromFileController();

FlightDesignator flightId = FlightDesignator.of("TP", 123, null);

ImportFlightPlanResult result = controller.importFlightPlanFromFile(
        flightId,
        Path.of("plans/my-plan.flight"));

if (result.isSuccess()) {
        // plan persisted on Flight aggregate
        } else {
        result.formattedErrors().forEach(System.out::println);
}
```

### Packages

| Component        | Class                                               |
|------------------|-----------------------------------------------------|
| Use case         | `ImportFlightPlanFromFileController`                |
| File read        | `FlightPlanFileReader` (UTF-8, empty/size checks)   |
| Format detection | `FlightPlanFormatDetector`                          |
| DSL validation   | `FlightDslValidator` (US083)                        |
| Domain mapping   | `FlightPlan.fromValidatedImport(DslFlightPlan)`     |
| Persistence      | `Flight.importFlightPlan` + `FlightRepository.save` |

### Supported formats

| Format                                                        | Status                          |
|---------------------------------------------------------------|---------------------------------|
| Core Flight DSL (`.flight` or content starting with `flight`) | **Supported**                   |
| Other                                                         | **Rejected** as unsupported     |

### Route matching

The flight plan route signature (`ORIGIN-DESTINATION`, e.g. `OPO-LIS`) must match the Flight's route designator.

This value is obtained from:

- DSL: first leg departure + last leg arrival
- Domain: `FlightRoute.routeDesignator()`

If they do not match, the import fails with `ROUTE_MISMATCH`.

Example: a flight with designator `TP123` and route `OPO-LIS` can import a DSL file with the same route signature.

---

### Domain integration

Each Flight is associated with a `FlightRoute` aggregate, which contains:

- `RouteName`
- `RouteDesignator` (origin-destination)

The imported DSL plan must match the `RouteDesignator`.

---

### Flight identification

Flights are identified by a `FlightDesignator`, composed of:

- Airline code (IATA, 2 letters)
- Flight number (1–4 digits)
- Optional operational suffix

Example:
TP123
TP123A

The controller provides a helper method:

```java
FlightDesignator generateFlightDesignator(String airlineCode, int number, String suffix);
```

---

## 3. Acceptance criteria mapping

| Criterion                     | Implementation                                               |
|-------------------------------|--------------------------------------------------------------|
| US081.1 Safe file read        | `FlightPlanFileReader` – UTF-8, empty, max 512 KB            |
| US081.2 Format detection      | `FlightPlanFormatDetector` before validation                 |
| US081.3 DSL lex/syntax        | `FlightDslValidator` (US083)                                 |
| US081.4 Semantic (all errors) | `FlightDslSemanticValidator` via US083                       |
| US081.5 No persist on failure | Controller saves only after full success                     |
| US081.6 Domain mapping        | `FlightPlan.fromValidatedImport` + `Flight.importFlightPlan` |

---

## 4. Error codes

| Code                             | Meaning                                                               |
|----------------------------------|-----------------------------------------------------------------------|
| `EMPTY_FILE`                     | File has no content                                                   |
| `FILE_TOO_LARGE`                 | Exceeds size limit                                                    |
| `FILE_READ_ERROR`                | I/O or missing file                                                   |
| `UNSUPPORTED_FORMAT`             | Not Core Flight DSL                                                   |
| `FLIGHT_NOT_FOUND`               | Unknown `FlightId`                                                    |
| `ROUTE_MISMATCH`                 | Plan route ≠ flight route                                             |
| `ACTIVE_PLAN_CANNOT_BE_REPLACED` | Cannot replace active plan                                            |
| `DSL_VALIDATION`                 | Lexical/syntax/semantic DSL errors (with line/column when applicable) |

---

## 4.1. Flight type in DSL vs `Flight` aggregate

Imported plans use the **Core Flight DSL** `type` keyword (`REGULAR` or `CHARTER`, same as domain `FlightType` per **US083** §3.9). US081 does not rewrite `Flight.type()` during import; the DSL `type` on the plan is validated and stored in `DslSource`. See **US083** §3.9 / §12.1 and **US080** §7.2.

---

## 5. Tests

```bash
mvn -pl aisafe.core test -Dtest=ImportFlightPlanFromFileControllerTest,FlightPlanImportTest,FlightDslValidatorTest
```

---

## 6. Sample file

Use `valid-single-leg.flight` with a flight whose route is `OPO-LIS` (e.g., designator TP123).

```flight
flight TP123 {
    route OPO-LIS {
        leg {
            departure OPO
            arrival LIS
            departureTime 2024-07-01T08:00:00Z
            arrivalTime 2024-07-01T09:00:00Z
        }
    }
}
```