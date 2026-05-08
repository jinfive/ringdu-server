package com.ringdu.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class RingduServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RingduServerApplication.class, args);
    }
}
