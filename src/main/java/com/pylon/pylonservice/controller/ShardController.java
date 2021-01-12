package com.pylon.pylonservice.controller;

import com.pylon.pylonservice.model.domain.Post;
import com.pylon.pylonservice.model.domain.Profile;
import com.pylon.pylonservice.model.domain.Shard;
import com.pylon.pylonservice.model.domain.notification.Notification;
import com.pylon.pylonservice.model.domain.notification.OwnedShardInclusionNotification;
import com.pylon.pylonservice.model.domain.notification.ProfileInclusionNotification;
import com.pylon.pylonservice.model.requests.GetPostsRequest;
import com.pylon.pylonservice.model.requests.shard.CreateShardRequest;
import com.pylon.pylonservice.model.requests.shard.UpdateShardRequest;
import com.pylon.pylonservice.pojo.PageRange;
import com.pylon.pylonservice.services.AccessTokenService;
import com.pylon.pylonservice.services.MetricsService;
import com.pylon.pylonservice.services.NotificationService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.log4j.Log4j2;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.pylon.pylonservice.constants.AuthenticationConstants.ACCESS_TOKEN_COOKIE_NAME;
import static com.pylon.pylonservice.constants.GraphConstants.COMMON_CREATED_AT_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.INVALID_USERNAME_VALUE;
import static com.pylon.pylonservice.constants.GraphConstants.POST_POSTED_IN_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.POST_POSTED_IN_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_AVATAR_FILENAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_BANNER_FILENAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_DESCRIPTION_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_FEATURED_IMAGE_FILENAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_FEATURED_IMAGE_LINK_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_FRIENDLY_NAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_INHERITS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_INHERITS_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_NAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_VERTEX_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FOLLOWS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_OWNS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_USERNAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERTEX_LABEL;
import static com.pylon.pylonservice.model.domain.Post.projectToPost;
import static com.pylon.pylonservice.model.domain.Profile.projectToProfile;
import static com.pylon.pylonservice.model.domain.Shard.projectToShard;
import static com.pylon.pylonservice.model.domain.Shard.projectToSingleShard;
import static com.pylon.pylonservice.util.PaginationUtil.getPageRange;
import static com.pylon.pylonservice.util.PaginationUtil.paginatePosts;
import static org.apache.tinkerpop.gremlin.process.traversal.Order.desc;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.V;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.outE;
import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.single;

@Log4j2
@RestController
public class ShardController {
    private static final String GET_SHARD_METRIC_NAME = "GetShard";
    private static final String GET_SHARD_INHERITANCE_METRIC_NAME = "GetShardInheritance";
    private static final String GET_SHARD_FOLLOWERS_METRIC_NAME = "GetShardFollowers";
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
    private AccessTokenService accessTokenService;
    @Autowired
    private MetricsService metricsService;
    @Autowired
    private NotificationService notificationService;

