package com.pylon.pylonservice.util;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.pylon.pylonservice.model.tables.UsernameUser;

public final class UserUtil {
    public static String getUserIdForUsername(final DynamoDBMapper dynamoDBMapper, final String username) {
        return dynamoDBMapper.load(UsernameUser.class, username).getUserId();
    }
}
