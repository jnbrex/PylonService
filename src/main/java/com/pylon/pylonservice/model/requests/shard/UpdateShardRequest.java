package com.pylon.pylonservice.model.requests.shard;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Collection;

@Value
@EqualsAndHashCode(callSuper=true)
public class UpdateShardRequest extends ShardRequest {
    private static final long serialVersionUID = 0L;

    UpdateShardRequest(final String shardName,
                       final String shardFriendlyName,
                       final String shardAvatarFilename,
                       final String shardBannerFilename,
                       final String shardDescription,
                       final Collection<String>inheritedShardNames,
                       final Collection<String> inheritedUsers) {
        super(shardName, shardFriendlyName, shardAvatarFilename, shardBannerFilename, shardDescription, inheritedShardNames,
            inheritedUsers);
    }
}
