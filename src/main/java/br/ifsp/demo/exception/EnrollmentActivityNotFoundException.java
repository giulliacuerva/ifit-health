package br.ifsp.demo.exception;

public class EnrollmentActivityNotFoundException extends RuntimeException {
    public EnrollmentActivityNotFoundException(String message) {
        super(message);
    }
}
