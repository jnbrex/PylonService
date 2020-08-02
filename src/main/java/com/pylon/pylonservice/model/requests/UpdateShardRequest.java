package com.pylon.pylonservice.model.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdateShardRequest implements Serializable {
    private static final long serialVersionUID = 0L;

    Collection<String> inheritedShardNames;
    Collection<String> inheritedUsers;

    public boolean isValid() {
        return isInheritedShardNamesValid()
            && isInheritedUsersValid();
    }

    private boolean isInheritedShardNamesValid() {
        return inheritedShardNames != null;
    }

    private boolean isInheritedUsersValid() {
        return inheritedUsers != null;
    }
}
