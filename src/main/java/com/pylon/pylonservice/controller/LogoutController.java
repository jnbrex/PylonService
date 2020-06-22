package com.pylon.pylonservice.controller;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.pylon.pylonservice.model.requests.RefreshRequest;
import com.pylon.pylonservice.model.tables.Refresh;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogoutController {
    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    /**
     * Call to logout a User.
     *
     * @param refreshRequest A JSON body optionally containing a refresh token like
     *                       {
     *                           "refreshToken": "exampleRefreshToken"
     *                       }
     *
     *                       Empty JSON body is also valid:
     *                       {
     *
     *                       }
     *
     * @return HTTP 200 OK - If the refresh token was deleted successfully or does not exist.
     */
    @PostMapping(value = "/logout")
    public ResponseEntity<?> logout(@RequestBody final RefreshRequest refreshRequest) {
        final String refreshToken = refreshRequest.getRefreshToken();

        if (refreshToken != null) {
            dynamoDBMapper.delete(
                Refresh.builder()
                    .refreshToken(refreshToken)
                    .build()
            );
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
