package com.singtel.interviewtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class InterviewTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterviewTestApplication.class, args);
    }

}
