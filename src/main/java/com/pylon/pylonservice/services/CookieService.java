package com.pylon.pylonservice.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;

import static com.pylon.pylonservice.constants.AuthenticationConstants.ACCESS_TOKEN_COOKIE_NAME;
import static com.pylon.pylonservice.constants.AuthenticationConstants.REFRESH_TOKEN_COOKIE_NAME;
import static com.pylon.pylonservice.constants.EnvironmentConstants.PROD_ENVIRONMENT_NAME;
import static com.pylon.pylonservice.constants.TimeConstants.ONE_DAY_IN_SECONDS;
import static com.pylon.pylonservice.constants.TimeConstants.ONE_YEAR_IN_SECONDS;

@Service
public class CookieService {
    private final boolean isProdEnvironment;

    CookieService(@Value("${environment.name}") String environmentName) {
        this.isProdEnvironment = environmentName.equals(PROD_ENVIRONMENT_NAME);
    }

    public Cookie createAccessTokenCookie(final String accessToken, final String requestOrigin) {
        return createCookie(
            ACCESS_TOKEN_COOKIE_NAME,
            accessToken,
            ONE_DAY_IN_SECONDS,
            requestOrigin

        );
    }

    public Cookie createRefreshTokenCookie(final String refreshToken, final String requestOrigin) {
        return createCookie(
            REFRESH_TOKEN_COOKIE_NAME,
            refreshToken,
            ONE_YEAR_IN_SECONDS,
            requestOrigin
        );
    }

    private Cookie createCookie(final String name,
                               final String value,
                               final int maxAge,
                               final String requestOrigin) {
        final Cookie cookie = new Cookie(name, value);

        cookie.setMaxAge(maxAge);
        cookie.setDomain(isProdEnvironment ? "pylon.gg" : requestOrigin);

        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        return cookie;
    }
}
