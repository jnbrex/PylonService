package com.pylon.pylonservice.controller;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.pylon.pylonservice.model.requests.JwtRequest;
import com.pylon.pylonservice.model.responses.JwtResponse;
import com.pylon.pylonservice.model.tables.Refresh;
import com.pylon.pylonservice.services.JwtUserDetailsService;
import com.pylon.pylonservice.util.JwtTokenUtil;
import com.pylon.pylonservice.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AuthenticateController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private JwtUserDetailsService userDetailsService;
    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    /**
     * Call to authenticate a User.
     *
     * @param authenticationRequest A JSON body containing the username and password of the User who is attempting to
     *                              authenticate like
     *                              {
     *                                  "username": "exampleUsername",
     *                                  "password": "examplePassword"
     *                              }
     *
     * @return HTTP 200 OK - If the User was authenticated successfully with a JSON body like
     *                       {
     *                           "jwtToken": "exampleJwtToken",
     *                           "refreshToken": "exampleRefreshToken"
     *                       }
     *         HTTP 401 Unauthorized - If the User was not authenticated successfully.
     */
    @PostMapping(value = "/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody final JwtRequest authenticationRequest) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                authenticationRequest.getUsername(),
                authenticationRequest.getPassword()
            )
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String jwtToken = jwtTokenUtil.generateJwtForUser(userDetails);

        final Refresh refresh = Refresh.builder()
            .refreshToken(UUID.randomUUID().toString())
            .userId(UserUtil.getUserIdForUsername(dynamoDBMapper, userDetails.getUsername()))
            .build();

        dynamoDBMapper.save(refresh);

        return ResponseEntity.ok(
            JwtResponse.builder()
                .jwtToken(jwtToken)
                .refreshToken(refresh.getRefreshToken())
                .build()
        );
    }
}