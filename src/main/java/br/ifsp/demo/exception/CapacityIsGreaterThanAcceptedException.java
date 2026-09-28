package br.ifsp.demo.exception;

public class CapacityIsGreaterThanAcceptedException extends RuntimeException {
  public CapacityIsGreaterThanAcceptedException(String message) {
    super(message);
  }
}
