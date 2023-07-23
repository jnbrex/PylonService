package com.pylon.pylonservice.model.requests.shard;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CreateShardRequestTests extends ShardRequestTests {
    @DataProvider
    private Object[][] provideValidCreateShardRequests() {
        return new Object[][] {
            {
                new CreateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS
                )
            },
            {
                new CreateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_JPG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS
                )
            },
            {
                new CreateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_GIF,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS
                )
            },
            {
                new CreateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_JPG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS
                )
            },
            {
                new CreateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_GIF,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS
                )
            },
            {
                new CreateShardRequest(
                    VALID_SHARD_NAME,
                    "",
                    "",
                    "",
                    "",
                    VALID_INHERITED_SHARD_NAMES_EMPTY,
                    VALID_INHERITED_USERS_EMPTY
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
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS
                )
            },
            {
                new CreateShardRequest(
                    INVALID_SHARD_NAME_CONTAINS_SPACE,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS
                )
            },
            {
                new CreateShardRequest(
                    INVALID_SHARD_NAME_CONTAINS_NON_ALPHANUMERIC,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS
                )
            },
            {
                new CreateShardRequest(
                    VALID_SHARD_NAME,
                    INVALID_SHARD_FRIENDLY_NAME_TOO_LONG,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS
                )
            },
            {
                new CreateShardRequest(
                    VALID_SHARD_NAME,
                    INVALID_SHARD_FRIENDLY_NAME_CONTAINS_NON_ALPHANUMERIC,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS
                )
            },
            {
                new CreateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    INVALID_SHARD_AVATAR_FILENAME_NON_UUID,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS
                )
            },
            {
                new CreateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    INVALID_SHARD_AVATAR_FILENAME_BAD_EXTENSION,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS
                )
            },
            {
                new CreateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_JPG,
                    INVALID_SHARD_BANNER_FILENAME_NON_UUID,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS
                )
            },
            {
                new CreateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_GIF,
                    INVALID_SHARD_BANNER_FILENAME_BAD_EXTENSION,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS
                )
            },
            {
                new CreateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    INVALID_SHARD_DESCRIPTION_TOO_LONG,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS
                )
            },
            {
                new CreateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    INVALID_INHERITED_SHARD_NAMES_NULL,
                    VALID_INHERITED_USERS
                )
            },
            {
                new CreateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    INVALID_INHERITED_USERS_NULL
                )
            },
            {
                new CreateShardRequest(
                    "",
                    "",
                    "",
                    "",
                    "",
                    VALID_INHERITED_SHARD_NAMES_EMPTY,
                    VALID_INHERITED_USERS_EMPTY
                )
            }
        };
    }

    @Test(dataProvider = "provideValidCreateShardRequests")
    public void testValidCreateShardRequests(final CreateShardRequest createShardRequest) {
        testValidShardRequests(createShardRequest);
    }

    @Test(dataProvider = "provideInvalidCreateShardRequests")
    public void testInvalidCreateShardRequests(final CreateShardRequest createShardRequest) {
        testInvalidShardRequests(createShardRequest);
    }
}
