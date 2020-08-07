package com.pylon.pylonservice.controller;

import com.pylon.pylonservice.model.domain.Post;
import com.pylon.pylonservice.util.JwtTokenUtil;
import com.pylon.pylonservice.util.MetricsUtil;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.pylon.pylonservice.constants.GraphConstants.POST_POSTED_IN_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.POST_POSTED_IN_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_INHERITS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_INHERITS_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_NAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FOLLOWS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FOLLOWS_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_SUBMITTED_POST_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_UPVOTED_POST_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_USERNAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERTEX_LABEL;
import static com.pylon.pylonservice.model.domain.Post.NUM_UPVOTES;
import static com.pylon.pylonservice.model.domain.Post.POSTED_IN_SHARD;
import static com.pylon.pylonservice.model.domain.Post.POSTED_IN_USER;
import static com.pylon.pylonservice.model.domain.Post.PROPERTIES;
import static com.pylon.pylonservice.model.domain.Post.SUBMITTER_USERNAME;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.in;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.project;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.valueMap;

@RestController
public class FeedController {
    private static final String GET_MY_FEED_METRIC_NAME = "GetMyFeed";

    @Qualifier("reader")
    @Autowired
    private GraphTraversalSource rG;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private MetricsUtil metricsUtil;

    /**
     * Call to retrieve a User's personalized feed.
     *
     * @param authorizationHeader A key-value header with key "Authorization" and value like "Bearer exampleJwtToken".
     *
     * @return HTTP 200 OK - If the Shard was retrieved successfully.
     *         HTTP 401 Unauthorized - If the User isn't authenticated.
     */
    @GetMapping(value = "/myFeed")
    public ResponseEntity<?> getShard(@RequestHeader(value = "Authorization") final String authorizationHeader) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_MY_FEED_METRIC_NAME);

        final String jwt = JwtTokenUtil.removeBearerFromAuthorizationHeader(authorizationHeader);
        final String username = jwtTokenUtil.getUsernameFromToken(jwt);

        final Date now = new Date();
        final List<Post> posts = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username)
            .out(USER_FOLLOWS_USER_EDGE_LABEL, USER_FOLLOWS_SHARD_EDGE_LABEL)
            .emit()
            .repeat(out(SHARD_INHERITS_USER_EDGE_LABEL, SHARD_INHERITS_SHARD_EDGE_LABEL))
            .in(POST_POSTED_IN_USER_EDGE_LABEL, POST_POSTED_IN_SHARD_EDGE_LABEL)
            .dedup()
            .flatMap(projectToPost())
            .toList()
            .stream()
            .map(Post::new)
            .sorted(Comparator.comparing((Post post) -> post.getPopularity(now)).reversed())
            .collect(Collectors.toList());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(posts);

        metricsUtil.addSuccessMetric(GET_MY_FEED_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_MY_FEED_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    private GraphTraversal<Object, Map<String, Object>> projectToPost() {
        return project(PROPERTIES, NUM_UPVOTES, SUBMITTER_USERNAME, POSTED_IN_SHARD, POSTED_IN_USER)
            .by(valueMap().by(unfold()))
            .by(in(USER_UPVOTED_POST_EDGE_LABEL).count())
            .by(in(USER_SUBMITTED_POST_EDGE_LABEL).values(USER_USERNAME_PROPERTY))
            .by(out(POST_POSTED_IN_SHARD_EDGE_LABEL).values(SHARD_NAME_PROPERTY).fold())
            .by(out(POST_POSTED_IN_USER_EDGE_LABEL).values(USER_USERNAME_PROPERTY).fold());
    }
}
