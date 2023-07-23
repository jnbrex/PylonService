package com.pylon.pylonservice.model.domain.notification;

import com.pylon.pylonservice.enums.NotificationType;
import com.pylon.pylonservice.model.tables.DatabaseNotification;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Date;

@Value
@EqualsAndHashCode(callSuper=true)
public class ProfileFollowNotification extends Notification {
    @Builder
    ProfileFollowNotification(final String notificationId,
                              final String toUsername,
                              final Date createdAt,
                              final String fromUsername,
                              final boolean isRead) {
        super(notificationId, toUsername, createdAt, fromUsername, isRead, NotificationType.PROFILE_FOLLOW);
    }

    ProfileFollowNotification(final DatabaseNotification databaseNotification) {
        super(
            databaseNotification.getNotificationId(),
            databaseNotification.getToUsername(),
            databaseNotification.getCreatedAt(),
            databaseNotification.getFromUsername(),
            databaseNotification.isRead(),
            NotificationType.PROFILE_FOLLOW
        );
    }

    public DatabaseNotification toDatabaseNotification() {
        return DatabaseNotification.builder()
            .notificationId(notificationId)
            .toUsername(toUsername)
            .createdAt(createdAt)
            .fromUsername(fromUsername)
            .isRead(isRead)
            .notificationType(NotificationType.PROFILE_FOLLOW.getValue())
            .build();
    }
}
