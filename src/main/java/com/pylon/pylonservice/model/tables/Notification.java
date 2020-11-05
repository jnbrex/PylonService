package com.pylon.pylonservice.model.tables;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
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
public class Notification {
    @NonNull
    String toUsername;
    @NonNull
    Date createdAt;
    @NonNull
    String fromUsername;
    @NonNull
    int notificationType;
    @NonNull
    boolean isRead;
    String postId;
    String commentPostId;
    String includedShardName;
    String includingShardName;

    @DynamoDBHashKey
    public String getToUsername() {
        return toUsername;
    }

    @DynamoDBRangeKey
    public Date getCreatedAt() {
        return createdAt;
    }

    @DynamoDBAttribute
    public String getFromUsername() {
        return fromUsername;
    }

    @DynamoDBAttribute
    public int getNotificationType() {
        return notificationType;
    }

    @DynamoDBAttribute
    public boolean isRead() {
        return isRead;
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
