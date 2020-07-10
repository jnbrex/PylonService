package com.pylon.pylonservice.controller;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.pylon.pylonservice.model.responses.JwtResponse;
import com.pylon.pylonservice.model.requests.RefreshRequest;
import com.pylon.pylonservice.model.tables.Refresh;
import com.pylon.pylonservice.services.JwtUserDetailsService;
import com.pylon.pylonservice.util.JwtTokenUtil;
import com.pylon.pylonservice.util.MetricsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RefreshController {
    private static final String REFRESH_METRIC_NAME = "Refresh";

    @Autowired
    private DynamoDBMapper dynamoDBMapper;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private JwtUserDetailsService userDetailsService;
    @Autowired
    private MetricsUtil metricsUtil;

    /**
     * Call to authenticate a User.
     *
     * @param refreshRequest A JSON body containing a refresh token like
     *                       {
     *                           "refreshToken": "exampleRefreshToken"
     *                       }
     *
     * @return HTTP 200 OK - If the refresh token exists with a JSON body containing a Jwt token like
     *                       {
     *                           "jwtToken": "exampleJwtToken"
     *                       }
     *         HTTP 404 Not Found - If the refresh token does not exist.
     */
    @PostMapping(value = "/refresh")
    public ResponseEntity<?> refresh(@RequestBody final RefreshRequest refreshRequest) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(REFRESH_METRIC_NAME);

        final Refresh refresh = dynamoDBMapper.load(Refresh.class, refreshRequest.getRefreshToken());

        if (refresh == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(refresh.getUsername());
        final String jwtToken = jwtTokenUtil.generateJwtForUser(userDetails);

        final ResponseEntity<?> responseEntity = ResponseEntity.ok(
            JwtResponse.builder()
                .jwtToken(jwtToken)
                .build()
        );

        metricsUtil.addSuccessMetric(REFRESH_METRIC_NAME);
        metricsUtil.addLatencyMetric(REFRESH_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }
}
