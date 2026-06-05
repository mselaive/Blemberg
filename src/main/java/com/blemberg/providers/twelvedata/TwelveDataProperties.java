package com.blemberg.providers.twelvedata;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "blemberg.providers.twelve-data")
public record TwelveDataProperties(
    String baseUrl,
    String apiKey,
    int creditsPerMinute,
    int creditsPerDay,
    Duration requestTimeout
) {
    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}
