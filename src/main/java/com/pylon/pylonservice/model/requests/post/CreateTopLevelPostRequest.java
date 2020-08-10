package com.pylon.pylonservice.model.requests.post;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper=true)
public class CreateTopLevelPostRequest extends CreatePostRequest {
    private static final long serialVersionUID = 0L;

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
                || !postContentUrl.isEmpty()
                || !postBody.isEmpty()
            )
                &&
            (
                postBody.length() < 201 || !postTitle.isEmpty()
            );
    }

    private boolean isPostTitleValid() {
        return postTitle != null && postTitle.length() < 451;
    }

    private boolean isPostFilenameValid() {
        return postFilename != null &&
            (postFilename.isEmpty() || FILENAME_REGEX_PATTERN.matcher(postFilename).matches());
    }

    private boolean isPostContentUrlValid() {
        return postContentUrl != null && postContentUrl.length() < 10001;
    }

    private boolean isPostBodyValid() {
        return postBody != null && postBody.length() < 80001;
    }
}
