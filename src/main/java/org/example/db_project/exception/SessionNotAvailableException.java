package org.example.db_project.exception;

public class SessionNotAvailableException extends RuntimeException {
    public SessionNotAvailableException(String message) {
        super(message);
    }

    public SessionNotAvailableException(Long sessionId) {
        super("Session is not available for booking: " + sessionId);
    }
}
