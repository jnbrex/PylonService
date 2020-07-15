package com.pylon.pylonservice.controller;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.pylon.pylonservice.model.requests.RefreshRequest;
import com.pylon.pylonservice.model.tables.Refresh;
import com.pylon.pylonservice.util.MetricsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
     * @param refreshRequest A JSON object optionally containing a refresh token like
     *                       {
     *                           "refreshToken": "exampleRefreshToken"
     *                       }
     *
     *                       Empty JSON object is also valid:
     *                       {
     *
     *                       }
     *
     * @return HTTP 200 OK - If the refresh token was deleted successfully or does not exist.
     */
    @PostMapping(value = "/logout")
    public ResponseEntity<?> logout(@RequestBody final RefreshRequest refreshRequest) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(LOGOUT_METRIC_NAME);

        final String refreshToken = refreshRequest.getRefreshToken();

        if (refreshToken != null) {
            dynamoDBMapper.delete(
                Refresh.builder()
                    .refreshToken(refreshToken)
                    .build()
            );
        }

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(HttpStatus.OK);

        metricsUtil.addSuccessMetric(LOGOUT_METRIC_NAME);
        metricsUtil.addLatencyMetric(LOGOUT_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }
}
