# Phase 1 — Foundation

## Goal
Create a reproducible backend foundation that can be extended into the Smart Project Allocation and Team Collaboration Platform.

## Architecture

```text
HTTP client
    |
    v
Spring Boot REST API
    |
    +--> HealthController
    |
    v
Spring JDBC / JdbcTemplate
    |
    v
PostgreSQL 16
```

Spring Boot runs the web application on embedded Tomcat. Spring JDBC provides database access through JdbcTemplate; the PostgreSQL JDBC driver handles the database protocol.

## Data model

- app_users: shared account identity, email, password-hash storage and role.
- students: one-to-one student profile linked to an account.
- faculty: one-to-one faculty profile and supervision capacity.
- projects: project details, required skills, supervisor and maximum team size.
- project_preferences: ranked project choices for each student.

Foreign keys enforce profile ownership and project/supervisor references. Unique constraints prevent duplicate emails, roll numbers, student-project preferences and duplicate preference ranks for one student.

## API contract

| Method | Endpoint | Purpose |
|---|---|---|
| GET | /api/health | Confirms the application process is responding |
| GET | /api/health/database | Checks PostgreSQL and returns database metadata |

A successful application health response does not by itself prove database availability; use the database endpoint for that check.

## Verification evidence

The GitHub Actions workflow Build and test runs Maven verify using Java 21 and a PostgreSQL 16 service. The initial Phase 1 workflow run completed successfully. The health controller tests cover application health, successful database metadata retrieval, and a failed database check returning HTTP 503.

## Deliberate limitations

- No login, registration or authorisation yet.
- No project CRUD or student preference API yet.
- No allocation algorithm or team assignment yet.
- The SQL schema is initialised on application startup; versioned migrations are a later improvement.
- The database password in Docker Compose is a local-development default, not a production credential.
