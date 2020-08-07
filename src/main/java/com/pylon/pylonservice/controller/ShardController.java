package com.pylon.pylonservice.controller;

import com.pylon.pylonservice.model.domain.Post;
import com.pylon.pylonservice.model.requests.CreateShardRequest;
import com.pylon.pylonservice.model.requests.UpdateShardRequest;
import com.pylon.pylonservice.util.JwtTokenUtil;
import com.pylon.pylonservice.util.MetricsUtil;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import static com.pylon.pylonservice.constants.GraphConstants.COMMON_CREATED_AT_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_POSTED_IN_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.POST_POSTED_IN_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_INHERITS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_INHERITS_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_NAME_CASE_SENSITIVE_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_NAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_VERTEX_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FOLLOWS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_OWNS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_USERNAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERTEX_LABEL;
import static com.pylon.pylonservice.model.domain.Post.projectToPost;
import static org.apache.tinkerpop.gremlin.process.traversal.Order.desc;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.V;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.outE;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold;
import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.single;

@RestController
public class ShardController {
    private static final String GET_SHARD_METRIC_NAME = "GetShard";
    private static final String GET_SHARD_INHERITANCE_METRIC_NAME = "GetShardInheritance";
    private static final String GET_SHARD_POSTS_METRIC_NAME = "GetShardPosts";
    private static final String CREATE_SHARD_METRIC_NAME = "CreateShard";
    private static final String UPDATE_SHARD_METRIC_NAME = "UpdateShard";

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
        final String shardNameLowercase = shardName.toLowerCase();

