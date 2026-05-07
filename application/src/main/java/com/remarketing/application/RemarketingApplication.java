package com.remarketing.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.remarketing")
@EntityScan(basePackages = "com.remarketing")
@EnableJpaRepositories(basePackages = "com.remarketing")
public class RemarketingApplication {

    public static void main(String[] args) {
        SpringApplication.run(RemarketingApplication.class, args);
    }
}
