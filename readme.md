<div align="center">

  <!-- Typing Effect Banner / Header -->
  <a href="https://git.io/typing-svg">
    <img src="https://readme-typing-svg.demolab.com?font=Fira+Code&weight=600&size=24&pause=1000&color=A6E3A1&center=true&vCenter=true&width=650&lines=%E2%9C%88%EF%B8%8F+Project+AIControl+%7C+AISafe;Air+Traffic+Management+%26+Flight+Simulation;Built+with+Java+21%2C+DDD+%26+Scrum;Scalable%2C+Secure+%26+Automated+Systems" alt="Typing SVG" />
  </a>

  <br />

  <!-- Animated Flight / Airplane GIF -->
  <img src="https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExM3Y5Y2ZicnkycmZnbnd0dDFpMmg5MHoweTN5bnhvbmZhZjMxcGpmdCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/3o7TKSx0OTIalS5fMI/giphy.gif" width="200px" alt="Airplane Animation GIF" />

  <h1>✈️ Project AIControl</h1>

  <p><i>Scalable & Integrated Air Traffic Management Support System for AISafe</i></p>

  <!-- Badges in Green/Mint Theme -->
  <img src="https://img.shields.io/badge/Java-21-a6e3a1?style=for-the-badge&logo=openjdk&logoColor=1e1e2e" />
  <img src="https://img.shields.io/badge/Build-Maven%203.9-94e2d5?style=for-the-badge&logo=apachemaven&logoColor=1e1e2e" />
  <img src="https://img.shields.io/badge/Architecture-DDD-a6e3a1?style=for-the-badge&logoColor=1e1e2e" />
  <img src="https://img.shields.io/badge/Methodology-Scrum-89dceb?style=for-the-badge&logoColor=1e1e2e" />

</div>

<br />

---

## 1. Description of the Project

This project consists of the development of a scalable and integrated air traffic management support system for a startup named AISafe. 
The system aims to modernize and improve existing flight control solutions by providing tools for aircraft configuration, flight management, weather data integration and flight simulation.

The platform will allow different stakeholders, such as administrators, backoffice operators, air transport companies, pilots and flight control operators, to interact with the system according to their roles. 
Core functionalities include the registration and management of aircraft, airports, routes and users, as well as the creation and validation of flight plans using a Domain Specific Language (DSL).

Additionally, the system will support the simulation of flights and air traffic control scenarios, enabling the detection of potential safety violations such as collisions. 
These simulations must be efficient and scalable, potentially distributed across multiple processes or systems.

The project follows a Domain-Driven Design (DDD) approach and incorporates modern software engineering practices, including continuous integration, automated deployment and test-driven development. 
It is developed collaboratively using an iterative and incremental methodology (Scrum), integrating knowledge from multiple areas such as software engineering, programming, systems and networks.

## 2. Planning and Technical Documentation

[Planning and Technical Documentation](docs/Planning)

### Prerequisites

Before proceeding, ensure the following tools are installed and available on your system:

| Tool                    | Purpose                                    | Minimum Version                          |
|-------------------------|--------------------------------------------|------------------------------------------|
| **Java**                | Runtime environment for the application    | JDK 21 or later                          |
| **Apache Maven**        | Build automation and dependency management | 3.9 or later                             |
| **PlantUML** (optional) | Generation of UML diagrams                 | Bundled via `libs/plantuml-1.2026.2.jar` |

To verify the required tools are installed:

```
java -version
mvn -version
```

> **Note:** All shell scripts (`.sh`) are intended for use on Linux, Unix, or macOS environments. Native Windows execution is not supported; however, Git Bash may be used as alternatives on Windows.

## 3. How to Build

The build process compiles all modules, copies dependencies, and generates reports (Surefire, Checkstyle).

Run the provided build script from the project root directory:
``` 
./Scripts/build.sh
```

Alternatively, execute the Maven command directly:

``` 
 mvn clean install
```

Upon successful completion, each module's JAR will be located under its respective `target/` directory, e.g.:

```
aisafe.app.bootstrap/target/aisafe.app.bootstrap-1.0-SNAPSHOT.jar
aisafe.app.backoffice.console/target/aisafe.app.backoffice.console-1.0-SNAPSHOT.jar
```

> **Note:** Ensure the script has execute permissions before running it. If not, grant them with:
> ```
> chmod +x Scripts/build.sh
> ```

## 4. How to Execute Tests

Tests are executed automatically as part of the build process when running:

```
mvn clean install
```

To run only the test phase without a full rebuild:

```
mvn test
```

## 5. How to Run

Before running the application, ensure it has been successfully built (see [Section 3](#3-how-to-build)).

The following scripts must be run from the **project root directory** (the folder containing the `Scripts/` directory).

### 5.1 Bootstrap (first run only)

The Bootstrap initialises the database schema and seeds the initial data (users, roles, etc.). It must be run **before** the Backoffice on a fresh database.

```
./Scripts/run-bootstrap.sh
```

### 5.2 Backoffice Console

Once the database has been initialised, start the Backoffice Console:

```
./Scripts/run-backoffice.sh
```

**Correct execution order:**

```
1. ./Scripts/build.sh           # compile all modules
2. ./Scripts/run-bootstrap.sh   # initialise the database (first time only)
3. ./Scripts/run-backoffice.sh  # start the application
```

> **Note:** Ensure the scripts have execute permissions before running them. If not, grant them with:
> ```
> chmod +x Scripts/run-bootstrap.sh Scripts/run-backoffice.sh
> ```

## 6. How to Install/Deploy into Another Machine (or Virtual Machine)

The deployment process copies the Backoffice JAR and all its runtime dependencies into a self-contained directory that can be transferred to a target machine.

Run the deployment script from the **project root directory**:

```
./Scripts/deploy-backoffice.sh
```

The script deploys to `libs/backoffice/` inside the project root, with the following structure:

```
libs/backoffice/
├── backoffice-app.jar   ← application JAR
└── libs/                ← all runtime dependencies
```

Once transferred to a target machine, the application can be launched with:

```
java -cp "libs/backoffice/backoffice-app.jar:libs/backoffice/libs/*" aiSafe.Main
```

> **Prerequisites for the target machine:** A compatible Java Runtime Environment (JRE 21 or later) must be installed.

> **Note:** Ensure the script has execute permissions before running it. If not, grant them with:
> ```
> chmod +x Scripts/deploy-backoffice.sh
> ```

---

## 7. How to Generate PlantUML Diagrams

To generate PlantUML diagrams for documentation, execute the script (Linux/Unix/macOS only):

```
./generate-plantuml-diagrams.sh
```
