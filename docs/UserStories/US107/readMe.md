# US107 – Generate Real-Time Violation Reports
### *Sprint 3*

---

## 1. Context

**User Story 107:**  

*As a simulation system, I want a separate thread to receive violation notifications from the detection thread and generate real-time reports, so that I/O operations do not block or slow down the main simulation loop.*

This story builds directly on **US105** (base simulation environment, shared memory, and threads) and **US102** (safety violation detection and logging). It introduces a dedicated thread for generating reports, communicating with the violation detector thread via a condition variable.

### Dependencies

- **US105** – Base simulation environment, shared memory, thread creation (`violation_thread` and `report_thread`).
- **US102** – Safety violation detection logic, which provides the actual violation data to be reported.
- POSIX Threads (pthreads) – Mutexes and condition variables for inter-thread synchronization.

### Summary

US107 focuses on:

- **Decoupling** the computationally intensive violation detection from the potentially blocking I/O of report generation.
- **Inter-thread communication** using a POSIX condition variable (`pthread_cond_t`).
- **Reporting** violations to a log file (`log/violations.log` or a fallback) immediately when they are detected, without waiting for the simulation step to finish completely.
- **Safe shutdown** of the report thread when the simulation terminates.

---

## 2. Requirements

### Acceptance Criteria

- The system must have a dedicated thread for generating real-time violation reports.
- The report thread must wait for notifications from the violation detection thread.
- Communication between the two threads must be done using a POSIX condition variable.
- The report thread must log the violations to a file.
- The reporting mechanism must not block the main simulation loop or the detection thread.
- The system must handle the safe termination of the report thread when the simulation ends.

### Refined Acceptance Criteria

| ID      | Criterion                                                                                                                                                                                                 |
|---------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| US107.1 | The simulation initializes a `pthread_cond_t` along with the main `pthread_mutex_t` in the shared memory.                                                                                                 |
| US107.2 | The `report_thread` runs in a loop, waiting on the condition variable (`pthread_cond_wait`) while a `new_violation_flag` is 0.                                                                            |
| US107.3 | The `violation_thread` (or the main loop, depending on the architecture) sets `new_violation_flag = 1` and signals the condition variable (`pthread_cond_signal`) when a new batch of violations is ready. |
| US107.4 | Upon waking up, the `report_thread` writes the violations to the log file (using the logic from US102/reportGeneration.c) and resets the flag.                                                            |
| US107.5 | During simulation shutdown, a broadcast or signal is sent to the condition variable to wake up the `report_thread` so it can check the `running` flag and terminate cleanly.                              |

---

## 3. Analysis

### Data Structures

The `SharedSimulationData` structure (in `common.h`) must contain the necessary synchronization primitives and communication flags.

```c
typedef struct {
    // ... existing fields (positions, active, running, step, etc.) ...
    
    // US107: Communication from Violation Thread to Report Thread
    int new_violation_flag;
    int num_violations;
    int global_violation_count;
    SafetyViolationEvent violations[MAX_VIOLATIONS];

    pthread_mutex_t mutex;
    pthread_cond_t cond;

} SharedSimulationData;
```

### Inter-Thread Synchronization Logic

The core of US107 is the producer-consumer pattern between the thread detecting violations and the thread reporting them.

**Producer (Violation Thread / Main Loop):**
1. Locks the mutex.
2. Detects violations.
3. If violations are found, populates the `violations` array, increments counters, and sets `new_violation_flag = 1`.
4. Calls `pthread_cond_signal(&shm->cond)`.
5. Unlocks the mutex.

**Consumer (Report Thread):**
1. Locks the mutex.
2. Enters a `while` loop: `while (shm->new_violation_flag == 0 && running) { pthread_cond_wait(&shm->cond, &shm->mutex); }`
3. Checks if `running == 0` to exit cleanly.
4. Processes the violations (writes to log).
5. Resets `new_violation_flag = 0` and `num_violations = 0`.
6. Unlocks the mutex.

---

## 4. Design

### 4.1 Initialization (`simulation.c` — `start_simulation`)

The mutex and condition variable are initialized before starting the threads:

```c
    pthread_mutex_init(&shm->mutex, NULL);
    pthread_cond_init(&shm->cond, NULL);
```

### 4.2 Report Thread (`simulation.c` — `report_thread`)

```c
void *report_thread(void *arg)
{
    SharedSimulationData *shm = (SharedSimulationData *)arg;

    while (running) {
        pthread_mutex_lock(&shm->mutex);

        // US107: Wait for a violation or for the simulation to stop.
        while (shm->new_violation_flag == 0 && running) {
            pthread_cond_wait(&shm->cond, &shm->mutex);
        }

        if (!running) {
            pthread_mutex_unlock(&shm->mutex);
            break;
        }

        if (shm->new_violation_flag) {
             log_live_violations(shm->current_step, shm->violations, shm->num_violations);
             shm->new_violation_flag = 0;
             shm->num_violations = 0; // Reset for next batch
        }

        pthread_mutex_unlock(&shm->mutex);
    }
    return NULL;
}
```

### 4.3 Shutdown Flow

To ensure the report thread does not hang indefinitely on `pthread_cond_wait` when the simulation ends:

```c
    // Wake up report_thread so it can terminate
    pthread_mutex_lock(&shm->mutex);
    pthread_cond_broadcast(&shm->cond);
    pthread_mutex_unlock(&shm->mutex);
```

This is executed in the shutdown phase of `start_simulation`.

---

## 5. Implementation

### Key Files

| File                 | Role                                                                                              |
|----------------------|---------------------------------------------------------------------------------------------------|
| `simulation.c`       | Thread creation, `report_thread` logic, `pthread_cond_wait` / `signal` / `broadcast` usage.       |
| `reportGeneration.c` | Formatting and writing the violations to the log file.                                            |
| `common.h`           | Definition of `SharedSimulationData` including `pthread_cond_t` and `new_violation_flag`.         |

---

## 6. Verification Against Acceptance Criteria

| Criterion                                                | Implementation                                                                                               |
|----------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| US107.1 — Initialize condition variable                  | `pthread_cond_init` is called during shared memory setup.                                                    |
| US107.2 — Report thread waits on condition               | `report_thread` uses `while(!flag) pthread_cond_wait(...)`.                                                  |
| US107.3 — Violation detection signals condition          | `violation_thread` sets `new_violation_flag = 1` and calls `pthread_cond_signal`.                            |
| US107.4 — Report thread logs and resets                  | `report_thread` calls `log_live_violations` and resets `new_violation_flag = 0`.                             |
| US107.5 — Safe termination                               | Main shutdown sequence calls `pthread_cond_broadcast` to wake up the reporting thread for termination check. |
