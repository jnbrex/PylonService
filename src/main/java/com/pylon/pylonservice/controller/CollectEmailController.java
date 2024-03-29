package com.pylon.pylonservice.controller;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.pylon.pylonservice.model.requests.CollectEmailRequest;
import com.pylon.pylonservice.model.tables.CollectedEmail;
import com.pylon.pylonservice.services.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CollectEmailController {
    private static final String COLLECT_EMAIL_METRIC_NAME = "CollectEmail";

    @Autowired
    private DynamoDBMapper dynamoDBMapper;
    @Autowired
    private MetricsService metricsService;

    /**
     * Call to save an email address for future communications.
     *
     * @param collectEmailRequest A JSON object like
     *                            {
     *                                "email": "jason@gmail.com"
     *                            }
     * @return HTTP 200 OK - String body like "Saved email jason@gmail.com".
     */
    @PostMapping(value = "/collectemail")
    public ResponseEntity<?> collectEmail(@RequestBody final CollectEmailRequest collectEmailRequest) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(COLLECT_EMAIL_METRIC_NAME);

        final String email = collectEmailRequest.getEmail();

        final CollectedEmail collectedEmail = CollectedEmail.builder()
            .email(email)
            .build();

        dynamoDBMapper.save(collectedEmail);

        final ResponseEntity<?> responseEntity = ResponseEntity.ok(String.format("Saved email %s", email));

        metricsService.addSuccessMetric(COLLECT_EMAIL_METRIC_NAME);
        metricsService.addLatencyMetric(COLLECT_EMAIL_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }
}
