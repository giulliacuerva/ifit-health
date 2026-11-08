package br.ifsp.demo.exception;

public class InvalidScheduleException extends RuntimeException {
  public InvalidScheduleException(String message) {
    super(message);
  }
}
