# US100 – Simulate Flights in a Given Area

---

# 1. Context
As a Flight Control Operator, I want to simulate flights in area. Simulations have many parameters, such
as time range, geographic area, included flights, weather conditions, safety thresholds, performance
settings. All required parameters should be validated.

**Acceptance criteria:**
- This component must be implemented in C and must utilize processes, pipes, and signals.
- The system should fork a new process for each flight.
- Each flight process should execute its designated flight plan.
- Pipes should facilitate communication between the main process and each flight process.
- The main process should track aircraft positions over time using an appropriate data structure.

# 2. Requirements
**Acceptance Criteria**
- US100.1 The component must be implemented in C.
- US100.2 The system must use processes, pipes, and signals.
- US100.3 The system must fork one process per flight.
- US100.4 Each flight process must execute its flight simulation.
- US100.5 Pipes must be used for communication between flights.
- US100.6 The main process must track aircraft positions over time.
- US100.7 All configuration parameters must be validated.
- US100.8 The system must terminate processes correctly and avoid zombies.

# 3. Analysis
US100 represents the core architecture of the flight simulation engine.
The objective is to create a simulation where:
- multiple aircraft run concurrently,
- each aircraft is executed independently,
- the controller process receives aircraft positions in real time,
- and all processes terminate safely.

# 4. Architecture
The system follows a parent-child process model.

**Main Process (Controller)**
Responsible for:
* validating simulation parameters,
* creating pipes,
* creating flight processes,
* receiving aircraft positions,
* storing flight history,
* handling signals,
* terminating children,
* waiting for all processes.

**Child Processes (Flights)**
Responsible for:
* simulating a flight,
* generating positions,
* sending updates through pipes,
* terminating correctly.

# 5. Data Structures

## 5.1 Flight Position
Stores a single aircraft position update.

typedef struct {
int flightId;
double x;
double y;
long timestamp;
} FlightPosition;

### Purpose
Used by child processes to send position updates to the controller.

## 5.2 Flight State
Stores simplified aircraft coordinates.

typedef struct {
int flightId;
double x;
double y;
} FlightState;

## 5.3 Flight History
Stores the complete trajectory history of each aircraft.

#define MAX_HISTORY 1000

typedef struct {
FlightState states[MAX_HISTORY];
int count;
} FlightHistory;

Purpose
Allows the controller to:
* track aircraft movement over time,
* prepare future conflict detection,
* support replay/debugging of simulations.

This structure satisfies the acceptance criterion:
`“The main process should track aircraft positions over time using an appropriate data structure.”`

# 6. Implementation

## 6.1 Signal Handling
The simulation handles CTRL+C using SIGINT.
static volatile sig_atomic_t running = 1;

### Explanation
`sig_atomic_t`
Special integer type guaranteed to be safely modified inside signal handlers.

`volatile`
Prevents compiler optimization problems.
Without volatile, the compiler could cache the value of running and never detect changes made by the signal handler.

### Signal Handler
void handle_sigint(int sig) {
(void)sig;
running = 0;
}

Behavior
When the user presses CTRL+C:
* the operating system sends SIGINT,
* the handler executes,
* running becomes 0,
* the simulation loop stops safely.

### Signal Registration
struct sigaction sa;

sa.sa_handler = handle_sigint;

sigemptyset(&sa.sa_mask);

sa.sa_flags = SA_RESTART;

sigaction(SIGINT, &sa, NULL);

**Explanation**

| Instruction   | Purpose                               |
| ------------- | ------------------------------------- |
| `sa_handler`  | Function executed when signal arrives |
| `sigemptyset` | No additional signals blocked         |
| `SA_RESTART`  | Restart interrupted system calls      |
| `sigaction`   | Registers the signal handler          |


## 6.2 Pipe Creation
Each flight receives its own pipe.

if (pipe(pipes[i]) == -1) {
perror("[ERROR] pipe creation failed");
return;
}

Purpose
Allows communication between:
* child process → writes positions,
* parent process → reads positions.

## 6.2.1 Pipe Descriptor Structure

Each pipe created by the system contains two file descriptors:

| Descriptor | Purpose |
|------------|---------|
| `fd[0]` | Read-end |
| `fd[1]` | Write-end |

The simulation stores one pipe per flight:

```c
int pipes[MAX_FLIGHTS][2];
```

Meaning:

```text
pipes[i][0] -> read descriptor
pipes[i][1] -> write descriptor
```

Each flight process communicates with the controller using its dedicated pipe.

