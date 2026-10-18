# US086 - Pilot Remote Access

## 1. Context

* As a Pilot, I want to remotely access the system through a dedicated TCP client, so that I can execute pilot operations without local access to the server application.
* This user story belongs to the RCOMP component and reuses the shared remote access infrastructure defined in US078.
* The client must communicate exclusively via TCP with the server embedded in the main AISafe application. Any direct database access from the client is forbidden.

It is also dependent on:

* US030 - Authentication and Authorization
* US080 - Register a flight plan
* US081 - Import flight plan from file
* US078 - Shared TCP remote access infrastructure
* US090 - External logging of remote accesses

### 1.1 List of issues

**Main Issue:** Issue #131

**Analysis:** Issue #180

**Design:** Issue #183

**Implement:** Issue #184

---

## 2. Requirements

**US086** - As a Pilot, I want to remotely access the system through a specific TCP-based client application.

### 2.1 Acceptance Criteria

* **US086.1** A dedicated TCP client application for Pilot must be implemented.
* **US086.2** All client interaction must be performed exclusively via TCP; direct database access is strictly forbidden.
* **US086.3** Authentication and authorization must be enforced for every request.
* **US086.4** Pilot operations (at least US080/US081, according to team scope) must be available remotely.
* **US086.5** The communication protocol must be documented and compatible with the shared server infrastructure.

### 2.2 Dependencies/References

Regarding this requirement, we understand that it relates to:

* **US030** - role-based authentication and authorization (`PILOT`).
* **US080/US081** - pilot operations exposed through TCP.
* **US078** - shared server-side remote access infrastructure and protocol baseline.
* **US090** - login/logout/disconnect/failed-login events must be logged via UDP by the server.
* **NFR08** - no direct data access from client applications.

---

## 3. Analysis

US086 is mainly an infrastructure and integration story. It does not introduce new domain concepts; it exposes existing pilot use cases through the network.

### 3.1 Analysis Checklist

- [x] Confirm which pilot operations are in scope for remote access.
- [x] Define client session lifecycle (connect, authenticate, execute operations, logout, disconnect).
- [x] Define error scenarios (invalid credentials, unauthorized role, malformed command, timeout, connection loss).
- [x] Confirm mapping between remote commands and existing application controllers/use cases.
- [x] Confirm event types that trigger UDP logs on the server (US090).

### 3.2 Remote Access Concepts

| Concept               | Description                                | Constraints                                          |
|-----------------------|--------------------------------------------|------------------------------------------------------|
| Client Connection     | TCP socket between Pilot client and server | Must use agreed application-level protocol           |
| Authenticated Session | Session associated with one Pilot user     | Requires valid credentials and role                  |
| Operation Request     | A pilot operation requested through TCP    | Must map to authorized pilot use cases               |
| Operation Response    | Server response to client request          | Must include success/error status and payload        |
| Access Event          | Login/logout/disconnect/failed login event | Must be forwarded by server to US090 logging service |

### 3.3 Relationship with Domain

The client acts only as a transport/UI layer. All business rules remain server-side in existing AISafe application services and domain model.

### 3.4 Scope and Session Analysis

Remote operations in scope for US086 are:

* Register a flight plan (`US080`) — `RegisterFlightPlanController`
* Import a flight plan from file (`US081`) — `ImportFlightPlanFromFileController`

Remote commands (to be detailed in design; aligned with shared TCP infrastructure from US078/US044):

* `LOGIN` — authenticate with role `PILOT`
* `REGISTER_FLIGHT_PLAN` — remote exposure of US080
* `IMPORT_FLIGHT_PLAN_FILE` — remote exposure of US081 (file path or payload on server host; to be agreed in design)
* `LOGOUT` — end session

The remote session lifecycle is defined as:

1. TCP connection establishment;
2. Authentication request with Pilot credentials;
3. Authorization check for role `PILOT`;
4. Execution of authorized pilot operations;
5. Session termination via logout or disconnect.

Expected error scenarios to be handled in the protocol:

* invalid credentials;
* user without role `PILOT` at login;
* operation requested before successful login;
* malformed or incomplete command payload;
* business/domain validation errors returned by server-side use cases (US080/US081);
* network failure (timeout / broken connection).

For US090 integration, the following access events must be produced server-side:

* `LOGIN_SUCCESS`
* `LOGIN_FAILED`
* `LOGOUT`
* `DISCONNECT`

Service identifier for US090 logs: `US86`.

---

## 4. Design

### 4.1 Realization

The design follows a client-server architecture over TCP, reusing the shared remote access infrastructure from US078 and the technical baseline already applied in US044.

