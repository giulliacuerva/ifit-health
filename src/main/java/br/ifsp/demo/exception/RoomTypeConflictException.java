package br.ifsp.demo.exception;

public class RoomTypeConflictException extends RuntimeException {
    public RoomTypeConflictException(String message) {
        super(message);
    }
}