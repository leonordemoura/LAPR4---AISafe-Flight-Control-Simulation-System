# US102 – Detect Aircraft Safety Violations in Real Time
### *Sprint 2*

---

## 1. Context

**User Story 102:**  

*As a simulation system, I want to continuously monitor aircraft positions for overlaps so that I can identify and report safety violations.*

This story extends the simulation stack built in **US100** (process model, pipes, signals, parameter validation) and **US101** (position updates and history). It builds directly on **US103** (step-by-step synchronization), which ensures that violation checks are performed at well-defined, synchronized instants rather than opportunistically.

### Dependencies

- **US100** – Simulation process model, pipes, signal infrastructure, parameter validation.
- **US101** – Position capture and `FlightHistory` accumulation.
- **US103** – Step-by-step synchronization (`ready_fd` + `SIGUSR2`), which guarantees that all active flight positions processed in a given call to `capture_violation` belong to the same simulation step.

### Summary

US102 focuses on:

- Per-step evaluation of aircraft proximity against configurable safety rules.
- **Logging** each violation with timestamp, flight IDs, positions, distances, and velocity vectors.
- **Notifying** involved flight processes via `SIGUSR1`.
- **Robust signal handling** in child processes — `SIGUSR1` masks all other signals during the handler.
- **Early termination** when the violation count reaches the configured threshold, followed by clean shutdown of all child processes.

---

## 2. Requirements

### Acceptance Criteria

- The system must detect when two or more aircraft violate safety separation rules.
- Upon detecting a violation, the system must **log** the event and **notify** the involved aircraft via signals.
- Each flight process must **handle** the received signal and **notify the system user** with a message.
- When a flight process receives **`SIGUSR1`**, it must **block other signals** while handling it.
- The system must allow **early termination** if safety violations exceed a **predefined threshold**, by sending termination signals to all aircraft.
- Flight processes must handle termination signals correctly and perform any necessary cleanup.

### Refined Acceptance Criteria

| ID      | Criterion                                                                                                                                                                                                             |
|---------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| US102.1 | The controller maintains sufficient state (positions per step) to evaluate separation between all pairs of active flights.                                                                                            |
| US102.2 | On each detected violation, an entry is appended to the **simulation log** with timestamp, distances, and identifiers of involved flights.                                                                            |
| US102.3 | The controller sends **`SIGUSR1`** to each involved child PID, only if that flight is still active (`flight_active[i] == 1`).                                                                                         |
| US102.4 | Flight processes install a `SIGUSR1` handler via `sigaction` with **`sigfillset(&sa_usr.sa_mask)`** — all signals are blocked for the duration of the handler.                                                        |
| US102.5 | Flight processes emit a **user-visible message** to stderr via `write` (async-signal-safe) when a violation signal is received.                                                                                       |
| US102.6 | A **global violation counter** is compared to the threshold; when reached, `capture_violation` returns `1`, the main loop sets `early_stop = 1`, sends `SIGTERM` to all flights, and calls `waitpid` on all children. |
| US102.7 | On normal or early shutdown, **no zombie processes** remain; all pipes and file descriptors are closed cleanly.                                                                                                       |

---

## 3. Analysis

### Data Structures

```c
typedef struct {
    double horizontal_separation;   /* Minimum allowed horizontal distance (degrees) */
    double vertical_separation;     /* Minimum allowed vertical distance (m or ft)   */
    int    violation_threshold;     /* Max violations before early stop              */
} SafetyRules;
```

```c
typedef struct {
    long timestamp_sec;

    int            flightA;
    FlightPosition posA;
    VelocityVector velA;      

    int            flightB;
    FlightPosition posB;
    VelocityVector velB;

    double horizontal_distance;   /* degrees — hypot(Δlat, Δlon)   */
    double vertical_distance;     /* absolute altitude difference   */
} SafetyViolationEvent;
```

```c
typedef struct {
    double vx;   /* km/h — latitude component  */
    double vy;   /* km/h — longitude component */
    double vz;   /* m/s  — altitude component  */
} VelocityVector;
```

### Violation Detection Logic

A violation is recorded when **all three** conditions hold simultaneously for a pair of flights (A, B):

```
hypot(posA.x − posB.x, posA.y − posB.y) < rules.horizontal_separation
fabs(posA.altitude − posB.altitude)      < rules.vertical_separation
posA.timestamp == posB.timestamp
```

The horizontal distance is `hypot(Δlat, Δlon)` in degrees. The vertical distance is `fabs(altA − altB)`.

The **timestamp equality condition** filters out comparisons between flights that are not temporally aligned. Because all flight processes are forked at the same time but each uses its own `departureTime` to compute position timestamps, two flights with different departure times will have different timestamps even in the same simulation step — and will correctly not trigger a violation against each other unless they are genuinely at the same point in time within their respective flight plans.

`find_closest_position` is used to locate the position in flight B's history whose timestamp is closest to flight A's last position timestamp. The equality check that follows ensures only exact matches produce a violation, making `find_closest_position` act as an efficient lookup rather than an approximation.

---

## 4. Design

### 4.1 Controller (`safetyViolation.c` — `capture_violation`)

**Signature:**
```c
int capture_violation(
    FILE                  *log,
    SimulationConfig       config,
    SafetyRules            rules,
    FlightHistory         *history,
    const pid_t            pids[],
    const int              flight_active[],
    int                   *violation_count,
    SafetyViolationEvent  *events
);
```

Called once per step at the end of the position-read loop in `simulation.c`. Responsibilities:

