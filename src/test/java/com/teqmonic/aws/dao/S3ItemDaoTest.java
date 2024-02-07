package com.teqmonic.aws.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class S3ItemDaoTest extends AWSServiceTest{

	private static final String FILE_1 = "test_1.pdf";
    private static final String FILE_2 = "test_2.pdf";
    private static final String FILE_1_KEY = "case-1/test_1.pdf";
    private static final String FILE_2_KEY = "case-2/test_2.pdf";

    @Autowired
    private S3ItemDao s3ItemDao;

    @Autowired
    private S3BucketBuilder s3BucketBuilder;

    @BeforeEach
    void setup() {
        s3BucketBuilder.reCreateBucket();
    }
    
    @Test
    void uploadFile() throws IOException {
        MultipartFile multipartFile = new MockMultipartFile(FILE_1,
                S3ItemDaoTest.class.getResourceAsStream(String.format("/%s", FILE_1)));

        s3ItemDao.uploadItem(FILE_1_KEY, generateMetadata(), multipartFile);

        List<String> files = s3ItemDao.listItems();
        assertThat(files).as("Check if s3 list item has only %s", FILE_1_KEY).containsOnly(FILE_1_KEY);
    }
    
    @Test
    void uploadMultipleFiles() throws IOException {
        MultipartFile multipartFile1 = new MockMultipartFile(FILE_1,
                S3ItemDaoTest.class.getResourceAsStream(String.format("/%s", FILE_1)));
        s3ItemDao.uploadItem(FILE_1_KEY, generateMetadata(), multipartFile1);

        MultipartFile multipartFile2 = new MockMultipartFile(FILE_2,
                S3ItemDaoTest.class.getResourceAsStream(String.format("/%s", FILE_2)));
        s3ItemDao.uploadItem(FILE_2_KEY, generateMetadata(), multipartFile2);

        List<String> files = s3ItemDao.listItems();
        assertThat(files).containsExactlyInAnyOrder(FILE_1_KEY, FILE_2_KEY);
    }
    
    @Test
    void listFileWithPrefix() throws IOException {
        MultipartFile multipartFile1 = new MockMultipartFile(FILE_1,
                S3ItemDaoTest.class.getResourceAsStream(String.format("/%s", FILE_1)));
        s3ItemDao.uploadItem(FILE_1_KEY, generateMetadata(), multipartFile1);

        MultipartFile multipartFile2 = new MockMultipartFile(FILE_2,
                S3ItemDaoTest.class.getResourceAsStream(String.format("/%s", FILE_2)));
        s3ItemDao.uploadItem(FILE_2_KEY, generateMetadata(), multipartFile2);

        List<String> files = s3ItemDao.listItems("case-2");
        assertThat(files).containsOnly(FILE_2_KEY);
    }
    
    @Test
    void downloadItemData() throws IOException {
        byte[] uploadFileContent = IOUtils.toByteArray(Objects.requireNonNull(
                S3ItemDaoTest.class.getResourceAsStream(String.format("/%s", FILE_1))));

        MultipartFile multipartFile = new MockMultipartFile(FILE_1, uploadFileContent);

        s3ItemDao.uploadItem(FILE_1_KEY, generateMetadata(), multipartFile);

        byte[] fileContent = s3ItemDao.downloadItemData(FILE_1_KEY);
        assertThat(fileContent).isEqualTo(uploadFileContent);
    }
    
    @Test
    void getPresignedUrl() throws IOException {
        byte[] uploadFileContent = IOUtils.toByteArray(Objects.requireNonNull(
                S3ItemDaoTest.class.getResourceAsStream(String.format("/%s", FILE_1))));

        MultipartFile multipartFile = new MockMultipartFile(FILE_1, uploadFileContent);
        s3ItemDao.uploadItem(FILE_1_KEY, generateMetadata(), multipartFile);

        URL presignedUrl = s3ItemDao.generatePresignedUrl(FILE_1_KEY);
        log.info("Presigned url = {}", presignedUrl);
    }

    
    private Map<String, String> generateMetadata() {
        return Map.of("timestamp", DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
    }
}
