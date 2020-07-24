package com.pylon.pylonservice.model.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class UpdateProfileRequest implements Serializable {
    private static final long serialVersionUID = 0L;

    String userAvatarImageId;
    String userBannerImageId;
    String userBio;
    String userLocation;

    // Urls
    String userFacebookUrl;
    String userTwitterUrl;
    String userInstagramUrl;
    String userTwitchUrl;
    String userYoutubeUrl;
    String userTiktokUrl;
    String userWebsiteUrl;
}
