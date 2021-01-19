package com.pylon.pylonservice.model.requests.post;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;

/**
 * {
 *     "postBody": "hi guys"
 * }
 */
@Value
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateCommentPostRequest extends CreatePostRequest {
    private static final long serialVersionUID = 0L;

    CreateCommentPostRequest(final String postBody) {
        super(postBody);
    }

    public String getPostBody() {
        return postBody;
    }

    public boolean isValid() {
        return postBody != null && postBody.length() > 0 && postBody.length() <= FULL_POST_BODY_MAX_LENGTH;
    }
}
