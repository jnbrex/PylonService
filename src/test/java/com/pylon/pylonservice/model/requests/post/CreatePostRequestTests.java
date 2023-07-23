package com.pylon.pylonservice.model.requests.post;

import org.assertj.core.api.Assertions;

abstract class CreatePostRequestTests {
    static final String VALID_POST_BODY = "Hi this is an awesome post body!";
    static final String INVALID_POST_BODY_TOO_LONG = "a".repeat(160001);

    void testValidCreatePostRequests(final CreatePostRequest createPostRequest) {
        Assertions.assertThat(createPostRequest.isValid()).isTrue();
    }

    void testInvalidCreatePostRequests(final CreatePostRequest createPostRequest) {
        Assertions.assertThat(createPostRequest.isValid()).isFalse();
    }
}
