package com.ringdu.server.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/api/health")
    public String health() {
        return "Ringdu server is running";
    }
}
