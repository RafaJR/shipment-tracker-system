package com.mpowerplus.shipmenttrackersystem.notificationservice.infrastructure.adapter.output.notification;

import com.mpowerplus.shipmenttrackersystem.notificationservice.application.port.output.NotificationSender;
import com.mpowerplus.shipmenttrackersystem.notificationservice.domain.model.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Simple notification sender that logs notifications instead of actually sending them.
 * In production, this would be replaced with actual email/SMS/push notification services.
 */
@Component
@Slf4j
public class LoggingNotificationSender implements NotificationSender {

    @Override
    public void send(NotificationType type, String recipient, String subject, String message) {
        log.info("=" .repeat(80));
        log.info("NOTIFICATION [{}]", type);
        log.info("To: {}", recipient);
        log.info("Subject: {}", subject);
        log.info("Message:\n{}", message);
        log.info("=".repeat(80));

        // Simulate notification sending
        // In production, integrate with:
        // - Email: JavaMailSender, SendGrid, AWS SES
        // - SMS: Twilio, AWS SNS
        // - Push: Firebase Cloud Messaging, OneSignal
    }
}
