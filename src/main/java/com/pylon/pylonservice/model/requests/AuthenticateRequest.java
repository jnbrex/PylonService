package com.pylon.pylonservice.model.requests;

import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;

@Value
public class AuthenticateRequest implements Serializable {
    private static final long serialVersionUID = 0L;

    @NonNull
    String usernameOrEmail;
    @NonNull
    String password;
}
