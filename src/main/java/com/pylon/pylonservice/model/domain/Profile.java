package com.pylon.pylonservice.model.domain;

import lombok.Data;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import static com.pylon.pylonservice.constants.GraphConstants.COMMON_CREATED_AT_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_INHERITS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.SHARD_INHERITS_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_AVATAR_FILENAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_BANNER_FILENAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_BIO_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_DISCORD_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FACEBOOK_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FOLLOWS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FOLLOWS_USER_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_INSTAGRAM_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_LOCATION_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_OWNS_SHARD_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_PINNED_POST_EDGE_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_TIKTOK_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_TWITCH_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_TWITTER_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_FRIENDLY_NAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_USERNAME_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERIFIED_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_VERTEX_LABEL;
import static com.pylon.pylonservice.constants.GraphConstants.USER_WEBSITE_URL_PROPERTY;
import static com.pylon.pylonservice.constants.GraphConstants.USER_YOUTUBE_URL_PROPERTY;
import static com.pylon.pylonservice.model.domain.Post.projectToPost;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.V;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.in;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.project;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.valueMap;

/**
 * {
 *     "username": "jason",
 *     "userFriendlyName": "Jason Bohrer",
 *     "userAvatarFilename": "5237af6c-6cf7-46ee-8537-f0b1b90d870a.png",
 *     "userBannerFilename": "5237af6c-6cf7-46ee-8537-f0b1b90d870a.jpg",
 *     "userBio": "My name is Jason and I'm one of the newest in a long line of Bohrers.",
 *     "userLocation": "Seattle, Washington",
 *     "userVerified": true,
 *     "userFacebookUrl": "https://facebook.com/jason.bohrer.10",
 *     "userTwitterUrl": "https://twitter.com/bohrer_jason",
 *     "userInstagramUrl": "https://instagram.com/jnbrex",
 *     "userTwitchUrl": "https://twitch.tv/haste",
 *     "userYoutubeUrl": "https://youtube.com/pewdiepie",
 *     "userTiktokUrl": "https://www.tiktok.com/@charlidamelio",
 *     "userDiscordUrl": "https://discord.gg/pJNRzPR",
 *     "userWebsiteUrl": "https://jnbrex.wordpress.com/",
 *     "createdAt": "2020-08-06T23:05:34.206+00:00",
 *     "numOwnedShards": "14",
 *     "numFollowedShards": "121",
 *     "userIsFollowed": true,
 *     "pinnedPost": {@link Post},
 *     "numFollowers": "328",
 *     "numFollowed": "316"
 * }
 */
@Data
public class Profile implements Serializable {
    private static final long serialVersionUID = 0L;

    private static final String PROPERTIES = "properties";
    private static final String NUM_FOLLOWERS = "numFollowers";
    private static final String NUM_FOLLOWED = "numFollowed";
    private static final String NUM_OWNED_SHARDS = "numOwnedShards";
    private static final String NUM_FOLLOWED_SHARDS = "numFollowedShards";
    private static final String USER_IS_FOLLOWED = "userIsFollowed";
    private static final String PINNED_POST = "pinnedPost";

    // Properties of profile vertex
    final String username;
    final String userFriendlyName;
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
    final String userDiscordUrl;
    final String userWebsiteUrl;
    final Date createdAt;

    // Derived from edges
    long numOwnedShards;
    long numFollowedShards;
    boolean userIsFollowed;
    Object pinnedPost;
    Long numFollowers;
    Long numFollowed;

    public Profile(final Map<String, Object> graphProfileMap) {
        this.numOwnedShards = (long) graphProfileMap.get(NUM_OWNED_SHARDS);
        this.numFollowedShards = (long) graphProfileMap.get(NUM_FOLLOWED_SHARDS);
        this.userIsFollowed = (long) graphProfileMap.get(USER_IS_FOLLOWED) > 0;
        this.numFollowers = (Long) graphProfileMap.get(NUM_FOLLOWERS);
        this.numFollowed = (Long) graphProfileMap.get(NUM_FOLLOWED);

        final Collection<Object> pinnedPosts = (Collection<Object>) graphProfileMap.get(PINNED_POST);
        this.pinnedPost = pinnedPosts.size() > 0 ? pinnedPosts.iterator().next() : null;

        final Map<String, Object> profileProperties = (Map<String, Object>) graphProfileMap.get(PROPERTIES);
        this.username = (String) profileProperties.get(USER_USERNAME_PROPERTY);
        this.userFriendlyName = (String) profileProperties.get(USER_FRIENDLY_NAME_PROPERTY);
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
        this.userDiscordUrl = (String) profileProperties.get(USER_DISCORD_URL_PROPERTY);
        this.userWebsiteUrl = (String) profileProperties.get(USER_WEBSITE_URL_PROPERTY);
        this.createdAt = (Date) profileProperties.get(COMMON_CREATED_AT_PROPERTY);
    }

    public static GraphTraversal<Object, Map<String, Object>> projectToSingleProfile(final String profileUsername,
                                                                                     final String callingUsername) {
        return project(PROPERTIES, NUM_OWNED_SHARDS, NUM_FOLLOWED_SHARDS, USER_IS_FOLLOWED, PINNED_POST, NUM_FOLLOWERS,
            NUM_FOLLOWED)
            .by(valueMap().by(unfold()))
            .by(out(USER_OWNS_SHARD_EDGE_LABEL).count())
            .by(out(USER_FOLLOWS_SHARD_EDGE_LABEL).count())
            .by(
                in(USER_FOLLOWS_USER_EDGE_LABEL)
                .has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, callingUsername)
                .count()
            )
            .by(out(USER_PINNED_POST_EDGE_LABEL).flatMap(projectToPost(callingUsername)).fold())
            .by(
                V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, profileUsername)
                    .emit()
                    .repeat(in(SHARD_INHERITS_USER_EDGE_LABEL, SHARD_INHERITS_SHARD_EDGE_LABEL))
                    .in(USER_FOLLOWS_SHARD_EDGE_LABEL, USER_FOLLOWS_USER_EDGE_LABEL)
                    .dedup()
                    .has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, P.without(profileUsername))
                    .count()
            )
            .by(
                V().has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, profileUsername)
                    .out(USER_FOLLOWS_SHARD_EDGE_LABEL, USER_FOLLOWS_USER_EDGE_LABEL)
                    .emit()
                    .repeat(out(SHARD_INHERITS_USER_EDGE_LABEL, SHARD_INHERITS_SHARD_EDGE_LABEL))
                    .dedup()
                    .has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, P.without(profileUsername))
                    .count()
            );
    }

    public static GraphTraversal<Object, Map<String, Object>> projectToProfile(final String callingUsername) {
        return project(PROPERTIES, NUM_OWNED_SHARDS, NUM_FOLLOWED_SHARDS, USER_IS_FOLLOWED, PINNED_POST)
            .by(valueMap().by(unfold()))
            .by(out(USER_OWNS_SHARD_EDGE_LABEL).count())
            .by(out(USER_FOLLOWS_SHARD_EDGE_LABEL).count())
            .by(
                in(USER_FOLLOWS_USER_EDGE_LABEL)
                    .has(USER_VERTEX_LABEL, USER_USERNAME_PROPERTY, callingUsername)
                    .count()
            )
            .by(out(USER_PINNED_POST_EDGE_LABEL).flatMap(projectToPost(callingUsername)).fold());
    }
}
