package com.mangban;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class
})
@EnableJpaRepositories(basePackages = "com.mangban")
@EntityScan(basePackages = "com.mangban")
public class MangbanApplication {
    public static void main(String[] args) {
        SpringApplication.run(MangbanApplication.class, args);
    }
}