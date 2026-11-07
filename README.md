# Smart Project Allocation and Team Collaboration Platform

A Java-based college mini-project that helps students choose academic projects and enables faculty and administrators to manage projects, collect student preferences, and allocate limited team seats.

The application provides an end-to-end browser workflow:

**Faculty creates projects → Students rank preferences → Faculty reviews submissions → Admin runs allocation → Students view their assigned projects and teams.**

This repository contains the backend, browser interface, database setup, automated tests, and a final demonstration guide.

## Project status

<badge>Completed</badge>

**Development status: Finished.** All four planned project phases have been implemented, and the core end-to-end classroom demonstration workflow has been manually tested. This repository represents the final academic mini-project submission; active feature development is concluded.

| Phase | Scope | Status |
|---|---|---|
| 1. Foundation | Spring Boot application, PostgreSQL integration, health endpoints and CI | Complete |
| 2. Accounts | Student registration/login, faculty and admin demo accounts, role-based access | Complete |
| 3. Allocation workflow | Project management, ranked preferences, preference review and allocation | Complete |
| 4. Final demonstration | Allocation results, project/team details and end-to-end browser workflow | Complete |

This is a completed academic prototype, not a production-ready deployment. The documented limitations below describe the delivered scope, not unfinished development tasks.

## Features

### Student
- Register an account and sign in.
- Browse active projects, descriptions, required skills and available seats.
- Submit and update up to five ranked project preferences.
- View saved preferences.
- View the allocated project, project description, faculty supervisor and allocated teammates.
- Log out.

### Faculty
- Sign in using the configured faculty demo account.
- Create projects with descriptions, optional required skills and maximum team size.
- View project details and remaining capacity.
- Review submitted student preferences, including student name, roll number, department, selected project and preference rank.
- View allocation results.

### Admin
- Sign in using the configured admin demo account.
- Create projects.
- Review submitted preferences and allocation results.
- Run or rerun the allocation process.

## Technology stack

| Area | Technology |
|---|---|
| Programming language | Java 21 |
| Backend framework | Spring Boot 3.5.6 |
| Web layer | Spring Web, REST APIs, embedded Tomcat |
| Database access | Spring JDBC (JdbcTemplate) |
| Database | PostgreSQL 16 |
| Authentication support | BCrypt password hashing and opaque bearer sessions |
| Frontend | HTML, CSS and JavaScript |
| Build and dependency management | Maven |
| Automated testing | Spring Boot Test / JUnit |
| CI | GitHub Actions |
| Local database environment | Docker Compose or Podman Compose |

## How allocation works

The project uses a deliberately simple, deterministic preference-and-capacity algorithm:

1. Students are processed in ascending database-record ID order.
2. For each student, the system checks submitted preferences from highest to lowest rank.
3. The student is assigned to the first project that still has an available seat.
4. If all of the student's selected projects are full, the student remains unallocated.
5. Rerunning allocation replaces the previous allocation results.

This approach is easy to explain and demonstrate, but it is **not a globally optimal matching algorithm**. Students with earlier database IDs are considered first.

## Requirements

- JDK 21 or newer
- Docker Engine with the Docker Compose plugin, or Podman with podman-compose
- Maven 3.9+ (optional if using the included Maven wrapper)
- Git

## Run locally

Run all commands from the repository root.

### 1. Start PostgreSQL

With Docker:

```bash
docker compose up -d db
docker compose ps
```

With Podman on Fedora:

```bash
podman-compose up -d db
podman ps
```

Wait until the database container is healthy before starting the backend.

### 2. Configure the database

