package com.pylon.pylonservice.model.requests.post;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CreateTopLevelPostRequestTests extends CreatePostRequestTests {
    private static final String VALID_TOP_LEVEL_POST_TITLE = "This is a POST TITLE!";
    private static final String VALID_TOP_LEVEL_POST_FILENAME_PNG = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.png";
    private static final String VALID_TOP_LEVEL_POST_FILENAME_JPG = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.jpg";
    private static final String VALID_TOP_LEVEL_POST_FILENAME_GIF = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.gif";
    private static final String VALID_TOP_LEVEL_POST_CONTENT_URL = "https://pylon.gg";

    private static final String INVALID_TOP_LEVEL_POST_TITLE_TOO_LONG = "a".repeat(451);
    private static final String INVALID_TOP_LEVEL_POST_FILENAME = "This is not a uuid";
    private static final String INVALID_TOP_LEVEL_POST_FILENAME_BAD_EXTENSION
        = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.abc";
    private static final String INVALID_TOP_LEVEL_POST_CONTENT_URL_TOO_LONG = "a".repeat(10001);
    private static final String INVALID_QUICK_POST_BODY_TOO_LONG = "a".repeat(401);

    @DataProvider
    private Object[][] provideValidCreateTopLevelPostRequests() {
        return new Object[][] {
            {
                new CreateTopLevelPostRequest(
                    VALID_TOP_LEVEL_POST_TITLE,
                    VALID_TOP_LEVEL_POST_FILENAME_PNG,
                    VALID_TOP_LEVEL_POST_CONTENT_URL,
                    VALID_POST_BODY
                )
            },
            {
                new CreateTopLevelPostRequest(
                    VALID_TOP_LEVEL_POST_TITLE,
                    VALID_TOP_LEVEL_POST_FILENAME_JPG,
                    VALID_TOP_LEVEL_POST_CONTENT_URL,
                    VALID_POST_BODY
                )
            },
            {
                new CreateTopLevelPostRequest(
                    VALID_TOP_LEVEL_POST_TITLE,
                    VALID_TOP_LEVEL_POST_FILENAME_GIF,
                    VALID_TOP_LEVEL_POST_CONTENT_URL,
                    VALID_POST_BODY
                )
            },
            {
                new CreateTopLevelPostRequest(
                    VALID_TOP_LEVEL_POST_TITLE,
                    "",
                    "",
                    ""
                )
            },
            {
                new CreateTopLevelPostRequest(
                    "",
                    VALID_TOP_LEVEL_POST_FILENAME_GIF,
                    "",
                    ""
                )
            },
            {
                new CreateTopLevelPostRequest(
                    "",
                    "",
                    "",
                    VALID_POST_BODY
                )
            }
        };
    }

    @DataProvider
    private Object[][] provideInvalidCreateTopLevelPostRequests() {
        return new Object[][] {
            {
                new CreateTopLevelPostRequest(
                    INVALID_TOP_LEVEL_POST_TITLE_TOO_LONG,
                    VALID_TOP_LEVEL_POST_FILENAME_PNG,
                    VALID_TOP_LEVEL_POST_CONTENT_URL,
                    VALID_POST_BODY
                )
            },
            {
                new CreateTopLevelPostRequest(
                    VALID_TOP_LEVEL_POST_TITLE,
                    INVALID_TOP_LEVEL_POST_FILENAME,
                    VALID_TOP_LEVEL_POST_CONTENT_URL,
                    VALID_POST_BODY
                )
            },
            {
                new CreateTopLevelPostRequest(
                    VALID_TOP_LEVEL_POST_TITLE,
                    INVALID_TOP_LEVEL_POST_FILENAME_BAD_EXTENSION,
                    VALID_TOP_LEVEL_POST_CONTENT_URL,
                    VALID_POST_BODY
                )
            },
            {
                new CreateTopLevelPostRequest(
                    VALID_TOP_LEVEL_POST_TITLE,
                    VALID_TOP_LEVEL_POST_FILENAME_PNG,
                    INVALID_TOP_LEVEL_POST_CONTENT_URL_TOO_LONG,
                    VALID_POST_BODY
                )
            },
            {
                new CreateTopLevelPostRequest(
                    VALID_TOP_LEVEL_POST_TITLE,
                    VALID_TOP_LEVEL_POST_FILENAME_PNG,
                    VALID_TOP_LEVEL_POST_CONTENT_URL,
                    INVALID_POST_BODY_TOO_LONG
                )
            },
            {
                new CreateTopLevelPostRequest(
                    "",
                    "",
                    "",
                    INVALID_QUICK_POST_BODY_TOO_LONG
                )
            },
            {
                new CreateTopLevelPostRequest(
                    null,
                    VALID_TOP_LEVEL_POST_FILENAME_PNG,
                    VALID_TOP_LEVEL_POST_CONTENT_URL,
                    VALID_POST_BODY
                )
            },
            {
                new CreateTopLevelPostRequest(
                    VALID_TOP_LEVEL_POST_TITLE,
                    null,
                    VALID_TOP_LEVEL_POST_CONTENT_URL,
                    VALID_POST_BODY
                )
            },
            {
                new CreateTopLevelPostRequest(
                    VALID_TOP_LEVEL_POST_TITLE,
                    VALID_TOP_LEVEL_POST_FILENAME_PNG,
                    null,
                    VALID_POST_BODY
                )
            },
            {
                new CreateTopLevelPostRequest(
                    VALID_TOP_LEVEL_POST_TITLE,
                    VALID_TOP_LEVEL_POST_FILENAME_PNG,
                    VALID_TOP_LEVEL_POST_CONTENT_URL,
                    null
                )
            },
            {
                new CreateTopLevelPostRequest(
                    "",
                    "",
                    VALID_TOP_LEVEL_POST_CONTENT_URL,
                    ""
                )
            },
            {
                new CreateTopLevelPostRequest(
                    "",
                    VALID_TOP_LEVEL_POST_FILENAME_JPG,
                    VALID_TOP_LEVEL_POST_CONTENT_URL,
                    ""
                )
            }
        };
    }

    @Test(dataProvider = "provideValidCreateTopLevelPostRequests")
    public void testValidCreateTopLevelPostRequests(final CreateTopLevelPostRequest createTopLevelPostRequest) {
        testValidCreatePostRequests(createTopLevelPostRequest);
    }

    @Test(dataProvider = "provideInvalidCreateTopLevelPostRequests")
    public void testInvalidCreateTopLevelPostRequests(final CreateTopLevelPostRequest createTopLevelPostRequest) {
        testInvalidCreatePostRequests(createTopLevelPostRequest);
    }
}
