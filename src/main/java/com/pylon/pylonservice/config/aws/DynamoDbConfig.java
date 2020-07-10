package com.pylon.pylonservice.config.aws;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class DynamoDbConfig {
    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        // Credentials provided by the AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY environment variables.
        // Region provided by the AWS_REGION environment variable.
        return AmazonDynamoDBClientBuilder.standard().build();
    }

    @Bean
    public DynamoDBMapper dynamoDBMapper(final AmazonDynamoDB amazonDynamoDB,
                                         @Value("${environment.name}") final String environmentName) {
        final DynamoDBMapperConfig dynamoDBMapperConfig = DynamoDBMapperConfig.builder()
            .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNamePrefix(environmentName))
            .build();
        return new DynamoDBMapper(amazonDynamoDB, dynamoDBMapperConfig);
    }
}
