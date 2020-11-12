package com.pylon.pylonservice.model.domain.notification;

import com.pylon.pylonservice.enums.NotificationType;
import lombok.Builder;

import java.util.Date;

public class ProfileFollowNotification extends Notification {
    @Builder
    public ProfileFollowNotification(final String notificationId,
                                     final String toUsername,
                                     final Date createdAt,
                                     final String fromUsername,
                                     final boolean isRead) {
        super(notificationId, toUsername, createdAt, fromUsername, isRead);
    }

    public com.pylon.pylonservice.model.tables.Notification toDatabaseNotification() {
        return com.pylon.pylonservice.model.tables.Notification.builder()
            .toUsername(toUsername)
            .createdAt(createdAt)
            .fromUsername(fromUsername)
            .isRead(isRead)
            .notificationType(NotificationType.PROFILE_FOLLOW.getValue())
            .build();
    }
}
