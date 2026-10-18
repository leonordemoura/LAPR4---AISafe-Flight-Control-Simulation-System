# US108 – Enforce Step-by-Step Simulation Synchronization
### *Sprint 3*

---

## 1. Context

**User Story 108:**

*As a PO, I want the simulation engine to synchronize the simulation's step-by-step progression using semaphores, so that all flight processes and parent threads advance in lockstep through each simulation time step.*

This story builds directly on **US105** (base simulation environment, shared memory, forked flight processes, and named semaphores). It enforces a strict barrier synchronization so that the simulator never advances to the next time step before every active flight has finished computing and writing its position for the current step.

### Dependencies

- **US105** – Base simulation environment: shared memory (`SimulationSharedState`), POSIX named semaphores (`SEM_MUTEX`, `SEM_STEP`), and `fork` of flight processes.
- POSIX Semaphores (`sem_t`) – Named semaphores for inter-process synchronization between the simulator (parent) and the flight processes (children).

### Summary

US108 focuses on:

- **Lockstep progression** — the simulator posts `SEM_STEP` once per active flight at the start of each time step, and waits on `SEM_DONE` until every active flight signals completion.
- **Startup barrier** — the simulator waits for all forked flight processes to mark themselves active in shared memory before dispatching the first step.
- **Safe shutdown** — on termination, the simulator unblocks any flight waiting on `SEM_STEP` before sending `SIGTERM`, preventing deadlocks during cleanup.

---

## 2. Requirements

### Acceptance Criteria

- Semaphores are used to control the progression of each simulation time step.
- All flight processes must complete their current step before the simulator advances to the next one.
- The simulator must wait for all flight processes to be ready before starting the first step.
- The system must handle safe termination without leaving processes blocked on semaphores.

### Refined Acceptance Criteria

| ID      | Criterion                                                                                                                                                      |
|---------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| US108.1 | A named semaphore `SEM_STEP` (initial value 0) is created by the simulator and opened by each flight process to gate step progression.                         |
| US108.2 | A named semaphore `SEM_DONE` (initial value 0) is created by the simulator; each flight posts it once after completing a step, and the simulator waits on it.  |
| US108.3 | Before the main loop starts, the simulator waits until all flight processes have set `shm->active[i] = 1` (startup barrier).                                   |
| US108.4 | Each iteration of the main loop posts `SEM_STEP` once per active flight, then waits on `SEM_DONE` exactly `active_count` times before advancing.              |
| US108.5 | On shutdown, the simulator posts `SEM_STEP` once per flight and sends `SIGTERM` to unblock and terminate any flight still waiting on the semaphore.            |

---

## 3. Analysis

### Data Structures

The `SimulationSharedState` structure (in `common.h`) contains the fields used by the lockstep mechanism:

```c
typedef struct {
    // Written by flight processes, read by the simulator
    FlightPositionShared positions[MAX_FLIGHTS];
    int active[MAX_FLIGHTS];

    // Written by the simulator, read by all
    int simulation_running;
    int current_step;

    // US109: trajectory history accumulated each step
    FlightHistory histories[MAX_FLIGHTS];

} SimulationSharedState;
```

The three named semaphores used are defined as constants in `common.h`:

```c
#define SEM_MUTEX "/sim_mutex"   // mutual exclusion on shared memory
#define SEM_STEP  "/sim_step"    // simulator → flights: advance one step
#define SEM_DONE  "/atc_done"    // flights → simulator: step complete
```

### Lockstep Synchronization Logic

The core of US108 is the barrier pattern between the simulator (parent process) and the flight processes (children).

**Simulator (Producer of permission, Consumer of completion):**
1. Waits for all flights to be active (startup barrier).
2. On each step: counts active flights, posts `SEM_STEP` once per active flight.
3. Waits on `SEM_DONE` exactly `active_count` times before advancing.

**Flight Process (Consumer of permission, Producer of completion):**
1. At the start of each step: waits on `SEM_STEP`.
2. Computes new position and writes it to shared memory under `SEM_MUTEX`.
3. Posts `SEM_DONE` to signal the simulator that the step is complete.

---

## 4. Design

### 4.1 Semaphore Initialization (`simulation.c` — `start_simulation`)

The three semaphores are created before forking any flight process:

