package in.krishchay.allocation.health;

import in.krishchay.allocation.SmartProjectAllocationApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SmartProjectAllocationApplication.class)
@AutoConfigureMockMvc
class HealthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @Test
    void applicationHealthReturnsUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("smart-project-allocation"));
    }

    @Test
    void databaseHealthReturnsDatabaseMetadata() throws Exception {
        when(jdbcTemplate.queryForObject(eq("SELECT CURRENT_TIMESTAMP"), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenReturn(OffsetDateTime.now());
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(5);

        mockMvc.perform(get("/api/health/database"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.database").value("PostgreSQL"))
                .andExpect(jsonPath("$.publicTableCount").value(5));
    }

    @Test
    void databaseHealthReturnsServiceUnavailableWhenDatabaseCheckFails() throws Exception {
        when(jdbcTemplate.queryForObject(eq("SELECT CURRENT_TIMESTAMP"), any(org.springframework.jdbc.core.RowMapper.class)))
                .thenThrow(new IllegalStateException("database unavailable"));

        mockMvc.perform(get("/api/health/database"))
                .andExpect(status().isServiceUnavailable());
    }
}
