package com.example.triagecopilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example")
public class TriageCopilotApplication {

    public static void main(String[] args) {
        SpringApplication.run(TriageCopilotApplication.class, args);
    }
}
