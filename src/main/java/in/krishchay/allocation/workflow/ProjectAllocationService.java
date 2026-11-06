package in.krishchay.allocation.workflow;

import in.krishchay.allocation.auth.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ProjectAllocationService {
    private final JdbcTemplate jdbc;
    private final AuthService auth;

    public ProjectAllocationService(JdbcTemplate jdbc, AuthService auth) {
        this.jdbc = jdbc;
        this.auth = auth;
    }

    public List<Map<String, Object>> listProjects(String authorization) {
        auth.authenticatedUserId(authorization);
        return jdbc.queryForList("""
                SELECT p.id, p.title, p.description, p.required_skills AS "requiredSkills",
                       p.max_members AS "maxMembers", u.full_name AS "facultyName",
                       p.max_members - COUNT(a.id) AS "seatsAvailable"
                FROM projects p
                JOIN faculty f ON f.id = p.faculty_id
                JOIN app_users u ON u.id = f.user_id
                LEFT JOIN project_allocations a ON a.project_id = p.id
                WHERE p.active = TRUE
                GROUP BY p.id, u.full_name
                ORDER BY p.id
                """);
    }

    @Transactional
    public Map<String, Object> createProject(String authorization, WorkflowRequests.ProjectRequest request) {
        Long userId = auth.authenticatedUserId(authorization);
        String role = roleOf(userId);
        if (!role.equals("ADMIN") && !role.equals("FACULTY")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only faculty or admin can add projects");
        }

        Long facultyId;
        if (role.equals("FACULTY")) {
            facultyId = jdbc.queryForObject("SELECT id FROM faculty WHERE user_id = ?", Long.class, userId);
        } else if (request.facultyId() != null) {
            facultyId = request.facultyId();
            Integer exists = jdbc.queryForObject("SELECT COUNT(*) FROM faculty WHERE id = ?", Integer.class, facultyId);
            if (exists == null || exists == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faculty account was not found");
            }
        } else {
            List<Long> facultyIds = jdbc.query("SELECT id FROM faculty ORDER BY id LIMIT 1",
                    (rs, row) -> rs.getLong(1));
            if (facultyIds.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Create a faculty account before adding projects");
            }
            facultyId = facultyIds.get(0);
        }

        Long projectId = jdbc.queryForObject("""
                INSERT INTO projects(title, description, required_skills, faculty_id, max_members)
                VALUES (?, ?, ?, ?, ?) RETURNING id
                """, Long.class, request.title().trim(), request.description().trim(),
                request.requiredSkills() == null ? "" : request.requiredSkills().trim(),
                facultyId, request.maxMembers());
        return Map.of("message", "Project created", "projectId", projectId);
    }

    @Transactional
    public Map<String, Object> savePreferences(String authorization, WorkflowRequests.PreferencesRequest request) {
        Long studentId = studentId(authorization);
        List<Long> projectIds = request.projectIds();
        if (new HashSet<>(projectIds).size() != projectIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A project can only be selected once");
        }

        for (Long projectId : projectIds) {
            Integer exists = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM projects WHERE id = ? AND active = TRUE", Integer.class, projectId);
            if (exists == null || exists == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One of the selected projects is unavailable");
            }
        }

        jdbc.update("DELETE FROM project_preferences WHERE student_id = ?", studentId);
        for (int i = 0; i < projectIds.size(); i++) {
            jdbc.update("""
                    INSERT INTO project_preferences(student_id, project_id, preference_rank)
                    VALUES (?, ?, ?)
                    """, studentId, projectIds.get(i), i + 1);
        }
        return Map.of("message", "Preferences saved", "numberOfPreferences", projectIds.size());
    }

    public List<Map<String, Object>> getMyPreferences(String authorization) {
        Long studentId = studentId(authorization);
        return jdbc.queryForList("""
                SELECT p.id AS "projectId", p.title, pp.preference_rank AS "rank"
                FROM project_preferences pp
                JOIN projects p ON p.id = pp.project_id
                WHERE pp.student_id = ?
                ORDER BY pp.preference_rank
                """, studentId);
    }

    @Transactional
    public Map<String, Object> runAllocation(String authorization) {
        requireRole(authorization, "ADMIN");
        jdbc.update("DELETE FROM project_allocations");

        Map<Long, Integer> remainingSeats = new HashMap<>();
        jdbc.query("""
                SELECT id, max_members FROM projects WHERE active = TRUE ORDER BY id
                """, (org.springframework.jdbc.core.RowCallbackHandler) rs -> {
            remainingSeats.put(rs.getLong("id"), rs.getInt("max_members"));
        });

        List<Map<String, Object>> choices = jdbc.queryForList("""
                SELECT pp.student_id, pp.project_id
                FROM project_preferences pp
                JOIN projects p ON p.id = pp.project_id AND p.active = TRUE
                ORDER BY pp.student_id, pp.preference_rank
                """);

        Set<Long> assignedStudents = new LinkedHashSet<>();
        for (Map<String, Object> choice : choices) {
            Long studentId = ((Number) choice.get("student_id")).longValue();
            Long projectId = ((Number) choice.get("project_id")).longValue();
            if (!assignedStudents.contains(studentId) && remainingSeats.getOrDefault(projectId, 0) > 0) {
                jdbc.update("INSERT INTO project_allocations(student_id, project_id) VALUES (?, ?)",
                        studentId, projectId);
                assignedStudents.add(studentId);
                remainingSeats.put(projectId, remainingSeats.get(projectId) - 1);
            }
        }

        Integer totalStudents = jdbc.queryForObject("SELECT COUNT(*) FROM students", Integer.class);
        int total = totalStudents == null ? 0 : totalStudents;
        return Map.of("message", "Allocation completed", "allocatedStudents", assignedStudents.size(),
                "unallocatedStudents", total - assignedStudents.size(), "totalStudents", total);
    }

    public List<Map<String, Object>> getMyAllocation(String authorization) {
        Long studentId = studentId(authorization);
        return jdbc.queryForList("""
                SELECT p.id AS "projectId", p.title, p.description,
                       u.full_name AS "facultyName", a.allocated_at AS "allocatedAt"
                FROM project_allocations a
                JOIN projects p ON p.id = a.project_id
                JOIN faculty f ON f.id = p.faculty_id
                JOIN app_users u ON u.id = f.user_id
                WHERE a.student_id = ?
                """, studentId);
    }

    public List<Map<String, Object>> listAllocations(String authorization) {
        Long userId = auth.authenticatedUserId(authorization);
        String role = roleOf(userId);
        if (!role.equals("ADMIN") && !role.equals("FACULTY")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only faculty or admin can view all allocations");
        }
        return jdbc.queryForList("""
                SELECT s.roll_number AS "rollNumber", u.full_name AS "studentName",
                       p.title AS "projectTitle", pp.preference_rank AS "preferenceRank"
                FROM project_allocations a
                JOIN students s ON s.id = a.student_id
                JOIN app_users u ON u.id = s.user_id
                JOIN projects p ON p.id = a.project_id
                LEFT JOIN project_preferences pp
                  ON pp.student_id = a.student_id AND pp.project_id = a.project_id
                ORDER BY p.title, s.roll_number
                """);
    }

    private Long studentId(String authorization) {
        Long userId = auth.authenticatedUserId(authorization);
        if (!roleOf(userId).equals("STUDENT")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only students can use this feature");
        }
        List<Long> ids = jdbc.query("SELECT id FROM students WHERE user_id = ?",
                (rs, row) -> rs.getLong(1), userId);
        if (ids.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student profile was not found");
        }
        return ids.get(0);
    }

    private void requireRole(String authorization, String requiredRole) {
        Long userId = auth.authenticatedUserId(authorization);
        if (!roleOf(userId).equals(requiredRole)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only an admin can run allocation");
        }
    }

    private String roleOf(Long userId) {
        return jdbc.queryForObject("SELECT role FROM app_users WHERE id = ?", String.class, userId);
    }
}
