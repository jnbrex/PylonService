package com.pylon.pylonservice.model.requests.post;

import com.pylon.pylonservice.model.requests.Request;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
abstract class CreatePostRequest implements Serializable, Request {
    private static final long serialVersionUID = 0L;

    static final int FULL_POST_BODY_MAX_LENGTH = 160000;

    String postBody;
}
