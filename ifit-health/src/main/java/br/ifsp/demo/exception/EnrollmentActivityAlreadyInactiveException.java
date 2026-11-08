package br.ifsp.demo.exception;

public class EnrollmentActivityAlreadyInactiveException extends RuntimeException {
    public EnrollmentActivityAlreadyInactiveException(String message) {
        super(message);
    }
}
