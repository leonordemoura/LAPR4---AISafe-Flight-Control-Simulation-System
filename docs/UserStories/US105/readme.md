# 📄 US105 – DOCUMENTAÇÃO (igual estilo US100)

---

# 1. Context

As a Flight Control Operator, I want to initialize a shared simulation environment where multiple flight processes can register their presence and communicate through shared memory and synchronization primitives.

This user story establishes the **base infrastructure for concurrency**, without yet simulating full flight movement (that comes in US106).

---

# 2. Requirements

**Acceptance Criteria**

* US105.1 The system must be implemented in C.
* US105.2 Shared memory must be used for inter-process communication.
* US105.3 Each flight must be executed in a separate process.
* US105.4 A POSIX semaphore must control access to shared memory.
* US105.5 A second semaphore must be reserved for future step synchronization.
* US105.6 The system must handle SIGINT (CTRL+C).
* US105.7 All resources must be correctly released at shutdown.

---

# 3. Analysis

US105 introduces the **core concurrency backbone** of the system:

* Shared memory (`shm_open + mmap`) is used to store global simulation state
* Each flight is a separate process (`fork`)
* Flights register themselves in shared memory
* Synchronization is handled via POSIX semaphores
* No movement simulation yet (this is intentionally deferred to US106)

---

# 4. Architecture

## Main Process (Controller)

Responsible for:

* initializing shared memory
* creating semaphores
* forking flight processes
* tracking process lifecycle
* handling shutdown signals

## Flight Processes (Children)

Responsible for:

* registering themselves in shared memory
* remaining alive until termination
* future: generating movement (US106)

---

# 5. Data Structures

US105 uses shared memory structure:

```c
typedef struct {
    FlightPosition positions[MAX_FLIGHTS];
    int active[MAX_FLIGHTS];

    int simulation_running;
    int current_step;
} SharedSimulationData;
```

### Purpose

Stores:

* active flights
* current flight positions (initially only registration)
* simulation state

---

# 6. Implementation

---

## 6.1 Signal Handling

SIGINT is used to safely stop simulation:

```c
static volatile sig_atomic_t running = 1;
```

When CTRL+C is pressed:

* running becomes 0
* all loops terminate safely

---

## 6.2 Shared Memory

The controller creates shared memory:

* `shm_open`
* `ftruncate`
* `mmap`

Purpose:

* allow all flight processes to access shared state

---

## 6.3 Semaphores

Two semaphores are defined:

### mutex_sem

Protects shared memory access:

```text
Prevents race conditions
```

### step_sem (reserved)

Used in US106 for step synchronization:

```text
Will trigger simulation steps
```

---

## 6.4 Process Creation

Each flight is created using:

```c
fork();
```

---

## 6.5 Flight Process Behavior

Each child process:

1. registers itself in shared memory
2. marks itself as active
3. stays alive until shutdown signal

```text
(No movement logic yet in US105)
```

---

## 6.6 Parent Process Behavior

The controller:

* stores PIDs
* monitors running state
* keeps simulation alive
* handles shutdown

---

## 6.7 Synchronization Model

US105 introduces a shared-memory + semaphore model:

```text
Flights → Shared Memory ← Controller
          (mutex protected)
```

No pipe system is used in US105.

---

## 6.8 Shutdown Procedure

On SIGINT:

1. controller stops loop
2. sends SIGTERM to all flights
3. waits for termination (waitpid)
4. frees shared memory
5. closes semaphores

---

## 6.9 Process Cleanup

Ensures:

* no zombie processes
* no memory leaks
* no semaphore leaks

---