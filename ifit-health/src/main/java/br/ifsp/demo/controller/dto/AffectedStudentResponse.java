package br.ifsp.demo.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Student whose enrolled activities conflict with the proposed schedule change.")
public record AffectedStudentResponse(
        @Schema(description = "Customer UUID.")
        UUID customerId,
        @Schema(description = "Student name.", example = "Ana Souza")
        String customerName,
        @Schema(description = "Activities that conflict with the proposed schedule.")
        List<ConflictingActivityResponse> conflictingActivities
) {
}
