package com.pylon.pylonservice.model.tables;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.Set;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Setter
@DynamoDBTable(tableName = "-Profile")
public class Profile {
    @NonNull
    String userId;
    @NonNull
    String username;

    String avatarUrl;
    String bio;

    // Social media urls
    String facebookUrl;
    String twitterUrl;
    String instagramUrl;
    String twitchUrl;
    String youtubeUrl;
    String mixerUrl;

    Set<String> posts; // postId of Posts that are posted to this Profile
    Set<String> followers; // userId of Users that follow this Profile
    Set<String> follows; // userId of Profiles that this Profile's User follows
    Set<String> followedShards; // shardId of public Shards that this User follows directly
    Set<String> ownedShards; // shardId of public Shards that this User owns

    @DynamoDBHashKey
    public String getUserId() {
        return userId;
    }

    @DynamoDBAttribute
    public String getUsername() {
        return username;
    }

    @DynamoDBAttribute
    public String getAvatarUrl() {
        return avatarUrl;
    }

    @DynamoDBAttribute
    public String getBio() {
        return bio;
    }

    @DynamoDBAttribute
    public String getFacebookUrl() {
        return facebookUrl;
    }

    @DynamoDBAttribute
    public String getTwitterUrl() {
        return twitterUrl;
    }

    @DynamoDBAttribute
    public String getInstagramUrl() {
        return instagramUrl;
    }

    @DynamoDBAttribute
    public String getTwitchUrl() {
        return twitchUrl;
    }

    @DynamoDBAttribute
    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    @DynamoDBAttribute
    public String getMixerUrl() {
        return mixerUrl;
    }

    @DynamoDBAttribute
    public Set<String> getPosts() {
        return posts;
    }

    @DynamoDBAttribute
    public Set<String> getFollowers() {
        return followers;
    }

    @DynamoDBAttribute
    public Set<String> getFollowedShards() {
        return followedShards;
    }

    @DynamoDBAttribute
    public Set<String> getOwnedShards() {
        return ownedShards;
    }
}