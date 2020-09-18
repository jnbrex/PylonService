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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static com.pylon.pylonservice.constants.AuthenticationConstants.ACCESS_TOKEN_COOKIE_NAME;
import static com.pylon.pylonservice.constants.GraphConstants.COMMON_CREATED_AT_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.INVALID_USERNAME_VALUE;
import static com.pylon.pylonservice.constants.GraphConstants.POST_VERTEX_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_VERTEX_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERTEX_LABEL;
import static com.pylon.pylonservice.model.domain.Post.projectToPost;
import static com.pylon.pylonservice.model.domain.Profile.projectToProfile;
import static com.pylon.pylonservice.model.domain.Shard.projectToShard;
import static com.pylon.pylonservice.util.PaginationUtil.getPageRange;
import static org.apache.tinkerpop.gremlin.process.traversal.Order.desc;

@RestController
public class AllController {
    private static final String GET_ALL_SHARDS_METRIC_NAME = "GetAllShards";
    private static final String GET_ALL_PROFILES_METRIC_NAME = "GetAllProfiles";
    private static final String GET_ALL_POSTS_METRIC_NAME = "GetAllPosts";

    @Qualifier("reader")
    @Autowired
    private GraphTraversalSource rG;
    @Autowired
    private AccessTokenService accessTokenService;
    @Autowired
    private MetricsService metricsService;

    @GetMapping("/all/shards/new")
    public ResponseEntity<?> getAllShards(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_ALL_SHARDS_METRIC_NAME);

        final String callingUsernameLowercase;
        try {
            callingUsernameLowercase = accessTokenService.getUsernameFromAccessTokenOrDefaultIfNull(
                accessToken, INVALID_USERNAME_VALUE
            );
        } catch (final ExpiredJwtException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        final List<Shard> allShards = rG.V()
            .hasLabel(SHARD_VERTEX_LABEL)
            .order().by(COMMON_CREATED_AT_PROPERTY, desc)
            .flatMap(projectToShard(callingUsernameLowercase))
            .toList()
            .stream()
            .map(Shard::new)
            .collect(Collectors.toList());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(allShards);

        metricsService.addSuccessMetric(GET_ALL_SHARDS_METRIC_NAME);
        metricsService.addLatencyMetric(GET_ALL_SHARDS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    @GetMapping("/all/profiles/new")
    public ResponseEntity<?> getAllProfiles(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_ALL_PROFILES_METRIC_NAME);

        final String callingUsernameLowercase;
        try {
            callingUsernameLowercase = accessTokenService.getUsernameFromAccessTokenOrDefaultIfNull(
                accessToken, INVALID_USERNAME_VALUE
            );
        } catch (final ExpiredJwtException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        final List<Profile> allProfiles = rG.V()
            .hasLabel(USER_VERTEX_LABEL)
            .order().by(COMMON_CREATED_AT_PROPERTY, desc)
            .flatMap(projectToProfile(callingUsernameLowercase))
            .toList()
            .stream()
            .map(Profile::new)
            .collect(Collectors.toList());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(allProfiles);

        metricsService.addSuccessMetric(GET_ALL_PROFILES_METRIC_NAME);
        metricsService.addLatencyMetric(GET_ALL_PROFILES_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    @GetMapping("/all/posts/new")
    public ResponseEntity<?> getAllPosts(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken,
        @RequestParam(name = "first", required = false) final Integer firstPostToReturn,
        @RequestParam(name = "count", required = false) final Integer countPostsToReturn) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_ALL_POSTS_METRIC_NAME);

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

        final PageRange pageRange = getPageRange(getPostsRequest);
        final List<Post> posts = rG.V()
            .hasLabel(POST_VERTEX_LABEL)
            .order().by(COMMON_CREATED_AT_PROPERTY, desc)
            .range(pageRange.getLow(), pageRange.getHigh())
            .flatMap(projectToPost(callingUsernameLowercase))
            .toList()
            .stream()
            .map(Post::new)
            .collect(Collectors.toList());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(posts);

        metricsService.addSuccessMetric(GET_ALL_POSTS_METRIC_NAME);
        metricsService.addLatencyMetric(GET_ALL_POSTS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }
}
