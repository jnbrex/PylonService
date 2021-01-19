package com.pylon.pylonservice.model.requests.auth;

import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;

/**
 * {
 *     "usernameOrEmail": "jason",
 *     "password": "abcd1234"
 * }
 */
@Value
public class AuthenticateRequest implements Serializable {
    private static final long serialVersionUID = 0L;

    @NonNull
    String usernameOrEmail;
    @NonNull
    String password;
}
