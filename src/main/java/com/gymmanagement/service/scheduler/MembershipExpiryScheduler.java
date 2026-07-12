package com.gymmanagement.service.scheduler;

import com.gymmanagement.entity.Member;
import com.gymmanagement.entity.MembershipStatus;
import com.gymmanagement.entity.Notification;
import com.gymmanagement.repository.MemberRepository;
import com.gymmanagement.repository.NotificationRepository;
import com.gymmanagement.repository.SettingsRepository;
import com.gymmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Nightly job that:
 *  1. Marks members whose expiry date has passed as EXPIRED and locks their
 *     login (their account can't sign in until an admin unlocks it or renews
 *     their membership).
 *  2. Creates in-app reminder notifications for members expiring soon
 *     (threshold configurable via Settings, default 7 days).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipExpiryScheduler {

    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;
    private final SettingsRepository settingsRepository;
    private final UserRepository userRepository;

    /** Runs every day at 1:00 AM server time. */
    @Scheduled(cron = "0 0 1 * * *")
    public void runExpiryCheck() {
        LocalDate today = LocalDate.now();

        // 1. Expire memberships whose expiry date has passed, and lock their login
        List<Member> expiredCandidates = memberRepository.findByExpiryDateBefore(today);
        for (Member member : expiredCandidates) {
            if (member.getStatus() != MembershipStatus.EXPIRED) {
                member.setStatus(MembershipStatus.EXPIRED);
                memberRepository.save(member);

                userRepository.findByUsernameIgnoreCase(member.getMemberCode()).ifPresent(user -> {
                    if (user.isEnabled()) {
                        user.setEnabled(false);
                        userRepository.save(user);
                    }
                });

                log.info("Membership auto-expired and login locked for member: {}", member.getMemberCode());
            }
        }

        // 2. Create expiry reminders for members expiring soon
        int reminderDays = settingsRepository.findAll().stream()
                .findFirst()
                .map(com.gymmanagement.entity.Settings::getExpiryReminderDays)
                .orElse(7);

        List<Member> expiringSoon = memberRepository.findByExpiryDateBetween(today, today.plusDays(reminderDays));
        for (Member member : expiringSoon) {
            Notification notification = Notification.builder()
                    .recipientId(member.getId())
                    .recipientType("MEMBER")
                    .title("Membership Expiring Soon")
                    .message("Hi " + member.getName() + ", your membership expires on " + member.getExpiryDate()
                            + ". Please renew to continue enjoying our services.")
                    .type("MEMBERSHIP_EXPIRY")
                    .read(false)
                    .build();
            notificationRepository.save(notification);
        }

        log.info("Membership expiry check complete. Expired: {}, Reminders sent: {}",
                expiredCandidates.size(), expiringSoon.size());
    }
}
