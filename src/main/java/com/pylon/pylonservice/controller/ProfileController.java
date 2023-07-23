package com.pylon.pylonservice.controller;

import com.pylon.pylonservice.model.domain.Post;
import com.pylon.pylonservice.model.domain.Profile;
import com.pylon.pylonservice.model.requests.GetPostsRequest;
import com.pylon.pylonservice.model.requests.UpdateProfileRequest;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.pylon.pylonservice.constants.AuthenticationConstants.ACCESS_TOKEN_COOKIE_NAME;
import static com.pylon.pylonservice.constants.GraphConstants.COMMON_CREATED_AT_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.INVALID_USERNAME_VALUE;
import static com.pylon.pylonservice.constants.GraphConstants.POST_ID_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_POSTED_IN_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.POST_VERTEX_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_AVATAR_FILENAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_BANNER_FILENAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_BIO_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_DISCORD_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FACEBOOK_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FRIENDLY_NAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_INSTAGRAM_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_LOCATION_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_PINNED_POST_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_SUBMITTED_POST_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_TIKTOK_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_TWITCH_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_TWITTER_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_USERNAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERIFIED_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERTEX_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_WEBSITE_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_YOUTUBE_URL_PROPERTY;
import static com.pylon.pylonservice.model.domain.Post.projectToPost;
import static com.pylon.pylonservice.model.domain.Profile.projectToSingleProfile;
import static com.pylon.pylonservice.util.PaginationUtil.getPageRange;
import static com.pylon.pylonservice.util.PaginationUtil.paginatePosts;
import static org.apache.tinkerpop.gremlin.process.traversal.Order.desc;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.V;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.addE;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.outE;
import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.single;

