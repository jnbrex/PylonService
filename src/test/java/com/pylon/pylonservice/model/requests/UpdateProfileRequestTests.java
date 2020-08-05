package com.pylon.pylonservice.model.requests;

import org.assertj.core.api.Assertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UpdateProfileRequestTests {
    private static final String VALID_USER_AVATAR_FILENAME = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.png";
    private static final String VALID_USER_BANNER_FILENAME = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.jpg";
    private static final String VALID_USER_BIO = "a".repeat(150);
    private static final String VALID_USER_LOCATION = "a".repeat(100);
    private static final String VALID_USER_FACEBOOK_URL = "a".repeat(200);
    private static final String VALID_USER_TWITTER_URL = "a".repeat(200);
    private static final String VALID_USER_INSTAGRAM_URL = "a".repeat(200);
    private static final String VALID_USER_TWITCH_URL = "a".repeat(200);
    private static final String VALID_USER_YOUTUBE_URL = "a".repeat(200);
    private static final String VALID_USER_TIKTOK_URL = "a".repeat(200);
    private static final String VALID_USER_WEBSITE_URL = "a".repeat(200);

    private static final String VALID_USER_AVATAR_FILENAME_BLANK = "";
    private static final String VALID_USER_BANNER_FILENAME_BLANK = "";
    private static final String VALID_USER_BIO_BLANK = "";
    private static final String VALID_USER_LOCATION_BLANK = "";
    private static final String VALID_USER_FACEBOOK_URL_BLANK = "";
    private static final String VALID_USER_TWITTER_URL_BLANK = "";
    private static final String VALID_USER_INSTAGRAM_URL_BLANK = "";
    private static final String VALID_USER_TWITCH_URL_BLANK = "";
    private static final String VALID_USER_YOUTUBE_URL_BLANK = "";
    private static final String VALID_USER_TIKTOK_URL_BLANK = "";
    private static final String VALID_USER_WEBSITE_URL_BLANK = "";

    private static final String INVALID_USER_AVATAR_FILENAME = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.svg";
    private static final String INVALID_USER_BANNER_FILENAME = "filename";
    private static final String INVALID_USER_BIO = "a".repeat(151);
    private static final String INVALID_USER_LOCATION = "a".repeat(101);
    private static final String INVALID_USER_FACEBOOK_URL = "a".repeat(201);
    private static final String INVALID_USER_TWITTER_URL = "a".repeat(201);
    private static final String INVALID_USER_INSTAGRAM_URL = "a".repeat(201);
    private static final String INVALID_USER_TWITCH_URL = "a".repeat(201);
    private static final String INVALID_USER_YOUTUBE_URL = "a".repeat(201);
    private static final String INVALID_USER_TIKTOK_URL = "a".repeat(201);
    private static final String INVALID_USER_WEBSITE_URL = "a".repeat(201);

    @DataProvider
    private Object[][] provideValidUpdateProfileRequests() {
        return new Object[][] {
            {
                new UpdateProfileRequest(
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_WEBSITE_URL
                )
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_AVATAR_FILENAME_BLANK,
                    VALID_USER_BANNER_FILENAME_BLANK,
                    VALID_USER_BIO_BLANK,
                    VALID_USER_LOCATION_BLANK,
                    VALID_USER_FACEBOOK_URL_BLANK,
                    VALID_USER_TWITTER_URL_BLANK,
                    VALID_USER_INSTAGRAM_URL_BLANK,
                    VALID_USER_TWITCH_URL_BLANK,
                    VALID_USER_YOUTUBE_URL_BLANK,
                    VALID_USER_TIKTOK_URL_BLANK,
                    VALID_USER_WEBSITE_URL_BLANK
                )
            }
        };
    }

    @DataProvider
    private Object[][] provideInvalidUpdateProfileRequests() {
        return new Object[][] {
            {
                new UpdateProfileRequest(
                    INVALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_AVATAR_FILENAME,
                    INVALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    INVALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    INVALID_USER_LOCATION,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    INVALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_FACEBOOK_URL,
                    INVALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    INVALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    INVALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    INVALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    INVALID_USER_TIKTOK_URL,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    INVALID_USER_WEBSITE_URL
                ),
            }
        };
    }

    @Test(dataProvider = "provideValidUpdateProfileRequests")
    public void testValidCreateTopLevelPostRequests(final UpdateProfileRequest updateProfileRequest) {
        Assertions.assertThat(updateProfileRequest.isValid()).isTrue();
    }

    @Test(dataProvider = "provideInvalidUpdateProfileRequests")
    public void testInvalidCreateTopLevelPostRequests(final UpdateProfileRequest updateProfileRequest) {
        Assertions.assertThat(updateProfileRequest.isValid()).isFalse();
    }
}
