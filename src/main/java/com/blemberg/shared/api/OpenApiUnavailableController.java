package com.blemberg.shared.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class OpenApiUnavailableController {

    @GetMapping("/v3/api-docs")
    public ResponseEntity<Map<String, Object>> apiDocsUnavailable(HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(Map.of(
            "timestamp", Instant.now(),
            "status", HttpStatus.NOT_IMPLEMENTED.value(),
            "error", HttpStatus.NOT_IMPLEMENTED.getReasonPhrase(),
            "message", "OpenAPI is not enabled in Blemberg V1.",
            "path", request.getRequestURI()
        ));
    }
}
