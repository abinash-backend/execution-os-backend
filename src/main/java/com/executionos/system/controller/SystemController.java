package com.executionos.system.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final String serviceName;

    public SystemController(
            @Value("${system.health.service-name:Execution OS Backend}") String serviceName) {
        this.serviceName = serviceName;
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> getHealth() {
        HealthResponse response = new HealthResponse(
                "UP",
                serviceName,
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Root endpoint for base URL
    @GetMapping("/")
    public ResponseEntity<Map<String, String>> home() {
        return ResponseEntity.ok(
                Map.of(
                        "message", "Execution OS Backend API is running",
                        "docs", "/swagger-ui/index.html",
                        "health", "/api/system/health"
                )
        );
    }

    public record HealthResponse(
            String status,
            String service,
            Instant timestamp
    ) {
    }
}