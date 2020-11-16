package com.pylon.pylonservice.model.domain.notification;

import com.pylon.pylonservice.enums.NotificationType;
import com.pylon.pylonservice.model.tables.DatabaseNotification;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Date;

@Value
@EqualsAndHashCode(callSuper=true)
public class PostCommentNotification extends Notification {
    String postId;
    String commentPostId;

    @Builder
    PostCommentNotification(final String notificationId,
                            final String toUsername,
                            final Date createdAt,
                            final String fromUsername,
                            final boolean isRead,
                            final String postId,
                            final String commentPostId) {
        super(notificationId, toUsername, createdAt, fromUsername, isRead, NotificationType.POST_COMMENT);
        this.postId = postId;
        this.commentPostId = commentPostId;
    }

    PostCommentNotification(final DatabaseNotification databaseNotification) {
        super(
            databaseNotification.getNotificationId(),
            databaseNotification.getToUsername(),
            databaseNotification.getCreatedAt(),
            databaseNotification.getFromUsername(),
            databaseNotification.isRead(),
            NotificationType.POST_COMMENT
        );
        this.postId = databaseNotification.getPostId();
        this.commentPostId = databaseNotification.getCommentPostId();
    }

    public DatabaseNotification toDatabaseNotification() {
        return DatabaseNotification.builder()
            .notificationId(notificationId)
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
