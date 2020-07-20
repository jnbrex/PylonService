package com.pylon.pylonservice.model.requests;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Collection;

@Data
@NoArgsConstructor
public class CreateShardRequest implements Serializable {
    private static final long serialVersionUID = 0L;

    @NonNull
    String shardName;
    @NonNull
    Collection<String> inheritedShardNames;
    @NonNull
    Collection<String> inheritedUsers;
}
