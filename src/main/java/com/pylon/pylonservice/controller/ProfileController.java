package com.pylon.pylonservice.controller;

import com.pylon.pylonservice.model.requests.UpdateProfileRequest;
import com.pylon.pylonservice.util.JwtTokenUtil;
import com.pylon.pylonservice.util.MetricsUtil;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
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

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.pylon.pylonservice.constants.GraphConstants.COMMON_CREATED_AT_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.POST_POSTED_IN_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_AVATAR_IMAGE_ID_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_BANNER_IMAGE_ID_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_BIO_PROPERTY;
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
import static org.apache.tinkerpop.gremlin.process.traversal.Order.desc;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold;
import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.single;

@RestController
public class ProfileController {
    private static final String GET_PROFILE_METRIC_NAME = "GetProfile";
    private static final String GET_MY_PROFILE_METRIC_NAME = "GetMyProfile";
    private static final String GET_PROFILE_POSTS_METRIC_NAME = "GetProfilePosts";
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
     * @return HTTP 200 OK - If the User's public profile data was retrieved successfully.
     *         HTTP 404 Not Found - If the User doesn't exist.
     */
    @GetMapping(value = "/profile/{username}")
    public ResponseEntity<?> getProfile(@PathVariable final String username) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_PROFILE_METRIC_NAME);

        final Map<Object, Object> profile;
        try {
            profile = rG
                .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username)
                .valueMap().by(unfold())
                .next();
        } catch (final NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

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
     * @return HTTP 200 OK - If the User's public profile data was retrieved successfully.
     *              HTTP 401 Unauthorized - If the User isn't authenticated.
     */
    @GetMapping(value = "/myProfile")
    public ResponseEntity<?> getMyProfile(@RequestHeader(value = "Authorization") final String authorizationHeader) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_MY_PROFILE_METRIC_NAME);

        final String jwt = JwtTokenUtil.removeBearerFromAuthorizationHeader(authorizationHeader);
        final String username = jwtTokenUtil.getUsernameFromToken(jwt);

        final Map<Object, Object> profile = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username)
            .valueMap().by(unfold())
            .next();

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(profile);

        metricsUtil.addSuccessMetric(GET_MY_PROFILE_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_MY_PROFILE_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve all the post headers for a Profile.
     *
     * @param username A String containing the username of the User's Profile to return.
     *
     * @return HTTP 200 OK - If the Posts on the Profile were retrieved successfully.
     *         HTTP 404 Not Found - If the Profile doesn't exist.
     */
    @GetMapping(value = "/profile/{username}/posts")
    public ResponseEntity<?> getProfilePosts(@PathVariable final String username) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(GET_PROFILE_POSTS_METRIC_NAME);

        if (!rG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username).hasNext()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final List<Map<Object, Object>> posts = rG
            .V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username) // Single user vertex
            .in(POST_POSTED_IN_USER_EDGE_LABEL) // All posts posted in the user's profile
            .order().by(COMMON_CREATED_AT_PROPERTY, desc)
            .elementMap()
            .toList();

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(posts);

        metricsUtil.addSuccessMetric(GET_PROFILE_POSTS_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_PROFILE_POSTS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     *
     * @param authorizationHeader A request header with key "Authorization" and body including a jwt like "Bearer {jwt}"
     * @param updateProfileRequest A JSON object containing the public Profile data to update like
     *                             {
     *                                 "userAvatarImageId": "exampleAvatarImageId",
     *                                 "userBannerImageId": "exampleBannerImageId",
     *                                 "userBio": "exampleBio",
     *                                 "userLocation": "exampleLocation",
     *                                 "userFacebookUrl": "exampleFacebookUrl",
     *                                 "userTwitterUrl": "exampleTwitterUrl",
     *                                 "userInstagramUrl": "exampleInstagramUrl",
     *                                 "userTwitchUrl": "exampleTwitchUrl",
     *                                 "userYoutubeUrl": "exampleYoutubeUrl",
     *                                 "userWebsiteUrl": "exampleTiktokUrl"
     *                             }
     *                             If a field is not included in the JSON object, it is not updated.
     *
     * @return HTTP 200 OK - If the User's public Profile data was updated successfully.
     *         HTTP 401 Unauthorized - If the User isn't authenticated.
     */
    @PutMapping(value = "/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader(value = "Authorization") final String authorizationHeader,
                                           @RequestBody final UpdateProfileRequest updateProfileRequest) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(PUT_PROFILE_METRIC_NAME);

        final String jwt = JwtTokenUtil.removeBearerFromAuthorizationHeader(authorizationHeader);
        final String username = jwtTokenUtil.getUsernameFromToken(jwt);

        updateProfileWithDataFromRequest(username, updateProfileRequest);

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(HttpStatus.OK);

        metricsUtil.addSuccessMetric(PUT_PROFILE_METRIC_NAME);
        metricsUtil.addLatencyMetric(PUT_PROFILE_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    private void updateProfileWithDataFromRequest(final String username,
                                                  final UpdateProfileRequest updateProfileRequest) {
        GraphTraversal graphTraversal =
            wG.V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username);

        final String userAvatarImageId = updateProfileRequest.getUserAvatarImageId();
        if (userAvatarImageId != null) {
            graphTraversal = graphTraversal.property(single, USER_AVATAR_IMAGE_ID_PROPERTY, userAvatarImageId);
        }

        final String userBannerImageId = updateProfileRequest.getUserBannerImageId();
        if (userBannerImageId != null) {
            graphTraversal = graphTraversal.property(single, USER_BANNER_IMAGE_ID_PROPERTY, userBannerImageId);
        }

        final String userBio = updateProfileRequest.getUserBio();
        if (userBio != null) {
            graphTraversal = graphTraversal.property(single, USER_BIO_PROPERTY, userBio);
        }

        final String userLocation = updateProfileRequest.getUserLocation();
        if (userLocation != null) {
            graphTraversal = graphTraversal.property(single, USER_LOCATION_PROPERTY, userLocation);
        }

        final String userFacebookUrl = updateProfileRequest.getUserFacebookUrl();
        if (userFacebookUrl != null) {
            graphTraversal = graphTraversal.property(single, USER_FACEBOOK_URL_PROPERTY, userFacebookUrl);
        }

        final String userTwitterUrl = updateProfileRequest.getUserTwitterUrl();
        if (userTwitterUrl != null) {
            graphTraversal = graphTraversal.property(single, USER_TWITTER_URL_PROPERTY, userTwitterUrl);
        }

        final String userInstagramUrl = updateProfileRequest.getUserInstagramUrl();
        if (userInstagramUrl != null) {
            graphTraversal = graphTraversal.property(single, USER_INSTAGRAM_URL_PROPERTY, userInstagramUrl);
        }

        final String userTwitchUrl = updateProfileRequest.getUserTwitchUrl();
        if (userTwitchUrl != null) {
            graphTraversal = graphTraversal.property(single, USER_TWITCH_URL_PROPERTY, userTwitchUrl);
        }

        final String userYoutubeUrl = updateProfileRequest.getUserYoutubeUrl();
        if (userYoutubeUrl != null) {
            graphTraversal = graphTraversal.property(single, USER_YOUTUBE_URL_PROPERTY, userYoutubeUrl);
        }

        final String userTiktokUrl = updateProfileRequest.getUserTiktokUrl();
        if (userTiktokUrl != null) {
            graphTraversal = graphTraversal.property(single, USER_TIKTOK_URL_PROPERTY, userTiktokUrl);
        }

        final String userWebsiteUrl = updateProfileRequest.getUserWebsiteUrl();
        if (userWebsiteUrl != null) {
            graphTraversal = graphTraversal.property(single, USER_WEBSITE_URL_PROPERTY, userWebsiteUrl);
        }

        graphTraversal.iterate();
    }
}
