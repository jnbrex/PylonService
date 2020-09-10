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
@DynamoDBTable(tableName = "-PasswordReset")
public class PasswordReset {
    @NonNull
    String passwordResetToken;
    @NonNull
    String username;
    long ttl;

    @DynamoDBHashKey
    public String getPasswordResetToken() {
        return passwordResetToken;
    }

    @DynamoDBAttribute
    public String getUsername() {
        return username;
    }

    @DynamoDBAttribute
    public long getTtl() { return ttl; }
}
