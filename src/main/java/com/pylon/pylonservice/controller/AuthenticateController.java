package com.pylon.pylonservice.controller;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.pylon.pylonservice.model.requests.auth.AuthenticateRequest;
import com.pylon.pylonservice.model.tables.EmailUser;
import com.pylon.pylonservice.model.tables.Refresh;
import com.pylon.pylonservice.model.tables.User;
import com.pylon.pylonservice.services.AccessTokenUserDetailsService;
import com.pylon.pylonservice.services.AccessTokenService;
import com.pylon.pylonservice.services.CookieService;
import com.pylon.pylonservice.services.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@RestController
public class AuthenticateController {
    private static final String AUTHENTICATE_METRIC_NAME = "Authenticate";

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private AccessTokenService accessTokenService;
    @Autowired
    private AccessTokenUserDetailsService accessTokenUserDetailsService;
    @Autowired
    private CookieService cookieService;
    @Autowired
    private DynamoDBMapper dynamoDBMapper;
    @Autowired
    private MetricsService metricsService;

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
     *         HTTP 400 Bad Request - If the request does not include an origin header.
     *         HTTP 401 Unauthorized - If the User was not authenticated successfully.
     */
    @PostMapping(value = "/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody final AuthenticateRequest authenticateRequest,
                                          final HttpServletRequest request,
                                          final HttpServletResponse response) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(AUTHENTICATE_METRIC_NAME);

        final String requestOrigin = request.getHeader("origin");
        if (requestOrigin == null) {
            return new ResponseEntity<>("Calls to /authenticate must include origin header", HttpStatus.BAD_REQUEST);
        }

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

        final UserDetails userDetails = accessTokenUserDetailsService.loadUserByUsername(username);

        final String refreshToken = UUID.randomUUID().toString();
        final Refresh refresh = Refresh.builder()
            .refreshToken(refreshToken)
            .username(userDetails.getUsername())
            .build();
        dynamoDBMapper.save(refresh);

        response.addCookie(
            cookieService.createRefreshTokenCookie(
                refreshToken,
                requestOrigin
            )
        );

        response.addCookie(
            cookieService.createAccessTokenCookie(
                accessTokenService.generateAccessTokenForUser(userDetails),
                requestOrigin
            )
        );

        metricsService.addSuccessMetric(AUTHENTICATE_METRIC_NAME);
        metricsService.addLatencyMetric(AUTHENTICATE_METRIC_NAME, System.nanoTime() - startTime);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
