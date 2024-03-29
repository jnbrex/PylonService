package com.pylon.pylonservice.model.requests.auth;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ForgotPasswordRequest implements Serializable {
    private static final long serialVersionUID = 0L;

    String toEmailAddress;
}
