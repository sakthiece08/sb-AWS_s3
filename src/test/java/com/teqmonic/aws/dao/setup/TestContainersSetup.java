package com.teqmonic.aws.dao.setup;

import java.net.URI;

import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import software.amazon.awssdk.regions.Region;

public class TestContainersSetup {

	private static final DockerImageName LOCAL_STACK_IMAGE = DockerImageName.parse("localstack/localstack:2.0.0");
	private static final LocalStackContainer LOCAL_STACK_CONTAINER = new LocalStackContainer(LOCAL_STACK_IMAGE);

	public static void initTestContainers() {
		LOCAL_STACK_CONTAINER.start();
	}

	public static URI getEndpointOverride() {
		return LOCAL_STACK_CONTAINER.getEndpointOverride(LocalStackContainer.Service.S3);
	}

	public static String getAccessKey() {
		return LOCAL_STACK_CONTAINER.getAccessKey();
	}

	public static String getSecretKey() {
		return LOCAL_STACK_CONTAINER.getSecretKey();
	}

	public static Region getRegion() {
		return Region.of(LOCAL_STACK_CONTAINER.getRegion());
	}

}
