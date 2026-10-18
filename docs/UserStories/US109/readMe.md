# US109 – Generate and Store Final Simulation Report
### *Sprint 2*

---

## 1. Context

**User Story 109:**

*As a Flight Control Operator, I want a comprehensive report that details the simulation outcomes — including flight execution statuses, safety violation events (with timestamps, positions and velocity vectors), and overall validation results — so that I can assess the safety and performance of the flights post-simulation.*

This story builds directly on **US102** (safety violation detection and event recording) and **US111** (basic report generation). It formalises and extends the report output to include velocity vectors, per-flight performance scores, and a clear pass/fail verdict.

### Dependencies

- **US100** – Simulation lifecycle, flight history collection.
- **US102** – `SafetyViolationEvent` array populated during the simulation run.

### Summary

US109 focuses on:

- Aggregating all simulation data **after** the run concludes (normal end, early stop, or error abort).
- Writing a **structured, human-readable report** to a timestamped file under `reports/`.
- Including **velocity vectors** for each violation event.
- Computing a **performance score** per flight based on violation-free positions.
- Producing a clear **SUCCEEDED / FAILED / ABORTED** verdict.
- Handling the special case of **fuel failure** with a dedicated minimal report.

---

## 2. Requirements

### Acceptance Criteria

- The report generation thread aggregates data once the simulation concludes.
- The report includes the total number of flights, individual execution statuses, and detailed safety violation events.
- The final validation result (pass/fail) is clearly indicated.
- The complete report is saved to a file for future reference.

### Refined Acceptance Criteria

| ID      | Criterion                                                                                                                                                                 |
|---------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| US109.1 | The report is written to a **new, uniquely named file** under `reports/`, using the simulation end timestamp as a suffix.                                                 |
| US109.2 | The report header includes total flight count, total violation count, and the configured violation threshold.                                                             |
| US109.3 | For each flight, the full **position history** is listed with formatted timestamps and coordinates.                                                                       |
| US109.4 | For each safety violation, the report includes: violation number, timestamp, horizontal and vertical distances, involved flight IDs, positions, and **velocity vectors**. |
| US109.5 | A **per-flight performance score** (percentage of violation-free positions) is computed and included, unless the simulation was aborted early due to error.               |
| US109.6 | The report concludes with one of three verdicts: **SIMULATION SUCCEEDED**, **SIMULATION FAILED** (threshold reached), or **SIMULATION ABORTED** (unexpected early stop).  |
| US109.7 | A separate minimal report is generated when the simulation is aborted due to **insufficient fuel**, before any flight processes are started.                              |

---

## 3. Analysis

### Report Structure

```
========= SIMULATION REPORT =========
Total Flights: N
Total Violations: V
Violation Threshold: T

-----------------------------------

FLIGHT HISTORIES:
  Flight <id>:
    Time: HH:MM  Position: (x, y)  Altitude: z
    ...

-----------------------------------

SAFETY VIOLATIONS:                        (only if V > 0)
  Violation #n:
    Time: HH:MM
    Horizontal Distance: d_h  Vertical Distance: d_v
    Flight A: Position: (x, y)  Altitude: z  Time: HH:MM
    Flight B: Position: (x, y)  Altitude: z  Time: HH:MM

-----------------------------------

FLIGHT STATUS:
  Flight <id>:
    Total Violations: n
    Performance: P.PP          (or "Unable to calculate" if aborted)
  Flight <id>: had no violations! Performance was at 100

<verdict line>

========= END OF REPORT =========
```

### Performance Score Formula

```
performance = ((total_positions - violation_positions) / total_positions) × 100
```

Where `violation_positions` is `FlightHistory.violation_count` and `total_positions` is `FlightHistory.count`.

A score of `100` means the flight operated with zero violations throughout the entire simulation. The score is omitted (replaced by an explanatory message) when `early_stop == 0`, i.e., when the simulation was aborted due to an unexpected error rather than threshold or time-out.

### Verdict Logic

| Condition                                         | Verdict              | 
|---------------------------------------------------|----------------------|
| `violation_count >= threshold && early_stop != 0` | SIMULATION FAILED    |
| `early_stop == 0`                                 | SIMULATION ABORTED   |
| Otherwise                                         | SIMULATION SUCCEEDED |

