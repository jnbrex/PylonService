package com.pylon.pylonservice.controller;

import com.pylon.pylonservice.model.domain.Post;
import com.pylon.pylonservice.model.requests.post.CreateCommentPostRequest;
import com.pylon.pylonservice.model.requests.post.CreateTopLevelPostRequest;
import com.pylon.pylonservice.model.responses.CreatePostResponse;
import com.pylon.pylonservice.services.AccessTokenService;
import com.pylon.pylonservice.services.MetricsService;
import io.jsonwebtoken.ExpiredJwtException;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.Tree;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.shaded.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.pylon.pylonservice.constants.AuthenticationConstants.ACCESS_TOKEN_COOKIE_NAME;
import static com.pylon.pylonservice.constants.GraphConstants.COMMON_CREATED_AT_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.INVALID_USERNAME_VALUE;
import static com.pylon.pylonservice.constants.GraphConstants.POST_BODY_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_COMMENT_ON_POST_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.POST_CONTENT_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_ID_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_FILENAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_POSTED_IN_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.POST_POSTED_IN_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.POST_TITLE_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_VERTEX_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_NAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_VERTEX_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_SUBMITTED_POST_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_UPVOTED_POST_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_USERNAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERTEX_LABEL;
import static com.pylon.pylonservice.model.domain.Post.projectToPost;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.V;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.addE;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.addV;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.flatMap;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.in;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.inV;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.outE;
import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.single;

