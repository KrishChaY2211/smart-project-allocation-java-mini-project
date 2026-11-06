package in.krishchay.allocation.auth;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;

    public AuthService(JdbcTemplate jdbc, PasswordEncoder passwordEncoder) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Map<String, Object> register(AuthRequests.RegisterRequest request) {
        try {
            Long userId = jdbc.queryForObject("""
                    INSERT INTO app_users(full_name, email, password_hash, role)
                    VALUES (?, ?, ?, 'STUDENT') RETURNING id
                    """, Long.class, request.fullName().trim(), request.email().trim().toLowerCase(),
                    passwordEncoder.encode(request.password()));
            jdbc.update("""
                    INSERT INTO students(user_id, roll_number, department, skills)
                    VALUES (?, ?, ?, ?)
                    """, userId, request.rollNumber().trim(), request.department().trim(),
                    request.skills() == null ? "" : request.skills().trim());
            return Map.of("message", "Student account created", "userId", userId, "role", "STUDENT");
        } catch (DuplicateKeyException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Email or roll number is already registered");
        }
    }

    @Transactional
    public Map<String, Object> login(AuthRequests.LoginRequest request) {
        var users = jdbc.query("""
                SELECT id, full_name, email, password_hash, role
                FROM app_users WHERE LOWER(email) = LOWER(?) AND active = TRUE
                """, (rs, row) -> new Object[] {
                rs.getLong("id"), rs.getString("full_name"), rs.getString("email"),
                rs.getString("password_hash"), rs.getString("role")
        }, request.email().trim());
        if (users.isEmpty() || !passwordEncoder.matches(request.password(), (String) users.get(0)[3])) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        Object[] user = users.get(0);
        String token = UUID.randomUUID().toString() + UUID.randomUUID();
        jdbc.update("""
                INSERT INTO app_sessions(user_id, token_hash, expires_at)
                VALUES (?, ?, ?)
                """, user[0], hash(token), OffsetDateTime.now().plusHours(12));
        return Map.of("token", token, "tokenType", "Bearer", "expiresInHours", 12,
                "user", Map.of("id", user[0], "fullName", user[1], "email", user[2], "role", user[4]));
    }

    public Map<String, Object> currentUser(String authorization) {
        Long userId = authenticatedUserId(authorization);
        return jdbc.queryForMap("""
                SELECT id, full_name AS "fullName", email, role
                FROM app_users WHERE id = ?
                """, userId);
    }

    public void logout(String authorization) {
        jdbc.update("DELETE FROM app_sessions WHERE token_hash = ?", hash(extractToken(authorization)));
    }

    public Long authenticatedUserId(String authorization) {
        String token = extractToken(authorization);
        var ids = jdbc.query("SELECT user_id FROM app_sessions WHERE token_hash = ? AND expires_at > ?",
                (rs, row) -> rs.getLong(1), hash(token), OffsetDateTime.now());
        if (ids.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session is invalid or expired");
        }
        return ids.get(0);
    }

    private String extractToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")
                || authorization.length() <= 7) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bearer token required");
        }
        return authorization.substring(7).trim();
    }

    private String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
