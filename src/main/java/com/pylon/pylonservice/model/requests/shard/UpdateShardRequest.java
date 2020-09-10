package com.pylon.pylonservice.model.requests.shard;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Collection;

/**
 * {
 *     "shardName": "fortnite",
 *     "shardFriendlyName": "Fortnite Battle Royale",
 *     "shardAvatarFilename": "f99269c2-9b0b-4dbf-b04a-385bc7ffa629.png",
 *     "shardBannerFilename": "f99269c2-9b0b-4dbf-b04a-385bc7ffa629.jpg",
 *     "shardDescription": "This is the best of all of the fortnite shards. JJOU(G*&^&F*D%^F&G*(F&GUOH)*OU!@#@$",
 *     "inheritedShardNames": [],
 *     "inheritedUsers": [
 *         "jason",
 *         "brett"
 *     ],
 *     "shardFeaturedImageFilename": "f99269c2-9b0b-4dbf-b04a-385bc7ffa629.png",
 *     "shardFeaturedImageLink": "https://google.com"
 * }
 */
@Value
@EqualsAndHashCode(callSuper=true)
public class UpdateShardRequest extends ShardRequest {
    private static final long serialVersionUID = 0L;

    String shardFeaturedImageFilename;
    String shardFeaturedImageLink;

    UpdateShardRequest(final String shardName,
                       final String shardFriendlyName,
                       final String shardAvatarFilename,
                       final String shardBannerFilename,
                       final String shardDescription,
                       final Collection<String> inheritedShardNames,
                       final Collection<String> inheritedUsers,
                       final String shardFeaturedImageFilename,
                       final String shardFeaturedImageLink) {
        super(shardName, shardFriendlyName, shardAvatarFilename, shardBannerFilename, shardDescription,
            inheritedShardNames, inheritedUsers);
        this.shardFeaturedImageFilename = shardFeaturedImageFilename;
        this.shardFeaturedImageLink = shardFeaturedImageLink;
    }

    @Override
    public boolean isValid() {
        return isShardNameValid()
            && isShardFriendlyNameValid()
            && isShardAvatarFilenameValid()
            && isShardBannerFilenameValid()
            && isShardDescriptionValid()
            && isInheritedShardNamesValid()
            && isInheritedUsersValid()
            && isShardFeaturedImageFilenameValid()
            && isShardFeaturedImageLinkValid();
    }

    private boolean isShardFeaturedImageFilenameValid() {
        return shardFeaturedImageFilename != null
            && (
                shardFeaturedImageFilename.isEmpty()
             || FILENAME_REGEX_PATTERN.matcher(shardFeaturedImageFilename).matches()
        );
    }

    private boolean isShardFeaturedImageLinkValid() {
        return shardFeaturedImageLink != null && shardFeaturedImageLink.length() < MAX_URL_LENGTH;
    }
}
