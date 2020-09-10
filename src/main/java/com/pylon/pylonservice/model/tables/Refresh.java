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
@DynamoDBTable(tableName = "-Refresh")
public class Refresh {
    @NonNull
    String refreshToken;
    String username;

    @DynamoDBHashKey
    public String getRefreshToken() {
        return refreshToken;
    }

    @DynamoDBAttribute
    public String getUsername() {
        return username;
    }
}
