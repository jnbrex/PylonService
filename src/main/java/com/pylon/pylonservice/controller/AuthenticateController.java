package com.pylon.pylonservice.controller;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.pylon.pylonservice.model.requests.AuthenticateRequest;
import com.pylon.pylonservice.model.responses.JwtResponse;
import com.pylon.pylonservice.model.tables.EmailUser;
import com.pylon.pylonservice.model.tables.Refresh;
import com.pylon.pylonservice.model.tables.User;
import com.pylon.pylonservice.services.JwtUserDetailsService;
import com.pylon.pylonservice.util.JwtTokenUtil;
import com.pylon.pylonservice.util.MetricsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AuthenticateController {
    private static final String AUTHENTICATE_METRIC_NAME = "Authenticate";

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private JwtUserDetailsService userDetailsService;
    @Autowired
    private DynamoDBMapper dynamoDBMapper;
    @Autowired
    private MetricsUtil metricsUtil;

    /**
     * Call to authenticate a User.
     *
     * @param authenticateRequest A JSON object containing the username and password of the User who is attempting to
     *                              authenticate like
     *                              {
     *                                  "usernameOrEmail": "exampleUsername",
     *                                  "password": "examplePassword"
     *                              }
     *
     *                              or
     *
     *                              {
     *                                  "usernameOrEmail": "exampleEmail@email.com",
     *                                  "password": "examplePassword"
     *                              }
     *
     * @return HTTP 200 OK - If the User was authenticated successfully, a JSON object like
     *                       {
     *                           "jwtToken": "exampleJwtToken",
     *                           "refreshToken": "exampleRefreshToken"
     *                       }
     *         HTTP 401 Unauthorized - If the User was not authenticated successfully.
     */
    @PostMapping(value = "/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody final AuthenticateRequest authenticateRequest) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(AUTHENTICATE_METRIC_NAME);

        String usernameOrEmail = authenticateRequest.getUsernameOrEmail();
        if (usernameOrEmail.contains("@")) {
            final EmailUser emailUser = dynamoDBMapper.load(EmailUser.class, usernameOrEmail);
            if (emailUser == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            final User user = dynamoDBMapper.load(User.class, emailUser.getUsername());
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            usernameOrEmail = user.getUsername();
        }

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                usernameOrEmail,
                authenticateRequest.getPassword()
            )
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(usernameOrEmail);
        final String jwtToken = jwtTokenUtil.generateJwtForUser(userDetails);

        final Refresh refresh = Refresh.builder()
            .refreshToken(UUID.randomUUID().toString())
            .username(userDetails.getUsername())
            .build();

        dynamoDBMapper.save(refresh);

        final ResponseEntity<?> responseEntity = ResponseEntity.ok(
            JwtResponse.builder()
                .jwtToken(jwtToken)
                .refreshToken(refresh.getRefreshToken())
                .build()
        );

        metricsUtil.addSuccessMetric(AUTHENTICATE_METRIC_NAME);
        metricsUtil.addLatencyMetric(AUTHENTICATE_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }
}
