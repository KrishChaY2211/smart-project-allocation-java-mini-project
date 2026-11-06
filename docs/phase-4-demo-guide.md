# Phase 4 — Final college demonstration

This is the final planned phase of the college mini-project. The goal is to make the working allocation flow easy to demonstrate and verify. It intentionally does not add complex features.

## 1. Start the application

From the repository root, start PostgreSQL:

```bash
podman-compose up -d db
podman ps
```

If you use Docker Compose instead:

```bash
docker compose up -d db
docker compose ps
```

Start Spring Boot on port 8081 if port 8080 is occupied:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

Open [http://localhost:8081/](http://localhost:8081/). If you run the application on its default port, use http://localhost:8080/.

## 2. Sign-in accounts

The seeded classroom demo accounts are:

| Role | Email | Password |
|---|---|---|
| Faculty | `faculty@college.local` | `Faculty@12345` |
| Admin | `admin@college.local` | `Admin@12345` |

Register at least two student accounts from the registration tab. Use different email addresses and roll numbers. These credentials are for a local classroom demo only.

## 3. Demonstrate the complete workflow

### A. Faculty adds projects

1. Sign in as faculty.
2. Add a project named **Library Management System**, with a maximum team size of **1**.
3. Add a second project named **Campus Event Portal**, also with a maximum team size of **1**.
4. Confirm both projects appear with their descriptions, required skills and available seats.

Create the first student account before the second so the simple allocation order is predictable for this demonstration.

### B. Students submit preferences

1. Sign in as the first student and rank Library Management System as 1 and Campus Event Portal as 2.
2. Save preferences.
3. Sign in as the second student and rank Library Management System as 1.
4. Save preferences.

### C. Faculty reviews submitted preferences

1. Sign in as faculty.
2. Open **Submitted student preferences**.
3. Confirm both students' choices, roll numbers, departments and preference ranks are visible. Use **Refresh** after students submit or change their choices.

### D. Admin runs allocation

1. Sign in as admin.
2. Select **Run allocation** and confirm.
3. The allocation rule processes students in database ID order and assigns each student their highest-ranked project that still has a free seat.
4. With the example above, the first student receives Library Management System and the second receives Campus Event Portal.

The results table should show both students, their roll numbers, assigned projects and preference ranks.

### E. Students inspect their teams

1. Sign in as each student.
2. Open **My allocation**.
3. Confirm the project title, description, faculty supervisor and allocated team member list are visible.

For a project with a capacity of one, the team list contains only that student. To demonstrate multiple members in one team, create a separate project with a larger capacity and assign multiple students to it.

## 4. Final acceptance checklist

- [ ] Database container is healthy.
- [ ] Application opens in the browser.
- [ ] Student registration and login work.
- [ ] Faculty can add projects.
- [ ] Project details and available seats are visible.
- [ ] Students can save and reload ranked preferences.
- [ ] Faculty can review submitted student preferences before allocation.
- [ ] Admin can run allocation.
- [ ] Allocation results respect project capacity.
- [ ] Admin and faculty can view allocation results.
- [ ] Students can view their assigned project and team members.
- [ ] Logout works for each role.

## 5. Known limitations

- Allocation is a simple deterministic preference-and-capacity rule, not an optimal matching algorithm.
- Students are processed in database ID order, so earlier student records get first consideration.
- If all ranked choices are full, a student remains unallocated.
- Rerunning allocation replaces the previous allocation results.
- There is no chat, notification system, mobile app or production deployment.
- Demo accounts and default database credentials must not be reused for a public deployment.

## 6. Presentation summary

**Problem:** Students need a simple way to express project preferences, while faculty/admin need to distribute limited project seats.

**Solution:** A browser-based Java application where faculty adds projects, students rank choices, and admin runs a seat-limited allocation.

**Main technologies:** Java 21, Spring Boot, Spring JDBC, PostgreSQL 16, HTML, CSS, JavaScript, Maven and GitHub Actions.

**Demonstration sequence:** Faculty adds projects → students submit preferences → admin runs allocation → students view their assigned project and team.
