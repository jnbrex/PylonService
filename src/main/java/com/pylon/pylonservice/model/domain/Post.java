package com.pylon.pylonservice.model.domain;

import lombok.Data;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static com.pylon.pylonservice.constants.GraphConstants.COMMON_CREATED_AT_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_BODY_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_COMMENT_ON_POST_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.POST_CONTENT_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_ID_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_FILENAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_POSTED_IN_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.POST_POSTED_IN_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.POST_TITLE_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_NAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_SUBMITTED_POST_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_UPVOTED_POST_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_USERNAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERTEX_LABEL;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.in;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.project;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.repeat;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.valueMap;

@Data
public class Post implements Serializable {
    private static final long serialVersionUID = 0L;

    private static final String PROPERTIES = "properties";
    private static final String NUM_LIKES = "numLikes";
    private static final String NUM_COMMENTS = "numComments";
    private static final String SUBMITTER_USERNAME = "submitterUsername";
    private static final String POSTED_IN_SHARD = "postedInShard";
    private static final String POSTED_IN_USER = "postedInUser";
    private static final String POST_LIKED_BY_USER = "postLikedByUser";
    private static final String COMMENT_ON_POST = "commentOnPost";

    private static final double TWO_HOURS = 2.0;
    private static final double DECAY_CONSTANT = 1.8;

    // Properties of post vertex
    final String postId;
    final String postTitle;
    final String postFilename;
    final String postContentUrl;
    final String postBody;
    final Date createdAt;

    // Derived from edges
    long numLikes;
    long numComments;
    String postSubmitter;
    boolean postLikedByUser;
    String postPostedInUser;
    String postPostedInShard;
    String commentOnPost;

    public Post(final Map<String, Object> graphPostMap) {
        this.numLikes = (long) graphPostMap.get(NUM_LIKES);
        this.numComments = (long) graphPostMap.get(NUM_COMMENTS);
        this.postSubmitter = (String) graphPostMap.get(SUBMITTER_USERNAME);
        this.postLikedByUser = (long) graphPostMap.get(POST_LIKED_BY_USER) > 0;

        final Collection<String> postedInShardNames = (Collection<String>) graphPostMap.get(POSTED_IN_SHARD);
        final Collection<String> postedInUserUsernames = (Collection<String>) graphPostMap.get(POSTED_IN_USER);
        final Collection<String> commentOnPosts = (Collection<String>) graphPostMap.get(COMMENT_ON_POST);

        this.postPostedInShard = postedInShardNames.size() > 0 ? postedInShardNames.iterator().next() : null;
        this.postPostedInUser = postedInUserUsernames.size() > 0 ? postedInUserUsernames.iterator().next() : null;
        this.commentOnPost = commentOnPosts.size() > 0 ? commentOnPosts.iterator().next() : null;

        final Map<String, Object> postProperties = (Map<String, Object>) graphPostMap.get(PROPERTIES);
        this.postId = (String) postProperties.get(POST_ID_PROPERTY);
        this.postTitle = (String) postProperties.get(POST_TITLE_PROPERTY);
        this.postFilename = (String) postProperties.get(POST_FILENAME_PROPERTY);
        this.postContentUrl = (String) postProperties.get(POST_CONTENT_URL_PROPERTY);
        this.postBody = (String) postProperties.get(POST_BODY_PROPERTY);
        this.createdAt = (Date) postProperties.get(COMMON_CREATED_AT_PROPERTY);
    }

    public static GraphTraversal<Object, Map<String, Object>> projectToPost(final String username) {
        return project(PROPERTIES, NUM_LIKES, NUM_COMMENTS, SUBMITTER_USERNAME, POST_LIKED_BY_USER, POSTED_IN_SHARD,
            POSTED_IN_USER, COMMENT_ON_POST)
            .by(valueMap().by(unfold()))
            .by(in(USER_UPVOTED_POST_EDGE_LABEL).count())
            .by(
                repeat(in(POST_COMMENT_ON_POST_EDGE_LABEL))
                .emit()
                .count()
            )
            .by(in(USER_SUBMITTED_POST_EDGE_LABEL).values(USER_USERNAME_PROPERTY))
            .by(in(USER_UPVOTED_POST_EDGE_LABEL).has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username).count())
            .by(out(POST_POSTED_IN_SHARD_EDGE_LABEL).values(SHARD_NAME_PROPERTY).fold())
            .by(out(POST_POSTED_IN_USER_EDGE_LABEL).values(USER_USERNAME_PROPERTY).fold())
            .by(out(POST_COMMENT_ON_POST_EDGE_LABEL).values(POST_ID_PROPERTY).fold());
    }

    public double getPopularity(final Date now) {
        return (this.numLikes + 1.0) /
            Math.pow(timeSincePostedInHours(now) + TWO_HOURS, DECAY_CONSTANT);
    }

    private double timeSincePostedInHours(final Date now) {
        return (now.getTime() - this.createdAt.getTime()) / (1000.0 * 60.0 * 60.0);
    }
}
