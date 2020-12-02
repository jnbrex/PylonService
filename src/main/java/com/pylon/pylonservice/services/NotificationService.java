package com.pylon.pylonservice.services;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.pylon.pylonservice.model.domain.notification.Notification;
import com.pylon.pylonservice.model.tables.DatabaseNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private static final String USER_NOTIFICATION_GLOBAL_SECONDARY_INDEX = "UserNotification";

    @Value("${environment.name}")
    private String environmentName;
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

    public List<Notification> retrieveAllNotifications(final String username) {
        final Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":v_username", new AttributeValue().withS(username));

        final DynamoDBQueryExpression<DatabaseNotification> queryExpression =
            new DynamoDBQueryExpression<DatabaseNotification>()
                .withKeyConditionExpression("toUsername = :v_username").withExpressionAttributeValues(eav)
                .withIndexName(USER_NOTIFICATION_GLOBAL_SECONDARY_INDEX)
                .withScanIndexForward(false)
                .withConsistentRead(false);

        List<DatabaseNotification> databaseNotifications =
            dynamoDBMapper.query(DatabaseNotification.class, queryExpression);

        return databaseNotifications.stream().map(Notification::fromDatabaseNotification).collect(Collectors.toList());
    }

    public Set<Notification> loadNotifications(final Set<String> notificationIds) {
        final Set<DatabaseNotification> databaseNotifications = notificationIds
            .stream()
            .map(
                notificationId -> DatabaseNotification.builder()
                    .notificationId(notificationId)
                    .build()
            ).collect(Collectors.toSet());

        final Map<String, List<Object>> loadedDatabaseNotificationsMap =
            dynamoDBMapper.batchLoad(databaseNotifications);

        final String notificationsTableName = String.format("%s-%s", environmentName, "Notification");
        final List<Object> loadedObjects = loadedDatabaseNotificationsMap.get(notificationsTableName);
        final Set<DatabaseNotification> loadedDatabaseNotifications = loadedObjects
            .stream()
            .map(obj -> (DatabaseNotification) obj)
            .collect(Collectors.toSet());

        return loadedDatabaseNotifications.stream()
            .map(Notification::fromDatabaseNotification)
            .collect(Collectors.toSet());
    }
}
