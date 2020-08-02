package com.pylon.pylonservice.model.requests;

import org.assertj.core.api.Assertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CreateShardRequestTests {
    private static final String VALID_SHARD_NAME = "Fortnite";
    private static final Collection<String> VALID_INHERITED_SHARD_NAMES =
        Stream.of("shard0", "shard1").collect(Collectors.toSet());
    private static final Collection<String> VALID_INHERITED_USERS =
        Stream.of("user0", "user1").collect(Collectors.toList());

    private static final String INVALID_SHARD_NAME_TOO_LONG = "a".repeat(121);
    private static final String INVALID_SHARD_NAME_CONTAINS_SPACE = "shard name";
    private static final String INVALID_SHARD_NAME_CONTAINS_NON_ALPHANUMERIC = "shard$";
    private static final Collection<String> INVALID_INHERITED_SHARD_NAMES = null;
    private static final Collection<String> INVALID_INHERITED_USERS = null;

    @DataProvider
    private Object[][] provideValidCreateShardRequests() {
        return new Object[][] {
            {
                new CreateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS
                )
            }
        };
    }

    @DataProvider
    private Object[][] provideInvalidCreateShardRequests() {
        return new Object[][] {
            {
                new CreateShardRequest(
                    INVALID_SHARD_NAME_TOO_LONG,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS
                )
            },
            {
                new CreateShardRequest(
                    INVALID_SHARD_NAME_CONTAINS_SPACE,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS
                )
            },
            {
                new CreateShardRequest(
                    INVALID_SHARD_NAME_CONTAINS_NON_ALPHANUMERIC,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS
                )
            },
            {
                new CreateShardRequest(
                    VALID_SHARD_NAME,
                    INVALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS
                )
            },
            {
                new CreateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_INHERITED_SHARD_NAMES,
                    INVALID_INHERITED_USERS
                )
            }
        };
    }

    @Test(dataProvider = "provideValidCreateShardRequests")
    public void testValidCreateTopLevelPostRequests(final CreateShardRequest createShardRequest) {
        Assertions.assertThat(createShardRequest.isValid()).isTrue();
    }

    @Test(dataProvider = "provideInvalidCreateShardRequests")
    public void testInvalidCreateTopLevelPostRequests(final CreateShardRequest createShardRequest) {
        Assertions.assertThat(createShardRequest.isValid()).isFalse();
    }
}
