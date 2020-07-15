package com.pylon.pylonservice.model.requests;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Set;

@Data
@NoArgsConstructor
public class CreateShardRequest implements Serializable {
    private static final long serialVersionUID = 0L;

    @NonNull
    String shardName;
    Set<String> inheritedShardNames;
}
