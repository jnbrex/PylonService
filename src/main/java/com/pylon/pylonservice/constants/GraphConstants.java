package com.pylon.pylonservice.constants;

public final class GraphConstants {
    /*
     * Vertex labels
     */
    public static final String USER_VERTEX_LABEL = "user";
    public static final String SHARD_VERTEX_LABEL = "shard";
    public static final String POST_VERTEX_LABEL = "post";

    /*
     * Edge labels
     */
    // User -> ?
    public static final String USER_FOLLOWS_USER_EDGE_LABEL = "userFollowsUser";
    public static final String USER_SUBMITTED_POST_EDGE_LABEL = "userSubmittedPost";
    public static final String USER_UPVOTED_POST_EDGE_LABEL = "userUpvotedPost";
    public static final String USER_FOLLOWS_SHARD_EDGE_LABEL = "userFollowsShard";
    public static final String USER_OWNS_SHARD_EDGE_LABEL = "userOwnsShard";
    public static final String USER_PINNED_POST_EDGE_LABEL = "userPinnedPost";

    // Shard -> ?
    public static final String SHARD_INHERITS_SHARD_EDGE_LABEL = "shardInheritsShard";
    public static final String SHARD_INHERITS_USER_EDGE_LABEL = "shardInheritsUser";

    // Post -> ?
    public static final String POST_POSTED_IN_SHARD_EDGE_LABEL = "postPostedInShard";
    public static final String POST_POSTED_IN_USER_EDGE_LABEL = "postPostedInUser";
    public static final String POST_COMMENT_ON_POST_EDGE_LABEL = "postCommentOnPost";

    /*
     * Property labels
     */
    // Common
    public static final String COMMON_CREATED_AT_PROPERTY = "createdAt";

    // User
    public static final String USER_USERNAME_PROPERTY = "username"; // Unique identifier of a User
    public static final String USER_FRIENDLY_NAME_PROPERTY = "userFriendlyName";
    public static final String USER_AVATAR_FILENAME_PROPERTY = "userAvatarFilename";
    public static final String USER_BANNER_FILENAME_PROPERTY = "userBannerFilename";
    public static final String USER_BIO_PROPERTY = "userBio";
    public static final String USER_LOCATION_PROPERTY = "userLocation";
    public static final String USER_VERIFIED_PROPERTY = "userVerified";
    public static final String USER_FEATURED_PROPERTY = "userFeatured";
    public static final String USER_UPLOADED_IMAGES_PROPERTY = "userUploadedImages";

    // Urls
    public static final String USER_FACEBOOK_URL_PROPERTY = "userFacebookUrl";
    public static final String USER_TWITTER_URL_PROPERTY = "userTwitterUrl";
    public static final String USER_INSTAGRAM_URL_PROPERTY = "userInstagramUrl";
    public static final String USER_TWITCH_URL_PROPERTY = "userTwitchUrl";
    public static final String USER_YOUTUBE_URL_PROPERTY = "userYoutubeUrl";
    public static final String USER_TIKTOK_URL_PROPERTY = "userTiktokUrl";
    public static final String USER_DISCORD_URL_PROPERTY = "userDiscordUrl";
    public static final String USER_WEBSITE_URL_PROPERTY = "userWebsiteUrl";

    // Shard
    public static final String SHARD_NAME_PROPERTY = "shardName"; // unique identifier of a Shard
    public static final String SHARD_FRIENDLY_NAME_PROPERTY = "shardFriendlyName";
    public static final String SHARD_AVATAR_FILENAME_PROPERTY = "shardAvatarFilename";
    public static final String SHARD_BANNER_FILENAME_PROPERTY = "shardBannerFilename";
    public static final String SHARD_DESCRIPTION_PROPERTY = "shardDescription";
    public static final String SHARD_FEATURED_IMAGE_FILENAME_PROPERTY = "shardFeaturedImageFilename";
    public static final String SHARD_FEATURED_IMAGE_LINK_PROPERTY = "shardFeaturedImageLink";
    public static final String SHARD_RULES_PROPERTY = "shardRules";
    public static final String SHARD_ADDITIONAL_LINKS_PROPERTY = "shardAdditionalLinks";
    public static final String SHARD_FEATURED_PROPERTY = "shardFeatured";

    // Post
    public static final String POST_ID_PROPERTY = "postId"; // unique identifier of a Post
    public static final String POST_TITLE_PROPERTY = "postTitle";
    public static final String POST_FILENAME_PROPERTY = "postFilename";
    public static final String POST_CONTENT_URL_PROPERTY = "postContentUrl";
    public static final  String POST_BODY_PROPERTY = "postBody";

    /*
     * Miscellaneous
     */
    public static final String INVALID_USERNAME_VALUE = "a";

    private GraphConstants() {}
}
