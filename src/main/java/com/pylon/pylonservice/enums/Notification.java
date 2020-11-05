package com.pylon.pylonservice.enums;

public enum Notification {
    POST_LIKE(1),
    POST_COMMENT(2),
    PROFILE_FOLLOW(3),
    OWNED_SHARD_INCLUSION(4);

    private final int value;

    Notification(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
