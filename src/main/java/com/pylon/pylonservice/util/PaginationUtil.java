package com.pylon.pylonservice.util;

import com.pylon.pylonservice.model.domain.Post;
import com.pylon.pylonservice.model.requests.GetPostsRequest;
import com.pylon.pylonservice.pojo.PageRange;

import java.util.ArrayList;
import java.util.List;

public final class PaginationUtil {
    private PaginationUtil() {}

    public static List<Post> paginatePosts(final List<Post> posts, final GetPostsRequest getPostsRequest) {
        if (getPostsRequest == null) {
            return posts;
        }

        if (getPostsRequest.getFirstPostToReturn() >= posts.size()) {
            return new ArrayList<>();
        }

        final int fromIndex = getPostsRequest.getFirstPostToReturn();
        final int toIndex = Integer.min(
            posts.size(),
            getPostsRequest.getFirstPostToReturn() + getPostsRequest.getNumPostsToReturn()
        );

        return posts.subList(fromIndex, toIndex);
    }

    public static PageRange getPageRange(final GetPostsRequest getPostsRequest) {
        final long rangeLow, rangeHigh;
        if (getPostsRequest != null) {
            rangeLow = getPostsRequest.getFirstPostToReturn();
            rangeHigh = rangeLow + getPostsRequest.getNumPostsToReturn();
        } else {
            rangeLow = 0;
            rangeHigh = -1;
        }

        return PageRange.builder()
            .low(rangeLow)
            .high(rangeHigh)
            .build();
    }
}
