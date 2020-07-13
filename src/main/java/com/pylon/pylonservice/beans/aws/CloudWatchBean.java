package com.pylon.pylonservice.beans.aws;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class CloudWatchBean {
    @Bean
    public AmazonCloudWatch amazonCloudWatch() {
        return AmazonCloudWatchClientBuilder.defaultClient();
    }
}
