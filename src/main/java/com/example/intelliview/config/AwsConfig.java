package com.example.intelliview.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.s3.S3Client;

import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

    @Value("${AWS_ACCESS_KEY}")
    private String accessKey;
    @Value("${AWS_SECRET_KEY}")
    private String secretKey;
    @Value("${AWS_REGION}")
    private String region;
    @Value("${AWS_ACCOUNT_ID}")
    private String accountId;

    @Bean
    public BedrockRuntimeClient bedrockRuntimeClient() {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        return BedrockRuntimeClient.builder()
            .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
            .region(Region.of(region))
            .build();
    }

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        return S3Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
            .region(Region.of(region))
            .build();
    }

    @Bean
    public BedrockAgentRuntimeClient bedrockAgentRuntimeClient() {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        return BedrockAgentRuntimeClient.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
            .build();
    }
}
