package com.pylon.pylonservice.model.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import static com.pylon.pylonservice.constants.RegexValidationPatterns.FILENAME_REGEX_PATTERN;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest implements Serializable {
    private static final long serialVersionUID = 0L;

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
    String userWebsiteUrl;

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
        return userFacebookUrl != null && userFacebookUrl.length() < 201;
    }

    private boolean isUserTwitterUrlValid() {
        return userTwitterUrl != null && userTwitterUrl.length() < 201;
    }

    private boolean isUserInstagramUrlValid() {
        return userInstagramUrl != null && userInstagramUrl.length() < 201;
    }

    private boolean isUserTwitchUrlValid() {
        return userTwitchUrl != null && userTwitchUrl.length() < 201;
    }

    private boolean isUserYoutubeUrlValid() {
        return userYoutubeUrl != null && userYoutubeUrl.length() < 201;
    }

    private boolean isUserTiktokUrlValid() {
        return userTiktokUrl != null && userTiktokUrl.length() < 201;
    }

    private boolean isUserWebsiteUrlValid() {
        return userWebsiteUrl != null && userWebsiteUrl.length() < 201;
    }
}
