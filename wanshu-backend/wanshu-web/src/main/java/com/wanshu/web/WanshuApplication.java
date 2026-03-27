package com.wanshu.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.wanshu")
public class WanshuApplication {

    public static void main(String[] args) {
        SpringApplication.run(WanshuApplication.class, args);
    }

}
