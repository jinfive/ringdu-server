package com.ringdu.server;

import com.ringdu.server.global.security.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@EnableConfigurationProperties(JwtProperties.class)
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class RingduServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RingduServerApplication.class, args);
    }
}
