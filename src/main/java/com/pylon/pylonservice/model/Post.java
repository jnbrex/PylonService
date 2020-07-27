package com.pylon.pylonservice.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static com.pylon.pylonservice.constants.GraphConstants.COMMON_CREATED_AT_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_BODY_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_CONTENT_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_ID_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_IMAGE_ID_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_TITLE_PROPERTY;

@Data
public class Post implements Serializable {
    private static final long serialVersionUID = 0L;

    public static final String PROPERTIES = "properties";
    public static final String NUM_UPVOTES = "numUpvotes";
    public static final String SUBMITTER_USERNAME = "submitterUsername";
    public static final String POSTED_IN_SHARD = "postedInShard";
    public static final String POSTED_IN_USER = "postedInUser";

    private static final double TWO_HOURS = 2.0;
    private static final double DECAY_CONSTANT = 1.8;

    // Properties of post vertex
    final String postId;
    final String postTitle;
    final String postImageId;
    final String postContentUrl;
    final String postBody;
    final Date createdAt;

    // Derived from edges
    long postUpvotes;
    String postSubmitter;
    String postPostedInUser;
    String postPostedInShard;

    public Post(final Object graphPost) {
        final Map<String, Object> graphPostMap = (Map<String, Object>) graphPost;

        this.postUpvotes = (long) graphPostMap.get(NUM_UPVOTES);
        this.postSubmitter = (String) graphPostMap.get(SUBMITTER_USERNAME);

        final Collection<String> postedInShardNames = (Collection<String>) graphPostMap.get(POSTED_IN_SHARD);
        final Collection<String> postedInUserUsernames = (Collection<String>) graphPostMap.get(POSTED_IN_USER);

        this.postPostedInShard = postedInShardNames.size() > 0 ? postedInShardNames.iterator().next() : null;
        this.postPostedInUser = postedInUserUsernames.size() > 0 ? postedInUserUsernames.iterator().next() : null;

        final Map<Object, Object> postProperties = (Map<Object, Object>) graphPostMap.get(PROPERTIES);
        this.postId = (String) postProperties.get(POST_ID_PROPERTY);
        this.postTitle = (String) postProperties.get(POST_TITLE_PROPERTY);
        this.postImageId = (String) postProperties.get(POST_IMAGE_ID_PROPERTY);
        this.postContentUrl = (String) postProperties.get(POST_CONTENT_URL_PROPERTY);
        this.postBody = (String) postProperties.get(POST_BODY_PROPERTY);
        this.createdAt = (Date) postProperties.get(COMMON_CREATED_AT_PROPERTY);
    }

    public double getPopularity(final Date now) {
        return (this.postUpvotes + 1.0) /
            Math.pow(timeSincePostedInHours(now) + TWO_HOURS, DECAY_CONSTANT);
    }

    private double timeSincePostedInHours(final Date now) {
        return (now.getTime() - this.createdAt.getTime()) / (1000.0 * 60.0 * 60.0);
    }
}
