package in.krishchay.allocation.health;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public Map<String, Object> applicationHealth() {
        return Map.of(
                "status", "UP",
                "application", "smart-project-allocation",
                "timestamp", OffsetDateTime.now()
        );
    }

    @GetMapping("/database")
    public Map<String, Object> databaseHealth() {
        try {
            OffsetDateTime databaseTime = jdbcTemplate.queryForObject(
                    "SELECT CURRENT_TIMESTAMP",
                    (rs, rowNum) -> rs.getObject(1, OffsetDateTime.class)
            );
            Integer tableCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public'",
                    Integer.class
            );
            return Map.of(
                    "status", "UP",
                    "database", "PostgreSQL",
                    "databaseTime", databaseTime,
                    "publicTableCount", tableCount
            );
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Database connectivity check failed",
                    exception
            );
        }
    }
}
