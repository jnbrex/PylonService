package com.pylon.pylonservice.controller;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.pylon.pylonservice.model.requests.ForgotPasswordRequest;
import com.pylon.pylonservice.model.requests.ResetPasswordRequest;
import com.pylon.pylonservice.model.tables.EmailUser;
import com.pylon.pylonservice.model.tables.PasswordReset;
import com.pylon.pylonservice.model.tables.User;
import com.pylon.pylonservice.util.MetricsUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@Log4j2
@RestController
public class PasswordController {
    private static final String SEND_FORGOT_PASSWORD_EMAIL_METRIC_NAME = "SendForgotPasswordEmail";
    private static final String FORGOT_PASSWORD_EMAIL_SUBJECT = "Reset your Pylon password";
    private static final String FORGOT_PASSWORD_EMAIL_BODY = "Click the link to reset your Pylon password.\n\n%s\n\n" +
        "This link will expire in 15 minutes. If you didn't make this " +
        "request, no action is needed and your account is secure.";
    private static final long FIFTEEN_MINUTES_IN_SECONDS = 60 * 15;

    @Autowired
    private AmazonSimpleEmailService ses;
    @Autowired
    private DynamoDBMapper dynamoDBMapper;
    @Autowired
    private MetricsUtil metricsUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${from.email.address}")
    private String fromEmailAddress;
    private String pylonForgotPasswordBaseUrl;

    PasswordController(@Value("${environment.name}") final String environmentName) {
        if (environmentName.equals("prod")) {
            this.pylonForgotPasswordBaseUrl = "https://pylon.gg/forgotPassword?";
        } else {
            this.pylonForgotPasswordBaseUrl = String.format("https://%s.pylon.gg/forgotPassword?", environmentName);
        }
    }

    /**
     *
     * @param forgotPasswordRequest Contains the email address of the user who forgot their password.
     * @return 200 OK - Regardless of whether the email was sent or not.
     */
    @PostMapping(value = "/password/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody final ForgotPasswordRequest forgotPasswordRequest) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(SEND_FORGOT_PASSWORD_EMAIL_METRIC_NAME);

        final String emailLowercase = forgotPasswordRequest.getToEmailAddress().toLowerCase();
        final EmailUser emailUser = dynamoDBMapper.load(EmailUser.class, emailLowercase);

        if (emailUser == null) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        final String passwordResetToken = UUID.randomUUID().toString();

        dynamoDBMapper.save(
            PasswordReset.builder()
                .passwordResetToken(passwordResetToken)
                .username(emailUser.getUsername())
                .ttl(Instant.now().getEpochSecond() + FIFTEEN_MINUTES_IN_SECONDS)
                .build()
        );

        SendEmailRequest request = new SendEmailRequest()
            .withDestination(
                new Destination().withToAddresses(forgotPasswordRequest.getToEmailAddress()))
            .withMessage(new Message()
                .withBody(
                    new Body()
                        .withText(
                            new Content()
                                .withCharset("UTF-8")
                                .withData(
                                    String.format(
                                        FORGOT_PASSWORD_EMAIL_BODY,
                                        pylonForgotPasswordBaseUrl + passwordResetToken
                                    )
                                )
                        )
                )
                .withSubject(new Content()
                    .withCharset("UTF-8").withData(FORGOT_PASSWORD_EMAIL_SUBJECT)))
            .withSource(fromEmailAddress);

        try {
            ses.sendEmail(request);
        } catch (final Exception e) {
            log.error(String.format("Sending email failed for ForgotPasswordRequest %s", forgotPasswordRequest), e);
        }

        metricsUtil.addSuccessMetric(SEND_FORGOT_PASSWORD_EMAIL_METRIC_NAME);
        metricsUtil.addLatencyMetric(SEND_FORGOT_PASSWORD_EMAIL_METRIC_NAME, System.nanoTime() - startTime);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     *
     * @param resetPasswordRequest Contains the email address of the user who forgot their password.
     * @return 200 OK - If the user's password was changed successfully.
     *         404 Not Found - If the password reset token wasn't found or expired.
     */
    @PostMapping(value = "/password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody final ResetPasswordRequest resetPasswordRequest) {
        final long startTime = System.nanoTime();
        metricsUtil.addCountMetric(SEND_FORGOT_PASSWORD_EMAIL_METRIC_NAME);

        final PasswordReset passwordReset =
            dynamoDBMapper.load(PasswordReset.class, resetPasswordRequest.getPasswordResetToken());

        if (passwordReset == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        final User user = dynamoDBMapper.load(User.class, passwordReset.getUsername());
        user.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));

        dynamoDBMapper.save(user);
        dynamoDBMapper.delete(passwordReset);

        metricsUtil.addSuccessMetric(SEND_FORGOT_PASSWORD_EMAIL_METRIC_NAME);
        metricsUtil.addLatencyMetric(SEND_FORGOT_PASSWORD_EMAIL_METRIC_NAME, System.nanoTime() - startTime);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
