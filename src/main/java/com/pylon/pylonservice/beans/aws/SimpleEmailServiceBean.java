package com.pylon.pylonservice.beans.aws;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class SimpleEmailServiceBean {
    @Bean
    public AmazonSimpleEmailService amazonSimpleEmailService() {
        // Credentials provided by the AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY environment variables.
        // Region provided by the AWS_REGION environment variable.
        return AmazonSimpleEmailServiceClientBuilder.defaultClient();
    }
}
