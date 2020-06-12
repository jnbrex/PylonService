package com.pylon.pylonservice.model.jwt;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;

@Builder
@Value
public class JwtResponse implements Serializable {
    private static final long serialVersionUID = 0L;

    @NonNull
    String jwtToken;

    String refreshToken;
}