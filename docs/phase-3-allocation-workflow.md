# Phase 3 — Main allocation workflow

## Goal
Let faculty add project options, let students rank project choices, and let an admin run a simple seat-limited allocation.

## Browser workflow
1. Sign in as faculty and add projects with a maximum team size.
2. Register or sign in as a student and rank available projects.
3. Sign in as faculty and review the submitted preferences, including each student's ranked project choices.
4. Sign in as admin and run allocation.
5. Sign back in as students to view the assigned project. Faculty and admin can inspect the allocation results table.

## Allocation rule
Students are processed in ascending student database ID. For each student, their preferences are checked from rank 1 upward. The first project with a remaining seat is assigned. If all their selected projects are full, the student remains unallocated. Running allocation again clears and recreates the results.

This intentionally simple rule is easy to explain in a college demonstration. It is not a globally optimal matching algorithm and does not attempt to balance teams by skill or preference score.

## Database
- `projects` stores project details, supervising faculty and team capacity.
- `project_preferences` stores each student's ordered choices.
- `project_allocations` stores one final project assignment per student.

The new `project_allocations` table is created by the existing SQL schema initialisation. It is safe to start the application against the Phase 2 database; the schema uses `CREATE TABLE IF NOT EXISTS`.

## API
- `GET /api/projects`
- `POST /api/projects` — faculty/admin
- `GET /api/preferences/me` — student
- `GET /api/preferences` — faculty/admin; lists submitted preferences with student details and rank
- `PUT /api/preferences/me` — student; JSON contains `projectIds` in rank order
- `POST /api/allocation/run` — admin
- `GET /api/allocation/me` — student
- `GET /api/allocation` — faculty/admin

All endpoints require a valid bearer session.

## Deliberate limits
- Up to five ranked choices are supported in the UI.
- Allocation processes students in database ID order.
- No team chat, notifications, automated skill matching, or advanced optimisation is included.
- Faculty can create projects under their own faculty profile. Admin-created projects are assigned to the first available faculty profile.

## Manual acceptance
- [ ] Faculty can add a project and it appears in the catalogue.
- [ ] Students can save and reload ranked preferences.
- [ ] Admin can run allocation and inspect result counts.
- [ ] Students can view their allocated project.
- [ ] No project exceeds its configured capacity.
- [ ] Students whose choices are all full remain unallocated.