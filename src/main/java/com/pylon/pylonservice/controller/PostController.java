package com.pylon.pylonservice.controller;

import com.pylon.pylonservice.model.requests.CreatePostRequest;
import com.pylon.pylonservice.model.responses.CreatePostResponse;
import com.pylon.pylonservice.util.JwtTokenUtil;
import com.pylon.pylonservice.util.MetricsUtil;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.Tree;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.shaded.jackson.core.JsonProcessingException;
import org.apache.tinkerpop.shaded.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.io.UncheckedIOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static com.pylon.pylonservice.constants.GraphConstants.COMMON_CREATED_AT_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_BODY_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_COMMENT_ON_POST_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.POST_CONTENT_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_ID_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_IMAGE_ID_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_POSTED_IN_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.POST_POSTED_IN_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.POST_TITLE_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_VERTEX_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_INHERITS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_INHERITS_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_NAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_VERTEX_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_SUBMITTED_POST_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_UPVOTED_POST_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_USERNAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERTEX_LABEL;
import static org.apache.tinkerpop.gremlin.process.traversal.Order.desc;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.V;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.addV;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.in;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.valueMap;
import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.single;

@RestController
public class PostController {
    private static final String GET_POST_METRIC_NAME = "GetPost";
    private static final String GET_POST_COMMENTS_METRIC_NAME = "GetPostComments";
    private static final String GET_PROFILE_POSTS_METRIC_NAME = "GetProfilePosts";
    private static final String GET_SHARD_POSTS_METRIC_NAME = "GetShardPosts";
    private static final String CREATE_SHARD_POST_METRIC_NAME = "CreateShardPost";
    private static final String CREATE_PROFILE_POST_METRIC_NAME = "CreateProfilePost";
    private static final String CREATE_COMMENT_POST_METRIC_NAME = "CreateCommentPost";

