package com.blemberg.providers.twelvedata;

import com.blemberg.shared.error.ProviderUnavailableException;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Component
public class TwelveDataClient {

    private final TwelveDataProperties properties;
    private final TwelveDataRateLimiter rateLimiter;
    private final RestClient restClient;

    public TwelveDataClient(TwelveDataProperties properties, TwelveDataRateLimiter rateLimiter) {
        this.properties = properties;
        this.rateLimiter = rateLimiter;
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(properties.requestTimeout())
            .build();
        this.restClient = RestClient.builder()
            .baseUrl(properties.baseUrl())
            .requestFactory(new JdkClientHttpRequestFactory(httpClient))
            .build();
    }

    public TwelveDataQuote fetchQuote(String providerSymbol) {
        requireApiKey();
        rateLimiter.acquire(1);
        Map<String, Object> body = restClient.get()
            .uri(uriBuilder -> uriBuilder.path("/quote")
                .queryParam("symbol", providerSymbol)
                .queryParam("apikey", properties.apiKey())
                .build())
            .retrieve()
            .body(Map.class);
        if (body == null || body.containsKey("code") || body.containsKey("status") && "error".equals(body.get("status"))) {
            throw new ProviderUnavailableException("Market data service unavailable");
        }
        BigDecimal last = decimal(first(body, "close", "price"));
        return new TwelveDataQuote(
            string(first(body, "symbol"), providerSymbol),
            last,
            decimalOrNull(first(body, "open")),
            decimalOrNull(first(body, "high")),
            decimalOrNull(first(body, "low")),
            decimalOrNull(first(body, "previous_close")),
            longOrNull(first(body, "volume")),
            string(first(body, "currency"), "USD"),
            asOf(body)
        );
    }

    public List<TwelveDataDailyBar> fetchDailyBars(String providerSymbol, int outputSize) {
        requireApiKey();
        rateLimiter.acquire(1);
        Map<String, Object> body = restClient.get()
            .uri(uriBuilder -> uriBuilder.path("/time_series")
                .queryParam("symbol", providerSymbol)
                .queryParam("interval", "1day")
                .queryParam("outputsize", outputSize)
                .queryParam("apikey", properties.apiKey())
                .build())
            .retrieve()
            .body(Map.class);
        if (body == null || body.containsKey("code") || body.containsKey("status") && "error".equals(body.get("status"))) {
            throw new ProviderUnavailableException("Market data service unavailable");
        }
        Object values = body.get("values");
        if (!(values instanceof List<?> rows)) {
            throw new ProviderUnavailableException("Market data service unavailable");
        }
        return rows.stream()
            .filter(Map.class::isInstance)
            .map(Map.class::cast)
            .map(row -> new TwelveDataDailyBar(
                LocalDate.parse(string(row.get("datetime"), null)),
                decimal(row.get("open")),
                decimal(row.get("high")),
                decimal(row.get("low")),
                decimal(row.get("close")),
                longOrNull(row.get("volume"))
            ))
            .toList();
    }

    private void requireApiKey() {
        if (!properties.hasApiKey()) {
            throw new ProviderUnavailableException("Market data service unavailable");
        }
    }

    private Object first(Map<String, Object> body, String... keys) {
        for (String key : keys) {
            Object value = body.get(key);
            if (value != null && !value.toString().isBlank()) {
                return value;
            }
        }
        return null;
    }

    private BigDecimal decimal(Object value) {
        BigDecimal parsed = decimalOrNull(value);
        if (parsed == null) {
            throw new ProviderUnavailableException("Market data service unavailable");
        }
        return parsed;
    }

    private BigDecimal decimalOrNull(Object value) {
        if (value == null || value.toString().isBlank()) {
            return null;
        }
        return new BigDecimal(value.toString());
    }

    private Long longOrNull(Object value) {
        if (value == null || value.toString().isBlank()) {
            return null;
        }
        return new BigDecimal(value.toString()).longValue();
    }

    private String string(Object value, String fallback) {
        if (value == null || value.toString().isBlank()) {
            return fallback;
        }
        return value.toString();
    }

    private Instant asOf(Map<String, Object> body) {
        Object timestamp = body.get("timestamp");
        if (timestamp != null && !timestamp.toString().isBlank()) {
            return Instant.ofEpochSecond(Long.parseLong(timestamp.toString()));
        }
        Object datetime = body.get("datetime");
        if (datetime != null && !datetime.toString().isBlank()) {
            return LocalDate.parse(datetime.toString()).atStartOfDay().toInstant(ZoneOffset.UTC);
        }
        return Instant.now();
    }
}
