package org.example.db_project.domain.enums;

public enum BookingStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    EXPIRED;

    public boolean canTransitionTo(BookingStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == CONFIRMED || newStatus == CANCELLED || newStatus == EXPIRED;
            case CONFIRMED -> newStatus == COMPLETED || newStatus == CANCELLED;
            case COMPLETED, CANCELLED, EXPIRED -> false;
        };
    }
}
