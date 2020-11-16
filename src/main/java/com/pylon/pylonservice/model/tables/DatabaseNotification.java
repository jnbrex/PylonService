package com.pylon.pylonservice.model.tables;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Setter
@DynamoDBTable(tableName = "-Notification")
public class DatabaseNotification {
    private static final String USER_NOTIFICATION_GSI_NAME = "UserNotification";

    @NonNull
    String notificationId;
    String toUsername;
    Date createdAt;
    String fromUsername;
    boolean isRead;
    int notificationType;
    String postId;
    String commentPostId;
    String includedShardName;
    String includingShardName;

    @DynamoDBHashKey
    public String getNotificationId() {
        return notificationId;
    }

    @DynamoDBAttribute
    @DynamoDBIndexHashKey(globalSecondaryIndexName = USER_NOTIFICATION_GSI_NAME)
    public String getToUsername() {
        return toUsername;
    }

    @DynamoDBAttribute
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = USER_NOTIFICATION_GSI_NAME)
    public Date getCreatedAt() {
        return createdAt;
    }

    @DynamoDBAttribute
    public String getFromUsername() {
        return fromUsername;
    }

    @DynamoDBAttribute
    public boolean isRead() {
        return isRead;
    }

    @DynamoDBAttribute
    public int getNotificationType() {
        return notificationType;
    }

    @DynamoDBAttribute
    public String getPostId() {
        return postId;
    }

    @DynamoDBAttribute
    public String getCommentPostId() {
        return commentPostId;
    }

    @DynamoDBAttribute
    public String getIncludedShardName() {
        return includedShardName;
    }

    @DynamoDBAttribute
    public String getIncludingShardName() {
        return includingShardName;
    }
}
