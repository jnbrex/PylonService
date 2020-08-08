package com.pylon.pylonservice.controller;

import com.pylon.pylonservice.model.domain.Post;
import com.pylon.pylonservice.util.JwtTokenUtil;
import com.pylon.pylonservice.util.MetricsUtil;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.pylon.pylonservice.constants.GraphConstants.COMMON_CREATED_AT_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.INVALID_USERNAME_VALUE;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_INHERITS_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_NAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FOLLOWS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FOLLOWS_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_OWNS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_SUBMITTED_POST_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_UPVOTED_POST_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_USERNAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERTEX_LABEL;
import static com.pylon.pylonservice.model.domain.Post.projectToPost;
import static org.apache.tinkerpop.gremlin.process.traversal.Order.desc;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold;

@RestController
public class UserController {
    private static final String GET_USER_OWNED_SHARDS_METRIC_NAME = "GetUserOwnedShards";
    private static final String GET_USER_FOLLOWED_SHARDS_METRIC_NAME = "GetUserFollowedShards";
    private static final String GET_USER_FOLLOWED_USERS_METRIC_NAME = "GetUserFollowedUsers";
    private static final String GET_USER_INHERITORS_METRIC_NAME = "GetUserInheritors";
    private static final String GET_USER_SUBMITTED_POSTS_METRIC_NAME = "GetUserSubmittedPosts";
    private static final String GET_USER_UPVOTED_POSTS_METRIC_NAME = "GetUserUpvotedPosts";

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
     * Call to retrieve the Shards owned by a User.
     *
     * @param username A String containing the username of the User whose owned Shards to return.
     *
     * @return HTTP 200 OK - If the set of owned Shards was retrieved successfully.
     *         HTTP 404 Not Found - If the User doesn't exist.
     */
    @GetMapping(value = "/user/{username}/ownedShards")
    public ResponseEntity<?> getOwnedShards(@PathVariable final String username) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_USER_OWNED_SHARDS_METRIC_NAME);
        final String usernameLowercase = username.toLowerCase();

        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Set<Map<Object, Object>> ownedShards = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase)
            .out(USER_OWNS_SHARD_EDGE_LABEL)
            .valueMap().by(unfold())
            .toSet();

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(ownedShards);

        metricsUtil.addSuccessMetric(GET_USER_OWNED_SHARDS_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_USER_OWNED_SHARDS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve the Shards followed by a User.
     *
     * @param username A String containing the username of the User whose followed Shards to return.
     *
     * @return HTTP 200 OK - If the set of followed Shards was retrieved successfully.
     *         HTTP 404 Not Found - If the User doesn't exist.
     */
    @GetMapping(value = "/user/{username}/followed/shards")
    public ResponseEntity<?> getFollowedShards(@PathVariable final String username) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_USER_FOLLOWED_SHARDS_METRIC_NAME);
        final String usernameLowercase = username.toLowerCase();

        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Set<Object> followedShards = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase)
            .out(USER_FOLLOWS_SHARD_EDGE_LABEL)
            .values(SHARD_NAME_PROPERTY)
            .toSet();

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(followedShards);

        metricsUtil.addSuccessMetric(GET_USER_FOLLOWED_SHARDS_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_USER_FOLLOWED_SHARDS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve the Users followed by a User.
     *
     * @param username A String containing the username of the User whose followed Users to return.
     *
     * @return HTTP 200 OK - If the set of followed Users was retrieved successfully.
     *         HTTP 404 Not Found - If the User doesn't exist.
     */
    @GetMapping(value = "/user/{username}/followed/users")
    public ResponseEntity<?> getFollowedUsers(@PathVariable final String username) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_USER_FOLLOWED_USERS_METRIC_NAME);
        final String usernameLowercase = username.toLowerCase();

        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Set<Object> followedUsers = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase)
            .out(USER_FOLLOWS_USER_EDGE_LABEL)
            .values(USER_USERNAME_PROPERTY)
            .toSet();

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(followedUsers);

        metricsUtil.addSuccessMetric(GET_USER_FOLLOWED_USERS_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_USER_FOLLOWED_USERS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve Shards that inherit a User.
     *
     * @param username A String containing a set of shardNames of Shards which inherit the User.
     *
     * @return HTTP 200 OK - If the set of shardNames of Shards which inherit the User was retrieved successfully.
     *         HTTP 404 Not Found - If the User doesn't exist.
     */
    @GetMapping(value = "/user/{username}/inheritors")
    public ResponseEntity<?> getInheritors(@PathVariable final String username) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_USER_INHERITORS_METRIC_NAME);
        final String usernameLowercase = username.toLowerCase();

        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Set<Object> inheritors = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase)
            .in(SHARD_INHERITS_USER_EDGE_LABEL)
            .values(SHARD_NAME_PROPERTY)
            .toSet();

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(inheritors);

        metricsUtil.addSuccessMetric(GET_USER_INHERITORS_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_USER_INHERITORS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve followers of a User.
     *
     * @param username A String containing the username of the User whose followers to return.
     *
     * @return HTTP 200 OK - If the set of followers of the User was retrieved successfully.
     *         HTTP 404 Not Found - If the User doesn't exist.
     */
    @GetMapping(value = "/user/{username}/followers")
    public ResponseEntity<?> getFollowers(@PathVariable final String username) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_USER_FOLLOWED_USERS_METRIC_NAME);
        final String usernameLowercase = username.toLowerCase();

        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Set<Object> followers = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase)
            .in(USER_FOLLOWS_USER_EDGE_LABEL)
            .values(USER_USERNAME_PROPERTY)
            .toSet();

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(followers);

        metricsUtil.addSuccessMetric(GET_USER_FOLLOWED_USERS_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_USER_FOLLOWED_USERS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve all Posts submitted by a User.
     *
     * @param username A String containing the username of the User whose submitted Posts to return.
     *
     * @return HTTP 200 OK - If the set of Posts submitted by the User was retrieved successfully. Body is an array of
     *                       {@link com.pylon.pylonservice.model.domain.Post Post}.
     *         HTTP 404 Not Found - If the User doesn't exist.
     */
    @GetMapping(value = "/user/{username}/submitted")
    public ResponseEntity<?> getSubmitted(
        @RequestHeader(value = "Authorization", required = false) final String authorizationHeader,
        @PathVariable final String username) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_USER_SUBMITTED_POSTS_METRIC_NAME);
        final String usernameLowercase = username.toLowerCase();

        String callingUsernameLowercase = INVALID_USERNAME_VALUE;
        if (authorizationHeader != null) {
            final String jwt = JwtTokenUtil.removeBearerFromAuthorizationHeader(authorizationHeader);
            callingUsernameLowercase = jwtTokenUtil.getUsernameFromToken(jwt);
        }

        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final List<Post> submittedPosts = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase)
            .out(USER_SUBMITTED_POST_EDGE_LABEL)
            .order().by(COMMON_CREATED_AT_PROPERTY, desc)
            .flatMap(projectToPost(callingUsernameLowercase))
            .toList()
            .stream()
            .map(Post::new)
            .collect(Collectors.toList());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(submittedPosts);

        metricsUtil.addSuccessMetric(GET_USER_SUBMITTED_POSTS_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_USER_SUBMITTED_POSTS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve all Posts upvoted by a User.
     *
     * @param username A String containing the username of the User whose upvoted Posts to return.
     *
     * @return HTTP 200 OK - If the set of Posts upvoted by the User was retrieved successfully. Body is an array of
     *                       {@link com.pylon.pylonservice.model.domain.Post Post}.
     *         HTTP 404 Not Found - If the User doesn't exist.
     */
    @GetMapping(value = "/user/{username}/upvoted")
    public ResponseEntity<?> getUpvoted(
        @RequestHeader(value = "Authorization", required = false) final String authorizationHeader,
        @PathVariable final String username) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_USER_UPVOTED_POSTS_METRIC_NAME);
        final String usernameLowercase = username.toLowerCase();

        String callingUsernameLowercase = INVALID_USERNAME_VALUE;
        if (authorizationHeader != null) {
            final String jwt = JwtTokenUtil.removeBearerFromAuthorizationHeader(authorizationHeader);
            callingUsernameLowercase = jwtTokenUtil.getUsernameFromToken(jwt);
        }

        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final List<Post> upvotedPosts = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase)
            .out(USER_UPVOTED_POST_EDGE_LABEL)
            .order().by(COMMON_CREATED_AT_PROPERTY, desc)
            .flatMap(projectToPost(callingUsernameLowercase))
            .toList()
            .stream()
            .map(Post::new)
            .collect(Collectors.toList());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(upvotedPosts);

        metricsUtil.addSuccessMetric(GET_USER_UPVOTED_POSTS_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_USER_UPVOTED_POSTS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }
}
