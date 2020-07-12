package com.pylon.pylonservice.model.responses;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

@Builder
@Value
public class CreatePostResponse implements Serializable {
    private static final long serialVersionUID = 0L;

    String postId;
}
