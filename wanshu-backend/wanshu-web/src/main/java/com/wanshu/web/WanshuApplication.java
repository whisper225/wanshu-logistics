package com.wanshu.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication(scanBasePackages = "com.wanshu")
@MapperScan("com.wanshu.**.mapper")
@EnableNeo4jRepositories(basePackages = "com.wanshu.dispatch.repository")
public class WanshuApplication {

    public static void main(String[] args) {
        SpringApplication.run(WanshuApplication.class, args);
    }

}
