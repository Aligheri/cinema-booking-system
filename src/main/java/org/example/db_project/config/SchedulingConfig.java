package org.example.db_project.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.db_project.service.BookingService;
import org.example.db_project.service.SessionService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulingConfig {

    private final BookingService bookingService;
    private final SessionService sessionService;

    @Scheduled(fixedRate = 300000)
    public void expirePendingBookings() {
        int expired = bookingService.expirePendingBookings(15);
        if (expired > 0) {
            log.info("Expired {} pending bookings", expired);
        }
    }

    @Scheduled(fixedRate = 60000)
    public void updateSessionStatuses() {
        int updated = sessionService.updateSessionStatuses();
        if (updated > 0) {
            log.debug("Updated {} session statuses", updated);
        }
    }
}
