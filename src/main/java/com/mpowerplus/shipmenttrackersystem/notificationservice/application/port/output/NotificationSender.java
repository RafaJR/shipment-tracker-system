package com.mpowerplus.shipmenttrackersystem.notificationservice.application.port.output;

import com.mpowerplus.shipmenttrackersystem.notificationservice.domain.model.NotificationType;

/**
 * Output port for sending notifications through different channels.
 * Abstracts the notification sending mechanism.
 */
public interface NotificationSender {

    /**
     * Sends a notification to the specified recipient.
     *
     * @param type      Type of notification (EMAIL, SMS, etc.)
     * @param recipient Recipient address (email, phone number, etc.)
     * @param subject   Notification subject
     * @param message   Notification message body
     * @throws NotificationSendException if sending fails
     */
    void send(NotificationType type, String recipient, String subject, String message);
}
