package br.ifsp.demo.controller.dto;

import java.util.List;
import java.util.UUID;

public record AffectedStudentResponse(
        UUID customerId,
        String customerName,
        List<ConflictingActivityResponse> conflictingActivities
) {
}
