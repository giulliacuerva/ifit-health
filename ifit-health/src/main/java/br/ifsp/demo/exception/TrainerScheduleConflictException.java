package br.ifsp.demo.exception;

public class TrainerScheduleConflictException extends RuntimeException {
    public TrainerScheduleConflictException(String message) {
        super(message);
    }
}
