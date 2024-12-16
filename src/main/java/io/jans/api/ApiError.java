package io.jans.api;

public class ApiError extends RuntimeException {
    
    public ApiError(final String message) {
        super(message);
    }

    public ApiError(final String message, final Throwable cause) {
        super(message,cause);
    }
}