The default local development configuration is:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/project_allocation
export DB_USERNAME=allocation
export DB_PASSWORD=allocation_dev
```

These are local development credentials. Do not reuse them in a public deployment.

### 3. Start the application

Using Maven:

```bash
mvn spring-boot:run
```

Or, if the repository's Maven wrapper is available:

```bash
./mvnw spring-boot:run
```

Open **http://localhost:8080/** in your browser.

If port 8080 is occupied, start the application on port 8081:

```bash
SERVER_PORT=8081 mvn spring-boot:run
```

Then open **http://localhost:8081/**.

### 4. Demo accounts

The application seeds local classroom demo accounts on startup when the configured email addresses do not already exist.

| Role | Email | Password |
|---|---|---|
| Admin | admin@college.local | Admin@12345 |
| Faculty | faculty@college.local | Faculty@12345 |

These credentials are for local demonstration only. Configure different values through the DEMO_ADMIN_* and DEMO_FACULTY_* environment variables before any deployment beyond a local classroom environment.

Students can register through the application. Public registration always creates a student account; it cannot grant faculty or admin privileges.

## Authentication and security notes

- Passwords are stored as BCrypt hashes, not plain text.
- Login creates a time-limited bearer session (12 hours).
- Only a SHA-256 hash of the session token is stored in PostgreSQL.
- Authenticated requests use the Authorization: Bearer <token> header.
- The current-user endpoint validates the session, and logout ends it.
- Public student registration cannot create privileged roles.

These are useful baseline protections for an academic prototype. The application has not been hardened or independently audited for production use.

## API overview

Except for registration and login, the endpoints below require an authenticated bearer token.

### Authentication

| Method | Endpoint | Purpose |
|---|---|---|
| POST | /api/auth/register | Register a student |
| POST | /api/auth/login | Log in and receive a bearer token |
| GET | /api/auth/me | Get the current account |
| POST | /api/auth/logout | End the current session |

### Projects, preferences and allocation

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| GET | /api/projects | Signed-in users | List active projects and available seats |
| POST | /api/projects | Faculty, admin | Create a project |
| GET | /api/preferences/me | Student | View own saved preferences |
| PUT | /api/preferences/me | Student | Save or replace ranked preferences |
| GET | /api/preferences | Faculty, admin | Review submitted student preferences |
| POST | /api/allocation/run | Admin | Run or rerun allocation |
| GET | /api/allocation/me | Student | View own allocation |
| GET | /api/allocation | Faculty, admin | View allocation results |

### Health checks

| Method | Endpoint | Purpose |
|---|---|---|
| GET | /api/health | Application health |
| GET | /api/health/database | Database connectivity and database status |

Example:

```bash
curl http://localhost:8080/api/health
curl http://localhost:8080/api/health/database
```

## Run tests

```bash
mvn --batch-mode verify
```

Or:

```bash
./mvnw --batch-mode verify
```

GitHub Actions runs Maven verification with a PostgreSQL service on pushes to main and pull requests targeting main.

## End-to-end demonstration workflow

1. Start PostgreSQL and the application.
2. Sign in as faculty and create two projects, each with one available seat.
3. Register two student accounts.
4. Have the first student rank Project A first and Project B second.
5. Have the second student rank Project A first.
6. Sign in as faculty and confirm both students' submitted preferences are visible.
7. Sign in as admin and run allocation.
8. Verify that the first student receives Project A and the second receives Project B.
9. Sign in as each student and inspect the assigned project and team details.

See [docs/phase-4-demo-guide.md](docs/phase-4-demo-guide.md) for the detailed setup, example scenario and presentation sequence.

## Repository structure

```text
.
├── .github/                 # GitHub Actions and repository templates
├── docs/                    # Project and demonstration documentation
│   └── phase-4-demo-guide.md
├── src/
│   ├── main/
│   │   ├── java/             # Spring Boot application and API
│   │   └── resources/        # Application configuration and database schema
│   └── test/                 # Automated tests
├── compose.yaml              # Local PostgreSQL service
├── pom.xml                  # Maven project configuration
└── README.md
```

## Scope and known limitations

This project focuses on demonstrating the core academic project-allocation workflow. It intentionally does not include:

- A globally optimal or AI/ML-based allocation algorithm.
- Microservices, cloud infrastructure or production-scale deployment.
- Real-time chat, notifications or a mobile application.
- Advanced analytics or a complex recommendation engine.

The allocation order is based on student database ID, and a student can remain unallocated when all their ranked choices are full. Demo credentials and local database settings must be changed before deployment in a shared or public environment.

## Final project note

The project is complete and is no longer under active development. The implementation, documented setup instructions and demonstration workflow represent the final submitted scope. No further features are planned as part of this mini-project.

---

**Project:** Smart Project Allocation and Team Collaboration Platform  
**Purpose:** College Java mini-project  
**Primary goal:** Make project preference submission and capacity-aware team allocation clear, manageable and demonstrable.
