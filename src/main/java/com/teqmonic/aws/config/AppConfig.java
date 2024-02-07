package com.teqmonic.aws.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.teqmonic.aws.dao.S3BucketBuilder;
import com.teqmonic.aws.dao.S3ItemDao;

import io.micrometer.common.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AppConfig {

	@Bean
	Region awsRegion(@Value("${aws.region}") String regionString) {
		return Region.of(regionString);
	}

	@Bean
	AwsCredentialsProvider awsCredentialsProvider(@Value("${aws.accessKeyId}") String accessKeyId,
			@Value("${aws.secretAccessKey}") String secretAccessKey) {
		return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
	}

	@Bean
	S3Client s3Client(Region region, AwsCredentialsProvider awsCredentialsProvider,
			@Value("${aws.endpointOverride:#{null}}") String endpointOverride) {

		S3ClientBuilder builder = S3Client.builder().region(region).credentialsProvider(awsCredentialsProvider);
		// Integration with LocalStack
		if (StringUtils.isNotBlank(endpointOverride)) {
			builder.endpointOverride(URI.create(endpointOverride))
					.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
		}

		return builder.build();
	}
	
	@Bean
	S3Presigner s3Presigner(Region region, AwsCredentialsProvider awsCredentialsProvider,
			@Value("${aws.endpointOverride:#{null}}") String endpointOverride) {

		S3Presigner.Builder builder = S3Presigner.builder().region(region).credentialsProvider(awsCredentialsProvider);

		if (StringUtils.isNotBlank(endpointOverride)) {
			builder.endpointOverride(URI.create(endpointOverride))
					.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
		}

		return builder.build();
	}

	@Bean
	S3ItemDao s3ItemDao(S3Client s3Client, S3Presigner s3Presigner, @Value("${aws.s3.bucketName}") String bucketName,
			@Value("${aws.s3.preSigner.expireInMinutes:2}") String expireInMinutes) {
		return new S3ItemDao(s3Client, s3Presigner, bucketName, Integer.parseInt(expireInMinutes));
	}

	@Bean
	S3BucketBuilder s3BucketBuilder(S3Client s3Client, @Value("${aws.s3.bucketName}") String bucketName) {
		return new S3BucketBuilder(s3Client, bucketName);
	}

}
