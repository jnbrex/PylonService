package com.pylon.pylonservice.model.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class GetPostsRequest implements Serializable, Request {
    private static final long serialVersionUID = 0L;

    int firstPostToReturn;
    int numPostsToReturn;

    public boolean isValid() {
        return firstPostToReturn >= 0 && numPostsToReturn > 0;
    }
}
