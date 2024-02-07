package com.teqmonic.aws.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;

@Slf4j
@RequiredArgsConstructor
public class S3BucketBuilder {
	
	private final S3Client s3Client;
    private final String bucketName;
    
    public void createBucket() {
        S3Waiter s3Waiter = s3Client.waiter();
        
        CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
                .bucket(bucketName)
                .build();

        s3Client.createBucket(bucketRequest);
        
        HeadBucketRequest bucketRequestWait = HeadBucketRequest.builder()
                .bucket(bucketName)
                .build();

        // Wait until the bucket is created and print out the response.
        WaiterResponse<HeadBucketResponse> waiterResponse = s3Waiter.waitUntilBucketExists(bucketRequestWait);
        waiterResponse.matched().response().ifPresent(action -> log.info("bucket creation - {}, bucket name - {}", action, bucketName));
    }
    
    public void reCreateBucket() {
    	log.info("In reCreateBucket, bucket name - {}", bucketName);
        try {
            deleteAllItemsAndBucket();
        } catch (Exception e) {
            // ignore exception as bucket does not exist when test is executed for the first time
        }

        createBucket();
    }
    
    public void deleteAllItemsAndBucket() {
        deleteAllItems();
        deleteBucket();
    }
    
    private void deleteBucket() {
        DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder()
                .bucket(bucketName)
                .build();
        s3Client.deleteBucket(deleteBucketRequest);
        log.info("bucket deleted, bucket name - {}", bucketName);
    }

    private void deleteAllItems() {
        // To delete a bucket, all the objects in the bucket must be deleted first.
        ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();
        ListObjectsV2Response listObjectsV2Response;

        do {
            listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);
            for (S3Object s3Object : listObjectsV2Response.contents()) {
                DeleteObjectRequest request = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Object.key())
                        .build();
                s3Client.deleteObject(request);
            }
        } while (Boolean.TRUE.equals(listObjectsV2Response.isTruncated()));
    }

}
