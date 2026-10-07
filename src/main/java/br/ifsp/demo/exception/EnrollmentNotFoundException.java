package br.ifsp.demo.exception;

public class EnrollmentNotFoundException extends RuntimeException {
    public EnrollmentNotFoundException(String message) {
        super(message);
    }
}
