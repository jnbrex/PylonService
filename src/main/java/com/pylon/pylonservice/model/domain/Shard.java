package com.pylon.pylonservice.model.domain;

import lombok.Data;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import static com.pylon.pylonservice.constants.GraphConstants.COMMON_CREATED_AT_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_AVATAR_FILENAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_BANNER_FILENAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_DESCRIPTION_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_FRIENDLY_NAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_INHERITS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_INHERITS_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_NAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_VERTEX_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FOLLOWS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_USERNAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERTEX_LABEL;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.V;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.in;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.project;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.valueMap;

/**
 * {
 *     "shardName": "fortnite47",
 *     "shardFriendlyName": "Fortnite Battle Royale",
 *     "shardAvatarFilename": "f99269c2-9b0b-4dbf-b04a-385bc7ffa629.png",
 *     "shardBannerFilename": "f99269c2-9b0b-4dbf-b04a-385bc7ffa629.jpg",
 *     "shardDescription": "This is the best of all of the fortnite shards. JJOU(G*&^&F*D%^F&G*(F&GUOH)*OU!@#@$",
 *     "createdAt": "2020-08-14T05:59:46.847+00:00",
 *     "numInheritedShards": 2,
 *     "numInheritedUsers": 2,
 *     "userFollowsShard": true,
 *     "numFollowers": 1
 * }
 */
@Data
public class Shard implements Serializable {
    private static final long serialVersionUID = 0L;

    private static final String PROPERTIES = "properties";
    private static final String NUM_INHERITED_SHARDS = "numInheritedShards";
    private static final String NUM_INHERITED_USERS = "numInheritedUsers";
    private static final String USER_FOLLOWS_SHARD = "userFollowsShard";
    private static final String NUM_FOLLOWERS = "numFollowers";

    // Properties of shard vertex
    String shardName;
    String shardFriendlyName;
    String shardAvatarFilename;
    String shardBannerFilename;
    String shardDescription;
    Date createdAt;

    // Derived from edges
    long numInheritedShards;
    long numInheritedUsers;
    boolean userFollowsShard;
    Long numFollowers;

    public Shard(final Map<String, Object> graphShardMap) {
        this.numInheritedShards = (long) graphShardMap.get(NUM_INHERITED_SHARDS);
        this.numInheritedUsers = (long) graphShardMap.get(NUM_INHERITED_USERS);
        this.userFollowsShard = (long) graphShardMap.get(USER_FOLLOWS_SHARD) > 0;
        this.numFollowers = (Long) graphShardMap.get(NUM_FOLLOWERS);

        final Map<String, Object> shardProperties = (Map<String, Object>) graphShardMap.get(PROPERTIES);
        this.shardName = (String) shardProperties.get(SHARD_NAME_PROPERTY);
        this.shardFriendlyName = (String) shardProperties.get(SHARD_FRIENDLY_NAME_PROPERTY);
        this.shardAvatarFilename = (String) shardProperties.get(SHARD_AVATAR_FILENAME_PROPERTY);
        this.shardBannerFilename = (String) shardProperties.get(SHARD_BANNER_FILENAME_PROPERTY);
        this.shardDescription = (String) shardProperties.get(SHARD_DESCRIPTION_PROPERTY);
        this.createdAt = (Date) shardProperties.get(COMMON_CREATED_AT_PROPERTY);
    }

    public static GraphTraversal<Object, Map<String, Object>> projectToSingleShard(final String shardName,
                                                                                   final String callingUsername) {
        return project(PROPERTIES, NUM_INHERITED_SHARDS, NUM_INHERITED_USERS, USER_FOLLOWS_SHARD, NUM_FOLLOWERS)
            .by(valueMap().by(unfold()))
            .by(out(SHARD_INHERITS_SHARD_EDGE_LABEL).count())
            .by(out(SHARD_INHERITS_USER_EDGE_LABEL).count())
            .by(
                in(USER_FOLLOWS_SHARD_EDGE_LABEL)
                    .has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, callingUsername)
                    .count()
            )
            .by(
                V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardName)
                    .emit()
                    .repeat(in(SHARD_INHERITS_SHARD_EDGE_LABEL))
                    .in(USER_FOLLOWS_SHARD_EDGE_LABEL)
                    .dedup()
                    .count()
            );
    }

    public static GraphTraversal<Object, Map<String, Object>> projectToShard(final String callingUsername) {
        return project(PROPERTIES, NUM_INHERITED_SHARDS, NUM_INHERITED_USERS, USER_FOLLOWS_SHARD)
            .by(valueMap().by(unfold()))
            .by(out(SHARD_INHERITS_SHARD_EDGE_LABEL).count())
            .by(out(SHARD_INHERITS_USER_EDGE_LABEL).count())
            .by(
                in(USER_FOLLOWS_SHARD_EDGE_LABEL)
                    .has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, callingUsername)
                    .count()
            );
    }
}
