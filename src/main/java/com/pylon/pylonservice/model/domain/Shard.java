package com.pylon.pylonservice.model.domain;

import lombok.Data;
import org.apache.tinkerpop.gremlin.process.traversal.P;
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
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.V;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.in;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.project;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.valueMap;

@Data
public class Shard implements Serializable {
    private static final long serialVersionUID = 0L;

    private static final String PROPERTIES = "properties";
    private static final String NUM_FOLLOWERS = "numFollowers";
    private static final String NUM_INHERITED_SHARDS = "numInheritedShards";
    private static final String NUM_INHERITED_USERS = "numInheritedUsers";

    // Properties of shard vertex
    String shardName;
    String shardFriendlyName;
    String shardAvatarFilename;
    String shardBannerFilename;
    String shardDescription;
    Date createdAt;

    // Derived from edges
    long numFollowers;
    long numInheritedShards;
    long numInheritedUsers;

    public Shard(final Map<String, Object> graphShardMap) {
        this.numFollowers = (long) graphShardMap.get(NUM_FOLLOWERS);
        this.numInheritedShards = (long) graphShardMap.get(NUM_INHERITED_SHARDS);
        this.numInheritedUsers = (long) graphShardMap.get(NUM_INHERITED_USERS);

        final Map<String, Object> shardProperties = (Map<String, Object>) graphShardMap.get(PROPERTIES);
        this.shardName = (String) shardProperties.get(SHARD_NAME_PROPERTY);
        this.shardFriendlyName = (String) shardProperties.get(SHARD_FRIENDLY_NAME_PROPERTY);
        this.shardAvatarFilename = (String) shardProperties.get(SHARD_AVATAR_FILENAME_PROPERTY);
        this.shardBannerFilename = (String) shardProperties.get(SHARD_BANNER_FILENAME_PROPERTY);
        this.shardDescription = (String) shardProperties.get(SHARD_DESCRIPTION_PROPERTY);
        this.createdAt = (Date) shardProperties.get(COMMON_CREATED_AT_PROPERTY);
    }

    public static GraphTraversal<Object, Map<String, Object>> projectToShard(final String shardName) {
        return project(PROPERTIES, NUM_FOLLOWERS, NUM_INHERITED_SHARDS, NUM_INHERITED_USERS)
            .by(valueMap().by(unfold()))
            .by(
                V().has(SHARD_VERTEX_LABEL, SHARD_NAME_PROPERTY, shardName)
                    .emit()
                    .repeat(in(SHARD_INHERITS_SHARD_EDGE_LABEL))
                    .in(USER_FOLLOWS_SHARD_EDGE_LABEL)
                    .dedup()
                    .count()
            )
            .by(out(SHARD_INHERITS_SHARD_EDGE_LABEL).count())
            .by(out(SHARD_INHERITS_USER_EDGE_LABEL).count());
    }
}
