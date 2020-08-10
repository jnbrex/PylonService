package com.pylon.pylonservice.model.requests.shard;

import com.pylon.pylonservice.model.requests.Request;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Collection;
import java.util.regex.Pattern;

@AllArgsConstructor
@Data
abstract class ShardRequest implements Serializable, Request {
    private static final long serialVersionUID = 0L;

    // shardNames are composed of alphanumeric characters and are between 3 and 24 characters in length
    private static final Pattern SHARD_NAME_REGEX_PATTERN = Pattern.compile("^[a-zA-Z0-9]{3,24}$");

    // shardFriendlyNames are composed of alphanumeric characters and spaces and are between 1 and 64 characters in
    // length
    private static final Pattern SHARD_FRIENDLY_NAME_REGEX_PATTERN = Pattern.compile("^[a-zA-Z0-9 ]{1,64}$");

    String shardName;
    String shardFriendlyName;
    String shardAvatarFilename;
    String shardBannerFilename;
    String shardDescription;
    Collection<String> inheritedShardNames;
    Collection<String> inheritedUsers;

    public boolean isValid() {
        return isShardNameValid()
            && isShardFriendlyNameValid()
            && isShardAvatarFilenameValid()
            && isShardBannerFilenameValid()
            && isShardDescriptionValid()
            && isInheritedShardNamesValid()
            && isInheritedUsersValid();
    }

    boolean isShardNameValid() {
        return shardName != null && SHARD_NAME_REGEX_PATTERN.matcher(shardName).matches();
    }

    boolean isShardFriendlyNameValid() {
        return shardFriendlyName != null
            && (shardFriendlyName.isEmpty() || SHARD_FRIENDLY_NAME_REGEX_PATTERN.matcher(shardFriendlyName).matches());
    }

    boolean isShardAvatarFilenameValid() {
        return shardAvatarFilename != null
            && (shardAvatarFilename.isEmpty() || FILENAME_REGEX_PATTERN.matcher(shardAvatarFilename).matches());
    }

    boolean isShardBannerFilenameValid() {
        return shardBannerFilename != null
            && (shardBannerFilename.isEmpty() || FILENAME_REGEX_PATTERN.matcher(shardBannerFilename).matches());
    }

    boolean isShardDescriptionValid() {
        return shardDescription != null && shardDescription.length() < 151;
    }

    boolean isInheritedShardNamesValid() {
        return inheritedShardNames != null && !inheritedShardNames.contains(shardName);
    }

    boolean isInheritedUsersValid() {
        return inheritedUsers != null;
    }
}