| Layer | Component | Technology | Responsibility |
|-------|-----------|------------|----------------|
| Client | Pilot remote app | C (Berkeley sockets) | Console UI, protocol encoding/decoding, session state |
| Server | Embedded TCP remote server | Java (`java.net`) | Accept connections, authenticate, authorize, dispatch Pilot commands |
| Application | Existing flight controllers | Java (EAPLI) | Execute US080 and US081 business logic |
| Logging | UDP access logger (server-side) | UDP socket | Emit access events to US090 logging server |

The Pilot client has **no database dependency**. All persistence and domain validation occur on the server.

### 4.2 Protocol Design

Protocol baseline is shared with US078/US044.

#### 4.2.1 Message Framing

Each TCP message is sent as:

```text
[4 bytes payload length (big-endian)][payload UTF-8]
```

#### 4.2.2 Request Format

```text
COMMAND;param1;param2;...
```

#### 4.2.3 Response Format

Success:

```text
OK;status;payload
```

Error:

```text
ERR;errorCode;message
```

#### 4.2.4 Standard Error Codes

| Code | Meaning |
|------|---------|
| `AUTH_FAILED` | Invalid username/password, or user without `PILOT` role |
| `SESSION_REQUIRED` | Operation requested before successful login |
| `BAD_REQUEST` | Malformed command or missing parameters |
| `VALIDATION_ERROR` | Business/domain validation failed |
| `INTERNAL_ERROR` | Unexpected server failure |

#### 4.2.5 Pilot command contracts (design baseline)

`REGISTER_FLIGHT_PLAN` targets the DTO used by `RegisterFlightPlanController`:

```text
REGISTER_FLIGHT_PLAN;airlineCode;flightNumber;operationalSuffix;aircraftRegistration;pilotId;departureDateTime;fuelQuantity;fuelUnit;dslContent[;arrivalDateTime]
```

- `departureDateTime` / optional `arrivalDateTime`: ISO local date-time (`yyyy-MM-ddTHH:mm`); if omitted, server uses departure + 1 hour
- `dslContent`: Core Flight DSL as a **single line without `;`** (protocol separator)
- `pilotId`: pilot certification id (e.g. `P-001`), not login email
- Success response: `OK;FLIGHT_PLAN_REGISTERED;<flightDesignator>`

Pilot login uses an optional 4th field on the shared server (same TCP port as US044):

```text
LOGIN;email;password;PILOT
```

`IMPORT_FLIGHT_PLAN_FILE` targets `ImportFlightPlanFromFileController`:

```text
IMPORT_FLIGHT_PLAN_FILE;airlineCode;flightNumber;operationalSuffix;filePath
```

- `filePath` is resolved on server host in this baseline
- Success response (planned): `OK;FLIGHT_PLAN_IMPORTED;<flightDesignator>`

### 4.3 Command Mapping

| Remote Command | Business US | Server Component | Required Role | Notes |
|----------------|-------------|------------------|---------------|-------|
| `LOGIN` | US030 | Authentication/session bootstrap | any (must become `PILOT`) | Use `LOGIN;email;password;PILOT` on shared server |
| `REGISTER_FLIGHT_PLAN` | US080 | `RegisterFlightPlanController` | `PILOT` | Uses DTO-aligned fields in §4.2.5 |
| `IMPORT_FLIGHT_PLAN_FILE` | US081 | `ImportFlightPlanFromFileController` | `PILOT` | Uses server-side file path baseline |
| `LOGOUT` | US030/US086 | Session termination handler | authenticated session | Ends session explicitly |

Service identifier for US090 logs: `US86`.

### 4.4 Client Structure (C)

Planned source layout under `RCOMP/`:

```text
RCOMP/
  include/
    protocol.h
  src/
    common/
      protocol.c
    client/
      pilot/
        pilot_client.c
  Makefile
```

Responsibilities:

- `protocol`: framed send/receive and connection lifecycle.
- `pilot_client`: menu loop, session state, command build/response handling for Pilot operations.

### 4.5 Server structure (Java) — design mapping

| Design role | Implementation class |
|-------------|----------------------|
| TCP server | `RemoteAccessTcpServer` |
| Per-connection loop | `RemoteConnectionHandler` |
| Role-based dispatch (shared) | `CompositeRemoteCommandHandler` |
| Handler contract | `RemoteCommandHandler` |
| Message codec | `RemoteMessageCodec` |
| Pilot dispatcher | `PilotRemoteCommandHandler` |
| Session context | `RemoteConnectionContext` |
| Response utility | `RemoteResponse` |
| UDP access logger | `RemoteAccessUdpLogger` |

Headless deployment entry point remains `aiSafe.RemoteAccessMain`. The same TCP server and port (`9000`) are shared with US044 and US078.

