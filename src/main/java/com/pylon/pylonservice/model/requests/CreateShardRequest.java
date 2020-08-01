package com.pylon.pylonservice.model.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collection;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreateShardRequest implements Serializable {
    private static final long serialVersionUID = 0L;

    // shardNames are composed of lowercase english letters, uppercase english letters, and numbers, and are between 3
    // and 120 characters in length.
    private static final Pattern SHARD_NAME_REGEX_PATTERN = Pattern.compile("^[a-zA-Z0-9]{3,120}$");

    String shardName;
    Collection<String> inheritedShardNames;
    Collection<String> inheritedUsers;

    public boolean isValid() {
        return isShardNameValid()
            && isInheritedShardNamesValid()
            && isInheritedUsersValid();
    }

    private boolean isShardNameValid() {
        return SHARD_NAME_REGEX_PATTERN.matcher(shardName).matches();
    }

    private boolean isInheritedShardNamesValid() {
        return inheritedShardNames != null;
    }

    private boolean isInheritedUsersValid() {
        return inheritedUsers != null;
    }
}
