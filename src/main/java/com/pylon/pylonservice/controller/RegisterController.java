package com.pylon.pylonservice.controller;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTransactionWriteExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.TransactionWriteRequest;
import com.pylon.pylonservice.model.requests.RegisterRequest;
import com.pylon.pylonservice.model.tables.EmailUser;
import com.pylon.pylonservice.model.tables.User;
import com.pylon.pylonservice.model.tables.UsernameUser;
import com.pylon.pylonservice.util.DynamoDbUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

@RestController
public class RegisterController {
    private static final String USERNAME_DOES_NOT_EXIST_CONDITION = "attribute_not_exists(username)";
    private static final String EMAIL_DOES_NOT_EXIST_CONDITION = "attribute_not_exists(email)";
    private static final String USER_ID_DOES_NOT_EXIST_CONDITION = "attribute_not_exists(userId)";

    @Autowired
    private DynamoDBMapper dynamoDBMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Call to register a User.
     *
     * @param registerRequest A JSON body containing the username, password, and email address of the User who is
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
        if (!registerRequest.isValid()) {
            return ResponseEntity.unprocessableEntity().body("Invalid register request");
        }

        final String username = registerRequest.getUsername();
        final String email = registerRequest.getEmail();

        if (dynamoDBMapper.load(UsernameUser.class, username) != null) {
            return new ResponseEntity<>(
                String.format("Username %s is already in use", username), HttpStatus.CONFLICT
            );
        }
        if (dynamoDBMapper.load(EmailUser.class, email) != null) {
            return new ResponseEntity<>(
                String.format("Email address %s is already in use", email), HttpStatus.CONFLICT
            );
        }

        persistUser(username, email, passwordEncoder.encode(registerRequest.getPassword()));

        return new ResponseEntity<>(
            String.format("User created with username %s and email %s", username, email), HttpStatus.CREATED
        );
    }

    private void persistUser(final String username, final String email, final String encodedPassword) {
        final String userId = UUID.randomUUID().toString();

        final TransactionWriteRequest transactionWriteRequest = new TransactionWriteRequest();
        transactionWriteRequest.addPut(
            UsernameUser.builder()
                .username(username)
                .userId(userId)
                .build(),
            new DynamoDBTransactionWriteExpression().withConditionExpression(USERNAME_DOES_NOT_EXIST_CONDITION)
        );
        transactionWriteRequest.addPut(
            EmailUser.builder()
                .email(email)
                .userId(userId)
                .build(),
            new DynamoDBTransactionWriteExpression().withConditionExpression(EMAIL_DOES_NOT_EXIST_CONDITION)
        );
        transactionWriteRequest.addPut(
            User.builder()
                .userId(userId)
                .username(username)
                .email(email)
                .password(encodedPassword)
                .createdAt(new Date())
                .build(),
            new DynamoDBTransactionWriteExpression().withConditionExpression(USER_ID_DOES_NOT_EXIST_CONDITION)
        );

        DynamoDbUtil.executeTransactionWrite(dynamoDBMapper, transactionWriteRequest);
    }
}