@RestController
public class ProfileController {
    private static final String GET_PROFILE_METRIC_NAME = "GetProfile";
    private static final String GET_MY_PROFILE_METRIC_NAME = "GetMyProfile";
    private static final String GET_NEW_PROFILE_POSTS_METRIC_NAME = "GetNewProfilePosts";
    private static final String GET_POPULAR_PROFILE_POSTS_METRIC_NAME = "GetPopularProfilePosts";
    private static final String PUT_PROFILE_METRIC_NAME = "PutProfile";
    private static final String PIN_POST_METRIC_NAME = "PinPost";
    private static final String UNPIN_POST_METRIC_NAME = "UnpinPost";

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
     * Call to retrieve a User's public profile data.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param username A String containing the username of the User's profile to return
     *
     * @return HTTP 200 OK - If the User's public profile data was retrieved successfully. Body is a
     *                       {@link com.pylon.pylonservice.model.domain.Profile Profile}.
     *         HTTP 404 Not Found - If the User doesn't exist.
     */
    @GetMapping(value = "/profile/{username}")
    public ResponseEntity<?> getProfile(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken,
        @PathVariable final String username) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_PROFILE_METRIC_NAME);

        final String callingUsernameLowercase;
        try {
            callingUsernameLowercase = accessTokenService.getUsernameFromAccessTokenOrDefaultIfNull(
                accessToken, INVALID_USERNAME_VALUE
            );
        } catch (final ExpiredJwtException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        final String usernameLowercase = username.toLowerCase();
        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Profile profile = new Profile(
            rG
                .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase)
                .flatMap(projectToSingleProfile(usernameLowercase, callingUsernameLowercase))
                .next()
        );

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(profile);

        metricsService.addSuccessMetric(GET_PROFILE_METRIC_NAME);
        metricsService.addLatencyMetric(GET_PROFILE_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve a User's public profile data.
     *
     * @param accessToken A cookie with name "accessToken"
     *
     * @return HTTP 200 OK - If the User's public profile data was retrieved successfully. Body is a
     *                       {@link com.pylon.pylonservice.model.domain.Profile Profile}.
     *              HTTP 401 Unauthorized - If the User isn't authenticated.
     */
    @GetMapping(value = "/myProfile")
    public ResponseEntity<?> getMyProfile(@CookieValue(name = ACCESS_TOKEN_COOKIE_NAME) final String accessToken) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_MY_PROFILE_METRIC_NAME);

        final String usernameLowercase = accessTokenService.getUsernameFromAccessToken(accessToken);

        final Profile profile = new Profile(
            rG
                .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase)
                .flatMap(projectToSingleProfile(usernameLowercase, usernameLowercase))
                .next()
        );

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(profile);

        metricsService.addSuccessMetric(GET_MY_PROFILE_METRIC_NAME);
        metricsService.addLatencyMetric(GET_MY_PROFILE_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve all the post headers for a Profile, newest posts first.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param username A String containing the username of the User's Profile to return.
     *
     * @return HTTP 200 OK - If the Posts on the Profile were retrieved successfully. Body is an array of
     *                       {@link com.pylon.pylonservice.model.domain.Post Post}.
     *         HTTP 404 Not Found - If the Profile doesn't exist.
     */
    @GetMapping(value = "/profile/{username}/posts/new")
    public ResponseEntity<?> getNewProfilePosts(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken,
        @PathVariable final String username,
        @RequestParam(name = "first", required = false) final Integer firstPostToReturn,
        @RequestParam(name = "count", required = false) final Integer countPostsToReturn) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_NEW_PROFILE_POSTS_METRIC_NAME);

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

        final String usernameLowercase = username.toLowerCase();
        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final PageRange pageRange = getPageRange(getPostsRequest);
        final List<Post> posts = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase)
            .in(POST_POSTED_IN_USER_EDGE_LABEL)
            .order().by(COMMON_CREATED_AT_PROPERTY, desc)
            .range(pageRange.getLow(), pageRange.getHigh())
            .flatMap(projectToPost(callingUsernameLowercase))
            .toList()
            .stream()
            .map(Post::new)
            .collect(Collectors.toList());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(posts);

        metricsService.addSuccessMetric(GET_NEW_PROFILE_POSTS_METRIC_NAME);
        metricsService.addLatencyMetric(GET_NEW_PROFILE_POSTS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve all the post headers for a Profile, most popular posts first.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param username A String containing the username of the User's Profile to return.
     *
     * @return HTTP 200 OK - If the Posts on the Profile were retrieved successfully. Body is an array of
     *                       {@link com.pylon.pylonservice.model.domain.Post Post}.
     *         HTTP 404 Not Found - If the Profile doesn't exist.
     */
    @GetMapping(value = "/profile/{username}/posts/popular")
    public ResponseEntity<?> getPopularProfilePosts(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken,
        @PathVariable final String username,
        @RequestParam(name = "first", required = false) final Integer firstPostToReturn,
        @RequestParam(name = "count", required = false) final Integer countPostsToReturn) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_POPULAR_PROFILE_POSTS_METRIC_NAME);

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

        final String usernameLowercase = username.toLowerCase();
        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Date now = new Date();
        final List<Post> posts = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase) // Single user vertex
            .in(POST_POSTED_IN_USER_EDGE_LABEL) // All posts posted in the user's profile
            .flatMap(projectToPost(callingUsernameLowercase))
            .toList()
            .stream()
            .map(Post::new)
            .sorted(Comparator.comparing((Post post) -> post.getPopularity(now)).reversed())
            .collect(Collectors.toList());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(paginatePosts(posts, getPostsRequest));

        metricsService.addSuccessMetric(GET_POPULAR_PROFILE_POSTS_METRIC_NAME);
        metricsService.addLatencyMetric(GET_POPULAR_PROFILE_POSTS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to update a User's public profile data. This is an idempotent operation, so all fields must be included. For
     * any fields which should not be present on the User's profile, send an empty string.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param updateProfileRequest A {@link UpdateProfileRequest}
     *
     * @return HTTP 200 OK - If the User's public Profile data was updated successfully.
     *         HTTP 401 Unauthorized - If the User isn't authenticated.
     *         HTTP 422 Unprocessable Entity - If {@link UpdateProfileRequest#isValid()} is false.
     */
    @PutMapping(value = "/profile")
    public ResponseEntity<?> updateProfile(@CookieValue(name = ACCESS_TOKEN_COOKIE_NAME) final String accessToken,
                                           @RequestBody final UpdateProfileRequest updateProfileRequest) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(PUT_PROFILE_METRIC_NAME);

        final String usernameLowercase = accessTokenService.getUsernameFromAccessToken(accessToken);

        // Do not trust userVerified value that user sends with request
        final boolean userVerified = (boolean) rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase)
            .values(USER_VERIFIED_PROPERTY)
            .next();
        updateProfileRequest.setUserVerified(userVerified);

        if (!updateProfileRequest.isValid()) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        wG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase)
            .property(single, USER_FRIENDLY_NAME_PROPERTY, updateProfileRequest.getUserFriendlyName())
            .property(single, USER_AVATAR_FILENAME_PROPERTY, updateProfileRequest.getUserAvatarFilename())
            .property(single, USER_BANNER_FILENAME_PROPERTY, updateProfileRequest.getUserBannerFilename())
            .property(single, USER_BIO_PROPERTY, updateProfileRequest.getUserBio())
            .property(single, USER_LOCATION_PROPERTY, updateProfileRequest.getUserLocation())
            .property(single, USER_FACEBOOK_URL_PROPERTY, updateProfileRequest.getUserFacebookUrl())
            .property(single, USER_TWITTER_URL_PROPERTY, updateProfileRequest.getUserTwitterUrl())
            .property(single, USER_INSTAGRAM_URL_PROPERTY, updateProfileRequest.getUserInstagramUrl())
            .property(single, USER_TWITCH_URL_PROPERTY, updateProfileRequest.getUserTwitchUrl())
            .property(single, USER_YOUTUBE_URL_PROPERTY, updateProfileRequest.getUserYoutubeUrl())
            .property(single, USER_TIKTOK_URL_PROPERTY, updateProfileRequest.getUserTiktokUrl())
            .property(single, USER_DISCORD_URL_PROPERTY, updateProfileRequest.getUserDiscordUrl())
            .property(single, USER_WEBSITE_URL_PROPERTY, updateProfileRequest.getUserWebsiteUrl())
            .iterate();

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(HttpStatus.OK);

        metricsService.addSuccessMetric(PUT_PROFILE_METRIC_NAME);
        metricsService.addLatencyMetric(PUT_PROFILE_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to set the profile's pinned post.
     *
     * @param accessToken A cookie with name "accessToken"
     * @param postId The postId of the Post to pin.
     *
     * @return HTTP 200 OK - If the Post was pinned successfully.
     *         HTTP 401 Unauthorized - If the User isn't authenticated.
     *         HTTP 403 Forbidden - If the Post with postId={postId} wasn't submitted by the calling User.
     *         HTTP 404 Not Found - If the Post with postId={postId} doesn't exist.
     */
    @PutMapping(value = "/profile/pin/{postId}")
    public ResponseEntity<?> pinPost(@CookieValue(name = ACCESS_TOKEN_COOKIE_NAME) final String accessToken,
                                     @PathVariable final String postId) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(PIN_POST_METRIC_NAME);

        final String username = accessTokenService.getUsernameFromAccessToken(accessToken);

        if (!rG.V().has(POST_VERTEX_LABEL, POST_ID_PROPERTY, postId).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final String postSubmitterUsername = (String) rG
            .V().has(POST_VERTEX_LABEL, POST_ID_PROPERTY, postId)
            .in(USER_SUBMITTED_POST_EDGE_LABEL)
            .values(USER_USERNAME_PROPERTY)
            .next();

        if (!username.equals(postSubmitterUsername)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        wG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username)
            .sideEffect(outE(USER_PINNED_POST_EDGE_LABEL).drop())
            .sideEffect(
                addE(USER_PINNED_POST_EDGE_LABEL).to(
                    V().has(POST_VERTEX_LABEL, POST_ID_PROPERTY, postId)
                )
            )
            .iterate();

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(HttpStatus.OK);
        metricsService.addSuccessMetric(PIN_POST_METRIC_NAME);
        metricsService.addLatencyMetric(PIN_POST_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to unpin the profile's pinned post.
     *
     * @param accessToken A cookie with name "accessToken"
     *
     * @return HTTP 200 OK - If the Post was unpinned successfully or there was no pinned Post.
     *         HTTP 401 Unauthorized - If the User isn't authenticated.
     */
    @PutMapping(value = "/profile/unpin")
    public ResponseEntity<?> unpinPost(@CookieValue(name = ACCESS_TOKEN_COOKIE_NAME) final String accessToken) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(UNPIN_POST_METRIC_NAME);

        final String username = accessTokenService.getUsernameFromAccessToken(accessToken);

        wG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username)
            .outE(USER_PINNED_POST_EDGE_LABEL).drop()
            .iterate();

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(HttpStatus.OK);
        metricsService.addSuccessMetric(UNPIN_POST_METRIC_NAME);
        metricsService.addLatencyMetric(UNPIN_POST_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }
}
