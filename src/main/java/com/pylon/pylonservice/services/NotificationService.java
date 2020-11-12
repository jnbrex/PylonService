package com.pylon.pylonservice.services;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.pylon.pylonservice.model.domain.notification.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    public void notify(final Notification notification) {
        dynamoDBMapper.save(notification.toDatabaseNotification());
    }

    public void notifyBatch(final Set<Notification> notifications) {
        dynamoDBMapper.batchSave(
            notifications.stream().map(Notification::toDatabaseNotification).collect(Collectors.toSet())
        );
    }
}
