package com.pylon.pylonservice.enums;

public enum NotificationType {
    POST_LIKE(1),
    POST_COMMENT(2),
    PROFILE_FOLLOW(3),
    OWNED_SHARD_INCLUSION(4),
    PROFILE_INCLUSION(5);

    private final int value;

    NotificationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
