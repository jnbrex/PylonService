package com.pylon.pylonservice.controller;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.pylon.pylonservice.model.requests.CollectEmailRequest;
import com.pylon.pylonservice.model.tables.CollectedEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CollectEmailController {
    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    @PostMapping(value = "/collectemail")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody final CollectEmailRequest collectEmailRequest) {
        final String email = collectEmailRequest.getEmail();

        final CollectedEmail collectedEmail = CollectedEmail.builder()
            .email(email)
            .build();

        dynamoDBMapper.save(collectedEmail);

        return ResponseEntity.ok(String.format("Saved email %s", email));
    }
}