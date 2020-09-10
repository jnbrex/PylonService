package com.pylon.pylonservice.model.requests.shard;

import org.assertj.core.api.Assertions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ShardRequestTests {
    static final String VALID_SHARD_NAME = "Fortnite";
    static final String VALID_SHARD_FRIENDLY_NAME = "Fortnite Shard 1";
    static final String VALID_SHARD_AVATAR_FILENAME_PNG = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.png";
    static final String VALID_SHARD_AVATAR_FILENAME_JPG = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.jpg";
    static final String VALID_SHARD_AVATAR_FILENAME_GIF = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.gif";
    static final String VALID_SHARD_BANNER_FILENAME_PNG = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.png";
    static final String VALID_SHARD_BANNER_FILENAME_JPG = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.jpg";
    static final String VALID_SHARD_BANNER_FILENAME_GIF = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.gif";
    static final String VALID_SHARD_DESCRIPTION = "This is the fortnite shard. Be nice and prosper";
    static final Collection<String> VALID_INHERITED_SHARD_NAMES =
        Stream.of("shard0", "shard1").collect(Collectors.toSet());
    static final Collection<String> VALID_INHERITED_USERS =
        Stream.of("user0", "user1").collect(Collectors.toList());
    static final Collection<String> VALID_INHERITED_SHARD_NAMES_EMPTY = new HashSet<>();
    static final Collection<String> VALID_INHERITED_USERS_EMPTY = new ArrayList<>();

    static final String INVALID_SHARD_NAME_TOO_LONG = "a".repeat(25);
    static final String INVALID_SHARD_NAME_CONTAINS_SPACE = "shard name";
    static final String INVALID_SHARD_NAME_CONTAINS_NON_ALPHANUMERIC = "shard$";
    static final String INVALID_SHARD_FRIENDLY_NAME_TOO_LONG = "a".repeat(65);
    static final String INVALID_SHARD_FRIENDLY_NAME_CONTAINS_NON_ALPHANUMERIC = "shard frie*ndly ";
    static final String INVALID_SHARD_AVATAR_FILENAME_NON_UUID = "alskjfalkjwfoijwajfoiaj";
    static final String INVALID_SHARD_AVATAR_FILENAME_BAD_EXTENSION = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.tiff";
    static final String INVALID_SHARD_BANNER_FILENAME_NON_UUID = "not a uuid";
    static final String INVALID_SHARD_BANNER_FILENAME_BAD_EXTENSION = "5237af6c-6cf7-46ee-8537-f0b1b90d870a.jpeg";
    static final String INVALID_SHARD_DESCRIPTION_TOO_LONG = "a".repeat(151);
    static final Collection<String> INVALID_INHERITED_SHARD_NAMES_NULL = null;
    static final Collection<String> INVALID_INHERITED_USERS_NULL = null;

    void testValidShardRequests(final ShardRequest shardRequest) {
        Assertions.assertThat(shardRequest.isValid()).isTrue();
    }

    void testInvalidShardRequests(final ShardRequest shardRequest) {
        Assertions.assertThat(shardRequest.isValid()).isFalse();
    }
}
