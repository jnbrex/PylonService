package com.pylon.pylonservice.model.requests.post;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper=true)
public class CreateTopLevelPostRequest extends CreatePostRequest {
    private static final long serialVersionUID = 0L;

    private static final int POST_TITLE_MAX_LENGTH = 450;
    private static final int POST_CONTENT_URL_MAX_LENGTH = 10000;
    private static final int QUICK_POST_BODY_MAX_LENGTH = 400;

    String postTitle;
    String postFilename;
    String postContentUrl;

    CreateTopLevelPostRequest(final String postTitle,
                              final String postFilename,
                              final String postContentUrl,
                              final String postBody) {
        super(postBody);
        this.postTitle = postTitle;
        this.postFilename = postFilename;
        this.postContentUrl = postContentUrl;
    }

    public String getPostBody() {
        return postBody;
    }

    public boolean isValid() {
        return
            (
                isPostTitleValid()
                    && isPostFilenameValid()
                    && isPostContentUrlValid()
                    && isPostBodyValid()
            )
                &&
            (
                !postTitle.isEmpty()
                || !postFilename.isEmpty()
                || !postBody.isEmpty()
            )
                &&
            (
                postBody.length() <= QUICK_POST_BODY_MAX_LENGTH || !postTitle.isEmpty()
            )
                &&
            (
                postContentUrl.isEmpty() || !postTitle.isEmpty() || !postBody.isEmpty()
            );
    }

    private boolean isPostTitleValid() {
        return postTitle != null && postTitle.length() <= POST_TITLE_MAX_LENGTH;
    }

    private boolean isPostFilenameValid() {
        return postFilename != null &&
            (postFilename.isEmpty() || FILENAME_REGEX_PATTERN.matcher(postFilename).matches());
    }

    private boolean isPostContentUrlValid() {
        return postContentUrl != null && postContentUrl.length() < POST_CONTENT_URL_MAX_LENGTH;
    }

    private boolean isPostBodyValid() {
        return postBody != null && postBody.length() <= FULL_POST_BODY_MAX_LENGTH;
    }
}
