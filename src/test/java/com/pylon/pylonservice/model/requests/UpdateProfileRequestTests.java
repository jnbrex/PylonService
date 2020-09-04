package com.pylon.pylonservice.model.requests;

import org.assertj.core.api.Assertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class UpdateProfileRequestTests {
    private static final String VALID_USER_FRIENDLY_NAME = "Jason 1 Bohrer 1";
    private static final String VALID_USER_AVATAR_FILENAME = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.png";
    private static final String VALID_USER_AVATAR_FILENAME_GIF = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.gif";
    private static final String VALID_USER_BANNER_FILENAME = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.jpg";
    private static final String VALID_USER_BANNER_FILENAME_GIF = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.gif";
    private static final String VALID_USER_BIO = "hi my name is jason, this is my bio.";
    private static final String VALID_USER_LOCATION = "atlanta, georgia";
    private static final Boolean VALID_USER_VERIFIED_TRUE = true;
    private static final String VALID_USER_FACEBOOK_URL = "https://www.facebook.com/jason.bohrer.10";
    private static final String VALID_USER_TWITTER_URL = "https://twitter.com/bohrer_jason";
    private static final String VALID_USER_INSTAGRAM_URL = "www.instagram.com/jnbrex/";
    private static final String VALID_USER_TWITCH_URL = "twitch.tv/haste";
    private static final String VALID_USER_YOUTUBE_URL = "http://www.youtube.com/channel/UCeBMccz-PDZf6OB4aV6a3eA";
    private static final String VALID_USER_TIKTOK_URL = "https://www.tiktok.com/@charlidamelio?lang=en";
    private static final String VALID_USER_DISCORD_URL_GG_DOMAIN = "https://discord.gg/pJNRzPR";
    private static final String VALID_USER_DISCORD_URL_COM_DOMAIN = "https://discord.com/pJNRzPR";
    private static final String VALID_USER_WEBSITE_URL = "http://blog.jason.com";

    private static final String VALID_USER_FRIENDLY_NAME_BLANK = "";
    private static final String VALID_USER_AVATAR_FILENAME_BLANK = "";
    private static final String VALID_USER_BANNER_FILENAME_BLANK = "";
    private static final String VALID_USER_BIO_BLANK = "";
    private static final String VALID_USER_LOCATION_BLANK = "";
    private static final Boolean VALID_USER_VERIFIED_FALSE = false;
    private static final String VALID_USER_FACEBOOK_URL_BLANK = "";
    private static final String VALID_USER_TWITTER_URL_BLANK = "";
    private static final String VALID_USER_INSTAGRAM_URL_BLANK = "";
    private static final String VALID_USER_TWITCH_URL_BLANK = "";
    private static final String VALID_USER_YOUTUBE_URL_BLANK = "";
    private static final String VALID_USER_TIKTOK_URL_BLANK = "";
    private static final String VALID_USER_DISCORD_URL_BLANK = "";
    private static final String VALID_USER_WEBSITE_URL_BLANK = "";

    private static final String INVALID_USER_FRIENDLY_NAME_TOO_LONG = "a".repeat(65);
    private static final String INVALID_USER_FRIENDLY_NAME_NON_ALPHANUMERIC_CHARACTER = "Ja$on Bohrer";
    private static final String INVALID_USER_AVATAR_FILENAME_BAD_EXTENSION = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.svg";
    private static final String INVALID_USER_BANNER_FILENAME_NON_UUID_PLUS_EXTENSION = "filename";
    private static final String INVALID_USER_BIO_TOO_LONG = "a".repeat(151);
    private static final String INVALID_USER_LOCATION_TOO_LONG = "a".repeat(101);

    private static final String INVALID_USER_FACEBOOK_URL_WRONG_DOMAIN = "https://www.focebook.com/jason.bohrer.10";
    private static final String INVALID_USER_TWITTER_URL_WRONG_DOMAIN = "https://twitter.facebook.com/bohrer_jason";
    private static final String INVALID_USER_INSTAGRAM_URL_WRONG_DOMAIN = "www.instagram.co/jnbrex/";
    private static final String INVALID_USER_TWITCH_URL_WRONG_DOMAIN = "twitch.a.tv/haste";
    private static final String INVALID_USER_YOUTUBE_URL_WRONG_DOMAIN
        = "http://www.youtu.be/channel/UCeBMccz-PDZf6OB4aV6a3eA";
    private static final String INVALID_USER_TIKTOK_URL_WRONG_DOMAIN = "https://blog.tiktok.com/@charlidamelio?lang=en";
    private static final String INVALID_USER_DISCORD_URL_WRONG_DOMAIN = "https://discord.tv/pJNRzPR";

    private static final int MAX_URL_LENGTH_PLUS_ONE = 501;
    private static final String INVALID_USER_FACEBOOK_URL_TOO_LONG = "a".repeat(MAX_URL_LENGTH_PLUS_ONE);
    private static final String INVALID_USER_TWITTER_URL_TOO_LONG = "a".repeat(MAX_URL_LENGTH_PLUS_ONE);
    private static final String INVALID_USER_INSTAGRAM_URL_TOO_LONG = "a".repeat(MAX_URL_LENGTH_PLUS_ONE);
    private static final String INVALID_USER_TWITCH_URL_TOO_LONG = "a".repeat(MAX_URL_LENGTH_PLUS_ONE);
    private static final String INVALID_USER_YOUTUBE_URL_TOO_LONG = "a".repeat(MAX_URL_LENGTH_PLUS_ONE);
    private static final String INVALID_USER_TIKTOK_URL_TOO_LONG = "a".repeat(MAX_URL_LENGTH_PLUS_ONE);
    private static final String INVALID_USER_DISCORD_URL_TOO_LONG = "a".repeat(MAX_URL_LENGTH_PLUS_ONE);
    private static final String INVALID_USER_WEBSITE_URL_TOO_LONG = "a".repeat(MAX_URL_LENGTH_PLUS_ONE);

    @DataProvider
    private Object[][] provideValidUpdateProfileRequests() {
        return new Object[][] {
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME,
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_VERIFIED_TRUE,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_DISCORD_URL_GG_DOMAIN,
                    VALID_USER_WEBSITE_URL
                )
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME,
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_VERIFIED_TRUE,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_DISCORD_URL_COM_DOMAIN,
                    VALID_USER_WEBSITE_URL
                )
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME_BLANK,
                    VALID_USER_AVATAR_FILENAME_BLANK,
                    VALID_USER_BANNER_FILENAME_BLANK,
                    VALID_USER_BIO_BLANK,
                    VALID_USER_LOCATION_BLANK,
                    VALID_USER_VERIFIED_FALSE,
                    VALID_USER_FACEBOOK_URL_BLANK,
                    VALID_USER_TWITTER_URL_BLANK,
                    VALID_USER_INSTAGRAM_URL_BLANK,
                    VALID_USER_TWITCH_URL_BLANK,
                    VALID_USER_YOUTUBE_URL_BLANK,
                    VALID_USER_TIKTOK_URL_BLANK,
                    VALID_USER_DISCORD_URL_BLANK,
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
                    INVALID_USER_FRIENDLY_NAME_TOO_LONG,
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_VERIFIED_TRUE,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_DISCORD_URL_GG_DOMAIN,
                    VALID_USER_WEBSITE_URL
                )
            },
            {
                new UpdateProfileRequest(
                    INVALID_USER_FRIENDLY_NAME_NON_ALPHANUMERIC_CHARACTER,
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_VERIFIED_TRUE,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_DISCORD_URL_GG_DOMAIN,
                    VALID_USER_WEBSITE_URL
                )
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME,
                    INVALID_USER_AVATAR_FILENAME_BAD_EXTENSION,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_VERIFIED_TRUE,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_DISCORD_URL_GG_DOMAIN,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME,
                    VALID_USER_AVATAR_FILENAME,
                    INVALID_USER_BANNER_FILENAME_NON_UUID_PLUS_EXTENSION,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_VERIFIED_TRUE,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_DISCORD_URL_GG_DOMAIN,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME,
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    INVALID_USER_BIO_TOO_LONG,
                    VALID_USER_LOCATION,
                    VALID_USER_VERIFIED_TRUE,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_DISCORD_URL_GG_DOMAIN,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME,
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    INVALID_USER_LOCATION_TOO_LONG,
                    VALID_USER_VERIFIED_TRUE,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_DISCORD_URL_GG_DOMAIN,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME,
                    VALID_USER_AVATAR_FILENAME_GIF,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    INVALID_USER_LOCATION_TOO_LONG,
                    VALID_USER_VERIFIED_FALSE,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_DISCORD_URL_GG_DOMAIN,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME,
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME_GIF,
                    VALID_USER_BIO,
                    INVALID_USER_LOCATION_TOO_LONG,
                    VALID_USER_VERIFIED_FALSE,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_DISCORD_URL_GG_DOMAIN,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME,
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_VERIFIED_TRUE,
                    INVALID_USER_FACEBOOK_URL_WRONG_DOMAIN,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_DISCORD_URL_GG_DOMAIN,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME,
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_VERIFIED_TRUE,
                    VALID_USER_FACEBOOK_URL,
                    INVALID_USER_TWITTER_URL_WRONG_DOMAIN,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_DISCORD_URL_COM_DOMAIN,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME,
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_VERIFIED_TRUE,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    INVALID_USER_INSTAGRAM_URL_WRONG_DOMAIN,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_DISCORD_URL_COM_DOMAIN,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME,
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_VERIFIED_TRUE,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    INVALID_USER_TWITCH_URL_WRONG_DOMAIN,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_DISCORD_URL_COM_DOMAIN,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME,
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_VERIFIED_TRUE,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    INVALID_USER_YOUTUBE_URL_WRONG_DOMAIN,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_DISCORD_URL_COM_DOMAIN,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME,
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_VERIFIED_TRUE,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    INVALID_USER_TIKTOK_URL_WRONG_DOMAIN,
                    VALID_USER_DISCORD_URL_COM_DOMAIN,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME,
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_VERIFIED_TRUE,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    INVALID_USER_DISCORD_URL_WRONG_DOMAIN,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME,
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_VERIFIED_TRUE,
                    INVALID_USER_FACEBOOK_URL_TOO_LONG,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_DISCORD_URL_COM_DOMAIN,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME,
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_VERIFIED_TRUE,
                    VALID_USER_FACEBOOK_URL,
                    INVALID_USER_TWITTER_URL_TOO_LONG,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_DISCORD_URL_COM_DOMAIN,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME,
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_VERIFIED_TRUE,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    INVALID_USER_INSTAGRAM_URL_TOO_LONG,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_DISCORD_URL_COM_DOMAIN,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME,
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_VERIFIED_TRUE,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    INVALID_USER_TWITCH_URL_TOO_LONG,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_DISCORD_URL_COM_DOMAIN,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME,
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_VERIFIED_TRUE,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    INVALID_USER_YOUTUBE_URL_TOO_LONG,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_DISCORD_URL_COM_DOMAIN,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME,
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_VERIFIED_TRUE,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    INVALID_USER_TIKTOK_URL_TOO_LONG,
                    VALID_USER_DISCORD_URL_COM_DOMAIN,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME,
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_VERIFIED_TRUE,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    INVALID_USER_DISCORD_URL_TOO_LONG,
                    VALID_USER_WEBSITE_URL
                ),
            },
            {
                new UpdateProfileRequest(
                    VALID_USER_FRIENDLY_NAME,
                    VALID_USER_AVATAR_FILENAME,
                    VALID_USER_BANNER_FILENAME,
                    VALID_USER_BIO,
                    VALID_USER_LOCATION,
                    VALID_USER_VERIFIED_TRUE,
                    VALID_USER_FACEBOOK_URL,
                    VALID_USER_TWITTER_URL,
                    VALID_USER_INSTAGRAM_URL,
                    VALID_USER_TWITCH_URL,
                    VALID_USER_YOUTUBE_URL,
                    VALID_USER_TIKTOK_URL,
                    VALID_USER_DISCORD_URL_COM_DOMAIN,
                    INVALID_USER_WEBSITE_URL_TOO_LONG
                ),
            }
        };
    }

    @Test(dataProvider = "provideValidUpdateProfileRequests")
    public void testValidUpdateProfileRequests(final UpdateProfileRequest updateProfileRequest) {
        Assertions.assertThat(updateProfileRequest.isValid()).isTrue();
    }

    @Test(dataProvider = "provideInvalidUpdateProfileRequests")
    public void testInvalidUpdateProfileRequests(final UpdateProfileRequest updateProfileRequest) {
        Assertions.assertThat(updateProfileRequest.isValid()).isFalse();
    }
}
