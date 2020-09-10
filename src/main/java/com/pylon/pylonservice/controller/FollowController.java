package com.pylon.pylonservice.controller;

import com.pylon.pylonservice.services.AccessTokenService;
import com.pylon.pylonservice.services.MetricsService;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.pylon.pylonservice.constants.AuthenticationConstants.ACCESS_TOKEN_COOKIE_NAME;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_NAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_VERTEX_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FOLLOWS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FOLLOWS_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_USERNAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERTEX_LABEL;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.V;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.addE;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.inV;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.outE;

@RestController
public class FollowController {
    private static final String FOLLOW_USER_METRIC_NAME = "FollowUser";
    private static final String FOLLOW_SHARD_METRIC_NAME = "FollowShard";
    private static final String UNFOLLOW_USER_METRIC_NAME = "UnfollowUser";
    private static final String UNFOLLOW_SHARD_METRIC_NAME = "UnfollowShard";

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

    /**
     * Call to add a follow relationship from the calling User to the User with username {usernameToFollow}.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param usernameToFollow A String containing the username of the User who the calling User should follow.
     *
     * @return HTTP 200 OK - If the follow relationship was added or already existed.
     *         HTTP 404 Not Found - If the User with username {usernameToFollow} doesn't exist.
     *         HTTP 422 Unprocessable Entity - If the calling User's username is equal to {usernameToFollow}.
     */
    @PutMapping(value = "/follow/user/{usernameToFollow}")
    public ResponseEntity<?> followUser(@CookieValue(name = ACCESS_TOKEN_COOKIE_NAME) final String accessToken,
                                        @PathVariable final String usernameToFollow) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(FOLLOW_USER_METRIC_NAME);
        final String usernameToFollowLowercase = usernameToFollow.toLowerCase();

        final String followerUsername = accessTokenService.getUsernameFromAccessToken(accessToken);

        if (usernameToFollow.equals(followerUsername)) {
            return new ResponseEntity<>("A user cannot follow themself.", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameToFollowLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        wG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, followerUsername)
            .coalesce(
                outE(USER_FOLLOWS_USER_EDGE_LABEL).filter(
                    inV().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameToFollowLowercase)
                ),
                addE(USER_FOLLOWS_USER_EDGE_LABEL).to(
                    V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameToFollowLowercase)
                )
            )
            .iterate();

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(HttpStatus.OK);

        metricsService.addSuccessMetric(FOLLOW_USER_METRIC_NAME);
        metricsService.addLatencyMetric(FOLLOW_USER_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to add a follow relationship from the calling User to the Shard with shardName {shardNameToFollow}.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param shardNameToFollow A String containing the shardName of the Shard who the calling User should follow.
     *
     * @return HTTP 200 OK - If the follow relationship was added or already existed.
     *         HTTP 404 Not Found - If the Shard with shardName {shardNameToFollow} doesn't exist.
     */
    @PutMapping(value = "/follow/shard/{shardNameToFollow}")
    public ResponseEntity<?> followShard(@CookieValue(name = ACCESS_TOKEN_COOKIE_NAME) final String accessToken,
                                         @PathVariable final String shardNameToFollow) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(FOLLOW_SHARD_METRIC_NAME);
        final String shardNameToFollowLowercase = shardNameToFollow.toLowerCase();

        final String followerUsername = accessTokenService.getUsernameFromAccessToken(accessToken);

        if (!rG.V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameToFollowLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        wG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, followerUsername)
            .coalesce(
                outE(USER_FOLLOWS_SHARD_EDGE_LABEL).filter(
                    inV().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameToFollowLowercase)
                ),
                addE(USER_FOLLOWS_SHARD_EDGE_LABEL).to(
                    V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameToFollowLowercase)
                )
            )
            .iterate();

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(HttpStatus.OK);

        metricsService.addSuccessMetric(FOLLOW_SHARD_METRIC_NAME);
        metricsService.addLatencyMetric(FOLLOW_SHARD_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to remove a follow relationship from the calling User to the User with username {usernameToUnfollow}.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param usernameToUnfollow A String containing the username of the User who the calling User should follow.
     *
     * @return HTTP 200 OK - If the follow relationship was removed or did not exist.
     *         HTTP 404 Not Found - If the User with username {usernameToUnfollow} doesn't exist.
     */
    @PutMapping(value = "/unfollow/user/{usernameToUnfollow}")
    public ResponseEntity<?> unfollowUser(@CookieValue(name = ACCESS_TOKEN_COOKIE_NAME) final String accessToken,
                                          @PathVariable final String usernameToUnfollow) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(UNFOLLOW_USER_METRIC_NAME);
        final String usernameToUnfollowLowercase = usernameToUnfollow.toLowerCase();

        final String followerUsername = accessTokenService.getUsernameFromAccessToken(accessToken);

        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameToUnfollowLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        wG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, followerUsername)
            .outE(USER_FOLLOWS_USER_EDGE_LABEL).where(
                inV().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameToUnfollowLowercase)
            )
            .drop()
            .iterate();

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(HttpStatus.OK);

        metricsService.addSuccessMetric(UNFOLLOW_USER_METRIC_NAME);
        metricsService.addLatencyMetric(UNFOLLOW_USER_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to remove a follow relationship from the calling User to the Shard with shardName {shardNameToUnfollow}.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param shardNameToUnfollow A String containing the shardName of the Shard who the calling User should follow.
     *
     * @return HTTP 200 OK - If the follow relationship was removed or did not exist.
     *         HTTP 404 Not Found - If the Shard with shardName {shardNameToUnfollow} doesn't exist.
     */
    @PutMapping(value = "/unfollow/shard/{shardNameToUnfollow}")
    public ResponseEntity<?> unfollowShard(@CookieValue(name = ACCESS_TOKEN_COOKIE_NAME) final String accessToken,
                                           @PathVariable final String shardNameToUnfollow) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(UNFOLLOW_SHARD_METRIC_NAME);
        final String shardNameToUnfollowLowercase = shardNameToUnfollow.toLowerCase();

        final String followerUsername = accessTokenService.getUsernameFromAccessToken(accessToken);

        if (!rG.V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameToUnfollowLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        wG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, followerUsername)
            .outE(USER_FOLLOWS_SHARD_EDGE_LABEL).where(
                inV().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameToUnfollowLowercase)
            )
            .drop()
            .iterate();

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(HttpStatus.OK);

        metricsService.addSuccessMetric(UNFOLLOW_SHARD_METRIC_NAME);
        metricsService.addLatencyMetric(UNFOLLOW_SHARD_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }
}
