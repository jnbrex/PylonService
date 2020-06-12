package com.pylon.pylonservice.util;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.pylon.pylonservice.model.tables.UsernameUser;
import lombok.NonNull;

import java.util.regex.Pattern;

public final class UserUtil {
    // https://stackoverflow.com/questions/12018245/regular-expression-to-validate-username
    final static Pattern USERNAME_REGEX_PATTERN =
        Pattern.compile("^(?=.{3,30}$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$");

    public static boolean isUsernameValid(@NonNull final String username) {
        return USERNAME_REGEX_PATTERN.matcher(username).matches();
    }

    public static String getUserIdForUsername(final DynamoDBMapper dynamoDBMapper, final String username) {
        return dynamoDBMapper.load(UsernameUser.class, username).getUserId();
    }
}
