# US101 – Receive Flight Position Updates
### *Sprint 2*

---

## 1. Context

**User Story (SCOMP):**  
*As the Simulation Controller, I want to receive periodic position updates from each flight process so that I can track aircraft movement in real time.*

This functionality is part of the SCOMP simulation engine and is implemented entirely in C using POSIX processes and pipes.

### Dependencies
- US100 – Real flight plan loading (JSON already implemented)
- POSIX IPC (pipes)
- POSIX process creation (`fork`)
- Basic error handling and resource management

### Summary
US101 focuses on:
- Creating one process per flight
- Establishing a unidirectional pipe per flight
- Sending periodic position updates from each flight process
- Reading and storing these updates in the controller process

---

## 2. Requirements

### Acceptance Criteria
- **US101.1** Each flight must run in its own process.
- **US101.2** Each flight must send periodic position updates to the controller.
- **US101.3** Communication must be done using POSIX pipes.
- **US101.4** The controller must read and store all received positions.
- **US101.5** The system must handle multiple flights concurrently.
- **US101.6** Proper error checking must be implemented for all IPC operations.
- **US101.7** All unused pipe ends must be closed to avoid deadlocks.
- **US101.8** The controller must avoid zombie processes (using `waitpid`).

---

## 3. Analysis

The goal of US101 is to establish the communication architecture of the simulation.

### Key Observations
- Each flight is simulated by a child process created via `fork()`.
- Each child process writes `FlightPosition` structures to a pipe.
- The controller (parent process) reads from all pipes in a loop.
- Flight movement is now based on real flight plans loaded from JSON (US100).
- Each flight process interpolates positions along its assigned route segments.
- The controller stores a history of positions for later use (e.g., conflict detection in US102).

### Data Structure
```
typedef struct {
    int flightId;
    float x;
    float y;
    time_t timestamp;
} FlightPosition;
```

### Business Rules
- Each flight process must only write to its own pipe.
- The controller must only read from the read-end of each pipe.
- All operations must be validated (pipe, fork, read, write, close).
- The system must remain responsive even with multiple flights.

---

## 4. Implementation

### 4.1 Controller (parent process)
Responsible for:
- Creating pipes
- Forking flight processes
- Closing unused pipe ends
- Reading positions
- Storing history
- Handling errors
- Avoiding zombies
  
Evidence (excerpt):
```
if (pipe(pipes[i]) == -1) {
    perror("pipe");
    exit(1);
}

pid = fork();
if (pid < 0) {
    perror("fork");
    exit(1);
}

if (pid == 0) {
    close(pipes[i][0]);
    run_flight(i, pipes[i][1]);
    close(pipes[i][1]);
    exit(0);
} else {
    pids[i] = pid;
    close(pipes[i][1]);
}
```

### 4.2 Flight Process (child process)
Responsible for:
- Computing real positions based on the flight plan (segment-by-segment interpolation)
- Writing them to the pipe
- Handling write errors
- Closing pipe on termination

Evidence (excerpt):
```
n = write(write_fd, &pos, sizeof(pos));
if (n == -1) {
    perror("write");
    close(write_fd);
    _exit(1);
}
```

### 4.3 Reading Loop

The controller continuously reads from all pipes:
```
ssize_t n = read(pipes[i][0], &pos, sizeof(pos));
if (n == -1) {
    perror("read");
    exit(1);
}
```

### 4.4 History Storage

```
if (count < MAX_HISTORY) {
    history[count++] = pos;
}
```

---

## 5. Verification Against Acceptance Criteria

| Acceptance Criterion                  | Status | Evidence                              |
|---------------------------------------|--------|---------------------------------------|
| US101.1 – One process per flight      | ✔      | ``fork()`` per flight                 |
| US101.2 – Periodic updates            | ✔      | ``run_flight()`` loop                 |
| US101.3 – Pipes used                  | ✔      | ``pipe()`` per flight                 |
| US101.4 – Controller stores positions | ✔      | ``history[count++] ``= ``pos``        |
| US101.5 – Multiple flights            | ✔      | ``for ``(i=0; ``i<N_FLIGHTS; ``i++)`` |
| US101.6 – Error checking              | ✔      | ``if ``(n ``== ``-1) ``perror(...)``  |
| US101.7 – Close unused pipe ends      | ✔      | ``close(pipes[i][1])`` etc.           |
| US101.8 – Avoid zombies               | ✔      | ``waitpid()`` prepared                |

---

## 6. Notes on Real Flight Movement(updated)

In the updated implementation, flight movement is no longer mocked.  
Each flight process now:

- Loads its real flight plan from JSON (US100)
- Iterates through each route segment
- Interpolates latitude and longitude between segment endpoints
- Sends real positions to the controller at each simulation step

This ensures that the controller receives realistic movement data and that the stored history reflects the actual flight trajectory.