package com.gymmanagement.service.realtime;

import com.gymmanagement.dto.DashboardStatsResponse;
import com.gymmanagement.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Pushes a fresh {@link DashboardStatsResponse} to every subscriber of
 * /topic/dashboard whenever a payment, check-in, or membership change occurs,
 * so the admin dashboard's cards and charts update live without a page refresh.
 *
 * Called directly (synchronously, after the triggering write has committed)
 * from PaymentServiceImpl, AttendanceServiceImpl and MemberServiceImpl.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimeDashboardPublisher {

    private final SimpMessagingTemplate messagingTemplate;
    private final ReportService reportService;

    public void publish(String eventType) {
        try {
            DashboardStatsResponse stats = reportService.getDashboardStats();
            messagingTemplate.convertAndSend("/topic/dashboard",
                    new DashboardEvent(eventType, stats));
            log.debug("Broadcast dashboard update for event: {}", eventType);
        } catch (Exception e) {
            // A broadcast failure must never break the underlying business transaction.
            log.warn("Failed to broadcast dashboard update: {}", e.getMessage());
        }
    }

    /** Envelope sent over the socket so the frontend knows what triggered the refresh. */
    public record DashboardEvent(String eventType, DashboardStatsResponse stats) {
    }
}
