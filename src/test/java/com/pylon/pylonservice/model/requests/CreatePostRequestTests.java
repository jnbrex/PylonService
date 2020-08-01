package com.pylon.pylonservice.model.requests;

import org.assertj.core.api.Assertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CreatePostRequestTests {
    private static final String VALID_TOP_LEVEL_POST_TITLE = "This is a POST TITLE!";
    private static final String VALID_TOP_LEVEL_POST_IMAGE_ID = "5237af6c-6cf7-46ee-8537-f0b1b90d870a";
    private static final String VALID_TOP_LEVEL_POST_CONTENT_URL = "https://pylon.gg";
    private static final String VALID_TOP_LEVEL_POST_BODY = "Hi this is an awesome post body!";

    private static final String VALID_COMMENT_POST_TITLE = null;
    private static final String VALID_COMMENT_POST_IMAGE_ID = null;
    private static final String VALID_COMMENT_POST_CONTENT_URL = null;
    private static final String VALID_COMMENT_POST_BODY = "Hi this is an awesome post body!";

    private static final String INVALID_FOUR_HUNDRED_FIFTY_ONE_CHARACTER_POST_TITLE = "a".repeat(451);
    private static final String INVALID_NON_UUID_POST_IMAGE_ID = "This is not a uuid";
    private static final String INVALID_TEN_THOUSAND_ONE_CHARACTER_POST_CONTENT_URL = "a".repeat(10001);
    private static final String INVALID_EIGHTY_THOUSAND_ONE_CHARACTER_POST_BODY = "a".repeat(80001);

    @DataProvider
    private Object[][] provideValidCreateTopLevelPostRequests() {
        return new Object[][] {
            {
                new CreatePostRequest(
                    VALID_TOP_LEVEL_POST_TITLE,
                    VALID_TOP_LEVEL_POST_IMAGE_ID,
                    VALID_TOP_LEVEL_POST_CONTENT_URL,
                    VALID_TOP_LEVEL_POST_BODY
                )
            }
        };
    }

    @DataProvider
    private Object[][] provideInvalidCreateTopLevelPostRequests() {
        return new Object[][] {
            {
                new CreatePostRequest(
                    INVALID_FOUR_HUNDRED_FIFTY_ONE_CHARACTER_POST_TITLE,
                    VALID_TOP_LEVEL_POST_IMAGE_ID,
                    VALID_TOP_LEVEL_POST_CONTENT_URL,
                    VALID_TOP_LEVEL_POST_BODY
                )
            },
            {
                new CreatePostRequest(
                    VALID_TOP_LEVEL_POST_TITLE,
                    INVALID_NON_UUID_POST_IMAGE_ID,
                    VALID_TOP_LEVEL_POST_CONTENT_URL,
                    VALID_TOP_LEVEL_POST_BODY
                )
            },
            {
                new CreatePostRequest(
                    VALID_TOP_LEVEL_POST_TITLE,
                    VALID_TOP_LEVEL_POST_IMAGE_ID,
                    INVALID_TEN_THOUSAND_ONE_CHARACTER_POST_CONTENT_URL,
                    VALID_TOP_LEVEL_POST_BODY
                )
            },
            {
                new CreatePostRequest(
                    VALID_TOP_LEVEL_POST_TITLE,
                    VALID_TOP_LEVEL_POST_IMAGE_ID,
                    VALID_TOP_LEVEL_POST_CONTENT_URL,
                    INVALID_EIGHTY_THOUSAND_ONE_CHARACTER_POST_BODY
                )
            }
        };
    }

    @DataProvider
    private Object[][] provideValidCreateCommentPostRequests() {
        return new Object[][] {
            {
                new CreatePostRequest(
                    VALID_COMMENT_POST_TITLE,
                    VALID_COMMENT_POST_IMAGE_ID,
                    VALID_COMMENT_POST_CONTENT_URL,
                    VALID_COMMENT_POST_BODY
                )
            }
        };
    }

    @DataProvider
    private Object[][] provideInvalidCreateCommentPostRequests() {
        return new Object[][] {
            {
                new CreatePostRequest(
                    "this is non-null",
                    VALID_COMMENT_POST_IMAGE_ID,
                    VALID_COMMENT_POST_CONTENT_URL,
                    VALID_COMMENT_POST_BODY
                )
            },
            {
                new CreatePostRequest(
                    VALID_COMMENT_POST_TITLE,
                    "this is non-null",
                    VALID_COMMENT_POST_CONTENT_URL,
                    VALID_COMMENT_POST_BODY
                )
            },
            {
                new CreatePostRequest(
                    VALID_COMMENT_POST_TITLE,
                    VALID_COMMENT_POST_IMAGE_ID,
                    "this is non-null",
                    VALID_COMMENT_POST_BODY
                )
            },
            {
                new CreatePostRequest(
                    VALID_COMMENT_POST_TITLE,
                    VALID_COMMENT_POST_IMAGE_ID,
                    VALID_COMMENT_POST_CONTENT_URL,
                    INVALID_EIGHTY_THOUSAND_ONE_CHARACTER_POST_BODY
                )
            }
        };
    }

    @Test(dataProvider = "provideValidCreateTopLevelPostRequests")
    public void testValidCreateTopLevelPostRequests(final CreatePostRequest createPostRequest) {
        Assertions.assertThat(createPostRequest.isValidTopLevelPost()).isTrue();
    }

    @Test(dataProvider = "provideInvalidCreateTopLevelPostRequests")
    public void testInvalidCreateTopLevelPostRequests(final CreatePostRequest createPostRequest) {
        Assertions.assertThat(createPostRequest.isValidTopLevelPost()).isFalse();
    }

    @Test(dataProvider = "provideValidCreateCommentPostRequests")
    public void testValidCreateCommentPostRequests(final CreatePostRequest createPostRequest) {
        Assertions.assertThat(createPostRequest.isValidCommentPost()).isTrue();
    }

    @Test(dataProvider = "provideInvalidCreateCommentPostRequests")
    public void testInvalidCreateCommentPostRequests(final CreatePostRequest createPostRequest) {
        Assertions.assertThat(createPostRequest.isValidCommentPost()).isFalse();
    }
}
