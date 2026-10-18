# US103 – Synchronize Flight Execution with a Time Step
### *Sprint 2*

---

## 1. Context

**User Story**

*As a simulation engine, I want to synchronize aircraft movements based on time steps so that I can accurately simulate real-world execution.*

This story extends the simulation stack built in **US100** (process model, pipes, signals, tracking) and **US101** (position updates and history). The controller must coordinate **all flight processes simultaneously**, ensuring that each time step is fully processed before the simulation advances — producing a deterministic, reproducible execution.

### Dependencies

- **US100** – Simulate flights in a given area (processes, pipes, signals, parameter validation).
- **US101** – Capture and process flight movements (position updates and history for analysis).
- **US102** (related) – Safety violation detection benefits from step-by-step synchronization, as it allows the controller to reason about the global airspace state at each discrete step.

### Summary

US103 focuses on:

- Dividing simulation time into **discrete time steps** of a fixed interval.
- Ensuring each flight process produces a position update for each time step.
- Coordinating all flight processes so that the simulation only advances when all updates for the current step have been received.
- Guaranteeing that no flight progresses ahead of others, preserving a consistent global simulation state at each step.

---

## 2. Requirements

### Acceptance criteria (product backlog)

- The simulation must **progress step by step**.
- Each flight process must **send position updates at defined intervals**.
- The system must ensure **all updates for a given time step are received before advancing**.

### Refined acceptance criteria

- **US103.1** A global time step counter exists in the controller and is only advanced after all flight updates for the current step are processed.
- **US103.2** Each flight process sends exactly one position update per time step.
- **US103.3** The system ensures all expected updates for a time step are received before continuing.
- **US103.4** After processing a time step, all flight processes are allowed to continue to the next step.
- **US103.5** Flight processes do not advance until authorization is given — enforced via `sigsuspend`, which blocks the process without busy-waiting until `SIGUSR2` is received.
- **US103.6** The synchronization mechanism works alongside the existing communication and safety mechanisms from US100/US101/US102.

---

## 3. Analysis

### Key observations

- Synchronization is coordinated centrally by the controller.
- The problem follows a **barrier synchronization pattern**, where all processes must reach a synchronization point before continuing.
- Two pipes are used per flight: one for **position data** (flight → controller) and one for **ready signalling** (flight → controller), while signals are used for control and authorization.
- The controller must ensure a consistent global view of all aircraft at each time step.
- The system follows a **bulk synchronous execution model**, where all processes execute a step, synchronize, and then proceed together.
- Flight processes must wait after signalling readiness, until the controller authorizes them to continue via `SIGUSR2`.
- The synchronization mechanism must coexist with safety and termination signals from US102.

---

## 4. Design

### 4.1 Controller (parent)

Responsible for:

- Managing the simulation time step counter (`current_step`), which is only incremented after all active flight updates for the current step are received and processed.
- Waiting for all active flights to signal readiness via `pipe_ready` before authorizing the next step.
- Receiving position updates from all active flights via the position pipe after authorization.
- Processing the global state after each step (safety violation detection, history storage).
- Authorizing flight processes to continue by sending `SIGUSR2` to each active flight.

### 4.2 Flight process (child)

Responsible for:

- Computing aircraft position for each time step.
- Signalling readiness to the controller by writing 1 byte to `pipe_ready` before each step.
- Blocking via `sigsuspend` until `SIGUSR2` is received from the controller — this avoids busy-waiting.
- Sending the computed position update through the position pipe after receiving authorization.
- Handling `SIGUSR1` (separation violation) and `SIGTERM` (termination) without interfering with synchronization. `SIGTERM` also sets `g_next_step = 1` to unblock any pending `sigsuspend`.

### 4.3 Synchronization flow

1. Each flight process computes its position for the current step.
2. Each flight writes 1 byte to `pipe_ready`, signalling it is ready and waiting for authorization.
3. Each flight blocks on `sigsuspend`, waiting for `SIGUSR2`.
4. The controller reads from all `pipe_ready` pipes, blocking until all active flights have signalled readiness.
5. The controller sends `SIGUSR2` to each active flight, authorizing them to proceed.
6. Each flight unblocks, sends its position update through the position pipe, and moves to the next step.
7. The controller reads all position updates, processes the global simulation state (history, violation detection).
8. The step counter is incremented and the next step begins.

### 4.4 Communication channels per flight

| Channel         | Direction            | Type    | Purpose                               |
|-----------------|----------------------|---------|---------------------------------------|
| `pipes[i]`      | Flight → Controller  | Pipe    | Position updates                      |
| `pipe_ready[i]` | Flight → Controller  | Pipe    | Ready signal (1 byte per step)        |
| `SIGUSR2`       | Controller → Flight  | Signal  | Authorization to advance to next step |
| `SIGUSR1`       | Controller → Flight  | Signal  | Safety violation detected (US102)     |
| `SIGTERM`       | Controller → Flight  | Signal  | Terminate simulation                  |

---

## 5. Verification against acceptance criteria

| Backlog criterion                         | Requirement | Verification idea                                                                                  |
|-------------------------------------------|-------------|----------------------------------------------------------------------------------------------------|
| Simulation progresses step by step        | US103.1     | Verify `current_step` only increments after all updates are received and processed                 |
| Position updates per step                 | US103.2     | Verify exactly one position write per flight per step                                              |
| Controller waits for all updates          | US103.3     | Simulation does not proceed if one flight has not written to `pipe_ready`                          |
| All flights advance together              | US103.4     | `SIGUSR2` is only sent after all active flights have signalled readiness                           |
| No busy-wait on flight side               | US103.5     | Flight blocks on `sigsuspend` — CPU is not consumed while waiting for authorization                |
| No interference with US102 signals        | US103.6     | Trigger a violation and verify `SIGUSR1` is delivered without breaking the synchronization barrier |