---

## 4. Design

### 4.1 `generate_report` (`reportGeneration.c`)

**Signature:**
```c
int generate_report(
    FlightHistory         *history,
    int                    number_flights,
    SafetyViolationEvent  *events,
    int                    threshold,
    int                    violation_count,
    int                    early_stop
);
```

**Parameters:**

| Parameter         | Description                                                                                                |
|-------------------|------------------------------------------------------------------------------------------------------------|
| `history`         | Array of `FlightHistory`, one per flight, containing all recorded positions and per-flight violation count |
| `number_flights`  | Number of active flights in the simulation                                                                 |
| `events`          | Array of `SafetyViolationEvent`, one per recorded violation                                                |
| `threshold`       | Violation threshold from `SafetyRules`                                                                     |
| `violation_count` | Total violations detected during the run                                                                   |
| `early_stop`      | `1` = simulation ended due to threshold or time-out; `0` = aborted due to unexpected error                 |

**Returns:** `0` on success, `-1` if the report file cannot be created.

**Behaviour:**

1. Generates a timestamped filename: `reports/report_YYYY-MM-DD_HH:MM:SS.txt`.
2. Creates the `reports/` directory if it does not exist (`mkdir` with mode `0755`).
3. Opens the file and writes each section sequentially.
4. Uses `turn_time_into_char` to convert integer timestamps (minutes since midnight) to `HH:MM` strings.
5. Closes the file and returns.

### 4.2 `generate_report_fuel_failure` (`reportGeneration.c`)

**Signature:**
```c
void generate_report_fuel_failure(int number_flights);
```

Called by `start_simulation` **before** forking any child process, when at least one flight has insufficient fuel. Produces a minimal report containing only the flight count and the `SIMULATION ABORTED` verdict. Uses the same filename convention as `generate_report`.

### 4.3 Integration Point (`simulation.c`)

```c
// after waitpid loop and pipe cleanup:
int result = generate_report(
    history,
    config.numFlights,
    events,
    threshold,
    violation_count,
    early_stop          // 1 = ended normally or by threshold; 0 = unexpected abort
);
```

The `early_stop` flag is set to `1` by the main loop (threshold reached or time expired) and remains `0` only if the loop exits abnormally. This flag drives both the performance section and the verdict.

---

## 5. Implementation

### Key Files

| File                 | Role                                                                             |
|----------------------|----------------------------------------------------------------------------------|
| `reportGeneration.c` | `generate_report()` and `generate_report_fuel_failure()`                         |
| `simulation.c`       | Calls report generation after cleanup; passes `early_stop` and `violation_count` |
| `safetyViolation.c`  | Populates `SafetyViolationEvent[]` during the run                                |
| `util.c`             | `turn_time_into_char()` — timestamp formatting                                   |
| `common.h`           | `FlightHistory`, `SafetyViolationEvent`, `FlightPosition`, `VelocityVector`      |

### Report File Location

Reports are written to `reports/` relative to the working directory. The directory is created automatically if absent. Each run produces exactly one report file. File names are unique by timestamp (second precision).

---

## 6. Verification Against Acceptance Criteria

| Criterion                                        | Status | Notes                                                                |
|--------------------------------------------------|--------|----------------------------------------------------------------------|
| US109.1 — Unique timestamped file in `reports/`  | ✅      | `strftime` + `mkdir` in `generate_report`                            |
| US109.2 — Header with totals and threshold       | ✅      | First block of the report                                            |
| US109.3 — Full position history per flight       | ✅      | Inner loop over `history[i].positions[j]`                            |
| US109.4 — Violation detail with velocity vectors | ✅      | Positions and distances written;                                     |
| US109.5 — Per-flight performance score           | ✅      | Computed and written; suppressed with message when `early_stop == 0` |
| US109.6 — Pass/fail verdict                      | ✅      | Three-branch logic at end of report                                  |
| US109.7 — Fuel failure minimal report            | ✅      | `generate_report_fuel_failure` called before fork                    |