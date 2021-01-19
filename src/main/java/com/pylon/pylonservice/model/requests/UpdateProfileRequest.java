package com.pylon.pylonservice.model.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/**
 * {
 *     "userFriendlyName": "jason 1",
 *     "userAvatarFilename": "b403b2fc-9d17-4ab1-b1c3-b7edca25d069.png",
 *     "userBannerFilename": "0f6c4b76-c1b4-40ba-abdc-43508f85c58c.gif",
 *     "userBio": "",
 *     "userLocation": "",
 *     "userVerified": false,
 *     "userFacebookUrl": "facebook.com/jasonsfdlka0",
 *     "userTwitterUrl": "http://twitter.com",
 *     "userInstagramUrl": "https://instagram.com",
 *     "userTwitchUrl": "https://www.twitch.tv/",
 *     "userYoutubeUrl": "http://www.youtube.com/jlasjkdljfkljal",
 *     "userTiktokUrl": "https://tiktok.com/asldfjk",
 *     "userDiscordUrl": "http://www.discord.com/jlasjkdljfkljal",
 *     "userWebsiteUrl": "jason.org"
 * }
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest implements Serializable, Request {
    private static final long serialVersionUID = 0L;

    // shardFriendlyNames are composed of alphanumeric characters and spaces and are between 1 and 64 characters in
    // length
    private static final Pattern USER_FRIENDLY_NAME_REGEX_PATTERN = Pattern.compile("^[a-zA-Z0-9 ]{1,64}$");
    // UUID ending in .png or .jpg
    private static final Pattern FILENAME_REGEX_PATTERN_NO_GIFS =
        Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|png)$");

    private static final String FACEBOOK_DOMAIN = "facebook.com";
    private static final String TWITTER_DOMAIN = "twitter.com";
    private static final String INSTAGRAM_DOMAIN = "instagram.com";
    private static final String TWITCH_DOMAIN = "twitch.tv";
    private static final String YOUTUBE_DOMAIN = "youtube.com";
    private static final String TIKTOK_DOMAIN = "tiktok.com";
    private static final String DISCORD_GG_DOMAIN = "discord.gg";
    private static final String DISCORD_COM_DOMAIN = "discord.com";

    String userFriendlyName;
    String userAvatarFilename;
    String userBannerFilename;
    String userBio;
    String userLocation;
    Boolean userVerified;
    String userFacebookUrl;
    String userTwitterUrl;
    String userInstagramUrl;
    String userTwitchUrl;
    String userYoutubeUrl;
    String userTiktokUrl;
    String userDiscordUrl;
    String userWebsiteUrl;

    public String getUserFacebookUrl() {
        return addHttpHttpsPrefixIfNotPresent(userFacebookUrl);
    }

    public String getUserTwitterUrl() {
        return addHttpHttpsPrefixIfNotPresent(userTwitterUrl);
    }

    public String getUserInstagramUrl() {
        return addHttpHttpsPrefixIfNotPresent(userInstagramUrl);
    }

    public String getUserTwitchUrl() {
        return addHttpHttpsPrefixIfNotPresent(userTwitchUrl);
    }

    public String getUserYoutubeUrl() {
        return addHttpHttpsPrefixIfNotPresent(userYoutubeUrl);
    }

    public String getUserTiktokUrl() {
        return addHttpHttpsPrefixIfNotPresent(userTiktokUrl);
    }

    public String getUserDiscordUrl() {
        return addHttpHttpsPrefixIfNotPresent(userDiscordUrl);
    }

    public String getUserWebsiteUrl() {
        return addHttpHttpsPrefixIfNotPresent(userWebsiteUrl);
    }

    private String addHttpHttpsPrefixIfNotPresent(final String url) {
        if (url.isEmpty() || url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        } else {
            return "http://" + url;
        }
    }

    public boolean isValid() {
        return isUserFriendlyNameValid()
            && isUserAvatarFilenameValid()
            && isUserBannerFilenameValid()
            && isUserBioValid()
            && isUserLocationValid()
            && isUserFacebookUrlValid()
            && isUserTwitterUrlValid()
            && isUserInstagramUrlValid()
            && isUserTwitchUrlValid()
            && isUserYoutubeUrlValid()
            && isUserTiktokUrlValid()
            && isUserDiscordUrlValid()
            && isUserWebsiteUrlValid();
    }

    private boolean isUserFriendlyNameValid() {
        return userFriendlyName != null &&
            (
                userFriendlyName.isEmpty() ||
                    (
                        !userFriendlyName.toLowerCase().contains("nigger")
                            && USER_FRIENDLY_NAME_REGEX_PATTERN.matcher(userFriendlyName).matches()
                    )
            );
    }

    private boolean isUserAvatarFilenameValid() {
        return isUserImageFilenameValid(userAvatarFilename);
    }

    private boolean isUserBannerFilenameValid() {
        return isUserImageFilenameValid(userBannerFilename);
    }

    private boolean isUserImageFilenameValid(String filename) {
        Pattern filenamePattern = FILENAME_REGEX_PATTERN_NO_GIFS;
        if (userVerified) {
            filenamePattern = FILENAME_REGEX_PATTERN;
        }

        return filename != null && (filename.isEmpty() || filenamePattern.matcher(filename).matches());
    }

    private boolean isUserBioValid() {
        return userBio != null && userBio.length() < 151;
    }

    private boolean isUserLocationValid() {
        return userLocation != null && userLocation.length() < 101;
    }

    private boolean isUserFacebookUrlValid() {
        return isSocialMediaUrlValid(getUserFacebookUrl(), FACEBOOK_DOMAIN);
    }

    private boolean isUserTwitterUrlValid() {
        return isSocialMediaUrlValid(getUserTwitterUrl(), TWITTER_DOMAIN);
    }

    private boolean isUserInstagramUrlValid() {
        return isSocialMediaUrlValid(getUserInstagramUrl(), INSTAGRAM_DOMAIN);
    }

    private boolean isUserTwitchUrlValid() {
        return isSocialMediaUrlValid(getUserTwitchUrl(), TWITCH_DOMAIN);
    }

    private boolean isUserYoutubeUrlValid() {
        return isSocialMediaUrlValid(getUserYoutubeUrl(), YOUTUBE_DOMAIN);
    }

    private boolean isUserTiktokUrlValid() {
        return isSocialMediaUrlValid(getUserTiktokUrl(), TIKTOK_DOMAIN);
    }

    private boolean isUserDiscordUrlValid() {
        return isSocialMediaUrlValid(getUserDiscordUrl(), DISCORD_COM_DOMAIN)
            || isSocialMediaUrlValid(getUserDiscordUrl(), DISCORD_GG_DOMAIN);
    }

    private boolean isUserWebsiteUrlValid() {
        return userWebsiteUrl != null && userWebsiteUrl.length() < MAX_URL_LENGTH;
    }

    private boolean isSocialMediaUrlValid(final String url, final String expectedDomain) {
        try {
            return url != null
            && url.length() < MAX_URL_LENGTH
            && (url.isEmpty() || getDomainName(url).equals(expectedDomain));
        } catch (final URISyntaxException e) {
            return false;
        }
    }

    private String getDomainName(final String url) throws URISyntaxException {
        final URI uri = new URI(url);
        final String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }
}
