package com.pylon.pylonservice.controller;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.pylon.pylonservice.model.tables.Refresh;
import com.pylon.pylonservice.util.MetricsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.pylon.pylonservice.constants.AuthenticationConstants.REFRESH_TOKEN_COOKIE_NAME;

@RestController
public class LogoutController {
    private static final String LOGOUT_METRIC_NAME = "Logout";

    @Autowired
    private DynamoDBMapper dynamoDBMapper;
    @Autowired
    private MetricsUtil metricsUtil;

    /**
     * Call to logout a User.
     *
     * @param refreshToken A cookie with name "refreshToken"
     *
     * @return HTTP 200 OK - If the refresh token was deleted successfully or does not exist.
     */
    @PostMapping(value = "/logout")
    public ResponseEntity<?> logout(
        @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) final String refreshToken,
        final HttpServletRequest request,
        final HttpServletResponse response) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(LOGOUT_METRIC_NAME);

        if (refreshToken != null) {
            dynamoDBMapper.delete(
                Refresh.builder()
                    .refreshToken(refreshToken)
                    .build()
            );
        }

        for (final Cookie cookie : request.getCookies()) {
            cookie.setMaxAge(0);
            cookie.setValue("");
            cookie.setPath("/");
            response.addCookie(cookie);
        }

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(HttpStatus.OK);

        metricsUtil.addSuccessMetric(LOGOUT_METRIC_NAME);
        metricsUtil.addLatencyMetric(LOGOUT_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }
}
