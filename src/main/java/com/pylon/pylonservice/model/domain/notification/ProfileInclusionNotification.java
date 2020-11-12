package com.pylon.pylonservice.model.domain.notification;

import com.pylon.pylonservice.enums.NotificationType;
import lombok.Builder;

import java.util.Date;

public class ProfileInclusionNotification extends Notification {
    private final String includingShardName;

    @Builder
    public ProfileInclusionNotification(final String notificationId,
                                        final String toUsername,
                                        final Date createdAt,
                                        final String fromUsername,
                                        final boolean isRead,
                                        final String includingShardName) {
        super(notificationId, toUsername, createdAt, fromUsername, isRead);
        this.includingShardName = includingShardName;
    }

    public com.pylon.pylonservice.model.tables.Notification toDatabaseNotification() {
        return com.pylon.pylonservice.model.tables.Notification.builder()
            .toUsername(toUsername)
            .createdAt(createdAt)
            .fromUsername(fromUsername)
            .isRead(isRead)
            .notificationType(NotificationType.PROFILE_INCLUSION.getValue())
            .includingShardName(includingShardName)
            .build();
    }
}
