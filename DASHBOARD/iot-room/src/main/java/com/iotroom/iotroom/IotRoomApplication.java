package com.iotroom.iotroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class IotRoomApplication {
    public static void main(String[] args) {
        SpringApplication.run(IotRoomApplication.class, args);
    }
}
