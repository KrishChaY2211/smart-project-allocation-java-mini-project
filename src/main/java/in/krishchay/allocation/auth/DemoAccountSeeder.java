package in.krishchay.allocation.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemoAccountSeeder implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;

    @Value("${demo.admin.name:Demo Administrator}")
    private String adminName;
    @Value("${demo.admin.email:admin@college.local}")
    private String adminEmail;
    @Value("${demo.admin.password:Admin@12345}")
    private String adminPassword;
    @Value("${demo.faculty.name:Demo Faculty}")
    private String facultyName;
    @Value("${demo.faculty.email:faculty@college.local}")
    private String facultyEmail;
    @Value("${demo.faculty.password:Faculty@12345}")
    private String facultyPassword;

    public DemoAccountSeeder(JdbcTemplate jdbc, PasswordEncoder encoder) {
        this.jdbc = jdbc;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createAccount(adminName, adminEmail, adminPassword, "ADMIN");
        Long facultyUserId = createAccount(facultyName, facultyEmail, facultyPassword, "FACULTY");
        jdbc.update("""
                INSERT INTO faculty(user_id, department, max_projects)
                VALUES (?, 'Computer Science', 3)
                ON CONFLICT (user_id) DO NOTHING
                """, facultyUserId);
    }

    private Long createAccount(String name, String email, String password, String role) {
        var existing = jdbc.query("SELECT id FROM app_users WHERE LOWER(email) = LOWER(?)",
                (rs, row) -> rs.getLong(1), email.trim());
        if (!existing.isEmpty()) {
            return existing.get(0);
        }
        return jdbc.queryForObject("""
                INSERT INTO app_users(full_name, email, password_hash, role)
                VALUES (?, ?, ?, ?) RETURNING id
                """, Long.class, name, email.trim().toLowerCase(), encoder.encode(password), role);
    }
}
