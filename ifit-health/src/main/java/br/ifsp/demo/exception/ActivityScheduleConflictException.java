package br.ifsp.demo.exception;

public class ActivityScheduleConflictException extends RuntimeException {
    public ActivityScheduleConflictException(String message) {
        super(message);
    }
}
