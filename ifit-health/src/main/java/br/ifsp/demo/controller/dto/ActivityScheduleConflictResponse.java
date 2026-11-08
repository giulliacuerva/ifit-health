package br.ifsp.demo.controller.dto;

import br.ifsp.demo.exception.ActivityScheduleConflictException;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Schema(description = "Conflict response returned when an activity schedule update affects enrolled students.")
public record ActivityScheduleConflictResponse(
        @Schema(description = "Human-readable conflict message.", example = "The new schedule conflicts with enrolled students.")
        String message,
        @Schema(description = "HTTP status.", example = "CONFLICT")
        HttpStatus status,
        @Schema(description = "Time when the conflict was detected.", example = "2026-11-07T10:15:30Z")
        ZonedDateTime timestamp,
        @Schema(description = "Exception type for diagnostics.")
        String developerMessage,
        @Schema(description = "Students and activities affected by the proposed schedule change.")
        List<AffectedStudentResponse> affectedStudents
) {

    public static ActivityScheduleConflictResponse from(
            ActivityScheduleConflictException exception,
            List<AffectedStudentResponse> affectedStudents
    ) {
        return new ActivityScheduleConflictResponse(
                exception.getMessage(),
                HttpStatus.CONFLICT,
                ZonedDateTime.now(ZoneId.of("Z")),
                exception.getClass().getName(),
                List.copyOf(affectedStudents)
        );
    }
}
