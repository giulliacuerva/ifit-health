package br.ifsp.demo.exception;

public class RoomScheduleConflictException extends RuntimeException {
    public RoomScheduleConflictException(String message) {
        super(message);
    }
}
