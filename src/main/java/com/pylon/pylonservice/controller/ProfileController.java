package com.pylon.pylonservice.controller;

import com.pylon.pylonservice.model.domain.Post;
import com.pylon.pylonservice.model.domain.Profile;
import com.pylon.pylonservice.model.requests.UpdateProfileRequest;
import com.pylon.pylonservice.util.JwtTokenUtil;
import com.pylon.pylonservice.util.MetricsUtil;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.pylon.pylonservice.constants.GraphConstants.COMMON_CREATED_AT_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.INVALID_USERNAME_VALUE;
import static com.pylon.pylonservice.constants.GraphConstants.POST_POSTED_IN_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_AVATAR_FILENAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_BANNER_FILENAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_BIO_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_DISCORD_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FACEBOOK_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_INSTAGRAM_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_LOCATION_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_TIKTOK_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_TWITCH_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_TWITTER_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_USERNAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERTEX_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_WEBSITE_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_YOUTUBE_URL_PROPERTY;
import static com.pylon.pylonservice.model.domain.Post.projectToPost;
import static com.pylon.pylonservice.model.domain.Profile.projectToProfile;
import static org.apache.tinkerpop.gremlin.process.traversal.Order.desc;
import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.single;

@RestController
public class ProfileController {
    private static final String GET_PROFILE_METRIC_NAME = "GetProfile";
    private static final String GET_MY_PROFILE_METRIC_NAME = "GetMyProfile";
    private static final String GET_NEW_PROFILE_POSTS_METRIC_NAME = "GetNewProfilePosts";
    private static final String GET_POPULAR_PROFILE_POSTS_METRIC_NAME = "GetPopularProfilePosts";
    private static final String PUT_PROFILE_METRIC_NAME = "PutProfile";

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
     * Call to retrieve a User's public profile data.
     *
     * @param username A String containing the username of the User's profile to return
     *
     * @return HTTP 200 OK - If the User's public profile data was retrieved successfully. Body is a
     *                       {@link com.pylon.pylonservice.model.domain.Profile Profile}.
     *         HTTP 404 Not Found - If the User doesn't exist.
     */
    @GetMapping(value = "/profile/{username}")
    public ResponseEntity<?> getProfile(@PathVariable final String username) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_PROFILE_METRIC_NAME);
        final String usernameLowercase = username.toLowerCase();

        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Profile profile = new Profile(
            rG
                .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase)
                .flatMap(projectToProfile(usernameLowercase))
                .next()
        );

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(profile);

        metricsUtil.addSuccessMetric(GET_PROFILE_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_PROFILE_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve a User's public profile data.
     *
     * @param authorizationHeader A request header with key "Authorization" and body including a jwt like "Bearer {jwt}"
     *
     * @return HTTP 200 OK - If the User's public profile data was retrieved successfully. Body is a
     *                       {@link com.pylon.pylonservice.model.domain.Profile Profile}.
     *              HTTP 401 Unauthorized - If the User isn't authenticated.
     */
    @GetMapping(value = "/myProfile")
    public ResponseEntity<?> getMyProfile(@RequestHeader(value = "Authorization") final String authorizationHeader) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_MY_PROFILE_METRIC_NAME);

        final String jwt = JwtTokenUtil.removeBearerFromAuthorizationHeader(authorizationHeader);
        final String usernameLowercase = jwtTokenUtil.getUsernameFromToken(jwt);

        final Profile profile = new Profile(
            rG
                .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase)
                .flatMap(projectToProfile(usernameLowercase))
                .next()
        );

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(profile);

        metricsUtil.addSuccessMetric(GET_MY_PROFILE_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_MY_PROFILE_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve all the post headers for a Profile, newest posts first.
     *
     * @param username A String containing the username of the User's Profile to return.
     *
     * @return HTTP 200 OK - If the Posts on the Profile were retrieved successfully. Body is an array of
     *                       {@link com.pylon.pylonservice.model.domain.Post Post}.
     *         HTTP 404 Not Found - If the Profile doesn't exist.
     */
    @GetMapping(value = "/profile/{username}/posts/new")
    public ResponseEntity<?> getNewProfilePosts(
        @RequestHeader(value = "Authorization", required = false) final String authorizationHeader,
        @PathVariable final String username) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_NEW_PROFILE_POSTS_METRIC_NAME);
        final String usernameLowercase = username.toLowerCase();

        String callingUsernameLowercase = INVALID_USERNAME_VALUE;
        if (authorizationHeader != null) {
            final String jwt = JwtTokenUtil.removeBearerFromAuthorizationHeader(authorizationHeader);
            callingUsernameLowercase = jwtTokenUtil.getUsernameFromToken(jwt);
        }

        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final List<Post> posts = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, usernameLowercase) // Single user vertex
            .in(POST_POSTED_IN_USER_EDGE_LABEL) // All posts posted in the user's profile
            .order().by(COMMON_CREATED_AT_PROPERTY, desc)
            .flatMap(projectToPost(callingUsernameLowercase))
            .toList()
            .stream()
            .map(Post::new)
            .collect(Collectors.toList());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(posts);

        metricsUtil.addSuccessMetric(GET_NEW_PROFILE_POSTS_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_NEW_PROFILE_POSTS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve all the post headers for a Profile, most popular posts first.
     *
     * @param username A String containing the username of the User's Profile to return.
     *
     * @return HTTP 200 OK - If the Posts on the Profile were retrieved successfully. Body is an array of
     *                       {@link com.pylon.pylonservice.model.domain.Post Post}.
     *         HTTP 404 Not Found - If the Profile doesn't exist.
     */
    @GetMapping(value = "/profile/{username}/posts/popular")
    public ResponseEntity<?> getPopularProfilePosts(
        @RequestHeader(value = "Authorization", required = false) final String authorizationHeader,
        @PathVariable final String username) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_POPULAR_PROFILE_POSTS_METRIC_NAME);
        final String usernameLowercase = username.toLowerCase();

        String callingUsernameLowercase = INVALID_USERNAME_VALUE;
        if (authorizationHeader != null) {
            final String jwt = JwtTokenUtil.removeBearerFromAuthorizationHeader(authorizationHeader);
            callingUsernameLowercase = jwtTokenUtil.getUsernameFromToken(jwt);
        }

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

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(posts);

        metricsUtil.addSuccessMetric(GET_POPULAR_PROFILE_POSTS_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_POPULAR_PROFILE_POSTS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to update a User's public profile data. This is an idempotent operation, so all fields must be included. For
     * any fields which should not be present on the User's profile, send an empty string.
     *
     * @param authorizationHeader A request header with key "Authorization" and body including a jwt like "Bearer {jwt}"
     * @param updateProfileRequest A JSON object containing the public Profile data to update like
     *                             {
     *                                 "userAvatarFilename": "exampleAvatarFilename",
     *                                 "userBannerFilename": "exampleBannerFilename",
     *                                 "userBio": "exampleBio",
     *                                 "userLocation": "exampleLocation",
     *                                 "userFacebookUrl": "exampleFacebookUrl",
     *                                 "userTwitterUrl": "exampleTwitterUrl",
     *                                 "userInstagramUrl": "exampleInstagramUrl",
     *                                 "userTwitchUrl": "exampleTwitchUrl",
     *                                 "userYoutubeUrl": "exampleYoutubeUrl",
     *                                 "userTiktokUrl": "exampleTiktokUrl",
     *                                 "userDiscordUrl": "exampleDiscordUrl",
     *                                 "userWebsiteUrl": "exampleWebsiteUrl"
     *                             }
     *
     * @return HTTP 200 OK - If the User's public Profile data was updated successfully.
     *         HTTP 401 Unauthorized - If the User isn't authenticated.
     *         HTTP 422 Unprocessable Entity - If the UpdateProfileRequest is not valid.
     */
    @PutMapping(value = "/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader(value = "Authorization") final String authorizationHeader,
                                           @RequestBody final UpdateProfileRequest updateProfileRequest) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(PUT_PROFILE_METRIC_NAME);

        if (!updateProfileRequest.isValid()) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        final String jwt = JwtTokenUtil.removeBearerFromAuthorizationHeader(authorizationHeader);
        final String username = jwtTokenUtil.getUsernameFromToken(jwt);

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(HttpStatus.OK);
        wG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username)
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

        metricsUtil.addSuccessMetric(PUT_PROFILE_METRIC_NAME);
        metricsUtil.addLatencyMetric(PUT_PROFILE_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }
}
