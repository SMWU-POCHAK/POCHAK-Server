package com.apps.pochak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PochakApplication {

    public static void main(String[] args) {
        SpringApplication.run(PochakApplication.class, args);
    }

//    @PostConstruct
//    public void init() {
//        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
//    }
}
