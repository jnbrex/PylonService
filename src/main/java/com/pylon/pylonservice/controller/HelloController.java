package com.pylon.pylonservice.controller;

import com.pylon.pylonservice.util.JwtTokenUtil;
import com.pylon.pylonservice.util.MetricsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    private static final String HELLO_METRIC_NAME = "Hello";

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private MetricsUtil metricsUtil;

    /**
     * Call to receive a hello message personalized to the calling User.
     *
     * @param authorizationHeader A key-value header with key "Authorization" and value like "Bearer exampleJwtToken".
     * @return HTTP 200 OK - String like "Hello Jason!".
     */
    @GetMapping("/hello")
    public ResponseEntity<?> hello(@RequestHeader(value = "Authorization") final String authorizationHeader) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(HELLO_METRIC_NAME);

        final String jwt = JwtTokenUtil.removeBearerFromAuthorizationHeader(authorizationHeader);
        final String response = String.format("Hello %s!", jwtTokenUtil.getUsernameFromToken(jwt));

        final ResponseEntity<?> responseEntity = ResponseEntity.ok(response);

        metricsUtil.addSuccessMetric(HELLO_METRIC_NAME);
        metricsUtil.addLatencyMetric(HELLO_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }
}
