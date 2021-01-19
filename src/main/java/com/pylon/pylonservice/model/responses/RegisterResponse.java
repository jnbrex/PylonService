package com.pylon.pylonservice.model.responses;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

/**
 * {
 *     "postisUsernameInUseId": true,
 *     "isEmailInUse": false
 * }
 */
@Builder
@Value
public class RegisterResponse implements Serializable {
    private static final long serialVersionUID = 0L;

    boolean isUsernameInUse;
    boolean isEmailInUse;
}
