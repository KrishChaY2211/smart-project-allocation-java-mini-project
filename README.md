# Smart Project Allocation and Team Collaboration Platform

A **small, working college mini-project** that demonstrates how students can choose projects and how a faculty/admin user can allocate them fairly. The goal is a clear end-to-end classroom demo—not a production-grade platform.

## Phase 1: Foundation

This phase establishes a runnable Spring Boot backend, PostgreSQL connectivity through Spring JDBC, database schema initialization, health endpoints, and automated CI testing.

### Technology
- Java 21
- Spring Boot 3.5.6
- Spring Web (REST API and embedded Tomcat)
- Spring JDBC (JdbcTemplate)
- PostgreSQL 16
- Maven
- GitHub Actions

### Prerequisites
- JDK 21+
- Docker Engine with the Docker Compose plugin
- Maven 3.9+ (or your IDE's Maven integration)

### Start the database
From the repository root:

```bash
docker compose up -d db
docker compose ps

For Fedora with Podman, use `podman-compose up -d db` instead. The image is pinned to Docker Hub explicitly so Podman does not try Fedora's registry.
```

Wait until the database container reports healthy.

### Run the backend
Spring Boot reads configuration from environment variables. The following defaults match the local Docker Compose database:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/project_allocation
export DB_USERNAME=allocation
export DB_PASSWORD=allocation_dev
mvn spring-boot:run

If port 8080 is already occupied, run `SERVER_PORT=8081 mvn spring-boot:run` and open http://localhost:8081/.
```

The API starts at http://localhost:8080.

### Verify the API
- GET /api/health — application status
- GET /api/health/database — PostgreSQL connectivity, database time, and public table count

Example:

```bash
curl http://localhost:8080/api/health
curl http://localhost:8080/api/health/database
```

### Run tests

```bash
mvn --batch-mode verify
```

GitHub Actions runs the same Maven verification against a PostgreSQL 16 service on every push to main and every pull request targeting main.

### Initial database model
The schema in src/main/resources/schema.sql creates:
- app_users — common identity and role information
- students — student profile, roll number, department and skills
- faculty — faculty profile and project supervision capacity
- projects — project catalogue, required skills, supervisor and team-size limit
- project_preferences — ranked student project choices

The schema is currently initialised by Spring SQL initialisation. Before adding production-grade schema evolution, later phases will introduce versioned database migrations.

### Phase 1 acceptance criteria
- [x] Java 21 / Spring Boot project structure is committed.
- [x] PostgreSQL configuration and reproducible local database service are present.
- [x] Initial relational schema and foreign-key/uniqueness constraints are present.
- [x] Application and database health endpoints are implemented.
- [x] Automated tests cover healthy endpoints and database-check failure handling.
- [x] GitHub Actions builds and tests the application with PostgreSQL.

### Current scope
Phase 1 is infrastructure only. Authentication, dashboards, project CRUD, preference submission and the allocation engine are not implemented yet. They will be added in later phases.

## Simple project plan

We are keeping this as a college working model. Each phase should result in something we can run and demonstrate.

| Phase | What we will build | Completion check |
|---|---|---|
| 1. Make the application run | Spring Boot backend, PostgreSQL database and health check | App starts and connects to the database |
| 2. Basic accounts | Student registration/login; simple faculty/admin demo accounts | Each role can sign in and see the appropriate page |
| 3. Main allocation workflow | Faculty/admin adds projects and seat limits; students rank project preferences; admin runs allocation | Students are assigned according to their preferences and available seats |
| 4. Finish the college demo | Show project/team lists and allocation results; add a small team discussion/chat feature only if it is stable; test and prepare demo instructions | A complete student-to-allocation workflow can be demonstrated from the UI |

### What we are deliberately NOT building
- No microservices, cloud deployment or production-scale infrastructure.
- No AI/ML recommendation engine or complicated optimisation algorithm.
- No elaborate notification system, live location tracking or advanced analytics.
- No complicated real-time chat infrastructure. A basic discussion feature is enough if time permits.

**Priority:** a simple interface and a reliable working flow. If a feature risks breaking the demo or taking too much time, we leave it out and document it as future scope.


## Phase 2: Basic student accounts (completed and manually tested)

The first account feature is implemented:
- Student registration creates both an app user and student profile.
- Passwords are hashed with BCrypt; plain-text passwords are never stored.
- Login creates a 12-hour bearer session. Only a SHA-256 hash of the session token is stored in PostgreSQL.
- The current-user endpoint validates the bearer session.
- Logout deletes the session.
- Public registration always creates the STUDENT role. It does not accept a role from the client.

### Authentication API

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/auth/register` | Register a student |
| POST | `/api/auth/login` | Log in and receive a bearer token |
| GET | `/api/auth/me` | Get the current account (send `Authorization: Bearer <token>`) |
| POST | `/api/auth/logout` | End the current session |

Registration JSON example:

```json
{
  "fullName": "Asha Rao",
  "email": "asha@example.com",
  "password": "a-strong-password",
  "rollNumber": "CSE001",
  "department": "CSE",
  "skills": "Java, Python"
}
```

Login JSON example:

```json
{
  "email": "asha@example.com",
  "password": "a-strong-password"
}
```

The login response includes a bearer token. Send it in the Authorization header for the `/me` and `/logout` endpoints. Faculty and admin account creation will be added through a controlled admin workflow, not public registration.


## Phase 2: Basic accounts and sign-in page

Phase 2 adds a small browser-based entry point and demo accounts for the college presentation.

### Open the application

Start PostgreSQL and the backend using the Phase 1 instructions, then open:

`http://localhost:8080/`

Students can register from the page and then sign in. Faculty and admin use the demo accounts below. The dashboard displays the signed-in user's role.

### Demo accounts

| Role | Email | Password |
|---|---|---|
| Admin | `admin@college.local` | `Admin@12345` |
| Faculty | `faculty@college.local` | `Faculty@12345` |

These are local classroom-demo credentials only. Change them through the `DEMO_ADMIN_*` and `DEMO_FACULTY_*` environment variables before using the app anywhere beyond a local demo. The accounts are created on startup if their email addresses do not already exist. Public student registration cannot create privileged roles.

### Phase 2 acceptance checklist

- [x] Student registration API and browser form.
- [x] Student login and logout.
- [x] Faculty and admin demo accounts seeded from configuration.
- [x] BCrypt password hashes and hashed session-token storage.
- [x] Role is shown on the signed-in dashboard.
- [x] Automated CI verification passed on GitHub Actions.
- [x] Manual browser verification completed: student registration/login/logout, invalid password, duplicate email, faculty login and admin login.


## Phase 3: Main allocation workflow (implemented; manual testing pending)

The main workflow is now available in the browser dashboard.

### What it does
- Faculty can add projects with a title, description, optional required skills and maximum team size.
- Admin can also add projects; they are assigned to the first seeded faculty account by default.
- Students can view active projects and rank up to five choices. Ranks must be consecutive (1, 2, 3…).
- Admin can run the allocation. Students are considered in ascending student-record order; each receives their highest-ranked project that still has a seat.
- Students can view their allocation. Admin can view the allocation results table.

### Phase 3 API
All endpoints below require the bearer token from `/api/auth/login`, except public registration and login.

| Method | Endpoint | Role | Purpose |
|---|---|---|---|
| GET | `/api/projects` | Any signed-in user | List active projects and available seats |
| POST | `/api/projects` | Faculty or admin | Add a project |
| GET | `/api/preferences/me` | Student | View saved ranked preferences |
| PUT | `/api/preferences/me` | Student | Replace ranked preferences |
| POST | `/api/allocation/run` | Admin | Run or rerun allocation |
| GET | `/api/allocation/me` | Student | View own assigned project |
| GET | `/api/allocation` | Faculty or admin | View allocation results |

Preference submission example:

```json
{
  "projectIds": [2, 1, 3]
}
```

The array order represents preference rank: project 2 is first choice, project 1 is second, and project 3 is third.

### Allocation rule and limitations
This is a deliberately simple, deterministic demo algorithm—not a globally optimal matching algorithm. It processes students by database ID and tries each student's choices in rank order until it finds a project with a free seat. If every selected project is full, that student remains unallocated. Rerunning allocation replaces the previous results.

### Phase 3 manual acceptance checklist
- [ ] Faculty can add a project and it appears in the catalogue.
- [ ] A student can save ranked preferences and reload them.
- [ ] Admin can run allocation and see the result counts.
- [ ] Students can view their assigned project.
- [ ] Capacity is respected when multiple students choose the same project.
- [ ] A student with no available preferred project remains unallocated.

Phase 3 is not marked complete until these checks and the GitHub Actions run have been verified.
