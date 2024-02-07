package com.teqmonic.aws.dao;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;


@Slf4j
@RequiredArgsConstructor
public class S3ItemDao {

	private final S3Client s3Client;
	private final S3Presigner s3Presigner;
	private final String bucketName;
	private final int expireInMinutes;
	
	public List<String> listItems() {
        return listItems("");
    }

    public List<String> listItems(String prefix) {
        ListObjectsRequest listObjects = ListObjectsRequest.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        ListObjectsResponse res = s3Client.listObjects(listObjects);
        return res.contents().stream()
                .map(S3Object::key) //.map(obj -> obj.key())
                .toList();
    }
    
    public Map<String, String> retrieveMetadata(String itemKey) {
        HeadObjectResponse response = s3Client.headObject(HeadObjectRequest.builder()
                        .bucket(bucketName)
                        .key(itemKey)
                        .build());

        return response.metadata();
    }
    
    public URL generatePresignedUrl(String itemKey) {

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expireInMinutes))
                .getObjectRequest(getObjectRequest -> getObjectRequest
                        .bucket(bucketName)
                        .key(itemKey)
                        .responseContentDisposition(String.format("attachment;filename=%s", getFilename(itemKey)))
                )
                .build();

        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
        return presignedGetObjectRequest.url();
    }
    
    @SneakyThrows()
    public void uploadItem(String itemKey, Map<String, String> metadata, MultipartFile file) {
        // First create a multipart upload and get the upload id
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(itemKey)
                .metadata(metadata)
                .build();

        try (InputStream fileInputStream = file.getInputStream()) {
            PutObjectResponse response = s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(fileInputStream, file.getSize()));
            log.info("file upload response {}", response.toString());
        } catch (IOException e) {
            log.error("file upload fail", e);
            throw e;
        }
    }
    
    public byte[] downloadItemData(String itemKey) {
        GetObjectRequest objectRequest = GetObjectRequest
                .builder()
                .key(itemKey)
                .bucket(bucketName)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(objectRequest);
        return objectBytes.asByteArray();
    }
    
    public void deleteItem(String itemKey) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(itemKey)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }
    
    
    private String getFilename(String itemKey) {
        String[] tokens = itemKey.split("/");
        return tokens[tokens.length - 1];
    }


}
