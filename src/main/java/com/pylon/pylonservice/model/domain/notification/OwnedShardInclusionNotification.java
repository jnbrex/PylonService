package com.pylon.pylonservice.model.domain.notification;

import com.pylon.pylonservice.enums.NotificationType;
import com.pylon.pylonservice.model.tables.DatabaseNotification;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Date;

@Value
@EqualsAndHashCode(callSuper=true)
public class OwnedShardInclusionNotification extends Notification {
    String includedShardName;
    String includingShardName;

    @Builder
    OwnedShardInclusionNotification(final String notificationId,
                                    final String toUsername,
                                    final Date createdAt,
                                    final String fromUsername,
                                    final boolean isRead,
                                    final String includedShardName,
                                    final String includingShardName) {
        super(notificationId, toUsername, createdAt, fromUsername, isRead, NotificationType.OWNED_SHARD_INCLUSION);
        this.includedShardName = includedShardName;
        this.includingShardName = includingShardName;
    }

    OwnedShardInclusionNotification(final DatabaseNotification databaseNotification) {
        super(
            databaseNotification.getNotificationId(),
            databaseNotification.getToUsername(),
            databaseNotification.getCreatedAt(),
            databaseNotification.getFromUsername(),
            databaseNotification.isRead(),
            NotificationType.OWNED_SHARD_INCLUSION
        );
        this.includedShardName = databaseNotification.getIncludedShardName();
        this.includingShardName = databaseNotification.getIncludingShardName();
    }

    public DatabaseNotification toDatabaseNotification() {
        return DatabaseNotification.builder()
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
