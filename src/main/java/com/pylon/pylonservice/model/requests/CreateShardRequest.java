package com.pylon.pylonservice.model.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collection;
import java.util.regex.Pattern;

@Data
@NoArgsConstructor
public class CreateShardRequest implements Serializable {
    private static final long serialVersionUID = 0L;

    private static final Pattern SHARD_NAME_REGEX_PATTERN = Pattern.compile("^[a-zA-Z0-9]{3,120}$");

    String shardName;
    Collection<String> inheritedShardNames;
    Collection<String> inheritedUsers;

    public boolean isValid() {
        return isShardNameValid() && isInheritedShardNamesValid() && isInheritedUsersValid();
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
