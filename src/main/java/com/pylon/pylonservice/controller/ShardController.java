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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static com.pylon.pylonservice.constants.GraphConstants.COMMON_CREATED_AT_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_INHERITS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_INHERITS_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_NAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_VERTEX_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FOLLOWS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_OWNS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_USERNAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERTEX_LABEL;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.V;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold;
import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.single;

@RestController
public class ShardController {
    private static final String GET_SHARD_METRIC_NAME = "GetShard";
    private static final String GET_SHARD_INHERITANCE_METRIC_NAME = "GetShardInheritance";
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
     * Call to retrieve a Shard.
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
     * Call to retrieve the Shard inheritance.
     *
     * @param shardName A String containing the name of the Shard whose inheritance to return.
     *
     * @return HTTP 200 OK - If the Shard inheritance was retrieved successfully.
     *         HTTP 404 Not Found - If the Shard doesn't exist.
     */
    @GetMapping(value = "/shard/{shardName}/inheritance")
    public ResponseEntity<?> getShardInheritance(@PathVariable final String shardName) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_SHARD_INHERITANCE_METRIC_NAME);

        if (!rG.V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardName).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Set<Map<Object, Object>> shardInheritance = rG
            .V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardName)
            .out(SHARD_INHERITS_SHARD_EDGE_LABEL, SHARD_INHERITS_USER_EDGE_LABEL)
            .valueMap().by(unfold())
            .toSet();

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(shardInheritance);

        metricsUtil.addSuccessMetric(GET_SHARD_INHERITANCE_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_SHARD_INHERITANCE_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to create a Shard.
     *
     * @param createShardRequest A JSON object containing shardName, inheritedShardNames, and inheritedUsers like
     *                           {
     *                               "shardName": "exampleShardName",
     *                               "inheritedShardNames": [
     *                                   "inheritedShardName1",
     *                                   "inheritedShardName2"
     *                               ],
     *                               "inheritedUsers": [
     *                                   "inheritedUser1",
     *                                   "inheritedUser2"
     *                               ]
     *                           }
     *
     *                           An object like below is also valid
     *
     *                           {
     *                               "shardName": "exampleShardName",
     *                               "inheritedShardNames": [],
     *                               "inheritedUsers": []
     *                           }
     *
     * @return HTTP 201 Created - If the Shard was created successfully.
     *         HTTP 401 Unauthenticated - If the User isn't authenticated.
     *         HTTP 409 Conflict - If a Shard with the same name already exists.
     */
    @PostMapping(value = "/shard")
    public ResponseEntity<?> createShard(@RequestHeader(value = "Authorization") final String authorizationHeader,
                                         @RequestBody final CreateShardRequest createShardRequest) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(CREATE_SHARD_METRIC_NAME);

        final String jwt = JwtTokenUtil.removeBearerFromAuthorizationHeader(authorizationHeader);
        final String username = jwtTokenUtil.getUsernameFromToken(jwt);

        if (rG.V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, createShardRequest.getShardName()).hasNext()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } else {
            wG
                .addV(SHARD_VERTEX_LABEL)
                    .property(single, SHARD_NAME_PROPERTY, createShardRequest.getShardName())
                    .property(single, COMMON_CREATED_AT_PROPERTY, new Date())
                    .as("newShard")
                .sideEffect(
                    V()
                        .hasLabel(SHARD_VERTEX_LABEL)
                        .has(SHARD_NAME_PROPERTY, P.within(createShardRequest.getInheritedShardNames()))
                        .addE(SHARD_INHERITS_SHARD_EDGE_LABEL).from("newShard")
                )
                .sideEffect(
                    V()
                        .hasLabel(USER_VERTEX_LABEL)
                        .has(USER_USERNAME_PROPERTY, P.within(createShardRequest.getInheritedUsers()))
                        .addE(SHARD_INHERITS_USER_EDGE_LABEL).from("newShard")
                )
                .V()
                    .has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username)
                    .as("user")
                    .addE(USER_OWNS_SHARD_EDGE_LABEL).from("user").to("newShard")
                    .addE(USER_FOLLOWS_SHARD_EDGE_LABEL).from("user").to("newShard")
                .iterate();
        }

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(HttpStatus.CREATED);

        metricsUtil.addSuccessMetric(CREATE_SHARD_METRIC_NAME);
        metricsUtil.addLatencyMetric(CREATE_SHARD_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }
}
