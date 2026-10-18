# US120 - Flight DSL Specification and Validation

## 1. Context

This User Story aligns the **Core Flight DSL** with the project's base requirements as per the Sprint 2 feedback. The DSL is used to formally define flight plans, which are then used for simulation and flight management.

US120 introduces mandatory physical properties to flight segments and enforces a strict format for flight identifiers to ensure consistency across the system.

---

## 2. Objectives

The US120 update to the Flight DSL achieves:
- **Strict Flight Identification**: Enforcement of standard airline flight designators.
- **Enhanced Segment Physics**: Inclusion of mandatory `width` and `wind` properties for every trajectory segment.
- **Robust Semantic Validation**: Validation of the new physical properties to ensure realistic simulation data.
- **Seamless Integration**: Full validation during both import (US081) and activation (US083/US085).

---

## 3. Updated Specifications

### 3.1 Strict Flight Designator
The flight identifier (designator) must follow a specific pattern:
- **Airline Code**: 2 uppercase letters (IATA).
- **Flight Number**: 1 to 4 digits.
- **Operational Suffix**: Optional single uppercase letter.

**Examples**: `TP123`, `AA4567`, `UA88X`.
**Rejected**: `tp123` (lowercase), `ABC123` (3 letters), `TP12345` (too many digits).

### 3.2 Segment Physical Properties
Every `segment` block must now include the following mandatory fields:

| Field        | Meaning         | Unit / Format                        | Example              |
|--------------|-----------------|--------------------------------------|----------------------|
| **altitude** | Flight level    | `m` (meters)                         | `altitude 12000 m`   |
| **width**    | Corridor width  | `km` (kilometers)                    | `width 10 km`        |
| **wind**     | Wind conditions | `deg` (direction) + `value` + `unit` | `wind 180 deg 15 kt` |

#### Wind Units Supported:
- `mps` (meters per second)
- `kmh` (kilometers per hour)
- `kt` (knots)

---

## 4. Formal Grammar (V2)

The `segment` rule in `FlightDSL.g4` has been updated as follows:

```antlr
segment
    : SEGMENT LBRACE
        START coordinates
        END coordinates
        ALTITUDE NUMBER UNIT_M
        WIDTH NUMBER UNIT_KM
        WIND NUMBER DEG NUMBER windUnit
      RBRACE
    ;

windUnit
    : UNIT_MPS
    | UNIT_KMH
    | UNIT_KT
    ;
```

---

## 5. Updated Semantic Validation

The `FlightDslSemanticValidator` now enforces these additional rules:

1. **Width**: Must be a positive number (`width > 0`).
2. **Wind Direction**: Must be between 0 and 359 degrees inclusive.
3. **Wind Speed**: Must be a non-negative number (`wind speed >= 0`).
4. **Altitude**: Must be a positive number (`altitude > 0`).

Failure to meet these criteria results in a **Semantic Error** with the corresponding line and column information.

---

## 6. Import and Activation Workflow

Unlike previous versions where semantic validation was deferred, US120 enforces **full validation during import**:

1. **Import (US081)**: `ImportFlightPlanFromFileController` calls the validator. Any lexical, syntactic, or **semantic** error (like an invalid wind direction) will cause the import to fail. The flight plan will NOT be saved as a draft if it is invalid.
2. **Activation (US083/US085)**: Re-validates the DSL before exporting to SCOMP JSON, ensuring the plan is still valid within the current system state.

---

## 7. Example of Valid US120 DSL

```txt
flight TP123 {
    type REGULAR

    leg {
        departure OPO 2026-05-11 08:00
        arrival LIS 2026-05-11 09:00

        route {
            origin OPO
            destination LIS
        }

        segment {
            start (41.242, -8.678)
            end (38.774, -9.134)
            altitude 12000 m
            width 10 km
            wind 180 deg 15 kt
        }

        fuel 5000 kg
    }
}
```

---

## 8. Verification

### 8.1 Automated Tests
Run the following command to verify the US120 implementation:

```bash
mvn test -pl aisafe.core -Dtest=FlightDsl*Test,DslModelTest
```

### 8.2 Manual Verification
Use the sample files in the `FlightPlans/` directory:
- `valid-single-leg`: Should import successfully.
- `test-width-wind-invalid.flight`: Should be rejected with clear semantic error messages.
