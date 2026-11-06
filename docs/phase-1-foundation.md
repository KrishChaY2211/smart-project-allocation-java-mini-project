# Phase 1 — Foundation

## Goal
Get a small Spring Boot and PostgreSQL application running as the base for the college project demo.

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

This is a college working model, not a production system. Keep the implementation small and easy to demonstrate.

- Basic student registration and login APIs have since been added under `/api/auth`; see the README for their current contract.
- Project CRUD, preference submission and allocation are still future implementation work.
- No cloud deployment, microservices, advanced optimisation, or production security infrastructure is planned.
- The SQL schema is initialised on application startup.
- The database password in Docker Compose is a local-development default.

## College demo acceptance

The project is useful when a student can register, submit ranked project choices, and later view an allocation made using project seat limits. A faculty/admin demo account should be able to add projects. Team display and a basic discussion feature are optional only after this main workflow works reliably.
