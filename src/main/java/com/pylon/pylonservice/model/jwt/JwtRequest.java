package com.pylon.pylonservice.model.jwt;

import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;

@Value
public class JwtRequest implements Serializable {
    private static final long serialVersionUID = 0L;

    @NonNull
    String username;
    @NonNull
    String password;
}