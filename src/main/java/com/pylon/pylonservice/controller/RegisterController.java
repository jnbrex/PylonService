package com.pylon.pylonservice.controller;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTransactionWriteExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.TransactionWriteRequest;
import com.pylon.pylonservice.model.requests.auth.RegisterRequest;
import com.pylon.pylonservice.model.responses.RegisterResponse;
import com.pylon.pylonservice.model.tables.EmailUser;
import com.pylon.pylonservice.model.tables.User;
import com.pylon.pylonservice.services.MetricsService;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

import static com.pylon.pylonservice.constants.GraphConstants.COMMON_CREATED_AT_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_AVATAR_FILENAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_BANNER_FILENAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_BIO_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FACEBOOK_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_INSTAGRAM_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_LOCATION_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_TIKTOK_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_TWITCH_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_TWITTER_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FRIENDLY_NAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_USERNAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERIFIED_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERTEX_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_WEBSITE_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_YOUTUBE_URL_PROPERTY;
import static org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.single;

@RestController
public class RegisterController {
    private static final String REGISTER_METRIC_NAME = "Register";
    private static final String USERNAME_DOES_NOT_EXIST_CONDITION = "attribute_not_exists(username)";
    private static final String EMAIL_DOES_NOT_EXIST_CONDITION = "attribute_not_exists(email)";
    private static final String EMPTY_STRING = "";

    @Autowired
    private DynamoDBMapper dynamoDBMapper;
    @Qualifier("writer")
    @Autowired
    private GraphTraversalSource wG;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MetricsService metricsService;

    /**
     * Call to register a User.
     *
     * @param registerRequest A JSON object containing the username, password, and email address of the User who is
     *                        attempting to register like
     *                        {
     *                            "username": "exampleUsername",
     *                            "password": "examplePassword",
     *                            "email": "exampleEmailAddress"
     *                        }
     *
     *                        Validation rules
     *                        username: * Between 3 and 30 characters, inclusive
     *                                  * Only contains alphanumeric characters, ".", and "_"
     *                                  * Must start and end with an alphanumeric character
     *                                  * Can't contain consecutive ".", consecutive "_", "._", or "_."
     *                        password: * Between 8 and 256 characters, inclusive
     *                                  * Must contain an uppercase letter, a lowercase letter, and a number
     *                        email:    * Between 1 and 255 characters, inclusive
     *                                  * Must be composed of two non-empty strings with an @ symbol between
     *
     * @return HTTP 201 Created - If the user was successfully registered.
     *         HTTP 409 Conflict - If the username or email address in the registration request is already in use.
     *         HTTP 422 Unprocessable Entity - If the username, password, or email address in the registration request
     *                                         is not valid.
     */
    @PostMapping(value = "/register")
    public ResponseEntity<?> register(@RequestBody final RegisterRequest registerRequest) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(REGISTER_METRIC_NAME);

        if (!registerRequest.isValid()) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        final String usernameLowercase = registerRequest.getUsername().toLowerCase();
        final String emailLowercase = registerRequest.getEmail().toLowerCase();

        final boolean isUsernameInUse = dynamoDBMapper.load(User.class, usernameLowercase) != null;
        final boolean isEmailInUse = dynamoDBMapper.load(EmailUser.class, emailLowercase) != null;

        if (isUsernameInUse || isEmailInUse) {
            return new ResponseEntity<>(
                RegisterResponse.builder()
                    .isUsernameInUse(isUsernameInUse)
                    .isEmailInUse(isEmailInUse)
                    .build(),
                HttpStatus.CONFLICT
            );
        }

        wG
            .addV(USER_VERTEX_LABEL)
            .property(single, USER_USERNAME_PROPERTY, usernameLowercase)
            .property(single, USER_FRIENDLY_NAME_PROPERTY, usernameLowercase)
            .property(single, USER_VERIFIED_PROPERTY, false)
            .property(single, COMMON_CREATED_AT_PROPERTY, new Date())
            .property(single, USER_AVATAR_FILENAME_PROPERTY, EMPTY_STRING)
            .property(single, USER_BANNER_FILENAME_PROPERTY, EMPTY_STRING)
            .property(single, USER_BIO_PROPERTY, EMPTY_STRING)
            .property(single, USER_LOCATION_PROPERTY, EMPTY_STRING)
            .property(single, USER_FACEBOOK_URL_PROPERTY, EMPTY_STRING)
            .property(single, USER_TWITTER_URL_PROPERTY, EMPTY_STRING)
            .property(single, USER_INSTAGRAM_URL_PROPERTY, EMPTY_STRING)
            .property(single, USER_TWITCH_URL_PROPERTY, EMPTY_STRING)
            .property(single, USER_YOUTUBE_URL_PROPERTY, EMPTY_STRING)
            .property(single, USER_TIKTOK_URL_PROPERTY, EMPTY_STRING)
            .property(single, USER_WEBSITE_URL_PROPERTY, EMPTY_STRING)
            .iterate();

        persistUser(usernameLowercase, emailLowercase, passwordEncoder.encode(registerRequest.getPassword()));

        final ResponseEntity<?> responseEntity = new ResponseEntity<>(
            HttpStatus.CREATED
        );

        metricsService.addSuccessMetric(REGISTER_METRIC_NAME);
        metricsService.addLatencyMetric(REGISTER_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    private void persistUser(final String username, final String email, final String encodedPassword) {
        final TransactionWriteRequest transactionWriteRequest = new TransactionWriteRequest();

        transactionWriteRequest.addPut(
            User.builder()
                .username(username)
                .email(email)
                .password(encodedPassword)
                .createdAt(new Date())
                .build(),
            new DynamoDBTransactionWriteExpression().withConditionExpression(USERNAME_DOES_NOT_EXIST_CONDITION)
        );
        transactionWriteRequest.addPut(
            EmailUser.builder()
                .email(email)
                .username(username)
                .build(),
            new DynamoDBTransactionWriteExpression().withConditionExpression(EMAIL_DOES_NOT_EXIST_CONDITION)
        );

        dynamoDBMapper.transactionWrite(transactionWriteRequest);
    }
}
