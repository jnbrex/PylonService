package com.pylon.pylonservice.controller;

import com.pylon.pylonservice.model.domain.notification.Notification;
import com.pylon.pylonservice.model.requests.ReadNotificationsRequest;
import com.pylon.pylonservice.services.AccessTokenService;
import com.pylon.pylonservice.services.MetricsService;
import com.pylon.pylonservice.services.NotificationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

import static com.pylon.pylonservice.constants.AuthenticationConstants.ACCESS_TOKEN_COOKIE_NAME;

@Log4j2
@RestController
public class NotificationController {
    private static final String GET_NOTIFICATIONS_METRIC_NAME = "GetNotifications";
    private static final String READ_NOTIFICATIONS_METRIC_NAME = "ReadNotifications";
    @Autowired
    private AccessTokenService accessTokenService;
    @Autowired
    private MetricsService metricsService;
    @Autowired
    private NotificationService notificationService;

    /**
     * Call to retrieve all notifications for the calling user.
     *
     * @param accessToken A cookie with name "accessToken"
     *
     * @return HTTP 200 OK - If the notifications were retrieved successfully.
     */
    @GetMapping(value = "/notifications/all")
    public ResponseEntity<?> getAllNotifications(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME) final String accessToken,
        @RequestParam(required = false) final Integer lastNotificationId) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(GET_NOTIFICATIONS_METRIC_NAME);

        final String callingUsernameLowercase = accessTokenService.getUsernameFromAccessToken(accessToken);

        final List<Notification> notifications = notificationService.retrieveAllNotifications(callingUsernameLowercase);

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(notifications);

        metricsService.addSuccessMetric(GET_NOTIFICATIONS_METRIC_NAME);
        metricsService.addLatencyMetric(GET_NOTIFICATIONS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }

    /**
     * Call to mark notifications as read.
     *
     * @param accessToken A cookie with name "accessToken"
     *
     * @return HTTP 200 OK - If the notifications were marked as read successfully.
     *         HTTP 401 Unauthorized - If the User isn't authenticated.
     *         HTTP 403 Forbidden - If the User is attempting to read a notification for another user.
     */
    @PutMapping(value = "/notifications/read")
    public ResponseEntity<?> readNotifications(
        @CookieValue(name = ACCESS_TOKEN_COOKIE_NAME) final String accessToken,
        @RequestBody final ReadNotificationsRequest readNotificationsRequest) {
        final long startTime = System.nanoTime();
        metricsService.addCountMetric(READ_NOTIFICATIONS_METRIC_NAME);

        final String callingUsernameLowercase = accessTokenService.getUsernameFromAccessToken(accessToken);

        final Set<Notification> notifications =
            notificationService.loadNotifications(readNotificationsRequest.getNotificationIds());

        if (!notifications.stream()
            .allMatch(notification -> notification.getToUsername().equals(callingUsernameLowercase))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        notifications.forEach(notification -> notification.setRead(true));
        notificationService.notifyBatch(notifications);

        final ResponseEntity<?> responseEntity = ResponseEntity.ok().body(notifications);

        metricsService.addSuccessMetric(READ_NOTIFICATIONS_METRIC_NAME);
        metricsService.addLatencyMetric(READ_NOTIFICATIONS_METRIC_NAME, System.nanoTime() - startTime);
        return responseEntity;
    }
}
