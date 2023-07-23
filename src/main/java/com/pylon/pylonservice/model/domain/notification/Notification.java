package com.pylon.pylonservice.model.domain.notification;

import com.pylon.pylonservice.enums.NotificationType;
import com.pylon.pylonservice.model.tables.DatabaseNotification;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@AllArgsConstructor
@Data
public abstract class Notification implements Serializable {
    private static final long serialVersionUID = 0L;

    protected final String notificationId;
    protected final String toUsername;
    protected final Date createdAt;
    protected final String fromUsername;
    protected boolean isRead;
    protected final NotificationType notificationType;

    public abstract DatabaseNotification toDatabaseNotification();

    public static Notification fromDatabaseNotification(final DatabaseNotification databaseNotification) {
        switch (databaseNotification.getNotificationType()) {
            case 1:
                return new PostLikeNotification(databaseNotification);
            case 2:
                return new PostCommentNotification(databaseNotification);
            case 3:
                return new ProfileFollowNotification(databaseNotification);
            case 4:
                return new OwnedShardInclusionNotification(databaseNotification);
            case 5:
                return new ProfileInclusionNotification(databaseNotification);
            default:
                throw new IllegalStateException(String.format(
                    "Unexpected notification type for notificationId: %s", databaseNotification.getNotificationId()
                ));
        }
    }
}
