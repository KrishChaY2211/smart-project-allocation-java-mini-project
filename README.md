# Smart Project Allocation and Team Collaboration Platform

A Java mini-project for transparent academic project allocation and student–faculty collaboration.

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
```

Wait until the database container reports healthy.

### Run the backend
Spring Boot reads configuration from environment variables. The following defaults match the local Docker Compose database:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/project_allocation
export DB_USERNAME=allocation
export DB_PASSWORD=allocation_dev
mvn spring-boot:run
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

## Project phases
1. Foundation and database connectivity
2. User management and role-based access
3. Project catalogue and student preferences
4. Hybrid allocation engine
5. Team formation
6. Collaboration, chat and notifications
7. Reports, testing and final documentation


## Phase 2: Basic student accounts (in progress)

The first user-management slice is implemented:
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
