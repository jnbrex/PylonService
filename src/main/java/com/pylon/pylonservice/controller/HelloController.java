package com.pylon.pylonservice.controller;

import com.pylon.pylonservice.services.AccessTokenService;
import com.pylon.pylonservice.services.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.pylon.pylonservice.constants.AuthenticationConstants.ACCESS_TOKEN_COOKIE_NAME;

@RestController
public class HelloController {
    private static final String HELLO_METRIC_NAME = "Hello";

    @Autowired
    private AccessTokenService accessTokenService;
    @Autowired
    private MetricsService metricsService;

    /**
     * Call to receive a hello message personalized to the calling User.
     *
     * @param accessToken A cookie with name "accessToken" issued by PylonService.
     * @return HTTP 200 OK - String like "Hello Jason!".
     */
    @GetMapping("/hello")
    public ResponseEntity<?> hello(@CookieValue(name = ACCESS_TOKEN_COOKIE_NAME) final String accessToken) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(HELLO_METRIC_NAME);

        final String response = String.format(
            "Hello %s!", accessTokenService.getUsernameFromAccessToken(accessToken)
        );

        final ResponseEntity<?> responseEntity = ResponseEntity.ok(response);

        metricsService.addSuccessMetric(HELLO_METRIC_NAME);
        metricsService.addLatencyMetric(HELLO_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }
}
