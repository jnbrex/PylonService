package com.pylon.pylonservice.model.domain.notification;

import com.pylon.pylonservice.enums.NotificationType;
import lombok.Builder;

import java.util.Date;

public class PostCommentNotification extends Notification {
    private final String postId;
    private final String commentPostId;

    @Builder
    public PostCommentNotification(final String notificationId,
                                   final String toUsername,
                                   final Date createdAt,
                                   final String fromUsername,
                                   final boolean isRead,
                                   final String postId,
                                   final String commentPostId) {
        super(notificationId, toUsername, createdAt, fromUsername, isRead);
        this.postId = postId;
        this.commentPostId = commentPostId;
    }

    public com.pylon.pylonservice.model.tables.Notification toDatabaseNotification() {
        return com.pylon.pylonservice.model.tables.Notification.builder()
            .toUsername(toUsername)
            .createdAt(createdAt)
            .fromUsername(fromUsername)
            .isRead(isRead)
            .notificationType(NotificationType.POST_COMMENT.getValue())
            .postId(postId)
            .commentPostId(commentPostId)
            .build();
    }
}