        final Map<Object, Object> shardMetadata;
        try {
            shardMetadata = rG
                .V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameLowercase)
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
     *                       {
     *                           "shards": [
     *                               "jasonshard3",
     *                               "jasonshard2"
     *                           ],
     *                           "users": [
     *                               "jason40"
     *                           ]
     *                       }
     *         HTTP 404 Not Found - If the Shard doesn't exist.
     */
    @GetMapping(value = "/shard/{shardName}/inheritance")
    public ResponseEntity<?> getShardInheritance(@PathVariable final String shardName) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_SHARD_INHERITANCE_METRIC_NAME);
        final String shardNameLowercase = shardName.toLowerCase();

        if (!rG.V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Map<String, Object> shardInheritance = rG
            .V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameLowercase)
            .project("shards", "users")
            .by(out(SHARD_INHERITS_SHARD_EDGE_LABEL).values(SHARD_NAME_PROPERTY).fold())
            .by(out(SHARD_INHERITS_USER_EDGE_LABEL).values(USER_USERNAME_PROPERTY).fold())
            .next();

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(shardInheritance);

        metricsUtil.addSuccessMetric(GET_SHARD_INHERITANCE_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_SHARD_INHERITANCE_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve all the post headers for a Shard, ordered by newest post first.
     *
     * @param shardName A String containing the shardName of the Shard whose posts to return.
     *
     * @return HTTP 200 OK - If the Posts in the Shard were retrieved successfully.
     *                       [
     *                           {
     *                               "postId": "f7cc41e2-8ae7-49ef-979c-37619b43b228",
     *                               "postTitle": "Third Shard Post!",
     *                               "postFilename": null,
     *                               "postContentUrl": "exampleContentUrl",
     *                               "postBody": "third post!",
     *                               "createdAt": "2020-07-25T21:32:56.090+00:00",
     *                               "postUpvotes": 1,
     *                               "postSubmitter": "jason25",
     *                               "postPostedInUser": null,
     *                               "postPostedInShard": "shard9"
     *                           },
     *                           {
     *                               "postId": "700d0092-5da2-423d-89db-174087b66e9e",
     *                               "postTitle": null,
     *                               "postFilename": null,
     *                               "postContentUrl": null,
     *                               "postBody": null,
     *                               "createdAt": "2020-07-25T21:31:54.682+00:00",
     *                               "postUpvotes": 1,
     *                               "postSubmitter": "jason25",
     *                               "postPostedInUser": null,
     *                               "postPostedInShard": "shard9"
     *                           },
     *                           {
     *                               "postId": "5be67901-bee1-446b-bb50-b62046311fac",
     *                               "postTitle": "Profile Post 1",
     *                               "postFilename": null,
     *                               "postContentUrl": null,
     *                               "postBody": "1",
     *                               "createdAt": "2020-07-19T23:39:28.403+00:00",
     *                               "postUpvotes": 1,
     *                               "postSubmitter": "jason25",
     *                               "postPostedInUser": "jason25",
     *                               "postPostedInShard": null
     *                           }
     *                       ]
     *
     *         HTTP 404 Not Found - If the Shard doesn't exist.
     */
    @GetMapping(value = "/shard/{shardName}/posts/new")
    public ResponseEntity<?> getNewShardPosts(@PathVariable final String shardName) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_SHARD_POSTS_METRIC_NAME);
        final String shardNameLowercase = shardName.toLowerCase();

        if (!rG.V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final List<Post> posts = getAllPostsInShard(shardNameLowercase)
            .order().by(COMMON_CREATED_AT_PROPERTY, desc)
            .flatMap(projectToPost())
            .toList()
            .stream()
            .map(Post::new)
            .collect(Collectors.toList());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(posts);

        metricsUtil.addSuccessMetric(GET_SHARD_POSTS_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_SHARD_POSTS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve all the post headers for a Shard, ordered by most popular post first.
     *
     * @param shardName A String containing the shardName of the Shard whose posts to return.
     *
     * @return HTTP 200 OK - If the Posts in the Shard were retrieved successfully.
     *                       [
     *                           {
     *                               "postId": "f7cc41e2-8ae7-49ef-979c-37619b43b228",
     *                               "postTitle": "Third Shard Post!",
     *                               "postFilename": null,
     *                               "postContentUrl": "exampleContentUrl",
     *                               "postBody": "third post!",
     *                               "createdAt": "2020-07-25T21:32:56.090+00:00",
     *                               "postUpvotes": 1,
     *                               "postSubmitter": "jason25",
     *                               "postPostedInUser": null,
     *                               "postPostedInShard": "shard9"
     *                           },
     *                           {
     *                               "postId": "700d0092-5da2-423d-89db-174087b66e9e",
     *                               "postTitle": null,
     *                               "postFilename": null,
     *                               "postContentUrl": null,
     *                               "postBody": null,
     *                               "createdAt": "2020-07-25T21:31:54.682+00:00",
     *                               "postUpvotes": 1,
     *                               "postSubmitter": "jason25",
     *                               "postPostedInUser": null,
     *                               "postPostedInShard": "shard9"
     *                           },
     *                           {
     *                               "postId": "5be67901-bee1-446b-bb50-b62046311fac",
     *                               "postTitle": "Profile Post 1",
     *                               "postFilename": null,
     *                               "postContentUrl": null,
     *                               "postBody": "1",
     *                               "createdAt": "2020-07-19T23:39:28.403+00:00",
     *                               "postUpvotes": 1,
     *                               "postSubmitter": "jason25",
     *                               "postPostedInUser": "jason25",
     *                               "postPostedInShard": null
     *                           }
     *                       ]
     *
     *         HTTP 404 Not Found - If the Shard doesn't exist.
     */
    @GetMapping(value = "/shard/{shardName}/posts/popular")
    public ResponseEntity<?> getPopularShardPosts(@PathVariable final String shardName) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_SHARD_POSTS_METRIC_NAME);
        final String shardNameLowercase = shardName.toLowerCase();

        if (!rG.V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Date now = new Date();
        final List<Post> posts = getAllPostsInShard(shardNameLowercase)
            .flatMap(projectToPost())
            .toList()
            .stream()
            .map(Post::new)
            .sorted(Comparator.comparing((Post post) -> post.getPopularity(now)).reversed())
            .collect(Collectors.toList());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(posts);

        metricsUtil.addSuccessMetric(GET_SHARD_POSTS_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_SHARD_POSTS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to create a Shard.
     *
     * @param authorizationHeader A key-value header with key "Authorization" and value like "Bearer exampleJwtToken".
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
     *         HTTP 401 Unauthorized - If the User isn't authenticated.
     *         HTTP 409 Conflict - If a Shard with the same name already exists.
     *         HTTP 422 Unprocessable Entity - If the CreateShardRequest isn't valid.
     */
    @PostMapping(value = "/shard")
    public ResponseEntity<?> createShard(@RequestHeader(value = "Authorization") final String authorizationHeader,
                                         @RequestBody final CreateShardRequest createShardRequest) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(CREATE_SHARD_METRIC_NAME);
        final String shardNameLowercase = createShardRequest.getShardName().toLowerCase();
        final Set<String> inheritedShardNamesLowercase = createShardRequest.getInheritedShardNames()
            .stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
        final Set<String> inheritedUsersLowercase = createShardRequest.getInheritedUsers()
            .stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        if (!createShardRequest.isValid()) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        final String jwt = JwtTokenUtil.removeBearerFromAuthorizationHeader(authorizationHeader);
        final String username = jwtTokenUtil.getUsernameFromToken(jwt);

        if (rG.V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } else {
            wG
                .addV(SHARD_VERTEX_LABEL)
                    .property(single, SHARD_NAME_PROPERTY, shardNameLowercase)
                    .property(single, SHARD_NAME_CASE_SENSITIVE_PROPERTY, createShardRequest.getShardName())
                    .property(single, COMMON_CREATED_AT_PROPERTY, new Date())
                    .as("newShard")
                .sideEffect(
                    V()
                        .hasLabel(SHARD_VERTEX_LABEL)
                        .has(SHARD_NAME_PROPERTY, P.within(inheritedShardNamesLowercase))
                        .addE(SHARD_INHERITS_SHARD_EDGE_LABEL).from("newShard")
                )
                .sideEffect(
                    V()
                        .hasLabel(USER_VERTEX_LABEL)
                        .has(USER_USERNAME_PROPERTY, P.within(inheritedUsersLowercase))
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

    /**
     * Call to update a Shard. This is an idempotent operation and replaces the inherited shards and inherited users of
     * the shard with the ones in the request.
     *
     * @param authorizationHeader A key-value header with key "Authorization" and value like "Bearer exampleJwtToken".
     * @param shardName A String containing the name of the Shard to update.
     * @param updateShardRequest A JSON object containing inheritedShardNames and inheritedUsers like
     *                           {
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
     *                           {
     *                               "inheritedShardNames": [],
     *                               "inheritedUsers": []
     *                           }
     *
     * @return HTTP 200 Created - If the Shard was updated successfully.
     *         HTTP 401 Unauthorized - If the User isn't authenticated.
     *         HTTP 403 Forbidden - If the User is attempting to update a Shard they don't own.
     *         HTTP 422 Unprocessable Entity - If the UpdateShardRequest isn't valid.
     */
    @PutMapping(value = "/shard/{shardName}")
    public ResponseEntity<?> updateShard(@RequestHeader(value = "Authorization") final String authorizationHeader,
                                         @PathVariable final String shardName,
                                         @RequestBody final UpdateShardRequest updateShardRequest) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(UPDATE_SHARD_METRIC_NAME);
        final String shardNameLowercase = shardName.toLowerCase();

        final String jwt = JwtTokenUtil.removeBearerFromAuthorizationHeader(authorizationHeader);
        final String username = jwtTokenUtil.getUsernameFromToken(jwt);

        final String shardOwnerUsername = (String) rG
            .V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameLowercase)
            .in(USER_OWNS_SHARD_EDGE_LABEL)
            .values(USER_USERNAME_PROPERTY)
            .next();

        if (!shardOwnerUsername.equals(username)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (!updateShardRequest.isValid()) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        final Set<String> inheritedShardNamesLowercase = updateShardRequest.getInheritedShardNames()
            .stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
        final Set<String> inheritedUsersLowercase = updateShardRequest.getInheritedUsers()
            .stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        wG
            .V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameLowercase).as("shard")
            .sideEffect(
                outE(SHARD_INHERITS_SHARD_EDGE_LABEL).drop()
            )
            .sideEffect(
                outE(SHARD_INHERITS_USER_EDGE_LABEL).drop()
            )
            .sideEffect(
                V()
                    .hasLabel(SHARD_VERTEX_LABEL)
                    .has(SHARD_NAME_PROPERTY, P.within(inheritedShardNamesLowercase))
                    .addE(SHARD_INHERITS_SHARD_EDGE_LABEL).from("shard")
            )
            .sideEffect(
                V()
                    .hasLabel(USER_VERTEX_LABEL)
                    .has(USER_USERNAME_PROPERTY, P.within(inheritedUsersLowercase))
                    .addE(SHARD_INHERITS_USER_EDGE_LABEL).from("shard")
            )
            .iterate();

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(HttpStatus.OK);

        metricsUtil.addSuccessMetric(UPDATE_SHARD_METRIC_NAME);
        metricsUtil.addLatencyMetric(UPDATE_SHARD_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    private GraphTraversal<Vertex, Vertex> getAllPostsInShard(final String shardName) {
        return rG
            .V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardName)
            .emit()
            .repeat(out(SHARD_INHERITS_USER_EDGE_LABEL, SHARD_INHERITS_SHARD_EDGE_LABEL))
            .in(POST_POSTED_IN_USER_EDGE_LABEL, POST_POSTED_IN_SHARD_EDGE_LABEL)
            .dedup();
    }
}
