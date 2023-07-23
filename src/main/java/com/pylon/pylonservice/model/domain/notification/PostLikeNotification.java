package com.pylon.pylonservice.model.domain.notification;

import com.pylon.pylonservice.enums.NotificationType;
import com.pylon.pylonservice.model.tables.DatabaseNotification;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Date;

@Value
@EqualsAndHashCode(callSuper=true)
public class PostLikeNotification extends Notification {
    String postId;

    @Builder
    PostLikeNotification(final String notificationId,
                         final String toUsername,
                         final Date createdAt,
                         final String fromUsername,
                         final boolean isRead,
                         final String postId) {
        super(notificationId, toUsername, createdAt, fromUsername, isRead, NotificationType.POST_LIKE);
        this.postId = postId;
    }

    PostLikeNotification(final DatabaseNotification databaseNotification) {
        super(
            databaseNotification.getNotificationId(),
            databaseNotification.getToUsername(),
            databaseNotification.getCreatedAt(),
            databaseNotification.getFromUsername(),
            databaseNotification.isRead(),
            NotificationType.POST_LIKE
        );
        this.postId = databaseNotification.getPostId();
    }

    public DatabaseNotification toDatabaseNotification() {
        return DatabaseNotification.builder()
            .notificationId(notificationId)
            .toUsername(toUsername)
            .createdAt(createdAt)
            .fromUsername(fromUsername)
            .isRead(isRead)
            .notificationType(NotificationType.POST_LIKE.getValue())
            .postId(postId)
            .build();
    }
}
