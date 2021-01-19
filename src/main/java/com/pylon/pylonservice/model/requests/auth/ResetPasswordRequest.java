package com.pylon.pylonservice.model.requests.auth;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * {
 *     "passwordResetToken": "abcd1234",
 *     "newPassword": "abcd12345"
 * }
 */
@Data
@NoArgsConstructor
public class ResetPasswordRequest implements Serializable {
    private static final long serialVersionUID = 0L;

    String passwordResetToken;
    String newPassword;
}
