package com.pylon.pylonservice.beans.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class S3Bean {
    @Bean
    public AmazonS3 amazonS3() {
        // Credentials provided by the AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY environment variables.
        // Region provided by the AWS_REGION environment variable.
        return AmazonS3ClientBuilder.defaultClient();
    }
}
