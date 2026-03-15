package com.github.martinfrank.vbuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VBuddyApplication {

    public static void main(String[] args) {
        SpringApplication.run(VBuddyApplication.class, args);
    }
}
