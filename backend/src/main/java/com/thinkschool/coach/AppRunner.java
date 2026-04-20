package com.thinkschool.coach;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class AppRunner {
    public static void main(String[] args) throws Exception {
    	SpringApplication.run(AppRunner.class, args);
    	System.out.println("Application Running......................");
    }
}

