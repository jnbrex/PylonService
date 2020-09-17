package com.pylon.pylonservice.controller;

import com.pylon.pylonservice.model.domain.Post;
import com.pylon.pylonservice.model.domain.Profile;
import com.pylon.pylonservice.model.domain.Shard;
import com.pylon.pylonservice.model.requests.GetPostsRequest;
import com.pylon.pylonservice.pojo.PageRange;
import com.pylon.pylonservice.services.AccessTokenService;
import com.pylon.pylonservice.services.MetricsService;
import io.jsonwebtoken.ExpiredJwtException;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.pylon.pylonservice.constants.AuthenticationConstants.ACCESS_TOKEN_COOKIE_NAME;
import static com.pylon.pylonservice.constants.GraphConstants.COMMON_CREATED_AT_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.INVALID_USERNAME_VALUE;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_INHERITS_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FOLLOWS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FOLLOWS_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_OWNS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_SUBMITTED_POST_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_UPVOTED_POST_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_USERNAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERTEX_LABEL;
import static com.pylon.pylonservice.model.domain.Post.projectToPost;
import static com.pylon.pylonservice.model.domain.Profile.projectToProfile;
import static com.pylon.pylonservice.model.domain.Shard.projectToShard;
import static com.pylon.pylonservice.util.PaginationUtil.getPageRange;
import static org.apache.tinkerpop.gremlin.process.traversal.Order.desc;

@RestController
public class UserController {
    private static final String GET_USER_OWNED_SHARDS_METRIC_NAME = "GetUserOwnedShards";
    private static final String GET_USER_FOLLOWED_SHARDS_METRIC_NAME = "GetUserFollowedShards";
    private static final String GET_USER_FOLLOWED_USERS_METRIC_NAME = "GetUserFollowedUsers";
    private static final String GET_USER_INHERITORS_METRIC_NAME = "GetUserInheritors";
    private static final String GET_USER_SUBMITTED_POSTS_METRIC_NAME = "GetUserSubmittedPosts";
    private static final String GET_USER_UPVOTED_POSTS_METRIC_NAME = "GetUserUpvotedPosts";

    @Qualifier("reader")
    @Autowired
    private GraphTraversalSource rG;
    @Autowired
    private AccessTokenService accessTokenService;
    @Autowired
    private MetricsService metricsService;

