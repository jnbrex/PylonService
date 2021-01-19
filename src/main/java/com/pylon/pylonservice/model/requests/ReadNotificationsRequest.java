package com.pylon.pylonservice.model.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * {
 *     "notificationIds": {
 *         "notificationId1",
 *         "notificationId2",
 *         "notificationId3"
 *     }
 * }
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadNotificationsRequest {
    Set<String> notificationIds;
}
