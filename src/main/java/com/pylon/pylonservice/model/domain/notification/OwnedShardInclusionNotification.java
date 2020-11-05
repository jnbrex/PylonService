package com.pylon.pylonservice.model.domain.notification;

import com.pylon.pylonservice.enums.NotificationType;

import java.util.Date;

public class OwnedShardInclusionNotification extends Notification {
    private final String includedShardName;
    private final String includingShardName;

    public OwnedShardInclusionNotification(final String toUsername,
                                           final Date createdAt,
                                           final String fromUsername,
                                           final boolean isRead,
                                           final String includedShardName,
                                           final String includingShardName) {
        super(toUsername, createdAt, fromUsername, isRead);
        this.includedShardName = includedShardName;
        this.includingShardName = includingShardName;
    }

    public com.pylon.pylonservice.model.tables.Notification toDatabaseNotification() {
        return com.pylon.pylonservice.model.tables.Notification.builder()
            .toUsername(toUsername)
            .createdAt(createdAt)
            .fromUsername(fromUsername)
            .isRead(isRead)
            .notificationType(NotificationType.OWNED_SHARD_INCLUSION.getValue())
            .includedShardName(includedShardName)
            .includingShardName(includingShardName)
            .build();
    }
}
