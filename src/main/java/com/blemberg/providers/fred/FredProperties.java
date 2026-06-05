package com.blemberg.providers.fred;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "blemberg.providers.fred")
public record FredProperties(
    String baseUrl,
    String apiKey,
    Duration requestTimeout
) {
    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}
