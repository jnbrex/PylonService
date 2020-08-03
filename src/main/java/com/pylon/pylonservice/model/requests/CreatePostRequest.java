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

    // Post image ids are UUIDs ending in .png, .jpg, or .gif
    private static final Pattern POST_FILENAME_REGEX_PATTERN =
        Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|gif|png)$");

    String postTitle;
    String postFilename;
    String postContentUrl;
    String postBody;

    public boolean isValidTopLevelPost() {
        return
            (
                isPostTitleValid()
                && isPostFilenameValid()
                && isPostContentUrlValid()
                && isPostBodyValid()
            )
            &&
            (
                postFilename != null
                || postContentUrl != null
                || postBody != null
            );
    }

    public boolean isValidCommentPost() {
        return postTitle == null
            && postFilename == null
            && postContentUrl == null
            && (postBody != null && (postBody.length() > 0 && postBody.length() < 80001));
    }

    private boolean isPostTitleValid() {
        return postTitle != null && postTitle.length() < 451;
    }

    private boolean isPostFilenameValid() {
        return postFilename == null || POST_FILENAME_REGEX_PATTERN.matcher(postFilename).matches();
    }

    private boolean isPostContentUrlValid() {
        return postContentUrl == null || postContentUrl.length() < 10001;
    }

    private boolean isPostBodyValid() {
        return postBody == null || postBody.length() < 80001;
    }
}
