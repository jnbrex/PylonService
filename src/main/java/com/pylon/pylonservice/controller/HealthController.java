package com.pylon.pylonservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    /**
     * Call to check if PylonService is healthy.
     *
     * @return HTTP 200 OK - String body like "Healthy at 457689786"
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok(String.format("Healthy at %s", System.currentTimeMillis()));
    }
}
