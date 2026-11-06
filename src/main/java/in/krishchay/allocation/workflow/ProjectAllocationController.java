package in.krishchay.allocation.workflow;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProjectAllocationController {
    private final ProjectAllocationService service;

    public ProjectAllocationController(ProjectAllocationService service) {
        this.service = service;
    }

    @GetMapping("/projects")
    public List<Map<String, Object>> projects(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return service.listProjects(authorization);
    }

    @PostMapping("/projects")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> createProject(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody WorkflowRequests.ProjectRequest request) {
        return service.createProject(authorization, request);
    }

    @PutMapping("/preferences/me")
    public Map<String, Object> savePreferences(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody WorkflowRequests.PreferencesRequest request) {
        return service.savePreferences(authorization, request);
    }

    @GetMapping("/preferences/me")
    public List<Map<String, Object>> myPreferences(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return service.getMyPreferences(authorization);
    }

    @PostMapping("/allocation/run")
    public Map<String, Object> runAllocation(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return service.runAllocation(authorization);
    }

    @GetMapping("/allocation/me")
    public List<Map<String, Object>> myAllocation(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return service.getMyAllocation(authorization);
    }

    @GetMapping("/allocation")
    public List<Map<String, Object>> allocations(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return service.listAllocations(authorization);
    }
}
