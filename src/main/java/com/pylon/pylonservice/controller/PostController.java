package com.pylon.pylonservice.controller;

import com.pylon.pylonservice.model.requests.CreatePostRequest;
import com.pylon.pylonservice.model.responses.CreatePostResponse;
import com.pylon.pylonservice.util.JwtTokenUtil;
import com.pylon.pylonservice.util.MetricsUtil;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.Tree;
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

import java.util.Date;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.in;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.valueMap;
import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.single;

@RestController
public class PostController {
    private static final String GET_POST_METRIC_NAME = "GetPost";
    private static final String POST_SHARD_POST_METRIC_NAME = "PostShardPost";
    private static final String POST_PROFILE_POST_METRIC_NAME = "PostProfilePost";
    private static final String POST_COMMENT_POST_METRIC_NAME = "PostCommentPost";

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

    /**
     * Call to retrieve a Post and all comments on it.
     *
     * @param postId A String containing the postId of the Post to return.
     *
     * @return HTTP 200 OK - If the User's public profile data was retrieved successfully.
     *         HTTP 404 Not Found - If the User doesn't exist.
     */
    @GetMapping(value = "/post/{postId}")
    public ResponseEntity<?> getPost(@PathVariable final String postId) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_POST_METRIC_NAME);

        final Tree tree;
        try {
                tree = rG.V().has("post", "postId", postId) // post vertex of post with postId: {postId}
                    .emit()
                    .repeat(in("commentOn"))
                    .tree()
                    .by(valueMap().by(unfold()))
                    .next();
        } catch (final NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(tree);

        metricsUtil.addSuccessMetric(GET_POST_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_POST_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to create a Post in a Shard.
     *
     * @param authorizationHeader A request header with key "Authorization" and body including a jwt like "Bearer {jwt}"
     * @param name The name of a Shard
     * @param createPostRequest A JSON body containing the Post data for the post to create like
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
    @PostMapping(value = "/post/shard/{name}")
    public ResponseEntity<?> shardPost(@RequestHeader(value = "Authorization") final String authorizationHeader,
                                       @PathVariable final String name,
                                       @RequestBody final CreatePostRequest createPostRequest) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(POST_SHARD_POST_METRIC_NAME);

        final String jwt = JwtTokenUtil.removeBearerFromAuthorizationHeader(authorizationHeader);
        final String username = jwtTokenUtil.getUsernameFromToken(jwt);

        final String postId = UUID.randomUUID().toString();

        try {
            createPostWithCommonProperties(createPostRequest, postId, username)
                .V().has("shard", "name", name).as("shard")
                .addE("in").from("post").to("shard")
                .iterate();
        } catch (final NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(
            CreatePostResponse.builder()
                .postId(postId)
                .build(),
            HttpStatus.CREATED
        );

        metricsUtil.addSuccessMetric(POST_SHARD_POST_METRIC_NAME);
        metricsUtil.addLatencyMetric(POST_SHARD_POST_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to create a Post in the authenticated User's public profile.
     *
     * @param authorizationHeader A request header with key "Authorization" and body including a jwt like "Bearer {jwt}"
     * @param createPostRequest A JSON body containing the Post data for the post to create like
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
    public ResponseEntity<?> profilePost(@RequestHeader(value = "Authorization") final String authorizationHeader,
                                         @RequestBody final CreatePostRequest createPostRequest) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(POST_PROFILE_POST_METRIC_NAME);

        final String jwt = JwtTokenUtil.removeBearerFromAuthorizationHeader(authorizationHeader);
        final String username = jwtTokenUtil.getUsernameFromToken(jwt);

        final String postId = UUID.randomUUID().toString();

        createPostWithCommonProperties(createPostRequest, postId, username)
            .V().has("user", "username", username).out("has").as("profile")
            .addE("in").from("post").to("profile")
            .iterate();

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(
            CreatePostResponse.builder()
                .postId(postId)
                .build(),
            HttpStatus.CREATED
        );

        metricsUtil.addSuccessMetric(POST_PROFILE_POST_METRIC_NAME);
        metricsUtil.addLatencyMetric(POST_PROFILE_POST_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to create a Post as a comment on another Post.
     *
     * @param authorizationHeader A request header with key "Authorization" and body including a jwt like "Bearer {jwt}"
     * @param parentPostId A postId of the parent Post
     * @param createPostRequest A JSON body containing the Post data for the post to create like
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
    public ResponseEntity<?> commentPost(@RequestHeader(value = "Authorization") final String authorizationHeader,
                                         @PathVariable final String parentPostId,
                                         @RequestBody final CreatePostRequest createPostRequest) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(POST_COMMENT_POST_METRIC_NAME);

        final String jwt = JwtTokenUtil.removeBearerFromAuthorizationHeader(authorizationHeader);
        final String username = jwtTokenUtil.getUsernameFromToken(jwt);

        final String postId = UUID.randomUUID().toString();

        try {
            createPostWithCommonProperties(createPostRequest, postId, username)
                .V().has("post", "postId", parentPostId).as("parentPost")
                .addE("commentOn").from("post").to("parentPost")
                .iterate();
        } catch (final NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(
            CreatePostResponse.builder()
                .postId(postId)
                .build(),
            HttpStatus.CREATED
        );

        metricsUtil.addSuccessMetric(POST_COMMENT_POST_METRIC_NAME);
        metricsUtil.addLatencyMetric(POST_COMMENT_POST_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    private GraphTraversal createPostWithCommonProperties(final CreatePostRequest createPostRequest,
                                                          final String postId,
                                                          final String username) {
        GraphTraversal graphTraversal = wG
            .addV("post").as("post")
            .property(single, "postId", postId);

        final String title = createPostRequest.getTitle();
        if (title != null) {
            graphTraversal = graphTraversal.property(single, "title", title);
        }
        final String imageId = createPostRequest.getImageId();
        if (imageId != null) {
            graphTraversal = graphTraversal.property(single, "imageId", imageId);
        }
        final String contentUrl = createPostRequest.getContentUrl();
        if (contentUrl != null) {
            graphTraversal = graphTraversal.property(single, "contentUrl", contentUrl);
        }
        final String body = createPostRequest.getBody();
        if (body != null) {
            graphTraversal = graphTraversal.property(single, "body", body);
        }

        graphTraversal = graphTraversal
            .property(single, "createdAt", new Date())
            .V().has("user", "username", username).as("user")
            .addE("submitted").from("user").to("post")
            .addE("liked").from("user").to("post");

        return graphTraversal;
    }
}