    /**
     * Call to retrieve a Shard.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param shardName A String containing the name of the Shard to return.
     *
     * @return HTTP 200 OK - If the Shard was retrieved successfully returns a {@link Shard}.
     *         HTTP 401 Unauthorized - If a JWT was sent with the request but was expired.
     *         HTTP 404 Not Found - If the Shard doesn't exist.
     */
    @GetMapping(value = "/shard/{shardName}")
    public ResponseEntity<?> getShard(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken,
        @PathVariable final String shardName) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_SHARD_METRIC_NAME);

        final String callingUsernameLowercase;
        try {
            callingUsernameLowercase = accessTokenService.getUsernameFromAccessTokenOrDefaultIfNull(
                accessToken, INVALID_USERNAME_VALUE
            );
        } catch (final ExpiredJwtException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        final String shardNameLowercase = shardName.toLowerCase();
        if (!rG.V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Shard shard = new Shard(
            rG
                .V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameLowercase)
                .flatMap(projectToSingleShard(shardName, callingUsernameLowercase))
                .next()
        );

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(shard);

        metricsService.addSuccessMetric(GET_SHARD_METRIC_NAME);
        metricsService.addLatencyMetric(GET_SHARD_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve all of the Shards and Users that a Shard directly inherits.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param shardName A String containing the name of the Shard whose inheritance to return.
     *
     * @return HTTP 200 OK - If the Shard inheritance was retrieved successfully, a Map with two keys: "shards" which
     *                       keys a collection of {@link Shard} and "profiles" which keys a collection of
     *                       {@link Profile}.
     *         HTTP 404 Not Found - If the Shard doesn't exist.
     */
    @GetMapping(value = "/shard/{shardName}/inheritance")
    public ResponseEntity<?> getShardInheritance(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken,
        @PathVariable final String shardName) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_SHARD_INHERITANCE_METRIC_NAME);

        final String callingUsernameLowercase;
        try {
            callingUsernameLowercase = accessTokenService.getUsernameFromAccessTokenOrDefaultIfNull(
                accessToken, INVALID_USERNAME_VALUE
            );
        } catch (final ExpiredJwtException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        final String shardNameLowercase = shardName.toLowerCase();
        if (!rG.V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Set<Shard> shards = rG
            .V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameLowercase)
            .out(SHARD_INHERITS_SHARD_EDGE_LABEL)
            .flatMap(projectToShard(callingUsernameLowercase))
            .toSet()
            .stream()
            .map(Shard::new)
            .collect(Collectors.toSet());

        final Set<Profile> profiles = rG
            .V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameLowercase)
            .out(SHARD_INHERITS_USER_EDGE_LABEL)
            .flatMap(projectToProfile(callingUsernameLowercase))
            .toSet()
            .stream()
            .map(Profile::new)
            .collect(Collectors.toSet());

        final Map<String, Object> shardInheritance = Map.of(
            "shards", shards,
            "profiles", profiles
        );

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(shardInheritance);

        metricsService.addSuccessMetric(GET_SHARD_INHERITANCE_METRIC_NAME);
        metricsService.addLatencyMetric(GET_SHARD_INHERITANCE_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve all of the Profiles of the Users who follow a Shard directly.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param shardName A String containing the name of the Shard whose inheritance to return.
     *
     * @return HTTP 200 OK - If the Shard followers were retrieved successfully, a Set of {@link Profile}.
     *         HTTP 404 Not Found - If the Shard doesn't exist.
     */
    @GetMapping(value = "/shard/{shardName}/followers")
    public ResponseEntity<?> getShardFollowers(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken,
        @PathVariable final String shardName) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_SHARD_FOLLOWERS_METRIC_NAME);

        final String callingUsernameLowercase;
        try {
            callingUsernameLowercase = accessTokenService.getUsernameFromAccessTokenOrDefaultIfNull(
                    accessToken, INVALID_USERNAME_VALUE
            );
        } catch (final ExpiredJwtException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        final String shardNameLowercase = shardName.toLowerCase();
        if (!rG.V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Set<Profile> shardFollowers = rG
                .V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameLowercase)
                .in(USER_FOLLOWS_SHARD_EDGE_LABEL)
                .flatMap(projectToProfile(callingUsernameLowercase))
                .toSet()
                .stream()
                .map(Profile::new)
                .collect(Collectors.toSet());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(shardFollowers);

        metricsService.addSuccessMetric(GET_SHARD_FOLLOWERS_METRIC_NAME);
        metricsService.addLatencyMetric(GET_SHARD_FOLLOWERS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve all the post headers for a Shard, ordered by newest post first.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param shardName A String containing the shardName of the Shard whose posts to return.
     *
     * @return HTTP 200 OK - If the Posts in the Shard were retrieved successfully. Body is an array of
     *                       {@link Post}.
     *         HTTP 404 Not Found - If the Shard doesn't exist.
     */
    @GetMapping(value = "/shard/{shardName}/posts/new")
    public ResponseEntity<?> getNewShardPosts(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken,
        @PathVariable final String shardName,
        @RequestParam(name = "first", required = false) final Integer firstPostToReturn,
        @RequestParam(name = "count", required = false) final Integer countPostsToReturn) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_SHARD_POSTS_METRIC_NAME);

        final GetPostsRequest getPostsRequest;
        if (firstPostToReturn == null || countPostsToReturn == null) {
            getPostsRequest = null;
        } else {
            getPostsRequest = new GetPostsRequest(firstPostToReturn, countPostsToReturn);
        }

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

        final String shardNameLowercase = shardName.toLowerCase();
        if (!rG.V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final PageRange pageRange = getPageRange(getPostsRequest);
        final List<Post> posts = getAllPostsInShard(shardNameLowercase)
            .order().by(COMMON_CREATED_AT_PROPERTY, desc)
            .range(pageRange.getLow(), pageRange.getHigh())
            .flatMap(projectToPost(callingUsernameLowercase))
            .toList()
            .stream()
            .map(Post::new)
            .collect(Collectors.toList());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(posts);

        metricsService.addSuccessMetric(GET_SHARD_POSTS_METRIC_NAME);
        metricsService.addLatencyMetric(GET_SHARD_POSTS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve all the post headers for a Shard, ordered by most popular post first.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param shardName A String containing the shardName of the Shard whose posts to return.
     *
     * @return HTTP 200 OK - If the Posts in the Shard were retrieved successfully. Body is an array of
     *                       {@link Post}.
     *         HTTP 404 Not Found - If the Shard doesn't exist.
     */
    @GetMapping(value = "/shard/{shardName}/posts/popular")
    public ResponseEntity<?> getPopularShardPosts(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken,
        @PathVariable final String shardName,
        @RequestParam(name = "first", required = false) final Integer firstPostToReturn,
        @RequestParam(name = "count", required = false) final Integer countPostsToReturn) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_SHARD_POSTS_METRIC_NAME);

        final GetPostsRequest getPostsRequest;
        if (firstPostToReturn == null || countPostsToReturn == null) {
            getPostsRequest = null;
        } else {
            getPostsRequest = new GetPostsRequest(firstPostToReturn, countPostsToReturn);
        }

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

        final String shardNameLowercase = shardName.toLowerCase();
        if (!rG.V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Date now = new Date();
        final List<Post> posts = getAllPostsInShard(shardNameLowercase)
            .flatMap(projectToPost(callingUsernameLowercase))
            .toList()
            .stream()
            .map(Post::new)
            .sorted(Comparator.comparing((Post post) -> post.getPopularity(now)).reversed())
            .collect(Collectors.toList());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(paginatePosts(posts, getPostsRequest));

        metricsService.addSuccessMetric(GET_SHARD_POSTS_METRIC_NAME);
        metricsService.addLatencyMetric(GET_SHARD_POSTS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to create a Shard.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param createShardRequest A {@link CreateShardRequest}.
     *
     * @return HTTP 201 Created - If the Shard was created successfully.
     *         HTTP 401 Unauthorized - If the User isn't authenticated.
     *         HTTP 409 Conflict - If a Shard with the same name already exists.
     *         HTTP 422 Unprocessable Entity - If the CreateShardRequest isn't valid.
     */
    @PostMapping(value = "/shard")
    public ResponseEntity<?> createShard(@CookieValue(name = ACCESS_TOKEN_COOKIE_NAME) final String accessToken,
                                         @RequestBody final CreateShardRequest createShardRequest) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(CREATE_SHARD_METRIC_NAME);

        if (!createShardRequest.isValid()) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        final String username = accessTokenService.getUsernameFromAccessToken(accessToken);

        final String shardNameLowercase = createShardRequest.getShardName().toLowerCase();
        final Set<String> inheritedShardNamesLowercase = createShardRequest.getInheritedShardNames()
            .stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
        final Set<String> inheritedUsersLowercase = createShardRequest.getInheritedUsers()
            .stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        if (rG.V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } else {
            wG
                .addV(SHARD_VERTEX_LABEL)
                    .property(single, SHARD_NAME_PROPERTY, shardNameLowercase)
                    .property(single, SHARD_FRIENDLY_NAME_PROPERTY, createShardRequest.getShardFriendlyName())
                    .property(single, SHARD_AVATAR_FILENAME_PROPERTY, createShardRequest.getShardAvatarFilename())
                    .property(single, SHARD_BANNER_FILENAME_PROPERTY, createShardRequest.getShardBannerFilename())
                    .property(single, SHARD_DESCRIPTION_PROPERTY, createShardRequest.getShardDescription())
                    .property(
                        single, SHARD_FEATURED_IMAGE_FILENAME_PROPERTY, ""
                    )
                    .property(single, SHARD_FEATURED_IMAGE_LINK_PROPERTY, "")
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

        metricsService.addSuccessMetric(CREATE_SHARD_METRIC_NAME);
        metricsService.addLatencyMetric(CREATE_SHARD_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to update a Shard. This is an idempotent operation and replaces the inherited shards and inherited users of
     * the shard with the ones in the request.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param updateShardRequest An {@link UpdateShardRequest}.
     *
     * @return HTTP 200 Created - If the Shard was updated successfully.
     *         HTTP 401 Unauthorized - If the User isn't authenticated.
     *         HTTP 403 Forbidden - If the User is attempting to update a Shard they don't own.
     *         HTTP 404 Not Found - If the Shard to be updated doesn't exist.
     *         HTTP 422 Unprocessable Entity - If the UpdateShardRequest isn't valid.
     */
    @PutMapping(value = "/shard")
    public ResponseEntity<?> updateShard(@CookieValue(name = ACCESS_TOKEN_COOKIE_NAME) final String accessToken,
                                         @RequestBody final UpdateShardRequest updateShardRequest) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(UPDATE_SHARD_METRIC_NAME);

        if (!updateShardRequest.isValid()) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        final String shardNameLowercase = updateShardRequest.getShardName().toLowerCase();
        if (!rG.V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final String shardOwnerUsername = (String) rG
            .V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameLowercase)
            .in(USER_OWNS_SHARD_EDGE_LABEL)
            .values(USER_USERNAME_PROPERTY)
            .next();

        final String callingUsernameLowercase = accessTokenService.getUsernameFromAccessToken(accessToken);
        if (!shardOwnerUsername.equals(callingUsernameLowercase)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final Set<String> inheritedShardNamesLowercase = updateShardRequest.getInheritedShardNames()
            .stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
        final Set<String> inheritedUsersLowercase = updateShardRequest.getInheritedUsers()
            .stream()
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        Set<String> currentlyIncludedShardNames = rG
            .V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameLowercase)
            .out(SHARD_INHERITS_SHARD_EDGE_LABEL)
            .values(SHARD_NAME_PROPERTY)
            .toSet()
            .stream()
            .map(shardName -> (String) shardName)
            .collect(Collectors.toSet());
        Set<String> currentlyIncludedUsernames = rG
            .V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardNameLowercase)
            .out(SHARD_INHERITS_USER_EDGE_LABEL)
            .values(USER_USERNAME_PROPERTY)
            .toSet()
            .stream()
            .map(username -> (String) username)
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
            .property(single, SHARD_FRIENDLY_NAME_PROPERTY, updateShardRequest.getShardFriendlyName())
            .property(single, SHARD_AVATAR_FILENAME_PROPERTY, updateShardRequest.getShardAvatarFilename())
            .property(single, SHARD_BANNER_FILENAME_PROPERTY, updateShardRequest.getShardBannerFilename())
            .property(single, SHARD_DESCRIPTION_PROPERTY, updateShardRequest.getShardDescription())
            .property(
                single, SHARD_FEATURED_IMAGE_FILENAME_PROPERTY, updateShardRequest.getShardFeaturedImageFilename()
            )
            .property(single, SHARD_FEATURED_IMAGE_LINK_PROPERTY, updateShardRequest.getShardFeaturedImageLink())
            .iterate();

        try {
            sendShardUpdateNotifications(
                shardNameLowercase,
                callingUsernameLowercase,
                inheritedShardNamesLowercase,
                inheritedUsersLowercase,
                currentlyIncludedShardNames,
                currentlyIncludedUsernames
            );
        } catch (final Exception e) {
            log.error(String.format(
                "Failed to send shard update notifications for shard name: %s",
                shardNameLowercase)
            );
        }

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(HttpStatus.OK);

        metricsService.addSuccessMetric(UPDATE_SHARD_METRIC_NAME);
        metricsService.addLatencyMetric(UPDATE_SHARD_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    private void sendShardUpdateNotifications(final String shardNameLowercase,
                                              final String callingUsernameLowercase,
                                              final Set<String> inheritedShardNamesLowercase,
                                              final Set<String> inheritedUsersLowercase,
                                              final Set<String> currentlyIncludedShardNames,
                                              final Set<String> currentlyIncludedUsernames) {
        Set<String> newlyIncludedShardNames = new HashSet<>(inheritedShardNamesLowercase);
        newlyIncludedShardNames.removeAll(currentlyIncludedShardNames);

        Set<String> newlyIncludedProfileUsernames = new HashSet<>(inheritedUsersLowercase);
        newlyIncludedProfileUsernames.removeAll(currentlyIncludedUsernames);

        final Set<Shard> newlyIncludedShards = rG
            .V().has(SHARD_NAME_PROPERTY, P.within(newlyIncludedShardNames))
            .flatMap(projectToShard(callingUsernameLowercase))
            .toSet()
            .stream()
            .map(Shard::new)
            .collect(Collectors.toSet());

        final Set<Notification> notifications = new HashSet<>();
        notifications.addAll(
            newlyIncludedShards.stream()
                .map(
                    shard ->
                        OwnedShardInclusionNotification.builder()
                            .notificationId(UUID.randomUUID().toString())
                            .toUsername(shard.getOwnerUsername())
                            .createdAt(new Date())
                            .fromUsername(callingUsernameLowercase)
                            .isRead(false)
                            .includedShardName(shard.getShardName())
                            .includingShardName(shardNameLowercase)
                            .build()
                ).collect(Collectors.toSet())
        );
        notifications.addAll(
            newlyIncludedProfileUsernames
                .stream()
                .map(
                    toUsername -> ProfileInclusionNotification.builder()
                        .notificationId(UUID.randomUUID().toString())
                        .toUsername(toUsername)
                        .createdAt(new Date())
                        .fromUsername(callingUsernameLowercase)
                        .isRead(false)
                        .includingShardName(shardNameLowercase)
                        .build()
                ).collect(Collectors.toSet())
        );

        notificationService.notifyBatch(notifications);
    }

    private GraphTraversal<Vertex, Vertex> getAllPostsInShard(final String shardName) {
        return rG
            .V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardName)
            .emit()
            .repeat(out(SHARD_INHERITS_USER_EDGE_LABEL, SHARD_INHERITS_SHARD_EDGE_LABEL).simplePath())
            .in(POST_POSTED_IN_USER_EDGE_LABEL, POST_POSTED_IN_SHARD_EDGE_LABEL)
            .dedup();
    }
}
