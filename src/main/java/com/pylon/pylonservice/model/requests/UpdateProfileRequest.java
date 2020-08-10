package com.pylon.pylonservice.model.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest implements Serializable, Request {
    private static final long serialVersionUID = 0L;

    private static final int MAX_URL_LENGTH = 201;

    private static final String FACEBOOK_DOMAIN = "facebook.com";
    private static final String TWITTER_DOMAIN = "twitter.com";
    private static final String INSTAGRAM_DOMAIN = "instagram.com";
    private static final String TWITCH_DOMAIN = "twitch.tv";
    private static final String YOUTUBE_DOMAIN = "youtube.com";
    private static final String TIKTOK_DOMAIN = "tiktok.com";
    private static final String DISCORD_GG_DOMAIN = "discord.gg";
    private static final String DISCORD_COM_DOMAIN = "discord.com";

    String userAvatarFilename;
    String userBannerFilename;
    String userBio;
    String userLocation;
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
        return isUserAvatarFilenameValid()
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

    private boolean isUserAvatarFilenameValid() {
        return userAvatarFilename != null
            && (userAvatarFilename.isEmpty() || FILENAME_REGEX_PATTERN.matcher(userAvatarFilename).matches());
    }

    private boolean isUserBannerFilenameValid() {
        return userBannerFilename != null
            && (userBannerFilename.isEmpty() || FILENAME_REGEX_PATTERN.matcher(userBannerFilename).matches());
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
