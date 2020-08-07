package com.pylon.pylonservice.model.domain;

import lombok.Data;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import static com.pylon.pylonservice.constants.GraphConstants.COMMON_CREATED_AT_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_INHERITS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_INHERITS_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_AVATAR_FILENAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_BANNER_FILENAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_BIO_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FACEBOOK_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FOLLOWS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FOLLOWS_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_INSTAGRAM_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_LOCATION_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_OWNS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_TIKTOK_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_TWITCH_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_TWITTER_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_USERNAME_CASE_SENSITIVE_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_USERNAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERIFIED_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERTEX_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_WEBSITE_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_YOUTUBE_URL_PROPERTY;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.V;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.in;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.project;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.valueMap;

@Data
public class Profile implements Serializable {
    private static final long serialVersionUID = 0L;

    private static final String PROPERTIES = "properties";
    private static final String NUM_FOLLOWERS = "numFollowers";
    private static final String NUM_FOLLOWED = "numFollowed";
    private static final String NUM_OWNED_SHARDS = "numOwnedShards";
    private static final String NUM_FOLLOWED_SHARDS = "numFollowedShards";

    // Properties of profile vertex
    final String username;
    final String usernameCaseSensitive;
    final String userAvatarFilename;
    final String userBannerFilename;
    final String userBio;
    final String userLocation;
    final boolean userVerified;
    final String userFacebookUrl;
    final String userTwitterUrl;
    final String userInstagramUrl;
    final String userTwitchUrl;
    final String userYoutubeUrl;
    final String userTiktokUrl;
    final String userWebsiteUrl;
    final Date createdAt;

    // Derived from edges
    long numFollowers;
    long numFollowed;
    long numOwnedShards;
    long numFollowedShards;

    public Profile(final Map<String, Object> graphProfileMap) {
        this.numFollowers = (long) graphProfileMap.get(NUM_FOLLOWERS);
        this.numFollowed = (long) graphProfileMap.get(NUM_FOLLOWED);
        this.numOwnedShards = (long) graphProfileMap.get(NUM_OWNED_SHARDS);
        this.numFollowedShards = (long) graphProfileMap.get(NUM_FOLLOWED_SHARDS);

        final Map<String, Object> profileProperties = (Map<String, Object>) graphProfileMap.get(PROPERTIES);
        this.username = (String) profileProperties.get(USER_USERNAME_PROPERTY);
        this.usernameCaseSensitive = (String) profileProperties.get(USER_USERNAME_CASE_SENSITIVE_PROPERTY);
        this.userAvatarFilename = (String) profileProperties.get(USER_AVATAR_FILENAME_PROPERTY);
        this.userBannerFilename = (String) profileProperties.get(USER_BANNER_FILENAME_PROPERTY);
        this.userBio = (String) profileProperties.get(USER_BIO_PROPERTY);
        this.userLocation = (String) profileProperties.get(USER_LOCATION_PROPERTY);
        this.userVerified = (boolean) profileProperties.get(USER_VERIFIED_PROPERTY);
        this.userFacebookUrl = (String) profileProperties.get(USER_FACEBOOK_URL_PROPERTY);
        this.userTwitterUrl = (String) profileProperties.get(USER_TWITTER_URL_PROPERTY);
        this.userInstagramUrl = (String) profileProperties.get(USER_INSTAGRAM_URL_PROPERTY);
        this.userTwitchUrl = (String) profileProperties.get(USER_TWITCH_URL_PROPERTY);
        this.userYoutubeUrl = (String) profileProperties.get(USER_YOUTUBE_URL_PROPERTY);
        this.userTiktokUrl = (String) profileProperties.get(USER_TIKTOK_URL_PROPERTY);
        this.userWebsiteUrl = (String) profileProperties.get(USER_WEBSITE_URL_PROPERTY);
        this.createdAt = (Date) profileProperties.get(COMMON_CREATED_AT_PROPERTY);
    }

    public static GraphTraversal<Object, Map<String, Object>> projectToProfile(final String username) {
        return project(PROPERTIES, NUM_FOLLOWERS, NUM_FOLLOWED, NUM_OWNED_SHARDS, NUM_FOLLOWED_SHARDS)
            .by(valueMap().by(unfold()))
            .by(
                V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username)
                    .emit()
                    .repeat(in(SHARD_INHERITS_USER_EDGE_LABEL, SHARD_INHERITS_SHARD_EDGE_LABEL))
                    .in(USER_FOLLOWS_SHARD_EDGE_LABEL, USER_FOLLOWS_USER_EDGE_LABEL)
                    .dedup()
                    .has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, P.without(username))
                    .count()
            )
            .by(
                V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, username)
                    .out(USER_FOLLOWS_SHARD_EDGE_LABEL, USER_FOLLOWS_USER_EDGE_LABEL)
                    .emit()
                    .repeat(out(SHARD_INHERITS_USER_EDGE_LABEL, SHARD_INHERITS_SHARD_EDGE_LABEL))
                    .dedup()
                    .has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, P.without(username))
                    .count()
            )
            .by(out(USER_OWNS_SHARD_EDGE_LABEL).count())
            .by(out(USER_FOLLOWS_SHARD_EDGE_LABEL).count());
    }
}
