package com.teqmonic.aws.dao.setup;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AWSServiceTestInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(@NotNull ConfigurableApplicationContext configurableApplicationContext) {

        // start up and initialize test containers
        TestContainersSetup.initTestContainers();

        // alter spring boot system properties so that it will connect to test containers and wiremock
        TestPropertyValues values = TestPropertyValues.of(
                "aws.region=" + TestContainersSetup.getRegion(),
                "aws.accessKey=" + TestContainersSetup.getAccessKey(),
                "aws.secretKey=" + TestContainersSetup.getSecretKey(),
                "aws.endpointOverride=" + TestContainersSetup.getEndpointOverride()
        );

        values.applyTo(configurableApplicationContext);

        log.info("======= Customized properties settings =======");
        log.info("aws.region=" + TestContainersSetup.getRegion());
        log.info("aws.accessKey=" + TestContainersSetup.getAccessKey());
        log.info("aws.secretKey=" + TestContainersSetup.getSecretKey());
        log.info("aws.endpointOverride=" + TestContainersSetup.getEndpointOverride());
        log.info("==============");

    }
}