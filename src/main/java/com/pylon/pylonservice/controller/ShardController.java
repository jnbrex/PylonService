package com.pylon.pylonservice.controller;

import com.pylon.pylonservice.model.requests.CreatePostRequest;
import com.pylon.pylonservice.model.requests.CreateShardRequest;
import com.pylon.pylonservice.model.responses.CreatePostResponse;
import com.pylon.pylonservice.util.JwtTokenUtil;
import com.pylon.pylonservice.util.MetricsUtil;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.Tree;
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
import java.util.UUID;

import static org.apache.tinkerpop.gremlin.process.traversal.Order.desc;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.in;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.valueMap;
import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.single;

@RestController
public class ShardController {
    private static final String GET_SHARD_METRIC_NAME = "GetShard";
    private static final String POST_SHARD_METRIC_NAME = "PostShard";

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

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body("PLACEHOLDER");

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
    public ResponseEntity<?> createShard(final CreateShardRequest createShardRequest) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(POST_SHARD_METRIC_NAME);

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(
           HttpStatus.CREATED
        );

        metricsUtil.addSuccessMetric(POST_SHARD_METRIC_NAME);
        metricsUtil.addLatencyMetric(POST_SHARD_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }
}
