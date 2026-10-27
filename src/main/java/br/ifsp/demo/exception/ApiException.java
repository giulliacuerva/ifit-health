package br.ifsp.demo.exception;

import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

public class ApiException {
    private final String message;
    private final HttpStatus status;
    private final ZonedDateTime timestamp;
    private final String developerMessage;

    public ApiException(String message, HttpStatus status, ZonedDateTime timestamp,
                        String developerMessage) {
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
        this.developerMessage = developerMessage;
    }

    public String getMessage() { return message; }
    public HttpStatus getStatus() { return status; }
    public ZonedDateTime getTimestamp() { return timestamp; }
    public String getDeveloperMessage() { return developerMessage; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String message;
        private HttpStatus status;
        private ZonedDateTime timestamp;
        private String developerMessage;

        public Builder message(String message) { this.message = message; return this; }
        public Builder status(HttpStatus status) { this.status = status; return this; }
        public Builder timestamp(ZonedDateTime timestamp) { this.timestamp = timestamp; return this; }
        public Builder developerMessage(String developerMessage) {
            this.developerMessage = developerMessage;
            return this;
        }
        public ApiException build() {
            return new ApiException(message, status, timestamp, developerMessage);
        }
    }
}
