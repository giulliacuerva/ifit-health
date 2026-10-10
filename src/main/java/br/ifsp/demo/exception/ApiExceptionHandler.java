package br.ifsp.demo.exception;

import br.ifsp.demo.controller.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.access.AccessDeniedException;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({ResourceNotFoundException.class, EnrollmentNotFoundException.class,
            EnrollmentActivityNotFoundException.class})
    public ResponseEntity<?> handleResourceNotFound(RuntimeException e) {
        return response(e, NOT_FOUND);
    }

    @ExceptionHandler(InvalidScheduleException.class)
    public ResponseEntity<?> handleInvalidSchedule(InvalidScheduleException e) {
        return response(e, BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException e) {
        return response(e, FORBIDDEN);
    }

    @ExceptionHandler({ActivityScheduleConflictException.class,
            RoomScheduleConflictException.class,
            TrainerScheduleConflictException.class,
            RoomTypeConflictException.class,
            CapacityIsGreaterThanAcceptedException.class,
            EnrollmentActivityAlreadyInactiveException.class})
    public ResponseEntity<?> handleDomainConflict(RuntimeException e) {
        return response(e, CONFLICT);
    }

    private ResponseEntity<ApiException> response(RuntimeException e, HttpStatus status) {
        final ApiException body = ApiException.builder()
                .status(status)
                .message(e.getMessage())
                .developerMessage(e.getClass().getName())
                .timestamp(ZonedDateTime.now(ZoneId.of("Z")))
                .build();
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(value = NullPointerException.class)
    public ResponseEntity<?> handleNullPointerException(NullPointerException e){
        final HttpStatus badRequest = BAD_REQUEST;
        final ApiException apiException = ApiException.builder()
                .status(badRequest)
                .message(e.getMessage())
                .developerMessage(e.getClass().getName())
                .timestamp(ZonedDateTime.now(ZoneId.of("Z")))
                .build();
        return new ResponseEntity<>(apiException, badRequest);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e){
        final HttpStatus badRequest = BAD_REQUEST;
        final ApiException apiException = ApiException.builder()
                .status(badRequest)
                .message(e.getMessage())
                .developerMessage(e.getClass().getName())
                .timestamp(ZonedDateTime.now(ZoneId.of("Z")))
                .build();
        return new ResponseEntity<>(apiException, badRequest);
    }

    @ExceptionHandler(value = IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException e){
        return response(e, CONFLICT);
    }

    @ExceptionHandler(value = EntityAlreadyExistsException.class)
    public ResponseEntity<?> handleEntityAlreadyExistsException(EntityAlreadyExistsException e){
        final HttpStatus conflict = CONFLICT;
        final ApiException apiException = ApiException.builder()
                .status(conflict)
                .message(e.getMessage())
                .developerMessage(e.getClass().getName())
                .timestamp(ZonedDateTime.now(ZoneId.of("Z")))
                .build();
        return new ResponseEntity<>(apiException, conflict);
    }
}
