package com.pylon.pylonservice.model.tables;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Setter
@DynamoDBTable(tableName = "-UsernameUser")
public class UsernameUser {
    @NonNull
    String username;
    @NonNull
    String userId;

    @DynamoDBHashKey
    public String getUsername() {
        return username;
    }

    @DynamoDBAttribute
    public String getUserId() {
        return userId;
    }
}