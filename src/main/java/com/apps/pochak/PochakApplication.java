package com.apps.pochak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PochakApplication {

    public static void main(String[] args) {
        SpringApplication.run(PochakApplication.class, args);
    }
}
