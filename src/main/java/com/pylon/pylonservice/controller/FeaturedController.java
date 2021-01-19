package com.pylon.pylonservice.controller;

import com.pylon.pylonservice.model.domain.Profile;
import com.pylon.pylonservice.model.domain.Shard;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static com.pylon.pylonservice.constants.AuthenticationConstants.ACCESS_TOKEN_COOKIE_NAME;
import static com.pylon.pylonservice.constants.GraphConstants.INVALID_USERNAME_VALUE;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_FEATURED_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_VERTEX_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FEATURED_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERTEX_LABEL;
import static com.pylon.pylonservice.model.domain.Profile.projectToProfile;
import static com.pylon.pylonservice.model.domain.Shard.projectToShard;

@RestController
public class FeaturedController {
    private static final String GET_FEATURED_SHARDS_METRIC_NAME = "GetFeaturedShards";
    private static final String GET_FEATURED_PROFILES_METRIC_NAME = "GetFeaturedProfiles";

    @Qualifier("reader")
    @Autowired
    private GraphTraversalSource rG;
    @Autowired
    private AccessTokenService accessTokenService;
    @Autowired
    private MetricsService metricsService;

    /**
     * Call to retrieve the featured shards.
     * @param accessToken A cookie with name "accessToken" issued by PylonService.
     * @return A List of {@link Shard}.
     */
    @GetMapping("/featured/shards")
    public ResponseEntity<?> getFeaturedShards(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_FEATURED_SHARDS_METRIC_NAME);

        final String callingUsernameLowercase;
        try {
            callingUsernameLowercase = accessTokenService.getUsernameFromAccessTokenOrDefaultIfNull(
                accessToken, INVALID_USERNAME_VALUE
            );
        } catch (final ExpiredJwtException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        final List<Shard> featuredShards = rG.V()
            .has(SHARD_VERTEX_LABEL, SHARD_FEATURED_PROPERTY, true)
            .flatMap(projectToShard(callingUsernameLowercase))
            .toList()
            .stream()
            .map(Shard::new)
            .collect(Collectors.toList());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(featuredShards);

        metricsService.addSuccessMetric(GET_FEATURED_SHARDS_METRIC_NAME);
        metricsService.addLatencyMetric(GET_FEATURED_SHARDS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to retrieve the featured profiles.
     * @param accessToken A cookie with name "accessToken" issued by PylonService.
     * @return A List of {@link Profile}.
     */
    @GetMapping("/featured/profiles")
    public ResponseEntity<?> getFeaturedProfiles(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_FEATURED_PROFILES_METRIC_NAME);

        final String callingUsernameLowercase;
        try {
            callingUsernameLowercase = accessTokenService.getUsernameFromAccessTokenOrDefaultIfNull(
                accessToken, INVALID_USERNAME_VALUE
            );
        } catch (final ExpiredJwtException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        final List<Profile> featuredProfiles = rG.V()
            .has(USER_VERTEX_LABEL, USER_FEATURED_PROPERTY, true)
            .flatMap(projectToProfile(callingUsernameLowercase))
            .toList()
            .stream()
            .map(Profile::new)
            .collect(Collectors.toList());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(featuredProfiles);

        metricsService.addSuccessMetric(GET_FEATURED_PROFILES_METRIC_NAME);
        metricsService.addLatencyMetric(GET_FEATURED_PROFILES_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }
}
