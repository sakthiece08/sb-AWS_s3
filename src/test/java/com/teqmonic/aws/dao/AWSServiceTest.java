package com.teqmonic.aws.dao;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import com.teqmonic.aws.config.AppConfig;
import com.teqmonic.aws.dao.setup.AWSServiceTestInitializer;

@SpringBootTest(classes = { AppConfig.class })
@ContextConfiguration(initializers = { AWSServiceTestInitializer.class })
public abstract class AWSServiceTest {

}
