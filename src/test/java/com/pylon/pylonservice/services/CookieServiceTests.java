package com.pylon.pylonservice.services;

import org.testng.annotations.Test;

import javax.servlet.http.Cookie;

import static org.assertj.core.api.Assertions.assertThat;

public class CookieServiceTests {
    private static final String LOCAL_ENVIRONMENT_NAME = "local";
    private static final String BETA_ENVIRONMENT_NAME = "beta";
    private static final String PROD_ENVIRONMENT_NAME = "prod";
    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final int ONE_DAY_IN_SECONDS = 60 * 60 * 24;
    private static final int ONE_YEAR_IN_SECONDS = ONE_DAY_IN_SECONDS * 365;
    private static final String LOCAL_ORIGIN = "http://localhost:3000";
    private static final String BETA_ORIGIN = "https://beta.pylon.gg";
    private static final String PROD_ORIGIN = "https://pylon.gg";

    private static final String TEST_ACCESS_TOKEN_VALUE = "testAccessTokenValue";

    @Test
    public void testCreateAccessTokenCookieLocal() {
        final CookieService cookieService = new CookieService(LOCAL_ENVIRONMENT_NAME);

        final Cookie accessTokenCookie =
            cookieService.createAccessTokenCookie(TEST_ACCESS_TOKEN_VALUE, LOCAL_ORIGIN);

        assertThat(accessTokenCookie).extracting(Cookie::getName).isEqualTo(ACCESS_TOKEN_COOKIE_NAME);
        assertThat(accessTokenCookie).extracting(Cookie::getValue).isEqualTo(TEST_ACCESS_TOKEN_VALUE);
        assertThat(accessTokenCookie).extracting(Cookie::getMaxAge).isEqualTo(ONE_DAY_IN_SECONDS);
        assertThat(accessTokenCookie).extracting(Cookie::getDomain).isEqualTo(LOCAL_ORIGIN);
        assertCommonCookieProperties(accessTokenCookie);
    }

    @Test
    public void testCreateAccessTokenCookieBeta() {
        final CookieService cookieService = new CookieService("beta");

    }

    @Test
    public void testCreateAccessTokenCookieProd() {
        final CookieService cookieService = new CookieService("prod");
    }

    private void assertCommonCookieProperties(final Cookie cookie) {
        assertThat(cookie).extracting(Cookie::getSecure).isEqualTo(true);
        assertThat(cookie).extracting(Cookie::isHttpOnly).isEqualTo(true);
        assertThat(cookie).extracting(Cookie::getPath).isEqualTo("/");
    }
}
