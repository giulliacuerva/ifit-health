package br.ifsp.demo.controller.dto;

import br.ifsp.demo.exception.ActivityScheduleConflictException;
import org.springframework.http.HttpStatus;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public record ActivityScheduleConflictResponse(
        String message,
        HttpStatus status,
        ZonedDateTime timestamp,
        String developerMessage,
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
