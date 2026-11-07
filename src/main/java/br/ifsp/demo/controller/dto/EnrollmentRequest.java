package br.ifsp.demo.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

@Schema(description = "Activity classes selected by the authenticated student for enrollment.")
public record EnrollmentRequest(
        @Schema(description = "List of activity class UUIDs. Must contain at least one item.",
                example = "[\"550e8400-e29b-41d4-a716-446655440000\"]",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty List<UUID> activityClassIds
) {
}
