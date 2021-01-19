package com.pylon.pylonservice.model.requests;

import com.pylon.pylonservice.model.requests.auth.RegisterRequest;
import org.assertj.core.api.Assertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RegisterRequestTests {
    private static final String VALID_USERNAME = "Jason";
    private static final String VALID_PASSWORD = "Password1";
    private static final String VALID_EMAIL = "jason@gmail.com";

    @DataProvider
    private Object[][] provideValidRegisterRequests() {
        return new Object[][] {
            {
                new RegisterRequest(VALID_USERNAME, VALID_PASSWORD, VALID_EMAIL)
            },
            {
                new RegisterRequest("jason.abcdef_1", "abcdefghi1234567890!@#$%^&*()7g8f6g6fd5ctf5d6CV^F&%CTVYUG*F^&CTV", "foo@bar.edu")
            }
        };
    }

    @DataProvider
    private Object[][] provideInvalidRegisterRequests() {
        return new Object[][] {
            {
                new RegisterRequest("", VALID_PASSWORD, VALID_EMAIL)
            },
            {
                new RegisterRequest("a", VALID_PASSWORD, VALID_EMAIL)
            },
            {
                new RegisterRequest("ab", VALID_PASSWORD, VALID_EMAIL)
            },
            {
                new RegisterRequest(".abc", VALID_PASSWORD, VALID_EMAIL)
            },
            {
                new RegisterRequest("_abc", VALID_PASSWORD, VALID_EMAIL)
            },
            {
                new RegisterRequest("abc.", VALID_PASSWORD, VALID_EMAIL)
            },
            {
                new RegisterRequest("abc_", VALID_PASSWORD, VALID_EMAIL)
            },
            {
                new RegisterRequest("ab..c", VALID_PASSWORD, VALID_EMAIL)
            },
            {
                new RegisterRequest("ab__c", VALID_PASSWORD, VALID_EMAIL)
            },
            {
                new RegisterRequest("ab._c", VALID_PASSWORD, VALID_EMAIL)
            },
            {
                new RegisterRequest("ab_.c", VALID_PASSWORD, VALID_EMAIL)
            },
            {
                new RegisterRequest("ab$c", VALID_PASSWORD, VALID_EMAIL)
            },
            {
                new RegisterRequest("abcdefghijabcdefghijabcdefghijabc", VALID_PASSWORD, VALID_EMAIL)
            },
            {
                new RegisterRequest(VALID_USERNAME, "", VALID_EMAIL)
            },
            {
                new RegisterRequest(VALID_USERNAME, "a", VALID_EMAIL)
            },
            {
                new RegisterRequest(VALID_USERNAME, "A", VALID_EMAIL)
            },
            {
                new RegisterRequest(VALID_USERNAME, "1", VALID_EMAIL)
            },
            {
                new RegisterRequest(VALID_USERNAME, "aA1aA1a", VALID_EMAIL)
            },
            {
                new RegisterRequest(VALID_USERNAME, "aaaaaaaaa", VALID_EMAIL)
            },
            {
                new RegisterRequest(VALID_USERNAME, "AAAAAAAAA", VALID_EMAIL)
            },
            {
                new RegisterRequest(VALID_USERNAME, "111111111", VALID_EMAIL)
            },
            {
                new RegisterRequest(VALID_USERNAME, "aaaaAAAAA", VALID_EMAIL)
            },
            {
                new RegisterRequest(VALID_USERNAME, "aaaa11111", VALID_EMAIL)
            },
            {
                new RegisterRequest(VALID_USERNAME, "AAAA11111", VALID_EMAIL)
            },
            {
                new RegisterRequest(
                    VALID_USERNAME,
                    "acTEUkQA9U2o4t5ZeZJj3Ov4aKVq9inyqxICuMOYqf4wd9dP9YjTxOaXOczHI6946UjMn5ssd7Dzul6m0pfd2PkqDKibVFspZZYD!@#$%^&*()DkWPl5Tri4QhO8sBTSoEFhp5BwTIFLuG5E4nxKXS5nlyraMQ7vZZfE588waPdugXwxXb622VedF2SGZrxhG2UIV9f9E34NyWYsalIm76lkvCCkZgeIMHOftRCgMIPrU01ZlPRualmeGrTda3RF3",
                    VALID_EMAIL)
            },
            {
                new RegisterRequest(
                    VALID_USERNAME,
                    VALID_PASSWORD,
                    "jason@")
            },
            {
                new RegisterRequest(
                    VALID_USERNAME,
                    VALID_PASSWORD,
                    "@gmail.com")
            },
            {
                new RegisterRequest(
                    VALID_USERNAME,
                    VALID_PASSWORD,
                    "jasongmail.com")
            },
            {
                new RegisterRequest(
                    VALID_USERNAME,
                    VALID_PASSWORD,
                    "abcDEF1234abcDEF1234abcDEF1234abcDEF1234abcDEF1234abcDEF1234abcDEF1234abcDEF1234abcDEF1234abcDEF1234abcDEF1234abcDEF1234abcDEF1234abcDEF1234abcDEF1234abcDEF1234abcDEF1234abcDEF1234abcDEF1@34abcDEF1234abcDEF1234abcDEF1234abcDEF1234abcDEF1234abcDEF123412.com")
            }
        };
    }

    @Test(dataProvider = "provideValidRegisterRequests")
    public void testValidRequests(final RegisterRequest registerRequest) {
        Assertions.assertThat(registerRequest.isValid()).isTrue();
    }

    @Test(dataProvider = "provideInvalidRegisterRequests")
    public void testInvalidRegisterRequests(final RegisterRequest registerRequest) {
        Assertions.assertThat(registerRequest.isValid()).isFalse();
    }
}