    @Qualifier("writer")
    @Autowired
    private GraphTraversalSource wG;
    @Qualifier("reader")
    @Autowired
    private GraphTraversalSource rG;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private MetricsUtil metricsUtil;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Call to retrieve a Post.
     *
     * @param postId A String containing the postId of the Post to return.
     *
     * @return HTTP 200 OK - If the Post was retrieved successfully.
     *         HTTP 404 Not Found - If the Post doesn't exist.
     */
    @GetMapping(value = "/post/{postId}")
    public ResponseEntity<?> getPost(@PathVariable final String postId) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_POST_METRIC_NAME);

        final Map<Object, Object> post;
        try {
            post = rG.V().has(POST_VERTEX_LABEL, POST_ID_PROPERTY, postId)
                .valueMap().by(unfold())
                .next();
        } catch (final NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(post);

        metricsUtil.addSuccessMetric(GET_POST_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_POST_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve all comments on a Post.
     *
     * @param postId A String containing the postId of the Post for which the comments should be returned.
     *
     * @return HTTP 200 OK - If the Post's comments were retrieved successfully.
     *         HTTP 404 Not Found - If the Post doesn't exist.
     */
    @GetMapping(value = "/post/{postId}/comments")
    public ResponseEntity<?> getComments(@PathVariable final String postId) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_POST_COMMENTS_METRIC_NAME);

        final Tree postAndComments = rG.V().has(POST_VERTEX_LABEL, POST_ID_PROPERTY, postId)
            .emit()
            .repeat(in(POST_COMMENT_ON_POST_EDGE_LABEL))
            .tree()
            .by(valueMap().by(unfold()))
            .next();

        if (postAndComments.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final ResponseEntity<?> responseEntity;
        try {
            responseEntity = ResponseEntity.ok().body(
                objectMapper.writeValueAsString(
                    postAndComments.getTreesAtDepth(2).get(0) // Tree with top-level comments at root
                )
            );
        } catch (final JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }

        metricsUtil.addSuccessMetric(GET_POST_COMMENTS_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_POST_COMMENTS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve all the post headers for a Profile.
     *
     * @param username A String containing the username of the User's Profile to return.
     *
     * @return HTTP 200 OK - If the Posts on the Profile were retrieved successfully.
     *         HTTP 404 Not Found - If the Profile doesn't exist.
     */
    @GetMapping(value = "/post/profile/{username}")
    public ResponseEntity<?> getProfilePosts(@PathVariable final String username) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_PROFILE_POSTS_METRIC_NAME);

        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final List<Map<Object, Object>> posts = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username) // Single user vertex
            .in(POST_POSTED_IN_USER_EDGE_LABEL) // All posts posted in the user's profile
            .order().by(COMMON_CREATED_AT_PROPERTY, desc)
            .valueMap().by(unfold())
            .toList();

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(posts);

        metricsUtil.addSuccessMetric(GET_PROFILE_POSTS_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_PROFILE_POSTS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve all the post headers for a Shard.
     *
     * @param shardName A String containing the shardName of the Shard to return.
     *
     * @return HTTP 200 OK - If the Posts on the Shard were retrieved successfully.
     *         HTTP 404 Not Found - If the Shard doesn't exist.
     */
    @GetMapping(value = "/post/shard/{shardName}")
    public ResponseEntity<?> getShardPosts(@PathVariable final String shardName) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_SHARD_POSTS_METRIC_NAME);

        if (!rG.V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardName).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final List<Map<Object, Object>> posts = rG
            .V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardName)
            .emit()
            .repeat(out(SHARD_INHERITS_USER_EDGE_LABEL, SHARD_INHERITS_SHARD_EDGE_LABEL))
            .in(POST_POSTED_IN_USER_EDGE_LABEL, POST_POSTED_IN_SHARD_EDGE_LABEL) // All posts posted in the user's profile
            .order().by(COMMON_CREATED_AT_PROPERTY, desc)
            .valueMap().by(unfold())
            .toList();

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(posts);

        metricsUtil.addSuccessMetric(GET_SHARD_POSTS_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_SHARD_POSTS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to create a Post in a Shard.
     *
     * @param authorizationHeader A request header with key "Authorization" and body including a jwt like "Bearer {jwt}"
     * @param shardName The name of a Shard
     * @param createPostRequest A JSON object containing the Post data for the post to create like
     *                             {
     *                                 "title": "exampleTitle",
     *                                 "imageId": "exampleImageId",
     *                                 "contentUrl": "exampleContentUrl",
     *                                 "body": "exampleBody
     *                             }
     *                             If a field is not included in the JSON object, it is not included.
     *
     * @return HTTP 201 Created - If the Post was created successfully.
     *         HTTP 401 Unauthorized - If the User isn't authenticated.
     *         HTTP 404 Not Found - If the Shard with name={name} doesn't exist.
     */
    @PostMapping(value = "/post/shard/{shardName}")
    public ResponseEntity<?> createShardPost(@RequestHeader(value = "Authorization") final String authorizationHeader,
                                       @PathVariable final String shardName,
                                       @RequestBody final CreatePostRequest createPostRequest) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(CREATE_SHARD_POST_METRIC_NAME);

        final String jwt = JwtTokenUtil.removeBearerFromAuthorizationHeader(authorizationHeader);
        final String username = jwtTokenUtil.getUsernameFromToken(jwt);

        final String postId = UUID.randomUUID().toString();

        final Optional<Edge> result = wG
            .V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardName).as("shard")
            .flatMap(addPost(createPostRequest, postId)).as("post")
            .addE(POST_POSTED_IN_SHARD_EDGE_LABEL).from("post").to("shard")
            .flatMap(relateUserToPost(username))
            .tryNext();

        if (result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(
            CreatePostResponse.builder()
                .postId(postId)
                .build(),
            HttpStatus.CREATED
        );

        metricsUtil.addSuccessMetric(CREATE_SHARD_POST_METRIC_NAME);
        metricsUtil.addLatencyMetric(CREATE_SHARD_POST_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to create a Post in the authenticated User's public profile.
     *
     * @param authorizationHeader A request header with key "Authorization" and body including a jwt like "Bearer {jwt}"
     * @param createPostRequest A JSON object containing the Post data for the post to create like
     *                             {
     *                                 "title": "exampleTitle",
     *                                 "imageId": "exampleImageId",
     *                                 "contentUrl": "exampleContentUrl",
     *                                 "body": "exampleBody
     *                             }
     *                             If a field is not included in the JSON object, it is not included.
     *
     * @return HTTP 201 Created - If the Post was created successfully.
     *         HTTP 401 Unauthorized - If the User isn't authenticated.
     */
    @PostMapping(value = "/post/profile")
    public ResponseEntity<?> createProfilePost(@RequestHeader(value = "Authorization") final String authorizationHeader,
                                         @RequestBody final CreatePostRequest createPostRequest) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(CREATE_PROFILE_POST_METRIC_NAME);

        final String jwt = JwtTokenUtil.removeBearerFromAuthorizationHeader(authorizationHeader);
        final String username = jwtTokenUtil.getUsernameFromToken(jwt);

        final String postId = UUID.randomUUID().toString();

        final Optional<Edge> result = wG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username).as("user")
            .flatMap(addPost(createPostRequest, postId)).as("post")
            .addE(POST_POSTED_IN_USER_EDGE_LABEL).from("post").to("user")
            .flatMap(relateUserToPost(username))
            .tryNext();

        if (result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(
            CreatePostResponse.builder()
                .postId(postId)
                .build(),
            HttpStatus.CREATED
        );

        metricsUtil.addSuccessMetric(CREATE_PROFILE_POST_METRIC_NAME);
        metricsUtil.addLatencyMetric(CREATE_PROFILE_POST_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to create a Post as a comment on another Post.
     *
     * @param authorizationHeader A request header with key "Authorization" and body including a jwt like "Bearer {jwt}"
     * @param parentPostId A postId of the parent Post
     * @param createPostRequest A JSON object containing the Post data for the post to create like
     *                             {
     *                                 "title": "exampleTitle",
     *                                 "imageId": "exampleImageId",
     *                                 "contentUrl": "exampleContentUrl",
     *                                 "body": "exampleBody
     *                             }
     *                             If a field is not included in the JSON object, it is not included.
     *
     * @return HTTP 201 Created - If the Post was created successfully.
     *         HTTP 401 Unauthorized - If the User isn't authenticated.
     *         HTTP 404 Not Found - If the Post with postId={parentPostId} doesn't exist.
     */
    @PostMapping(value = "/post/comment/{parentPostId}")
    public ResponseEntity<?> createCommentPost(@RequestHeader(value = "Authorization") final String authorizationHeader,
                                         @PathVariable final String parentPostId,
                                         @RequestBody final CreatePostRequest createPostRequest) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(CREATE_COMMENT_POST_METRIC_NAME);

        final String jwt = JwtTokenUtil.removeBearerFromAuthorizationHeader(authorizationHeader);
        final String username = jwtTokenUtil.getUsernameFromToken(jwt);
        final String postId = UUID.randomUUID().toString();

        final Optional<Edge> result = wG
            .V().has(POST_VERTEX_LABEL, POST_ID_PROPERTY, parentPostId).as("parentPost")
            .flatMap(addPost(createPostRequest, postId)).as("post")
            .addE(POST_COMMENT_ON_POST_EDGE_LABEL).from("post").to("parentPost")
            .flatMap(relateUserToPost(username))
            .tryNext();

        if (result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(
            CreatePostResponse.builder()
                .postId(postId)
                .build(),
            HttpStatus.CREATED
        );

        metricsUtil.addSuccessMetric(CREATE_COMMENT_POST_METRIC_NAME);
        metricsUtil.addLatencyMetric(CREATE_COMMENT_POST_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    private GraphTraversal<Object, Vertex> addPost(final CreatePostRequest createPostRequest,
                                                       final String postId) {
        GraphTraversal<Object, Vertex> g = addV(POST_VERTEX_LABEL)
            .property(single, POST_ID_PROPERTY, postId)
            .property(single, COMMON_CREATED_AT_PROPERTY, new Date());

        final String title = createPostRequest.getTitle();
        if (title != null) {
            g = g.property(single, POST_TITLE_PROPERTY, title);
        }
        final String imageId = createPostRequest.getImageId();
        if (imageId != null) {
            g = g.property(single, POST_IMAGE_ID_PROPERTY, imageId);
        }
        final String contentUrl = createPostRequest.getContentUrl();
        if (contentUrl != null) {
            g = g.property(single, POST_CONTENT_URL_PROPERTY, contentUrl);
        }
        final String body = createPostRequest.getBody();
        if (body != null) {
            g = g.property(single, POST_BODY_PROPERTY, body);
        }

        return g;
    }

    // Invoking traversals MUST contain a vertex with label "post"
    private GraphTraversal<Object, Edge> relateUserToPost(final String username) {
        return V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username).as("user")
            .addE(USER_SUBMITTED_POST_EDGE_LABEL).from("user").to("post")
            .addE(USER_UPVOTED_POST_EDGE_LABEL).from("user").to("post");
    }
}
