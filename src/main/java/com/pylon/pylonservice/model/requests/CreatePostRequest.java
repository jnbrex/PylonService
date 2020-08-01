package com.pylon.pylonservice.model.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePostRequest implements Serializable {
    private static final long serialVersionUID = 0L;

    // Post image ids are UUIDs.
    private static final Pattern POST_IMAGE_ID_REGEX_PATTERN =
        Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

    String postTitle;
    String postImageId;
    String postContentUrl;
    String postBody;

    public boolean isValidTopLevelPost() {
        return isPostTitleValid()
            && isPostImageIdValid()
            && isPostContentUrlValid()
            && isPostBodyValid();
    }

    public boolean isValidCommentPost() {
        return postTitle == null
            && postImageId == null
            && postContentUrl == null
            && isPostBodyValid();
    }

    private boolean isPostTitleValid() {
        return postTitle != null && postTitle.length() < 451;
    }

    private boolean isPostImageIdValid() {
        return POST_IMAGE_ID_REGEX_PATTERN.matcher(postImageId).matches();
    }

    private boolean isPostContentUrlValid() {
        return postContentUrl != null && postContentUrl.length() < 10001;
    }

    private boolean isPostBodyValid() {
        return postBody != null && postBody.length() < 80001;
    }
}
