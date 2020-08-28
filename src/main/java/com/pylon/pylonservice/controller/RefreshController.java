package com.pylon.pylonservice.controller;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.pylon.pylonservice.model.tables.Refresh;
import com.pylon.pylonservice.services.AccessTokenUserDetailsService;
import com.pylon.pylonservice.services.AccessTokenService;
import com.pylon.pylonservice.util.MetricsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

import static com.pylon.pylonservice.constants.AuthenticationConstants.ACCESS_TOKEN_COOKIE_NAME;
import static com.pylon.pylonservice.constants.AuthenticationConstants.REFRESH_TOKEN_COOKIE_NAME;
import static com.pylon.pylonservice.constants.TimeConstants.ONE_DAY_IN_SECONDS;

@RestController
public class RefreshController {
    private static final String REFRESH_METRIC_NAME = "Refresh";

    @Autowired
    private DynamoDBMapper dynamoDBMapper;
    @Autowired
    private AccessTokenService accessTokenService;
    @Autowired
    private AccessTokenUserDetailsService userDetailsService;
    @Autowired
    private MetricsUtil metricsUtil;

    /**
     * Call to authenticate a User.
     *
     * @param refreshToken A cookie with name "refreshToken"
     *
     * @return HTTP 200 OK - Responds with an accessToken Set-Cookie header
     *         HTTP 404 Not Found - If the refresh token does not exist.
     */
    @PostMapping(value = "/refresh")
    public ResponseEntity<?> refresh(
        @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) final String refreshToken,
        final HttpServletResponse response) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(REFRESH_METRIC_NAME);

        final Refresh refresh = dynamoDBMapper.load(Refresh.class, refreshToken);

        if (refresh == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(refresh.getUsername());

        response.addCookie(accessTokenService.createCookie(
                ACCESS_TOKEN_COOKIE_NAME,
                accessTokenService.generateAccessTokenForUser(userDetails),
                ONE_DAY_IN_SECONDS
        ));

        metricsUtil.addSuccessMetric(REFRESH_METRIC_NAME);
        metricsUtil.addLatencyMetric(REFRESH_METRIC_NAME, System.nanoTime() - startTime);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