```c
mutex_sem = sem_open(SEM_MUTEX, O_CREAT, 0666, 1); // mutex: starts unlocked
step_sem  = sem_open(SEM_STEP,  O_CREAT, 0666, 0); // step: starts locked
done_sem  = sem_open(SEM_DONE,  O_CREAT, 0666, 0); // done: starts locked
```

### 4.2 Startup Barrier (`simulation.c` — `start_simulation`)

After forking all flight processes, the simulator polls shared memory until every flight has marked itself active:

```c
int all_ready = 0;
while (!all_ready) {
    all_ready = 1;
    if (safe_sem_wait(mutex_sem) != -1) {
        for (int i = 0; i < config.numFlights; i++) {
            if (shm->active[i] == 0) { all_ready = 0; break; }
        }
        sem_post(mutex_sem);
    }
    if (!all_ready) usleep(1000);
}
```

Each flight sets `shm->active[index] = 1` immediately on startup in `flight.c`, before waiting on `SEM_STEP`.

### 4.3 Main Loop — Lockstep Barrier (`simulation.c` — `start_simulation`)

```c
// Post SEM_STEP once per active flight
int active_count = 0;
if (safe_sem_wait(mutex_sem) == -1) break;
for (int i = 0; i < config.numFlights; i++) {
    if (shm->active[i]) {
        sem_post(step_sem);
        active_count++;
    }
}
sem_post(mutex_sem);

// Wait for all active flights to complete their step
for (int i = 0; i < active_count; i++) {
    if (safe_sem_wait(done_sem) == -1) break;
}
```

### 4.4 Flight Step Gate (`flight.c` — `run_flight`)

```c
// Wait for the simulator to permit this step
if (safe_sem_wait(step_sem) == -1) { interrupted = 1; break; }

// Compute new position and write under mutex
if (safe_sem_wait(mutex_sem) == -1) { interrupted = 1; break; }
shm->positions[index] = pos;
if (shm->histories[index].count < MAX_HISTORY)
    shm->histories[index].positions[shm->histories[index].count++] = pos;
sem_post(mutex_sem);

// Signal step complete
sem_post(done_sem);
```

### 4.5 Shutdown Flow

To ensure no flight remains blocked on `SEM_STEP` when the simulation ends:

```c
// Unblock any flight waiting on SEM_STEP
for (int i = 0; i < config.numFlights; i++)
    sem_post(step_sem);

// Terminate remaining flight processes
for (int i = 0; i < config.numFlights; i++)
    kill(pids[i], SIGTERM);

// Collect all child processes
for (int i = 0; i < config.numFlights; i++)
    waitpid(pids[i], &status, 0);
```

This is executed in the shutdown phase of `start_simulation`.

---

## 5. Implementation

### Key Files

| File             | Role                                                                                                          |
|------------------|---------------------------------------------------------------------------------------------------------------|
| `simulation.c`   | Creates semaphores; startup barrier; posts `SEM_STEP` and waits on `SEM_DONE` each step; shutdown unblocking. |
| `flight.c`       | Opens semaphores; waits on `SEM_STEP` before each step; writes position under `SEM_MUTEX`; posts `SEM_DONE`.  |
| `common.h`       | Defines semaphore name constants (`SEM_MUTEX`, `SEM_STEP`, `SEM_DONE`) and `SimulationSharedState`.           |

---

## 6. Verification Against Acceptance Criteria

| Criterion                                          | Implementation                                                                                                             |
|----------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------|
| US108.1 — `SEM_STEP` gates step progression        | Created with initial value 0; flight blocks on `sem_wait(step_sem)` at the start of every step.                            |
| US108.2 — `SEM_DONE` signals step completion       | Each flight calls `sem_post(done_sem)` after writing its position; simulator calls `sem_wait(done_sem)` per active flight. |
| US108.3 — Startup barrier                          | Simulator polls `shm->active[i]` under `SEM_MUTEX` until all flights are ready before entering the main loop.              |
| US108.4 — Lockstep enforced every step             | Simulator posts `SEM_STEP` exactly `active_count` times and waits on `SEM_DONE` exactly `active_count` times.              |
| US108.5 — Safe shutdown without deadlock           | Simulator posts `SEM_STEP` once per flight before sending `SIGTERM`; `safe_sem_wait` exits on `running == 0`.              |