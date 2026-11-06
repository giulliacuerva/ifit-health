package br.ifsp.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.LinkedHashMap;
import java.util.Map;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", BAD_REQUEST.value());
        body.put("message", "Existem campos inválidos no cadastro.");
        body.put("errors", errors);
        body.put("timestamp", ZonedDateTime.now(ZoneId.of("Z")));
        return new ResponseEntity<>(body, BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(DataIntegrityViolationException e) {
        return response(new IllegalStateException(
                "A operação viola uma restrição de integridade do banco de dados.", e), CONFLICT);
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