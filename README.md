# Smart Project Allocation and Team Collaboration Platform

A Java mini-project for transparent academic project allocation and student–faculty collaboration.

## Phase 1: Foundation

This phase establishes a runnable Spring Boot backend, PostgreSQL connectivity through Spring JDBC, database schema initialization, and health endpoints.

### Technology
- Java 21
- Spring Boot 3.5.x
- Spring Web
- Spring JDBC (JdbcTemplate)
- PostgreSQL
- Maven

### Prerequisites
- JDK 21+
- Docker Engine with Docker Compose plugin, or a local PostgreSQL 16+ instance
- Maven (or use the Maven wrapper once generated)

### Run PostgreSQL
```bash
docker compose up -d db
```

### Configure and run the backend
Copy `.env.example` to `.env` if you use the supplied environment values. Spring reads environment variables directly; export them in your shell:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/project_allocation
export DB_USERNAME=allocation
export DB_PASSWORD=allocation_dev
mvn spring-boot:run
```

The API starts at `http://localhost:8080`.

### Verify
- `GET /api/health` — application status
- `GET /api/health/database` — PostgreSQL connectivity and database time

### Database
The schema in `src/main/resources/schema.sql` is initialized by Spring on startup. It currently creates the initial user, student, faculty, project and preference tables. Later phases will add authentication, allocation runs, teams, messages and notifications through versioned migrations.

### Current scope
Phase 1 is infrastructure only. Authentication, dashboards, project CRUD and the allocation engine are not implemented yet. They will be added in later phases.

## Project phases
1. Foundation and database connectivity
2. User management and role-based access
3. Project catalogue and student preferences
4. Hybrid allocation engine
5. Team formation
6. Collaboration, chat and notifications
7. Reports, testing and final documentation
