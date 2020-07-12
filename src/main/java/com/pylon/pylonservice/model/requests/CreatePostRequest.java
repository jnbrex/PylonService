package com.pylon.pylonservice.model.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class CreatePostRequest implements Serializable {
    private static final long serialVersionUID = 0L;

    String title;
    String imageId;
    String contentUrl;
    String body;
}
