package com.ringdu.ringduserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.ringdu")
public class RingduServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RingduServerApplication.class, args);
    }

}
