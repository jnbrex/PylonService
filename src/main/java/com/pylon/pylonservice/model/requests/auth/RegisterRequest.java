package com.pylon.pylonservice.model.requests.auth;

import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;
import java.util.regex.Pattern;

@Value
public class RegisterRequest implements Serializable {
    private static final long serialVersionUID = 0L;
    // https://stackoverflow.com/a/12019115
    private static final Pattern USERNAME_REGEX_PATTERN =
        Pattern.compile("^(?=.{3,30}$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$");
    // https://stackoverflow.com/a/19605207
    private static final Pattern PASSWORD_REGEX_PATTERN =
        Pattern.compile("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,256}$");
    // https://stackoverflow.com/a/742455
    private static final Pattern EMAIL_REGEX_PATTERN = Pattern.compile("^(?=.{1,255}$)\\S+@\\S+$");

    @NonNull
    String username;
    @NonNull
    String password;
    @NonNull
    String email;

    public boolean isValid() {
        return isUsernameValid() && isPasswordValid() && isEmailValid();
    }

    private boolean isUsernameValid() {
        return !username.contains("nigger") && USERNAME_REGEX_PATTERN.matcher(username).matches();
    }

    private boolean isPasswordValid() {
        return PASSWORD_REGEX_PATTERN.matcher(password).matches();
    }

    private boolean isEmailValid() {
        return EMAIL_REGEX_PATTERN.matcher(email).matches();
    }
}
