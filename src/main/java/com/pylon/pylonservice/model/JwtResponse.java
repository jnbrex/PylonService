package com.pylon.pylonservice.model;

import lombok.Value;

import java.io.Serializable;

@Value
public class JwtResponse implements Serializable {
    private static final long serialVersionUID = 0L;

    String jwtToken;
}