---

## 6.2.2 Pipe Creation Before fork()

The pipe is created before the `fork()` call:

```c
pipe(pipes[i]);

pid_t pid = fork();
```

### Purpose

This is necessary because:

* after `fork()`, the child inherits the file descriptors from the parent,
* both processes must share the same pipe,
* otherwise communication would not be possible.

This follows the UNIX process communication model.

---

## 6.2.3 Pipe Communication Model

The simulation uses unidirectional communication:

```text
Flight Process -----> Controller Process
(write)                    (read)
```

Each child process:
* writes aircraft positions into the pipe,
* never reads from the pipe.

The controller:
* reads aircraft positions,
* never writes into the pipe.

---

## 6.2.4 Closing Unused Pipe Ends

To avoid descriptor leaks and incorrect pipe behavior:

### Child Process

```c
close(pipes[i][0]);
```

The child closes the unused read-end.

### Parent Process

```c
close(pipes[i][1]);
```

The parent closes the unused write-end.

### Purpose

This guarantees:
* proper pipe ownership,
* reduced resource usage,
* correct EOF behavior,
* prevention of deadlocks.

---

## 6.2.5 Atomic Pipe Writes

Aircraft positions are sent using:

```c
write(write_fd, &pos, sizeof(pos));
```

The structure size is significantly smaller than `PIPE_BUF`.

### Consequence

The write operation is atomic:
* the entire structure is written as a single operation,
* partial/interleaved writes cannot occur,
* the controller always receives complete `FlightPosition` structures.

This ensures communication consistency between processes.

---

## 6.2.6 Pipe Data Ordering

Pipes preserve byte order.

If a flight writes positions sequentially:

```text
Position A
Position B
Position C
```

the controller receives them in the same order.

### Purpose

This guarantees:
* correct trajectory reconstruction,
* accurate history tracking,
* deterministic simulation behavior.

---

## 6.2.7 Pipe Consumption Behavior

Reading from a pipe removes the consumed data:

```c
read(pipes[i][0], &pos, sizeof(pos));
```

After a successful read:
* the data no longer exists inside the pipe buffer.

### Consequence

The controller stores received positions in `FlightHistory` to preserve aircraft trajectory information over time.

## 6.3 Process Creation
Each flight runs in its own process.

pid_t pid = fork();

### Fork Error Handling
if (pid < 0) {
perror("[ERROR] fork failed");
return;
}

**Meaning**
If fork() returns -1, the process creation failed.


## 6.4 Child Process
if (pid == 0) {

    close(pipes[i][0]);

    run_flight(config.flightIds[i], pipes[i][1]);

    close(pipes[i][1]);

    exit(0);
}

### Responsibilities
The child process:
* closes unused read-end,
* executes flight simulation,
* sends position updates,
* closes pipe,
* terminates normally.


## 6.5 Parent Process
pids[i] = pid;
close(pipes[i][1]);

### Responsibilities
The parent:
* stores child PID,
* closes unused write-end,
* reads positions from pipe.


## 6.6 Main Simulation Loop
while (running) {

The controller continuously:
* receives aircraft positions,
* prints updates,
* stores history,
* checks simulation duration.

## 6.7 Reading Flight Updates
ssize_t n = read(pipes[i][0], &pos, sizeof(pos));

**Behavior**
The controller reads one FlightPosition structure from the pipe.

## 6.8 Position Tracking
history[i].states[history[i].count].flightId = pos.flightId;
history[i].states[history[i].count].x = pos.x;
history[i].states[history[i].count].y = pos.y;

history[i].count++;

### Purpose
Stores aircraft movement history over time.

Example stored trajectory:
Flight 101:
(10.0, 20.0)
(15.0, 24.0)
(19.0, 30.0)


## 6.9 Process Termination
When simulation ends:

kill(pids[i], SIGTERM);

### Purpose
The controller sends SIGTERM to every flight process.

This guarantees:
* controlled shutdown,
* no orphan processes,
* no infinite child execution.

## 6.10 Waiting for Children
waitpid(pids[i], &status, 0);

### Purpose
Ensures:
* all child processes are collected,
* zombie processes are avoided,
* resources are released correctly.

### Process State Verification
**Normal Exit**
WIFEXITED(status)
Checks if child terminated normally.

**Signal Termination**
WIFSIGNALED(status)
Checks if child terminated because of a signal.

**Signal Identification**
WTERMSIG(status)
Returns the signal number responsible for termination.


