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

import java.util.Map;
import java.util.NoSuchElementException;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold;
import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.single;

@RestController
public class ProfileController {
    private static final String GET_PROFILE_METRIC_NAME = "GetProfile";
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
            profile =
                rG.V().has("user", "username", username) // user vertex of user with username: {username}
                    .out("has") // profile vertex of user with username: {username}
                    .valueMap().by(unfold()) // values of all properties on profile, unfolded
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
     *
     * @param authorizationHeader A request header with key "Authorization" and body including a jwt like "Bearer {jwt}"
     * @param updateProfileRequest A JSON object containing the public Profile data to update like
     *                             {
     *                                 "avatarImageId": "exampleAvatarImageId",
     *                                 "bannerImageId": "exampleBannerImageId",
     *                                 "bio": "exampleBio",
     *                                 "facebookUrl": "exampleFacebookUrl",
     *                                 "twitterUrl": "exampleTwitterUrl",
     *                                 "instagramUrl": "exampleInstagramUrl",
     *                                 "twitchUrl": "exampleTwitchUrl",
     *                                 "youtubeUrl": "exampleYoutubeUrl",
     *                                 "tiktokUrl": "exampleTiktokUrl"
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
            wG.V().has("user", "username", username) // user vertex of user with username: {username}
            .out("has"); // profile vertex of user with username: {username}

        final String avatarImageId = updateProfileRequest.getAvatarImageId();
        if (avatarImageId != null) {
            graphTraversal = graphTraversal.property(single, "avatarImageId", avatarImageId);
        }

        final String bannerImageId = updateProfileRequest.getBannerImageId();
        if (bannerImageId != null) {
            graphTraversal = graphTraversal.property(single, "bannerImageId", bannerImageId);
        }

        final String bio = updateProfileRequest.getBio();
        if (bio != null) {
            graphTraversal = graphTraversal.property(single, "bio", bio);
        }

        final String facebookUrl = updateProfileRequest.getFacebookUrl();
        if (facebookUrl != null) {
            graphTraversal = graphTraversal.property(single, "facebookUrl", facebookUrl);
        }

        final String twitterUrl = updateProfileRequest.getTwitterUrl();
        if (twitterUrl != null) {
            graphTraversal = graphTraversal.property(single, "twitterUrl", twitterUrl);
        }

        final String instagramUrl = updateProfileRequest.getInstagramUrl();
        if (instagramUrl != null) {
            graphTraversal = graphTraversal.property(single, "instagramUrl", instagramUrl);
        }

        final String twitchUrl = updateProfileRequest.getTwitchUrl();
        if (twitchUrl != null) {
            graphTraversal = graphTraversal.property(single, "twitchUrl", twitchUrl);
        }

        final String youtubeUrl = updateProfileRequest.getYoutubeUrl();
        if (youtubeUrl != null) {
            graphTraversal = graphTraversal.property(single, "youtubeUrl", youtubeUrl);
        }

        final String tiktokUrl = updateProfileRequest.getTiktokUrl();
        if (tiktokUrl != null) {
            graphTraversal = graphTraversal.property(single, "tiktokUrl", tiktokUrl);
        }

        graphTraversal.iterate();
    }
}
