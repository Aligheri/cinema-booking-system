package org.example.db_project.exception;

public class SessionOverlapException extends RuntimeException {
    public SessionOverlapException(String message) {
        super(message);
    }

    public SessionOverlapException(Long hallId) {
        super("Session overlaps with existing session in hall: " + hallId);
    }
}