@RestController
public class PostController {
    private static final String GET_POST_METRIC_NAME = "GetPost";
    private static final String GET_POST_COMMENTS_METRIC_NAME = "GetPostComments";
    private static final String UPVOTE_POST_METRIC_NAME = "UpvotePost";
    private static final String REMOVE_UPVOTE_POST_METRIC_NAME = "RemoveUpvotePost";
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
    private AccessTokenService accessTokenService;
    @Autowired
    private MetricsService metricsService;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Call to retrieve a Post.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param postId A String containing the postId of the Post to return.
     *
     * @return HTTP 200 OK - If the Post was retrieved successfully. Body is an array of
     *                       {@link com.pylon.pylonservice.model.domain.Post Post}.
     *         HTTP 404 Not Found - If the Post doesn't exist.
     */
    @GetMapping(value = "/post/{postId}")
    public ResponseEntity<?> getPost(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken,
        @PathVariable final String postId) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_POST_METRIC_NAME);

        final String callingUsernameLowercase;
        try {
            callingUsernameLowercase = accessTokenService.getUsernameFromAccessTokenOrDefaultIfNull(
                accessToken, INVALID_USERNAME_VALUE
            );
        } catch (final ExpiredJwtException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        final Post post;
        try {
            post = rG
                .V().has(POST_VERTEX_LABEL, POST_ID_PROPERTY, postId)
                .flatMap(Post.projectToPost(callingUsernameLowercase))
                .toList()
                .stream()
                .map(Post::new)
                .collect(toSingleton());
        } catch (final NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(post);

        metricsService.addSuccessMetric(GET_POST_METRIC_NAME);
        metricsService.addLatencyMetric(GET_POST_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve all comments on a Post.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param postId A String containing the postId of the Post for which the comments should be returned.
     *
     * @return HTTP 200 OK - If the Post's comments were retrieved successfully.
     *         HTTP 404 Not Found - If the Post doesn't exist.
     */
    @GetMapping(value = "/post/{postId}/comments")
    public ResponseEntity<?> getComments(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken,
        @PathVariable final String postId) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_POST_COMMENTS_METRIC_NAME);

        final String callingUsernameLowercase;
        try {
            callingUsernameLowercase = accessTokenService.getUsernameFromAccessTokenOrDefaultIfNull(
                accessToken, INVALID_USERNAME_VALUE
            );
        } catch (final ExpiredJwtException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        final Tree<Map<String, Object>> postAndComments = rG.V().has(POST_VERTEX_LABEL, POST_ID_PROPERTY, postId)
            .emit()
            .repeat(in(POST_COMMENT_ON_POST_EDGE_LABEL))
            .tree()
            .by(flatMap(projectToPost(callingUsernameLowercase)))
            .next();

        if (postAndComments.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Post root = convertTreeToPostWithNestedComments(postAndComments);

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(root.getComments());

        metricsService.addSuccessMetric(GET_POST_COMMENTS_METRIC_NAME);
        metricsService.addLatencyMetric(GET_POST_COMMENTS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call for the calling User to upvote a Post.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param postId A String containing the postId of the Post to upvote.
     *
     * @return HTTP 200 OK - If the Post was upvoted successfully or was already upvoted by the calling User.
     *         HTTP 404 Not Found - If the Post doesn't exist.
     */
    @PutMapping(value = "/post/upvote/{postId}")
    public ResponseEntity<?> upvotePost(@CookieValue(name = ACCESS_TOKEN_COOKIE_NAME) final String accessToken,
                                        @PathVariable final String postId) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(UPVOTE_POST_METRIC_NAME);

        final String username = accessTokenService.getUsernameFromAccessToken(accessToken);

        if (!rG.V().has(POST_VERTEX_LABEL, POST_ID_PROPERTY, postId).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        wG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username)
            .coalesce(
                outE(USER_UPVOTED_POST_EDGE_LABEL).filter(
                    inV().has(POST_VERTEX_LABEL, POST_ID_PROPERTY, postId)
                ),
                addE(USER_UPVOTED_POST_EDGE_LABEL).to(
                    V().has(POST_VERTEX_LABEL, POST_ID_PROPERTY, postId)
                )
            )
            .iterate();

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(HttpStatus.OK);

        metricsService.addSuccessMetric(UPVOTE_POST_METRIC_NAME);
        metricsService.addLatencyMetric(UPVOTE_POST_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call for the calling User to remove their upvote on a Post.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param postId A String containing the postId of the Post to remove their upvote on.
     *
     * @return HTTP 200 OK - If the upvote on the Post was removed successfully or if the Post hadn't been upvoted by
     *                       the calling User.
     *         HTTP 404 Not Found - If the Post doesn't exist.
     */
    @PutMapping(value = "/post/removeUpvote/{postId}")
    public ResponseEntity<?> removeUpvoteOnPost(@CookieValue(name = ACCESS_TOKEN_COOKIE_NAME) final String accessToken,
                                                @PathVariable final String postId) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(REMOVE_UPVOTE_POST_METRIC_NAME);

        final String username = accessTokenService.getUsernameFromAccessToken(accessToken);

        if (!rG.V().has(POST_VERTEX_LABEL, POST_ID_PROPERTY, postId).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        wG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username)
            .outE(USER_UPVOTED_POST_EDGE_LABEL).where(
                inV().has(POST_VERTEX_LABEL, POST_ID_PROPERTY, postId)
            )
            .drop()
            .iterate();

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(HttpStatus.OK);

        metricsService.addSuccessMetric(REMOVE_UPVOTE_POST_METRIC_NAME);
        metricsService.addLatencyMetric(REMOVE_UPVOTE_POST_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to create a Post in a Shard.
     * @see CreateTopLevelPostRequest#isValid() for validation rules.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param shardName The name of a Shard
     * @param createTopLevelPostRequest A {@link CreateTopLevelPostRequest}
     *
     * @return HTTP 201 Created - If the Post was created successfully.
     *         HTTP 401 Unauthorized - If the User isn't authenticated.
     *         HTTP 404 Not Found - If the Shard with shardName={shardName} doesn't exist.
     */
    @PostMapping(value = "/post/shard/{shardName}")
    public ResponseEntity<?> createShardPost(@CookieValue(name = ACCESS_TOKEN_COOKIE_NAME) final String accessToken,
                                             @PathVariable final String shardName,
                                             @RequestBody final CreateTopLevelPostRequest createTopLevelPostRequest) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(CREATE_SHARD_POST_METRIC_NAME);
        final String shardNameLowercase = shardName.toLowerCase();

        if (!createTopLevelPostRequest.isValid()) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        final String username = accessTokenService.getUsernameFromAccessToken(accessToken);

        final String postId = UUID.randomUUID().toString();

        final Optional<Edge> result = wG
            .V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameLowercase).as("shard")
            .flatMap(addTopLevelPost(createTopLevelPostRequest, postId)).as("post")
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

        metricsService.addSuccessMetric(CREATE_SHARD_POST_METRIC_NAME);
        metricsService.addLatencyMetric(CREATE_SHARD_POST_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to create a Post in the calling User's public profile.
     * @see CreateTopLevelPostRequest#isValid() for validation rules.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param createTopLevelPostRequest A {@link CreateTopLevelPostRequest}
     *
     * @return HTTP 201 Created - If the Post was created successfully.
     *         HTTP 401 Unauthorized - If the User isn't authenticated.
     */
    @PostMapping(value = "/post/profile")
    public ResponseEntity<?> createProfilePost(@CookieValue(name = ACCESS_TOKEN_COOKIE_NAME) final String accessToken,
                                               @RequestBody final CreateTopLevelPostRequest createTopLevelPostRequest) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(CREATE_PROFILE_POST_METRIC_NAME);

        if (!createTopLevelPostRequest.isValid()) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        final String username = accessTokenService.getUsernameFromAccessToken(accessToken);

        final String postId = UUID.randomUUID().toString();

        final Optional<Edge> result = wG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username).as("user")
            .flatMap(addTopLevelPost(createTopLevelPostRequest, postId)).as("post")
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

        metricsService.addSuccessMetric(CREATE_PROFILE_POST_METRIC_NAME);
        metricsService.addLatencyMetric(CREATE_PROFILE_POST_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to create a Post as a comment on another Post.
     * @see CreateCommentPostRequest#isValid() for validation rules.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param parentPostId The postId of the parent Post.
     * @param createCommentPostRequest A {@link CreateCommentPostRequest}
     *
     * @return HTTP 201 Created - If the Post was created successfully.
     *         HTTP 401 Unauthorized - If the User isn't authenticated.
     *         HTTP 404 Not Found - If the Post with postId={parentPostId} doesn't exist.
     */
    @PostMapping(value = "/post/comment/{parentPostId}")
    public ResponseEntity<?> createCommentPost(@CookieValue(name = ACCESS_TOKEN_COOKIE_NAME) final String accessToken,
                                               @PathVariable final String parentPostId,
                                               @RequestBody final CreateCommentPostRequest createCommentPostRequest) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(CREATE_COMMENT_POST_METRIC_NAME);

        if (!createCommentPostRequest.isValid()) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        final String username = accessTokenService.getUsernameFromAccessToken(accessToken);

        final String postId = UUID.randomUUID().toString();
        final Optional<Edge> result = wG
            .V().has(POST_VERTEX_LABEL, POST_ID_PROPERTY, parentPostId).as("parentPost")
            .flatMap(addCommentPost(createCommentPostRequest, postId)).as("post")
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

        metricsService.addSuccessMetric(CREATE_COMMENT_POST_METRIC_NAME);
        metricsService.addLatencyMetric(CREATE_COMMENT_POST_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    private GraphTraversal<Object, Vertex> addTopLevelPost(final CreateTopLevelPostRequest createTopLevelPostRequest,
                                                           final String postId) {
        return addV(POST_VERTEX_LABEL)
            .property(single, POST_ID_PROPERTY, postId)
            .property(single, POST_TITLE_PROPERTY, createTopLevelPostRequest.getPostTitle())
            .property(single, POST_FILENAME_PROPERTY, createTopLevelPostRequest.getPostFilename())
            .property(single, POST_CONTENT_URL_PROPERTY, createTopLevelPostRequest.getPostContentUrl())
            .property(single, POST_BODY_PROPERTY, createTopLevelPostRequest.getPostBody())
            .property(single, COMMON_CREATED_AT_PROPERTY, new Date());
    }

    private GraphTraversal<Object, Vertex> addCommentPost(final CreateCommentPostRequest createCommentPostRequest,
                                                           final String postId) {
        return addV(POST_VERTEX_LABEL)
            .property(single, POST_ID_PROPERTY, postId)
            .property(single, POST_BODY_PROPERTY, createCommentPostRequest.getPostBody())
            .property(single, COMMON_CREATED_AT_PROPERTY, new Date());
    }

    // Invoking traversals MUST contain a vertex with label "post"
    private GraphTraversal<Object, Edge> relateUserToPost(final String username) {
        return V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username).as("user")
            .addE(USER_SUBMITTED_POST_EDGE_LABEL).from("user").to("post");
    }

    private static <T> Collector<T, ?, T> toSingleton() {
        return Collectors.collectingAndThen(
            Collectors.toList(),
            list -> {
                if (list.size() != 1) {
                    throw new IllegalStateException();
                }
                return list.get(0);
            }
        );
    }



    private static Post convertTreeToPostWithNestedComments(final Tree<Map<String, Object>> t) {
        final Post post = new Post(t.keySet().iterator().next());
        final List<Tree<Map<String, Object>>> comments = t.values().iterator().next().splitParents();
        comments.forEach(comment -> post.addComment(convertTreeToPostWithNestedComments(comment)));
        return post;
    }
}
