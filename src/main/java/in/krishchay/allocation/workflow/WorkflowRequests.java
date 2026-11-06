package in.krishchay.allocation.workflow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class WorkflowRequests {
    private WorkflowRequests() {}

    public record ProjectRequest(
            @NotBlank @Size(max = 160) String title,
            @NotBlank String description,
            @Size(max = 1000) String requiredSkills,
            @NotNull @Positive Integer maxMembers,
            Long facultyId
    ) {}

    public record PreferencesRequest(
            @NotEmpty @Size(max = 5) List<@NotNull @Positive Long> projectIds
    ) {}
}
