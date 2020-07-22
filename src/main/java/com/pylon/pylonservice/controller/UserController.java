package com.pylon.pylonservice.controller;

import com.pylon.pylonservice.util.JwtTokenUtil;
import com.pylon.pylonservice.util.MetricsUtil;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

import static com.pylon.pylonservice.constants.GraphConstants.SHARD_INHERITS_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_NAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FOLLOWS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FOLLOWS_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_OWNS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_USERNAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERTEX_LABEL;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold;

@RestController
public class UserController {
    private static final String GET_USER_OWNED_SHARDS_METRIC_NAME = "GetUserOwnedShards";
    private static final String GET_USER_FOLLOWED_SHARDS_METRIC_NAME = "GetUserFollowedShards";
    private static final String GET_USER_FOLLOWED_USERS_METRIC_NAME = "GetUserFollowedUsers";

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

        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Set<Map<Object, Object>> ownedShards = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username)
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

        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Set<Object> followedShards = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username)
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

        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Set<Object> followedUsers = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username)
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
        metricsUtil.addCountMetric(GET_USER_FOLLOWED_USERS_METRIC_NAME);

        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Set<Object> inheritors = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username)
            .in(SHARD_INHERITS_USER_EDGE_LABEL)
            .values(SHARD_NAME_PROPERTY)
            .toSet();

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(inheritors);

        metricsUtil.addSuccessMetric(GET_USER_FOLLOWED_USERS_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_USER_FOLLOWED_USERS_METRIC_NAME, System.nanoTime() - startTime);
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

        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Set<Object> followers = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username)
            .in(USER_FOLLOWS_USER_EDGE_LABEL)
            .values(USER_USERNAME_PROPERTY)
            .toSet();

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(followers);

        metricsUtil.addSuccessMetric(GET_USER_FOLLOWED_USERS_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_USER_FOLLOWED_USERS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }
}