### 4.6 Diagrams

Class diagram source: [`classDiagram/us086_ClassDiagram.puml`](classDiagram/us086_ClassDiagram.puml)  
Sequence diagram source: [`sequenceDiagram/us086_SequenceDiagram.puml`](sequenceDiagram/us086_SequenceDiagram.puml)

### 4.7 Acceptance Tests (design-level)

**AT1 - Successful remote pilot session**  
Given a valid Pilot user, when `LOGIN` is sent and one authorized command (`REGISTER_FLIGHT_PLAN` or `IMPORT_FLIGHT_PLAN_FILE`) is executed, the server returns `OK` and `LOGOUT` closes the session.

**AT2 - Authentication failure**  
Given invalid credentials (or user without `PILOT` role), when `LOGIN` is sent, the server returns `ERR;AUTH_FAILED;...` and no pilot operation is accepted.

**AT3 - Session enforcement**  
Given a non-authenticated connection, when a pilot command is sent before login, the server returns `ERR;SESSION_REQUIRED;...`.

**AT4 - Validation failure**  
Given malformed parameters or invalid domain data, when a pilot command is sent, the server returns `ERR;BAD_REQUEST;...` or `ERR;VALIDATION_ERROR;...`.

**AT5 - Access logging integration**  
For login success, login failure, logout and disconnect, the server emits UDP events with service `US86` (validated together with US090).

---

## 5. Implementation

### 5.1 Evidence of implementation aligned with design

The US086 implementation follows section 4 and extends the shared TCP infrastructure from US044/US078.

**Client (C, Berkeley sockets):**

| File                                    | Role                                                |
|-----------------------------------------|-----------------------------------------------------|
| `RCOMP/include/protocol.h`              | Public protocol API (shared)                        |
| `RCOMP/src/common/protocol.c`           | Length-prefixed TCP send/receive (shared)           |
| `RCOMP/src/client/pilot/pilot_client.c` | Pilot menu client (login, register, import, logout) |
| `RCOMP/Makefile`                        | Build targets `weather_client` and `pilot_client`   |

**Server (Java, embedded in AISafe):**

| File                                            | Role                                               |
|-------------------------------------------------|----------------------------------------------------|
| `.../remote/RemoteAccessTcpServer.java`         | TCP accept loop (port 9000)                        |
| `.../remote/RemoteConnectionHandler.java`       | Per-connection handler + session gate              |
| `.../remote/CompositeRemoteCommandHandler.java` | Routes LOGIN/commands by role (Weather vs Pilot)   |
| `.../remote/RemoteCommandHandler.java`          | Handler interface                                  |
| `.../remote/PilotRemoteCommandHandler.java`     | US086 command dispatch → US080/US081 controllers   |
| `.../remote/RemoteConnectionContext.java`       | Per-connection session (username, role, serviceId) |
| `.../remote/RemoteMessageCodec.java`            | Message framing                                    |
| `.../remote/RemoteResponse.java`                | `OK` / `ERR` builder                               |
| `.../remote/RemoteAccessUdpLogger.java`         | US090 UDP events                                   |
| `aiSafe/RemoteAccessMain.java`                  | Headless server entry point                        |

Business logic is **not duplicated**: `PilotRemoteCommandHandler` delegates to `RegisterFlightPlanController` and `ImportFlightPlanFromFileController`.

### 5.2 Design–implementation alignment

Verified against running implementation on DEI VM (`RemoteAccessMain` + `pilot_client`):

- [x] Shared TCP server on port **9000** (`CompositeRemoteCommandHandler`)
- [x] Pilot login: `LOGIN;email;password;PILOT`
- [x] Commands `REGISTER_FLIGHT_PLAN`, `IMPORT_FLIGHT_PLAN_FILE`, `LOGOUT`
- [x] Responses `OK;...` / `ERR;...` with codes in §4.2.4
- [x] AuthZ via `PILOT` role; session required for pilot commands
- [x] Persistence only server-side (existing flight controllers + JPA)
- [x] UDP access events `LOGIN_SUCCESS`, `LOGIN_FAILED`, `LOGOUT`, `DISCONNECT` with service **`US86`**
- [x] Class/sequence diagrams updated to include `CompositeRemoteCommandHandler` and `LOGIN;...;PILOT`

### 5.3 How to run (VM DEI)

**Terminal 1 — server (keep open):**

```bash
cd /opt/sem4pi2526-sem4pi2526_2de3/aisafe.app.backoffice.console
mvn dependency:copy-dependencies -Dmaven.test.skip=true
java -cp "target/aisafe.app.backoffice.console-1.0-SNAPSHOT.jar:target/dependency/*" aiSafe.RemoteAccessMain
```

