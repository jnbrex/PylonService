package com.pylon.pylonservice.model.requests.shard;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class UpdateShardRequestTests extends ShardRequestTests {
    private static final String VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.png";
    private static final String VALID_SHARD_FEATURED_IMAGE_FILENAME_JPG = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.jpg";
    private static final String VALID_SHARD_FEATURED_IMAGE_FILENAME_GIF = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.gif";
    private static final String VALID_SHARD_FEATURED_IMAGE_LINK = "http://whatever.com";

    private static final String INVALID_SHARD_FEATURED_IMAGE_FILENAME_NON_UUID = "not a uuid";
    private static final String INVALID_SHARD_FEATURED_IMAGE_FILENAME_BAD_EXTENSION
        = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.jpeg";
    private static final String INVALID_SHARD_FEATURED_IMAGE_LINK_TOO_LONG = "b".repeat(501);

    @DataProvider
    private Object[][] provideValidUpdateShardRequests() {
        return new Object[][] {
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_JPG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_GIF,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_JPG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_GIF,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_JPG,
                    VALID_SHARD_FEATURED_IMAGE_LINK
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_GIF,
                    VALID_SHARD_FEATURED_IMAGE_LINK
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    "",
                    "",
                    "",
                    "",
                    VALID_INHERITED_SHARD_NAMES_EMPTY,
                    VALID_INHERITED_USERS_EMPTY,
                    "",
                    ""
                )
            }
        };
    }

    @DataProvider
    private Object[][] provideInvalidUpdateShardRequests() {
        return new Object[][] {
            {
                new UpdateShardRequest(
                    INVALID_SHARD_NAME_TOO_LONG,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK
                )
            },
            {
                new UpdateShardRequest(
                    INVALID_SHARD_NAME_CONTAINS_SPACE,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK
                )
            },
            {
                new UpdateShardRequest(
                    INVALID_SHARD_NAME_CONTAINS_NON_ALPHANUMERIC,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK
                )
            },
            {
                new UpdateShardRequest(
                    "",
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    INVALID_INHERITED_USERS_NULL,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    INVALID_SHARD_FRIENDLY_NAME_TOO_LONG,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    INVALID_SHARD_FRIENDLY_NAME_CONTAINS_NON_ALPHANUMERIC,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    INVALID_SHARD_AVATAR_FILENAME_NON_UUID,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    INVALID_SHARD_AVATAR_FILENAME_BAD_EXTENSION,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_JPG,
                    INVALID_SHARD_BANNER_FILENAME_NON_UUID,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_GIF,
                    INVALID_SHARD_BANNER_FILENAME_BAD_EXTENSION,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    INVALID_SHARD_DESCRIPTION_TOO_LONG,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    INVALID_INHERITED_SHARD_NAMES_NULL,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_PNG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    INVALID_INHERITED_USERS_NULL,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    VALID_SHARD_FEATURED_IMAGE_LINK
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_JPG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    INVALID_SHARD_FEATURED_IMAGE_FILENAME_NON_UUID,
                    VALID_SHARD_FEATURED_IMAGE_LINK
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_JPG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    INVALID_SHARD_FEATURED_IMAGE_FILENAME_BAD_EXTENSION,
                    VALID_SHARD_FEATURED_IMAGE_LINK
                )
            },
            {
                new UpdateShardRequest(
                    VALID_SHARD_NAME,
                    VALID_SHARD_FRIENDLY_NAME,
                    VALID_SHARD_AVATAR_FILENAME_JPG,
                    VALID_SHARD_BANNER_FILENAME_PNG,
                    VALID_SHARD_DESCRIPTION,
                    VALID_INHERITED_SHARD_NAMES,
                    VALID_INHERITED_USERS,
                    VALID_SHARD_FEATURED_IMAGE_FILENAME_PNG,
                    INVALID_SHARD_FEATURED_IMAGE_LINK_TOO_LONG
                )
            }
        };
    }

    @Test(dataProvider = "provideValidUpdateShardRequests")
    public void testValidUpdateShardRequests(final UpdateShardRequest updateShardRequest) {
        testValidShardRequests(updateShardRequest);
    }

    @Test(dataProvider = "provideInvalidUpdateShardRequests")
    public void testInvalidUpdateShardRequests(final UpdateShardRequest updateShardRequest) {
        testInvalidShardRequests(updateShardRequest);
    }
}
