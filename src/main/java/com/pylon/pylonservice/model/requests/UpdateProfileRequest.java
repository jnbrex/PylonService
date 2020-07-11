package com.pylon.pylonservice.model.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class UpdateProfileRequest implements Serializable {
    private static final long serialVersionUID = 0L;

    String avatarImageId;
    String bannerImageId;
    String bio;

    // Social media urls
    String facebookUrl;
    String twitterUrl;
    String instagramUrl;
    String twitchUrl;
    String youtubeUrl;
    String tiktokUrl;
}
