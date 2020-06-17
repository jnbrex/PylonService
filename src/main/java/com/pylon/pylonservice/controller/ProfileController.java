package com.pylon.pylonservice.controller;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.pylon.pylonservice.model.responses.ProfileResponse;
import com.pylon.pylonservice.model.tables.Profile;
import com.pylon.pylonservice.model.tables.UsernameUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileController {
    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    /**
     * Call to retrieve a User's public profile data.
     *
     * @param username A String containing the username of the User's profile to return
     *
     * @return HTTP 200 OK - If the User's public profile data was retrieved successfully.
     *         HTTP 404 Not Found - If the User doesn't exist.
     */
    @GetMapping(value = "/profile/{username}")
    public ResponseEntity<?> profile(@PathVariable final String username) {
        final UsernameUser usernameUser = dynamoDBMapper.load(UsernameUser.class, username);

        if (usernameUser == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().body(
            ProfileResponse.builder()
                .profile(dynamoDBMapper.load(Profile.class, usernameUser.getUserId()))
                .build()
        );
    }
}
