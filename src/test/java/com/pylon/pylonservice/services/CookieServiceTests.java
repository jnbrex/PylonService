package com.pylon.pylonservice.services;

import org.testng.annotations.DataProvider;
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
    private static final String LOCAL_COOKIE_DOMAIN = "localhost";
    private static final String NON_LOCAL_COOKIE_DOMAIN = "pylon.gg";
    private static final String TEST_ACCESS_TOKEN_VALUE = "testAccessTokenValue";
    private static final String TEST_REFRESH_TOKEN_VALUE = "af23e7ba-25c9-4844-9094-771676f26fc5";

    @DataProvider
    public Object[][] provideAccessTokenCookie() {
        return new Object[][] {
            {
                LOCAL_ENVIRONMENT_NAME, TEST_ACCESS_TOKEN_VALUE, LOCAL_ORIGIN, LOCAL_COOKIE_DOMAIN, false
            },
            {
                BETA_ENVIRONMENT_NAME, TEST_ACCESS_TOKEN_VALUE, LOCAL_ORIGIN, LOCAL_COOKIE_DOMAIN, true
            },
            {
                BETA_ENVIRONMENT_NAME, TEST_ACCESS_TOKEN_VALUE, BETA_ORIGIN, NON_LOCAL_COOKIE_DOMAIN, true
            },
            {
                PROD_ENVIRONMENT_NAME, TEST_ACCESS_TOKEN_VALUE, LOCAL_ORIGIN, NON_LOCAL_COOKIE_DOMAIN, true
            },
            {
                PROD_ENVIRONMENT_NAME, TEST_ACCESS_TOKEN_VALUE, PROD_ORIGIN, NON_LOCAL_COOKIE_DOMAIN, true
            }
        };
    }

    @Test(dataProvider = "provideAccessTokenCookie")
    public void testCreateAccessTokenCookie(final String environmentName,
                                            final String cookieValue,
                                            final String requestOrigin,
                                            final String expectedCookieDomain,
                                            final boolean expectedCookieSecure) {
        final CookieService cookieService = new CookieService(environmentName);
        final Cookie accessTokenCookie = cookieService.createAccessTokenCookie(cookieValue, requestOrigin);

        assertThat(accessTokenCookie).extracting(Cookie::getValue).isEqualTo(cookieValue);
        assertThat(accessTokenCookie).extracting(Cookie::getDomain).isEqualTo(expectedCookieDomain);
        assertThat(accessTokenCookie).extracting(Cookie::getSecure).isEqualTo(expectedCookieSecure);

        assertCommonAccessTokenCookieProperties(accessTokenCookie);
    }

    @DataProvider
    public Object[][] provideRefreshTokenCookie() {
        return new Object[][] {
            {
                LOCAL_ENVIRONMENT_NAME, TEST_REFRESH_TOKEN_VALUE, LOCAL_ORIGIN, LOCAL_COOKIE_DOMAIN, false
            },
            {
                BETA_ENVIRONMENT_NAME, TEST_REFRESH_TOKEN_VALUE, LOCAL_ORIGIN, LOCAL_COOKIE_DOMAIN, true
            },
            {
                BETA_ENVIRONMENT_NAME, TEST_REFRESH_TOKEN_VALUE, BETA_ORIGIN, NON_LOCAL_COOKIE_DOMAIN, true
            },
            {
                PROD_ENVIRONMENT_NAME, TEST_REFRESH_TOKEN_VALUE, LOCAL_ORIGIN, NON_LOCAL_COOKIE_DOMAIN, true
            },
            {
                PROD_ENVIRONMENT_NAME, TEST_REFRESH_TOKEN_VALUE, PROD_ORIGIN, NON_LOCAL_COOKIE_DOMAIN, true
            }
        };
    }

    @Test(dataProvider = "provideRefreshTokenCookie")
    public void testCreateRefreshTokenCookie(final String environmentName,
                                            final String cookieValue,
                                            final String requestOrigin,
                                            final String expectedCookieDomain,
                                            final boolean expectedCookieSecure) {
        final CookieService cookieService = new CookieService(environmentName);
        final Cookie refreshTokenCookie = cookieService.createRefreshTokenCookie(cookieValue, requestOrigin);

        assertThat(refreshTokenCookie).extracting(Cookie::getValue).isEqualTo(cookieValue);
        assertThat(refreshTokenCookie).extracting(Cookie::getDomain).isEqualTo(expectedCookieDomain);
        assertThat(refreshTokenCookie).extracting(Cookie::getSecure).isEqualTo(expectedCookieSecure);

        assertCommonRefreshTokenCookieProperties(refreshTokenCookie);
    }

    private void assertCommonAccessTokenCookieProperties(final Cookie accessTokenCookie) {
        assertThat(accessTokenCookie).extracting(Cookie::getName).isEqualTo(ACCESS_TOKEN_COOKIE_NAME);
        assertThat(accessTokenCookie).extracting(Cookie::getMaxAge).isEqualTo(ONE_DAY_IN_SECONDS);
        assertCommonCookieProperties(accessTokenCookie);
    }

    private void assertCommonRefreshTokenCookieProperties(final Cookie refreshTokenCookie) {
        assertThat(refreshTokenCookie).extracting(Cookie::getName).isEqualTo(REFRESH_TOKEN_COOKIE_NAME);
        assertThat(refreshTokenCookie).extracting(Cookie::getMaxAge).isEqualTo(ONE_YEAR_IN_SECONDS);
        assertCommonCookieProperties(refreshTokenCookie);
    }

    private void assertCommonCookieProperties(final Cookie cookie) {
        assertThat(cookie).extracting(Cookie::isHttpOnly).isEqualTo(true);
        assertThat(cookie).extracting(Cookie::getPath).isEqualTo("/");
    }
}