    /**
     * Call to retrieve the Shards owned by a User.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param username A String containing the username of the User whose owned Shards to return.
     *
     * @return HTTP 200 OK - If the set of owned Shards was retrieved successfully. Body contains a collection of
     *                       {@link Shard}.
     *         HTTP 404 Not Found - If the User doesn't exist.
     */
    @GetMapping(value = "/user/{username}/ownedShards")
    public ResponseEntity<?> getOwnedShards(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken,
        @PathVariable final String username) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_USER_OWNED_SHARDS_METRIC_NAME);

        final String callingUsernameLowercase;
        try {
            callingUsernameLowercase = accessTokenService.getUsernameFromAccessTokenOrDefaultIfNull(
                accessToken, INVALID_USERNAME_VALUE
            );
        } catch (final ExpiredJwtException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        final String usernameLowercase = username.toLowerCase();
        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Set<Shard> ownedShards = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase)
            .out(USER_OWNS_SHARD_EDGE_LABEL)
            .flatMap(projectToShard(callingUsernameLowercase))
            .toSet()
            .stream()
            .map(Shard::new)
            .collect(Collectors.toSet());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(ownedShards);

        metricsService.addSuccessMetric(GET_USER_OWNED_SHARDS_METRIC_NAME);
        metricsService.addLatencyMetric(GET_USER_OWNED_SHARDS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve the Shards followed by a User.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param username A String containing the username of the User whose followed Shards to return.
     *
     * @return HTTP 200 OK - If the set of followed Shards was retrieved successfully. Body contains a collection of
     *                       {@link Shard}.
     *         HTTP 404 Not Found - If the User doesn't exist.
     */
    @GetMapping(value = "/user/{username}/followed/shards")
    public ResponseEntity<?> getFollowedShards(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken,
        @PathVariable final String username) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_USER_FOLLOWED_SHARDS_METRIC_NAME);

        final String callingUsernameLowercase;
        try {
            callingUsernameLowercase = accessTokenService.getUsernameFromAccessTokenOrDefaultIfNull(
                accessToken, INVALID_USERNAME_VALUE
            );
        } catch (final ExpiredJwtException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        final String usernameLowercase = username.toLowerCase();
        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Set<Shard> followedShards = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase)
            .out(USER_FOLLOWS_SHARD_EDGE_LABEL)
            .flatMap(projectToShard(callingUsernameLowercase))
            .toSet()
            .stream()
            .map(Shard::new)
            .collect(Collectors.toSet());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(followedShards);

        metricsService.addSuccessMetric(GET_USER_FOLLOWED_SHARDS_METRIC_NAME);
        metricsService.addLatencyMetric(GET_USER_FOLLOWED_SHARDS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve the Users followed by a User.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param username A String containing the username of the User whose followed Users to return.
     *
     * @return HTTP 200 OK - If the set of followed Users was retrieved successfully. Body contains a collection of
     *                       {@link Profile}.
     *         HTTP 404 Not Found - If the User doesn't exist.
     */
    @GetMapping(value = "/user/{username}/followed/users")
    public ResponseEntity<?> getFollowedUsers(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken,
        @PathVariable final String username) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_USER_FOLLOWED_USERS_METRIC_NAME);

        final String callingUsernameLowercase;
        try {
            callingUsernameLowercase = accessTokenService.getUsernameFromAccessTokenOrDefaultIfNull(
                accessToken, INVALID_USERNAME_VALUE
            );
        } catch (final ExpiredJwtException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        final String usernameLowercase = username.toLowerCase();
        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Set<Profile> followedUsers = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase)
            .out(USER_FOLLOWS_USER_EDGE_LABEL)
            .flatMap(projectToProfile(callingUsernameLowercase))
            .toSet()
            .stream()
            .map(Profile::new)
            .collect(Collectors.toSet());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(followedUsers);

        metricsService.addSuccessMetric(GET_USER_FOLLOWED_USERS_METRIC_NAME);
        metricsService.addLatencyMetric(GET_USER_FOLLOWED_USERS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve Shards that inherit a User.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param username A String containing a set of shardNames of Shards which inherit the User.
     *
     * @return HTTP 200 OK - If the set of Shards which inherit the User was retrieved successfully. Body contains a
     *                       collection of {@link Shard}.
     *         HTTP 404 Not Found - If the User doesn't exist.
     */
    @GetMapping(value = "/user/{username}/inheritors")
    public ResponseEntity<?> getInheritors(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken,
        @PathVariable final String username) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_USER_INHERITORS_METRIC_NAME);

        final String callingUsernameLowercase;
        try {
            callingUsernameLowercase = accessTokenService.getUsernameFromAccessTokenOrDefaultIfNull(
                accessToken, INVALID_USERNAME_VALUE
            );
        } catch (final ExpiredJwtException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        final String usernameLowercase = username.toLowerCase();
        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Set<Shard> inheritors = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase)
            .in(SHARD_INHERITS_USER_EDGE_LABEL)
            .flatMap(projectToShard(callingUsernameLowercase))
            .toSet()
            .stream()
            .map(Shard::new)
            .collect(Collectors.toSet());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(inheritors);

        metricsService.addSuccessMetric(GET_USER_INHERITORS_METRIC_NAME);
        metricsService.addLatencyMetric(GET_USER_INHERITORS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve followers of a User.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param username A String containing the username of the User whose followers to return.
     *
     * @return HTTP 200 OK - If the set of followers of the User was retrieved successfully. Body contains a collection
     *                       of {@link Profile}.
     *         HTTP 404 Not Found - If the User doesn't exist.
     */
    @GetMapping(value = "/user/{username}/followers")
    public ResponseEntity<?> getFollowers(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken,
        @PathVariable final String username) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_USER_FOLLOWED_USERS_METRIC_NAME);

        final String callingUsernameLowercase;
        try {
            callingUsernameLowercase = accessTokenService.getUsernameFromAccessTokenOrDefaultIfNull(
                accessToken, INVALID_USERNAME_VALUE
            );
        } catch (final ExpiredJwtException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        final String usernameLowercase = username.toLowerCase();
        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Set<Profile> followers = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase)
            .in(USER_FOLLOWS_USER_EDGE_LABEL)
            .flatMap(projectToProfile(callingUsernameLowercase))
            .toSet()
            .stream()
            .map(Profile::new)
            .collect(Collectors.toSet());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(followers);

        metricsService.addSuccessMetric(GET_USER_FOLLOWED_USERS_METRIC_NAME);
        metricsService.addLatencyMetric(GET_USER_FOLLOWED_USERS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve all Posts submitted by a User.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param username A String containing the username of the User whose submitted Posts to return.
     *
     * @return HTTP 200 OK - If the set of Posts submitted by the User was retrieved successfully. Body is an array of
     *                       {@link Post}.
     *         HTTP 404 Not Found - If the User doesn't exist.
     */
    @GetMapping(value = "/user/{username}/submitted")
    public ResponseEntity<?> getSubmitted(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken,
        @PathVariable final String username,
        @RequestBody(required = false) final GetPostsRequest getPostsRequest) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_USER_SUBMITTED_POSTS_METRIC_NAME);

        if (getPostsRequest != null && !getPostsRequest.isValid()) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        final String callingUsernameLowercase;
        try {
            callingUsernameLowercase = accessTokenService.getUsernameFromAccessTokenOrDefaultIfNull(
                accessToken, INVALID_USERNAME_VALUE
            );
        } catch (final ExpiredJwtException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        final String usernameLowercase = username.toLowerCase();
        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final PageRange pageRange = getPageRange(getPostsRequest);
        final List<Post> posts = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase)
            .out(USER_SUBMITTED_POST_EDGE_LABEL)
            .order().by(COMMON_CREATED_AT_PROPERTY, desc)
            .range(pageRange.getLow(), pageRange.getHigh())
            .flatMap(projectToPost(callingUsernameLowercase))
            .toList()
            .stream()
            .map(Post::new)
            .collect(Collectors.toList());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(posts);

        metricsService.addSuccessMetric(GET_USER_SUBMITTED_POSTS_METRIC_NAME);
        metricsService.addLatencyMetric(GET_USER_SUBMITTED_POSTS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve all Posts upvoted by a User.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param username A String containing the username of the User whose upvoted Posts to return.
     *
     * @return HTTP 200 OK - If the set of Posts upvoted by the User was retrieved successfully. Body is an array of
     *                       {@link Post}.
     *         HTTP 404 Not Found - If the User doesn't exist.
     */
    @GetMapping(value = "/user/{username}/upvoted")
    public ResponseEntity<?> getUpvoted(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken,
        @PathVariable final String username,
        @RequestBody(required = false) final GetPostsRequest getPostsRequest) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_USER_UPVOTED_POSTS_METRIC_NAME);

        if (getPostsRequest != null && !getPostsRequest.isValid()) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        final String callingUsernameLowercase;
        try {
            callingUsernameLowercase = accessTokenService.getUsernameFromAccessTokenOrDefaultIfNull(
                accessToken, INVALID_USERNAME_VALUE
            );
        } catch (final ExpiredJwtException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        final String usernameLowercase = username.toLowerCase();
        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final PageRange pageRange = getPageRange(getPostsRequest);
        final List<Post> posts = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase)
            .out(USER_UPVOTED_POST_EDGE_LABEL)
            .order().by(COMMON_CREATED_AT_PROPERTY, desc)
            .range(pageRange.getLow(), pageRange.getHigh())
            .flatMap(projectToPost(callingUsernameLowercase))
            .toList()
            .stream()
            .map(Post::new)
            .collect(Collectors.toList());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(posts);

        metricsService.addSuccessMetric(GET_USER_UPVOTED_POSTS_METRIC_NAME);
        metricsService.addLatencyMetric(GET_USER_UPVOTED_POSTS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }
}
