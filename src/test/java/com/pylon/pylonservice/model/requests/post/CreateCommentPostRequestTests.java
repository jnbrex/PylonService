package com.pylon.pylonservice.model.requests.post;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CreateCommentPostRequestTests extends CreatePostRequestTests {
    @DataProvider
    private Object[][] provideValidCreateCommentPostRequests() {
        return new Object[][] {
            {
                new CreateCommentPostRequest(
                    VALID_POST_BODY
                )
            }
        };
    }

    @DataProvider
    private Object[][] provideInvalidCreateCommentPostRequests() {
        return new Object[][] {
            {
                new CreateCommentPostRequest(
                    INVALID_POST_BODY_TOO_LONG
                )
            }
        };
    }

    @Test(dataProvider = "provideValidCreateCommentPostRequests")
    public void testValidCreateCommentPostRequests(final CreateCommentPostRequest createCommentPostRequest) {
        testValidCreatePostRequests(createCommentPostRequest);
    }

    @Test(dataProvider = "provideInvalidCreateCommentPostRequests")
    public void testInvalidCreateCommentPostRequests(final CreateCommentPostRequest createCommentPostRequest) {
        testInvalidCreatePostRequests(createCommentPostRequest);
    }
}
