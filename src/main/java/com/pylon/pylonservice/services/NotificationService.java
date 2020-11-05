package com.pylon.pylonservice.services;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.pylon.pylonservice.model.domain.notification.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    public void notify(final Notification notification) {
        dynamoDBMapper.save(notification.toDatabaseNotification());
    }
}
