package com.dbt.chatease;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ChatEaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatEaseApplication.class, args);
    }

}
