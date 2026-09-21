package com.cmtdevsolutions.iam.resource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.cmtdevsolutions.iam", exclude = FlywayAutoConfiguration.class)
@EnableJpaRepositories(basePackages = "com.cmtdevsolutions.iam.common.repository")
@EntityScan(basePackages = "com.cmtdevsolutions.iam.common.entity")
public class ResourceServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResourceServerApplication.class, args);
    }
}