package com.pylon.pylonservice.model.domain.notification;

import lombok.AllArgsConstructor;

import java.util.Date;

@AllArgsConstructor
public abstract class Notification {
    protected final String notificationId;
    protected final String toUsername;
    protected final Date createdAt;
    protected final String fromUsername;
    protected final boolean isRead;

    public abstract com.pylon.pylonservice.model.tables.Notification toDatabaseNotification();
}
