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
@DynamoDBTable(tableName = "-User")
public class User {
    @NonNull
    String userId;
    @NonNull
    String username;
    @NonNull
    String email;
    @NonNull
    String password;

    @DynamoDBHashKey
    public String getUserId() {
        return userId;
    }

    @DynamoDBAttribute
    public String getUsername() {
        return username;
    }

    @DynamoDBAttribute
    public String getEmail() {
        return email;
    }

    @DynamoDBAttribute
    public String getPassword() {
        return password;
    }
}