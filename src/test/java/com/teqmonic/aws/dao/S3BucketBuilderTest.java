package com.teqmonic.aws.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class S3BucketBuilderTest extends AWSServiceTest {

	@Autowired
	private S3BucketBuilder s3BucketBuilder;

	@BeforeEach
	void setup() {
		s3BucketBuilder.reCreateBucket();
	}

	@Test
	void testCreateBucket() {
		s3BucketBuilder.createBucket();
	}

	@Test
	void testDeleteBucket() {
		//s3BucketBuilder.createBucket();
		s3BucketBuilder.deleteAllItemsAndBucket();
	}

}
