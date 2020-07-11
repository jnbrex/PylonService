package com.pylon.pylonservice.controller;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.pylon.pylonservice.model.requests.UpdateProfileRequest;
import com.pylon.pylonservice.model.tables.Profile;
import com.pylon.pylonservice.model.tables.UsernameUser;
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

import java.util.Map;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold;

@RestController
public class ProfileController {
    private static final String GET_PROFILE_METRIC_NAME = "GetProfile";
    private static final String PUT_PROFILE_METRIC_NAME = "PutProfile";

    @Autowired
    private DynamoDBMapper dynamoDBMapper;
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

        final UsernameUser usernameUser = dynamoDBMapper.load(UsernameUser.class, username);

        if (usernameUser == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Map<Object, Object> profile =
            rG.V().has("user", "username", username) // user vertex of user with username: {username}
            .out("has") // profile vertex of user with username: {username}
            .valueMap().by(unfold()) // values of all properties on profile, unfolded
            .next();

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(profile);

        metricsUtil.addSuccessMetric(GET_PROFILE_METRIC_NAME);
        metricsUtil.addLatencyMetric(GET_PROFILE_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     *
     * @param username A String containing the username of the User's public Profile data to update.
     * @param authorizationHeader A request header with key "Authorization" and body including a jwt like "Bearer {jwt}"
     * @param updateProfileRequest A JSON body containing the public Profile data to update like
     *                             {
     *                                 "avatarImageId": "exampleAvatarImageId",
     *                                 "bannerImageId": "exampleBannerImageId",
     *                                 "bio": "exampleBio",
     *                                 "facebookUrl": "exampleFacebookUrl",
     *                                 "twitterUrl": "exampleTwitterUrl",
     *                                 "instagramUrl": "exampleInstagramUrl",
     *                                 "twitchUrl": "exampleTwitchUrl",
     *                                 "youtubeUrl": "exampleYoutubeUrl",
     *                             }
     *                             If a field is not included in the JSON object, it is not updated.
     *
     * @return HTTP 200 OK - If the User's public Profile data was updated successfully.
     *         HTTP 403 Forbidden - If the User if attempting to update another User's public Profile data.
     *         HTTP 404 Not Found - If the User or Profile doesn't exist.
     */
    @PutMapping(value = "/profile/{username}")
    public ResponseEntity<?> updateProfile(@PathVariable final String username,
                                           @RequestHeader(value = "Authorization") final String authorizationHeader,
                                           @RequestBody final UpdateProfileRequest updateProfileRequest) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(PUT_PROFILE_METRIC_NAME);

        final String jwt = JwtTokenUtil.removeBearerFromAuthorizationHeader(authorizationHeader);
        final String jwtUsername = jwtTokenUtil.getUsernameFromToken(jwt);

        if (!username.equals(jwtUsername)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        final UsernameUser usernameUser = dynamoDBMapper.load(UsernameUser.class, username);
        if (usernameUser == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final Profile profile = dynamoDBMapper.load(Profile.class, usernameUser.getUserId());
        if (profile == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        updateProfileWithDataFromRequest(profile, updateProfileRequest);
        dynamoDBMapper.save(profile);

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(HttpStatus.OK);

        metricsUtil.addSuccessMetric(PUT_PROFILE_METRIC_NAME);
        metricsUtil.addLatencyMetric(PUT_PROFILE_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    private void updateProfileWithDataFromRequest(final Profile profile,
                                                  final UpdateProfileRequest updateProfileRequest) {
        final String avatarImageId = updateProfileRequest.getAvatarImageId();
        if (avatarImageId != null) {
            profile.setAvatarImageId(avatarImageId);
        }

        final String bannerImageId = updateProfileRequest.getBannerImageId();
        if (bannerImageId != null) {
            profile.setBannerImageId(bannerImageId);
        }

        final String bio = updateProfileRequest.getBio();
        if (bio != null) {
            profile.setBio(bio);
        }

        final String facebookUrl = updateProfileRequest.getFacebookUrl();
        if (facebookUrl != null) {
            profile.setFacebookUrl(facebookUrl);
        }

        final String twitterUrl = updateProfileRequest.getTwitterUrl();
        if (twitterUrl != null) {
            profile.setTwitterUrl(twitterUrl);
        }

        final String instagramUrl = updateProfileRequest.getInstagramUrl();
        if (instagramUrl != null) {
            profile.setInstagramUrl(instagramUrl);
        }

        final String twitchUrl = updateProfileRequest.getTwitchUrl();
        if (twitchUrl != null) {
            profile.setTwitchUrl(twitchUrl);
        }

        final String youtubeUrl = updateProfileRequest.getYoutubeUrl();
        if (youtubeUrl != null) {
            profile.setYoutubeUrl(youtubeUrl);
        }
    }
}
