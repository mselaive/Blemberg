package com.blemberg.providers.fred;

import com.blemberg.shared.error.ProviderUnavailableException;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class FredClient {

    private final FredProperties properties;
    private final RestClient restClient;

    public FredClient(FredProperties properties) {
        this.properties = properties;
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(properties.requestTimeout())
            .build();
        this.restClient = RestClient.builder()
            .baseUrl(properties.baseUrl())
            .requestFactory(new JdkClientHttpRequestFactory(httpClient))
            .build();
    }

    public List<FredRateObservation> fetchLatestTreasuryRates() {
        requireApiKey();
        return Arrays.stream(FredTenor.values())
            .map(this::fetchLatestTreasuryRate)
            .toList();
    }

    public FredRateObservation fetchLatestTreasuryRate(FredTenor tenor) {
        requireApiKey();
        Map<String, Object> body = restClient.get()
            .uri(uriBuilder -> uriBuilder.path("/fred/series/observations")
                .queryParam("series_id", tenor.seriesId())
                .queryParam("api_key", properties.apiKey())
                .queryParam("file_type", "json")
                .queryParam("sort_order", "desc")
                .queryParam("limit", 10)
                .build())
            .retrieve()
            .body(Map.class);
        Object observations = body == null ? null : body.get("observations");
        if (!(observations instanceof List<?> rows)) {
            throw new ProviderUnavailableException("Market data service unavailable");
        }
        for (Object row : rows) {
            if (row instanceof Map<?, ?> observation) {
                Object value = observation.get("value");
                if (value != null && !".".equals(value.toString())) {
                    BigDecimal decimal = new BigDecimal(value.toString()).movePointLeft(2);
                    return new FredRateObservation(
                        tenor,
                        decimal,
                        LocalDate.parse(observation.get("date").toString()),
                        Instant.now()
                    );
                }
            }
        }
        throw new ProviderUnavailableException("Market data service unavailable");
    }

    private void requireApiKey() {
        if (!properties.hasApiKey()) {
            throw new ProviderUnavailableException("Market data service unavailable");
        }
    }
}
