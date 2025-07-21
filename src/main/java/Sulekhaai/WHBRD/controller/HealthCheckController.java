package com.yourpackage.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Spring Boot is running behind NGINX!");
    }
}
