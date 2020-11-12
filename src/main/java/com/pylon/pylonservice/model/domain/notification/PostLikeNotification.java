package com.pylon.pylonservice.model.domain.notification;

import com.pylon.pylonservice.enums.NotificationType;
import lombok.Builder;

import java.util.Date;

public class PostLikeNotification extends Notification {
    private final String postId;

    @Builder
    public PostLikeNotification(final String notificationId,
                                final String toUsername,
                                final Date createdAt,
                                final String fromUsername,
                                final boolean isRead,
                                final String postId) {
        super(notificationId, toUsername, createdAt, fromUsername, isRead);
        this.postId = postId;
    }

    public com.pylon.pylonservice.model.tables.Notification toDatabaseNotification() {
        return com.pylon.pylonservice.model.tables.Notification.builder()
            .toUsername(toUsername)
            .createdAt(createdAt)
            .fromUsername(fromUsername)
            .isRead(isRead)
            .notificationType(NotificationType.POST_LIKE.getValue())
            .postId(postId)
            .build();
    }
}
