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
 *     "shardDescription": "This is the best of all of the fortnite shards. J",
 *     "inheritedShardNames": [],
 *     "inheritedUsers": [
 *         "jason",
 *         "brett"
 *     ]
 * }
 */
@Value
@EqualsAndHashCode(callSuper=true)
public class CreateShardRequest extends ShardRequest {
    private static final long serialVersionUID = 0L;

    CreateShardRequest(final String shardName,
                       final String shardFriendlyName,
                       final String shardAvatarFilename,
                       final String shardBannerFilename,
                       final String shardDescription,
                       final Collection<String> inheritedShardNames,
                       final Collection<String> inheritedUsers) {
        super(shardName, shardFriendlyName, shardAvatarFilename, shardBannerFilename, shardDescription, inheritedShardNames,
            inheritedUsers);
    }
}