- Iterates over all pairs (a, b) of flights with non-empty history.
- Retrieves the last position of flight A (`history[a].count - 1`).
- Uses `find_closest_position(&history[b], pa->timestamp)` to find the best candidate in B's history.
- Applies the three-condition check above; discards the pair if timestamps differ.
- On violation:
  - Increments `*violation_count` and per-flight `violation_count` in `FlightHistory`.
  - Computes velocity vectors using `compute_velocity_vector`:
    - Flight A: positions `[count-2]` → `[count-1]`
    - Flight B: positions `[flightBIndex-1]` → `[flightBIndex]`
  - Stores the full `SafetyViolationEvent` at index `(*violation_count) − 1`.
  - Appends a structured entry to the simulation log and calls `fflush`.
  - Sends `SIGUSR1` to the involved PIDs **only if** `flight_active[a]` / `flight_active[b]` are non-zero.
- Returns `1` if `*violation_count >= rules.violation_threshold`; `0` otherwise.

### 4.2 Flight Process (`flight.c`)

Signal setup at startup, before the position loop:

| Signal    | Handler           | `sa_mask `                 | Effect                                                  |
|-----------|-------------------|----------------------------|---------------------------------------------------------|
| `SIGUSR1` | `handle_SIGUSR1`  | `sigfillset` — all blocked | Writes warning to stderr; async-signal-safe via `write` |
| `SIGTERM` | `handler_SIGTERM` | `sigemptyset`              | Sets `g_stop = 1`; loop exits cleanly                   |
| `SIGUSR2` | `handler_SIGUSR2` | `sigemptyset`              | Sets `g_next_step = 1`; unblocks `sigsuspend`           |

`SIGUSR2` is blocked via `sigprocmask(SIG_BLOCK, ...)` before the loop and atomically unblocked only inside `sigsuspend`, eliminating the race condition between checking `g_next_step` and sleeping.

**Step synchronization sequence (per position):**

```
flight: write(ready_fd, 1 byte)     ← signals controller it is ready
flight: sigsuspend()                ← atomically waits for SIGUSR2

controller: read(pipe_ready) × N   ← waits for all active flights
controller: kill(SIGUSR2) × N      ← releases all flights simultaneously

flight: write(write_fd, FlightPosition)  ← sends position
controller: read(pipes[i]) × N          ← collects all positions
controller: capture_violation()          ← checks for violations
```

### 4.3 Early Termination Flow

```
capture_violation() returns 1
        │
        ▼
  early_stop = 1  →  break main loop
        │
        ▼
  close(pipe_ready[i][0]) for all i   ← unblocks flights blocked in write()
        │
        ▼
  kill(pids[i], SIGTERM)  for all i
        │
        ▼
  waitpid(pids[i], ...)   for all i
        │
        ▼
  close(pipes[i][0])      for all i
        │
        ▼
  generate_report(... early_stop=1 ...)
```

Closing `pipe_ready[i][0]` **before** `SIGTERM` is intentional: any flight blocked in `write(ready_fd, ...)` will receive `EPIPE` and exit via `if (errno == EPIPE || g_stop) break`, avoiding a deadlock.

---

## 5. Implementation

### Key Files

| File                | Role                                                                             |
|---------------------|----------------------------------------------------------------------------------|
| `safetyViolation.c` | `capture_violation()`, `check_fuel()`                                            |
| `flight.c`          | Signal handlers, position emission, step synchronization via `sigsuspend`        |
| `simulation.c`      | Main loop, step coordination, process lifecycle, shutdown sequence               |
| `common.h`          | `SafetyRules`, `SafetyViolationEvent`, `VelocityVector`, `FlightHistory`         |
| `util.c`            | `find_closest_position()`, `compute_velocity_vector()`, `calculate_kilometers()` |

### Signal Table

| Signal    | Direction      | Handler location                | Effect                                                |
|-----------|----------------|---------------------------------|-------------------------------------------------------|
| `SIGUSR1` | Parent → Child | `flight.c :: handle_SIGUSR1`    | Warning to stderr; all signals blocked during handler |
| `SIGUSR2` | Parent → Child | `flight.c :: handler_SIGUSR2`   | Sets `g_next_step = 1`; releases `sigsuspend`         |
| `SIGTERM` | Parent → Child | `flight.c :: handler_SIGTERM`   | Sets `g_stop = 1`; flight exits after current step    |
| `SIGINT`  | User → Parent  | `simulation.c :: handle_SIGINT` | Sets `running = 0`; graceful shutdown                 |

---

## 6. Verification Against Acceptance Criteria

| Criterion                                            |  Implementation                                                                              |
|------------------------------------------------------|----------------------------------------------------------------------------------------------|
| US102.1 — Position state maintained per step         |  `FlightHistory.positions[]` stores all positions; called once per step after all reads      |
| US102.2 — Log on every violation                     |  `fprintf` + `fflush` on every violation in `capture_violation`                              |
| US102.3 — `SIGUSR1` to involved PIDs                 |  Guarded by `flight_active[a/b]` check before `kill`                                         |
| US102.4 — Block all signals during `SIGUSR1` handler |  `sigfillset(&sa_usr.sa_mask)` in `flight.c`                                                 |
| US102.5 — User-visible message on violation          |  `write(STDERR_FILENO, msg, ...)` — async-signal-safe                                        |
| US102.6 — Early stop on threshold                    |  `capture_violation` returns `1`; caller sets `early_stop`, sends `SIGTERM`, calls `waitpid` |
| US102.7 — No zombies; clean resource release         |  `waitpid` loop + all pipe FDs closed; `pipe_ready` closed before `SIGTERM`                  |