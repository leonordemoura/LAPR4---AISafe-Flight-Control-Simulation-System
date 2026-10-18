# US121 – Create a Flight Plan from a File

### *Sprint 3*

---

## 1. Context

**User Story 121:**

*As a Pilot, I want to create a valid flight plan from a file.*

There may be multiple file formats; the system validates the file format before use. **Core Flight DSL** validation is provided by **US120**.

### Dependencies


| Story     | Role                                                       |
| --------- | ---------------------------------------------------------- |
| **US120** | Grammar, lexer/parser, semantic rules,`FlightDslValidator` |
| **US030** | Authentication (`PILOT` role required)                     |
| **US080** | Flight aggregate and `FlightPlan` domain model            |

---

## 2. Acceptance Criteria


| Criterion                               | Implementation                                                   |
| --------------------------------------- | ---------------------------------------------------------------- |
| File must conform to Flight DSL         | `DslFlightPlanImporter` → `FlightDslValidator.validate` (US120) |
| Validated according to US120            | Full lexical, syntactic and semantic analysis before persistence |
| Invalid files produce meaningful errors | `ImportFlightPlanError` with line/column for DSL errors          |
| Only valid plans are imported           | Controller persists only after full success                      |
| Multiple formats detected before use    | `FlightPlanFormatDetector` rejects unknown/JSON formats early    |

---

## 3. Execution Flow

1. Pilot provides:

   - Flight designator (airline + number + optional suffix)
   - Path to `.flight` file
2. System:

   - Reads file (UTF-8, empty/size checks)
   - Detects format (Core Flight DSL vs JSON vs unknown)
   - Validates DSL via US120 (`FlightDslValidator`)
   - Retrieves `Flight` from repository
   - Maps DSL → `FlightPlan` domain object
   - Validates route compatibility and pilot schedule
   - Persists `Flight` with imported plan (status: DRAFT)
3. Output:

   - Success → `FlightPlan` stored on the `Flight` aggregate
   - Failure → list of formatted errors (no persistence)

### Entry point

```java
ImportFlightPlanFromFileController controller = new ImportFlightPlanFromFileController();

FlightDesignator flightId = FlightDesignator.of("TP", 123, null);

ImportFlightPlanResult result = controller.importFlightPlanFromFile(
        flightId,
        Path.of("FlightPlans/valid-single-leg.flight"));

if (result.isSuccess()) {
    result.flightPlan().ifPresent(fp ->
            System.out.println("Imported: " + fp.routeSignature()));
} else {
    result.formattedErrors().forEach(System.out::println);
}
```

---

## 4. Components


| Component        | Class                                                           |
| ---------------- | --------------------------------------------------------------- |
| Use case         | `ImportFlightPlanFromFileController`                            |
| Console UI       | `ImportFlightPlanFromFileUI` / `ImportFlightPlanFromFileAction` |
| Remote (US086)   | `PilotRemoteCommandHandler` → `IMPORT_FLIGHT_PLAN_FILE`        |
| File read        | `FlightPlanFileReader` (UTF-8, empty, max 512 KB)               |
| Format detection | `FlightPlanFormatDetector`                                      |
| DSL validation   | `FlightDslValidator` (US120)                                    |
| DSL import       | `DslFlightPlanImporter`                                         |
| Domain mapping   | `FlightPlan.fromDslImport` + `Flight.importFlightPlan`          |
| Persistence      | `ImportFlightPlanService` + `FlightRepository.save`             |

### Supported formats


| Format                                                        | Detection                   | Status                          |
| ------------------------------------------------------------- | --------------------------- | ------------------------------- |
| Core Flight DSL (`.flight` or content starting with `flight`) | Extension or content prefix | **Supported**                   |
| JSON flight document (`.json`, `"Leg"` keys)                  | Content heuristics          | **Rejected** with clear message |
| Other (`.txt`, random text)                                   | No match                    | **Rejected** as unsupported     |

---

## 5. Error Codes


| Code                             | Meaning                                                         |
| -------------------------------- | --------------------------------------------------------------- |
| `EMPTY_FILE`                     | File has no content                                             |
| `FILE_TOO_LARGE`                 | Exceeds 512 KB limit                                            |
| `FILE_READ_ERROR`                | I/O or missing file                                             |
| `UNSUPPORTED_FORMAT`             | Not Core Flight DSL                                             |
| `DSL_VALIDATION`                 | Lexical/syntax/semantic DSL errors (line/column when available) |
| `FLIGHT_NOT_FOUND`               | Unknown flight designator                                       |
| `FLIGHT_ID_MISMATCH`             | DSL`flight` id ≠ selected designator                           |
| `ROUTE_MISMATCH`                 | Plan route ≠ flight route                                      |
| `PILOT_NOT_FOUND`                | Authenticated user is not an active pilot                       |
| `PILOT_SCHEDULE_CONFLICT`        | Overlapping pilot schedule                                      |
| `ACTIVE_PLAN_CANNOT_BE_REPLACED` | Cannot replace active plan                                      |
| `FLIGHT_PLAN_NOT_REPLACEABLE`    | Cannot replace validated plan                                   |

---

## 6. Sample Files

Sample DSL files are in the `FlightPlans/` directory at the project root:


| File                                    | Expected result                       |
| --------------------------------------- | ------------------------------------- |
| `valid-single-leg.flight`               | DSL validation passes                 |
| `valid-two-legs.flight`                 | DSL validation passes                 |
| `test-width-wind-invalid.flight`        | Rejected (semantic: width/wind rules) |
| `error-invalid-flightDesignator.flight` | Rejected (syntax/semantic)            |
| `different-fuel-units.flight`           | Rejected (invalid fuel unit)          |

Use `valid-single-leg.flight` with a flight whose route is `OPO-LIS` (e.g. designator `TP123`).

---

## 7. Tests

```bash
mvn -pl aisafe.core test -Dtest=ImportFlightPlanFromFileControllerTest,FlightPlanImportInfrastructureTest,ImportFlightPlanSampleFilesTest
```

---

## 8. Diagrams

- Class diagram: `classDiagram/us121_ClassDiagram.puml`
- Sequence diagram: `sequenceDiagram/us121_SequenceDiagram.puml`
