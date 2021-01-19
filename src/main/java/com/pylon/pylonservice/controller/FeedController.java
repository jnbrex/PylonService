package com.pylon.pylonservice.controller;

import com.pylon.pylonservice.model.domain.Post;
import com.pylon.pylonservice.model.requests.GetPostsRequest;
import com.pylon.pylonservice.services.AccessTokenService;
import com.pylon.pylonservice.services.MetricsService;
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

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.pylon.pylonservice.constants.AuthenticationConstants.ACCESS_TOKEN_COOKIE_NAME;
import static com.pylon.pylonservice.constants.GraphConstants.POST_POSTED_IN_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.POST_POSTED_IN_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_INHERITS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_INHERITS_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FOLLOWS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FOLLOWS_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_USERNAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERTEX_LABEL;
import static com.pylon.pylonservice.model.domain.Post.projectToPost;
import static com.pylon.pylonservice.util.PaginationUtil.paginatePosts;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;

@RestController
public class FeedController {
    private static final String GET_MY_FEED_METRIC_NAME = "GetMyFeed";

    @Qualifier("reader")
    @Autowired
    private GraphTraversalSource rG;
    @Autowired
    private AccessTokenService accessTokenService;
    @Autowired
    private MetricsService metricsService;

    /**
     * Call to retrieve a User's personalized feed.
     *
     * @param accessToken A cookie with name "accessToken" issued by PylonService.
     * @param firstPostToReturn The first post to return, used for pagination.
     * @param countPostsToReturn The number of posts to return, used for pagination. It should be called with
     *                           value less than or equal to 100.
     *
     * @return HTTP 200 OK - A List of {@link Post}.
     *         HTTP 401 Unauthorized - If the User isn't authenticated.
     */
    @GetMapping(value = "/myFeed")
    public ResponseEntity<?> getMyFeed(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME) final String accessToken,
        @RequestParam(name = "first", required = false) final Integer firstPostToReturn,
        @RequestParam(name = "count", required = false) final Integer countPostsToReturn) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_MY_FEED_METRIC_NAME);

        final GetPostsRequest getPostsRequest;
        if (firstPostToReturn == null || countPostsToReturn == null) {
            getPostsRequest = null;
        } else {
            getPostsRequest = new GetPostsRequest(firstPostToReturn, countPostsToReturn);
        }

        if (getPostsRequest != null && !getPostsRequest.isValid()) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        final String username = accessTokenService.getUsernameFromAccessToken(accessToken);

        final Date now = new Date();
        final List<Post> posts = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username)
            .out(USER_FOLLOWS_USER_EDGE_LABEL, USER_FOLLOWS_SHARD_EDGE_LABEL)
            .emit()
            .repeat(out(SHARD_INHERITS_USER_EDGE_LABEL, SHARD_INHERITS_SHARD_EDGE_LABEL).simplePath())
            .in(POST_POSTED_IN_USER_EDGE_LABEL, POST_POSTED_IN_SHARD_EDGE_LABEL)
            .dedup()
            .flatMap(projectToPost(username))
            .toList()
            .stream()
            .map(Post::new)
            .sorted(Comparator.comparing((Post post) -> post.getPopularity(now)).reversed())
            .collect(Collectors.toList());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(paginatePosts(posts, getPostsRequest));

        metricsService.addSuccessMetric(GET_MY_FEED_METRIC_NAME);
        metricsService.addLatencyMetric(GET_MY_FEED_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }
}
