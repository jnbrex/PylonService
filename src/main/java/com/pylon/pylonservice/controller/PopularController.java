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
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_VERTEX_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FOLLOWS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FOLLOWS_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERTEX_LABEL;
import static com.pylon.pylonservice.model.domain.Profile.projectToProfile;
import static com.pylon.pylonservice.model.domain.Shard.projectToShard;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.inE;
import static org.apache.tinkerpop.gremlin.process.traversal.Order.desc;

@RestController
public class PopularController {
    private static final String GET_POPULAR_SHARDS_METRIC_NAME = "GetPopularShards";
    private static final String GET_POPULAR_PROFILES_METRIC_NAME = "GetPopularProfiles";

    @Qualifier("reader")
    @Autowired
    private GraphTraversalSource rG;
    @Autowired
    private AccessTokenService accessTokenService;
    @Autowired
    private MetricsService metricsService;

    @GetMapping("/popular/shards")
    public ResponseEntity<?> getPopularShards(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_POPULAR_SHARDS_METRIC_NAME);

        final String callingUsernameLowercase;
        try {
            callingUsernameLowercase = accessTokenService.getUsernameFromAccessTokenOrDefaultIfNull(
                accessToken, INVALID_USERNAME_VALUE
            );
        } catch (final ExpiredJwtException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        final List<Shard> popularShards = rG.V()
            .hasLabel(SHARD_VERTEX_LABEL)
            .order().by(inE(USER_FOLLOWS_SHARD_EDGE_LABEL).count(), desc)
            .limit(5)
            .flatMap(projectToShard(callingUsernameLowercase))
            .toList()
            .stream()
            .map(Shard::new)
            .collect(Collectors.toList());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(popularShards);

        metricsService.addSuccessMetric(GET_POPULAR_SHARDS_METRIC_NAME);
        metricsService.addLatencyMetric(GET_POPULAR_SHARDS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    @GetMapping("/popular/profiles")
    public ResponseEntity<?> getPopularProfiles(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME, required = false) final String accessToken) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_POPULAR_PROFILES_METRIC_NAME);

        final String callingUsernameLowercase;
        try {
            callingUsernameLowercase = accessTokenService.getUsernameFromAccessTokenOrDefaultIfNull(
                accessToken, INVALID_USERNAME_VALUE
            );
        } catch (final ExpiredJwtException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        final List<Profile> popularProfiles = rG.V()
            .hasLabel(USER_VERTEX_LABEL)
            .order().by(inE(USER_FOLLOWS_USER_EDGE_LABEL).count(), desc)
            .limit(5)
            .flatMap(projectToProfile(callingUsernameLowercase))
            .toList()
            .stream()
            .map(Profile::new)
            .collect(Collectors.toList());

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(popularProfiles);

        metricsService.addSuccessMetric(GET_POPULAR_PROFILES_METRIC_NAME);
        metricsService.addLatencyMetric(GET_POPULAR_PROFILES_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }
}