**Terminal 2 — pilot client:**

```bash
cd /opt/sem4pi2526-sem4pi2526_2de3/RCOMP
make clean && make
./pilot_client 127.0.0.1 9000
```

**Test credentials (after bootstrap):** `pilot@aisafe.local` / `Pilot123`

**Example — import flight plan (option 3):**

| Field             | Example                                                               |
|-------------------|-----------------------------------------------------------------------|
| Airline           | `TP`                                                                  |
| Flight number     | `123`                                                                 |
| Suffix            | *(blank)*                                                             |
| File path (on VM) | `/opt/sem4pi2526-sem4pi2526_2de3/FlightPlans/valid-single-leg.flight` |

**Example — register flight plan (option 2):** see §6.2.

Build at project root (VM or local):

```bash
mvn clean install -Dmaven.test.skip=true
```

Weather Person remote tests use the same server and [`RCOMP/GUIDE/US044-GUIDE.md`](../../RCOMP/GUIDE/US044-GUIDE.md).

### 5.4 Configuration

`aisafe.app.backoffice.console/src/main/resources/application.properties`:

```properties
remote.tcp.enabled=true
remote.tcp.port=9000
remote.udp.logging.host=127.0.0.1
remote.udp.logging.port=5000
```

| Property                  | Purpose                                         |
|---------------------------|-------------------------------------------------|
| `remote.tcp.port`         | Shared TCP port (default `9000`)                |
| `remote.udp.logging.host` | US090/US91 logging server; empty = UDP disabled |
| `remote.udp.logging.port` | UDP port (default `5000`, matches `logging_server`) |

---

## 6. Integration/Demonstration

### 6.1 Demonstration script (Pilot)

1. Start `RemoteAccessMain` on the VM (§5.3).
2. Run `./pilot_client 127.0.0.1 9000`.
3. **Login** (option 1): `pilot@aisafe.local` / `Pilot123` → expect `OK;SESSION;...`.
4. Execute **one** pilot operation:
   - **Import** (option 3) — recommended for demo (`TP` / `123`, path to `.flight` on VM), or
   - **Register** (option 2) — requires existing flight, aircraft, pilot certification, and valid DSL (§6.2).
5. **Logout** (option 4) → `OK;LOGOUT;...`.
6. (Optional) With `./logging_server` running (US091, UDP **5000**), login/logout emit UDP events with service **`US86`**; view at `http://127.0.0.1:8080`.

### 6.2 Register flight plan — sample inputs

| Field                  | Example                                                                                                                                                                                                                        |
|------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Airline (IATA)         | `TP`                                                                                                                                                                                                                           |
| Flight number          | `123`                                                                                                                                                                                                                          |
| Operational suffix     | *(blank)*                                                                                                                                                                                                                      |
| Aircraft registration  | `CS-TUA`                                                                                                                                                                                                                       |
| Pilot ID               | `P-001`                                                                                                                                                                                                                        |
| Departure              | `2026-06-03T10:00`                                                                                                                                                                                                             |
| Arrival                | `2026-06-03T11:00`                                                                                                                                                                                                             |
| Fuel quantity          | `5000`                                                                                                                                                                                                                         |
| Fuel unit              | `kg`                                                                                                                                                                                                                           |
| DSL (one line, no `;`) | `flight TP123 { type REGULAR leg { departure OPO 2026-06-03 10:00 arrival LIS 2026-06-03 11:00 route { origin OPO destination LIS } segment { start (41.242, -8.678) end (38.774, -9.134) altitude 12000 m } fuel 5000 kg } }` |

Success: `OK;FLIGHT_PLAN_REGISTERED;TP123`

### 6.3 Acceptance tests (demonstration mapping)

| Test | How to demonstrate                                                    |
|------|-----------------------------------------------------------------------|
| AT1  | Login + import or register + logout with `OK` responses               |
| AT2  | Login with wrong password → `ERR;AUTH_FAILED;...`                     |
| AT3  | Send `REGISTER_FLIGHT_PLAN` before login → `ERR;SESSION_REQUIRED;...` |
| AT4  | Invalid flight or file path → `ERR;VALIDATION_ERROR;...`              |
| AT5  | UDP events with service `US86` (when US090 host configured)           |

### 6.4 Shared infrastructure note

US086 does **not** open a separate TCP port. The Pilot client connects to the same `RemoteAccessTcpServer` as US044. Role-specific behaviour is selected at login (`;PILOT`) and by command name in `CompositeRemoteCommandHandler`. US078 (ATCC) should extend this composite pattern rather than adding a second server.
