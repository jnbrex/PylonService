package com.pylon.pylonservice.controller;

import com.pylon.pylonservice.model.requests.CreateShardRequest;
import com.pylon.pylonservice.util.JwtTokenUtil;
import com.pylon.pylonservice.util.MetricsUtil;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.pylon.pylonservice.constants.GraphConstants.COMMON_CREATED_AT_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_INHERITS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_INHERITS_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_NAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_VERTEX_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FOLLOWS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_OWNS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_USERNAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERTEX_LABEL;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold;
import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.single;

@RestController
public class ShardController {
    private static final String GET_SHARD_METRIC_NAME = "GetShard";
    private static final String CREATE_SHARD_METRIC_NAME = "CreateShard";

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
     * Call to retrieve a Post.
     *
     * @param shardName A String containing the name of the Shard to return.
     *
     * @return HTTP 200 OK - If the Shard was retrieved successfully.
     *         HTTP 404 Not Found - If the Shard doesn't exist.
     */
    @GetMapping(value = "/shard/{shardName}")
    public ResponseEntity<?> getShard(@PathVariable final String shardName) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_SHARD_METRIC_NAME);

        final Map<Object, Object> shardMetadata;
        try {
            shardMetadata = rG
                .V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardName)
                .valueMap().by(unfold())
                .next();
        } catch (final NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(shardMetadata);

        metricsUtil.addSuccessMetric(GET_SHARD_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_SHARD_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to create a Shard.
     *
     * @param createShardRequest A String containing the postId of the Post for which the comments should be returned.
     *
     * @return HTTP 200 OK - If the Post's comments were retrieved successfully.
     *         HTTP 404 Not Found - If the Post doesn't exist.
     */
    @PostMapping(value = "/shard")
    public ResponseEntity<?> createShard(@RequestHeader(value = "Authorization") final String authorizationHeader,
                                         final CreateShardRequest createShardRequest) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(CREATE_SHARD_METRIC_NAME);

        final String jwt = JwtTokenUtil.removeBearerFromAuthorizationHeader(authorizationHeader);
        final String username = jwtTokenUtil.getUsernameFromToken(jwt);

        wG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username).as("user")
            .addV(SHARD_VERTEX_LABEL)
                .property(single, SHARD_NAME_PROPERTY, createShardRequest.getShardName())
                .property(single, COMMON_CREATED_AT_PROPERTY, new Date())
                .as("newShard")
            .V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, P.within(createShardRequest.getInheritedShardNames()))
                .as("inheritedShards")
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, P.within(createShardRequest.getInheritedUsers()))
                .as("inheritedUsers")
            .addE(SHARD_INHERITS_SHARD_EDGE_LABEL).from("newShard").to("inheritedShards")
            .addE(SHARD_INHERITS_USER_EDGE_LABEL).from("newShard").to("inheritedUsers")
            .addE(USER_OWNS_SHARD_EDGE_LABEL).from("user").to("newShard")
            .addE(USER_FOLLOWS_SHARD_EDGE_LABEL).from("user").to("newShard")
            .iterate();

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(
           HttpStatus.CREATED
        );

        metricsUtil.addSuccessMetric(CREATE_SHARD_METRIC_NAME);
        metricsUtil.addLatencyMetric(CREATE_SHARD_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }
}
