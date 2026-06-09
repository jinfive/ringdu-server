package com.ringdu.server.billing.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BillingTimeConfig {

    @Bean
    public Clock billingClock() {
        return Clock.systemDefaultZone();
    }
}
