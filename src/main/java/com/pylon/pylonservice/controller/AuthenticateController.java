package com.pylon.pylonservice.controller;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.pylon.pylonservice.model.requests.auth.AuthenticateRequest;
import com.pylon.pylonservice.model.tables.EmailUser;
import com.pylon.pylonservice.model.tables.Refresh;
import com.pylon.pylonservice.model.tables.User;
import com.pylon.pylonservice.services.AccessTokenUserDetailsService;
import com.pylon.pylonservice.services.AccessTokenService;
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

import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

import static com.pylon.pylonservice.constants.AuthenticationConstants.ACCESS_TOKEN_COOKIE_NAME;
import static com.pylon.pylonservice.constants.AuthenticationConstants.REFRESH_TOKEN_COOKIE_NAME;

@RestController
public class AuthenticateController {
    private static final String AUTHENTICATE_METRIC_NAME = "Authenticate";

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private AccessTokenService accessTokenUtil;
    @Autowired
    private AccessTokenUserDetailsService userDetailsService;
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
    public ResponseEntity<?> authenticate(@RequestBody final AuthenticateRequest authenticateRequest,
                                          final HttpServletResponse response) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(AUTHENTICATE_METRIC_NAME);

        final String usernameOrEmail = authenticateRequest.getUsernameOrEmail().toLowerCase();
        String username = usernameOrEmail;
        if (usernameOrEmail.contains("@")) {
            final EmailUser emailUser = dynamoDBMapper.load(EmailUser.class, usernameOrEmail);
            if (emailUser == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            final User user = dynamoDBMapper.load(User.class, emailUser.getUsername());
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            username = user.getUsername();
        }

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                username,
                authenticateRequest.getPassword()
            )
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        final String refreshToken = UUID.randomUUID().toString();
        final Refresh refresh = Refresh.builder()
            .refreshToken(refreshToken)
            .username(userDetails.getUsername())
            .build();
        dynamoDBMapper.save(refresh);

        response.addCookie(
            accessTokenUtil.createCookie(
                ACCESS_TOKEN_COOKIE_NAME,
                accessTokenUtil.generateAccessTokenForUser(userDetails)
            )
        );

        response.addCookie(accessTokenUtil.createCookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken));

        metricsUtil.addSuccessMetric(AUTHENTICATE_METRIC_NAME);
        metricsUtil.addLatencyMetric(AUTHENTICATE_METRIC_NAME, System.nanoTime() - startTime);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
