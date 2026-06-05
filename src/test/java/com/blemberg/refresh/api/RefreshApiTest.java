package com.blemberg.refresh.api;

import com.blemberg.providers.fred.FredClient;
import com.blemberg.providers.fred.FredProperties;
import com.blemberg.providers.fred.FredRateObservation;
import com.blemberg.providers.fred.FredTenor;
import com.blemberg.providers.twelvedata.TwelveDataClient;
import com.blemberg.providers.twelvedata.TwelveDataDailyBar;
import com.blemberg.providers.twelvedata.TwelveDataProperties;
import com.blemberg.providers.twelvedata.TwelveDataQuote;
import com.blemberg.providers.twelvedata.TwelveDataRateLimitException;
import com.blemberg.providers.twelvedata.TwelveDataRateLimiter;
import com.blemberg.shared.error.ProviderUnavailableException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RefreshApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void manualRefreshBootstrapsAaplAndExposesDetails() throws Exception {
        String body = mockMvc.perform(post("/api/admin/market-data/refresh"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.symbolsRequested").value(25))
            .andExpect(jsonPath("$.symbolsSucceeded").value(2))
            .andExpect(jsonPath("$.symbolsFailed").value(23))
            .andExpect(jsonPath("$.jobSummaries").isArray())
            .andExpect(jsonPath("$.errors").isArray())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode response = objectMapper.readTree(body);
        String runId = response.get("runId").asText();

        mockMvc.perform(get("/api/market-data/snapshots").param("symbols", "AAPL"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.snapshots[0].symbol").value("AAPL"))
            .andExpect(jsonPath("$.snapshots[0].lastPrice").value(190.0));

        mockMvc.perform(get("/api/market-data/snapshots").param("symbols", "AAPL,SPY,QQQ,MSFT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.snapshots[0].symbol").value("AAPL"))
            .andExpect(jsonPath("$.missingSymbols").isArray());

        mockMvc.perform(get("/api/market-data/pricing-inputs/european-option")
                .param("symbol", "AAPL")
                .param("maturityDate", LocalDate.now().plusMonths(18).toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.symbol").value("AAPL"))
            .andExpect(jsonPath("$.spot").value(190.0))
            .andExpect(jsonPath("$.volatility").isNumber())
            .andExpect(jsonPath("$.riskFreeRate").isNumber())
            .andExpect(jsonPath("$.dividendYield").value(0.0))
            .andExpect(jsonPath("$.currency").value("USD"))
            .andExpect(jsonPath("$.source").value("BLEMBERG"))
            .andExpect(jsonPath("$.stale").isBoolean());

        mockMvc.perform(get("/api/market-data/pricing-inputs/european-option")
                .param("symbol", "AMZN")
                .param("maturityDate", LocalDate.now().plusMonths(18).toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.symbol").value("AMZN"))
            .andExpect(jsonPath("$.spot").value(170.0))
            .andExpect(jsonPath("$.volatility").isNumber())
            .andExpect(jsonPath("$.riskFreeRate").isNumber())
            .andExpect(jsonPath("$.dividendYield").value(0.0))
            .andExpect(jsonPath("$.dividendYieldMethod").value("UNKNOWN_ZERO"));

        String detailBody = mockMvc.perform(get("/api/admin/market-data/refresh/{runId}", runId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.runId").value(runId))
            .andExpect(jsonPath("$.items").isArray())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode detail = objectMapper.readTree(detailBody);
        assertThat(detail.get("items").toString()).contains("SKIPPED_RATE_LIMIT");
        assertThat(detail.get("items").toString()).doesNotContain("api_key");
        assertThat(detail.get("items").toString()).doesNotContain("stacktrace");

        mockMvc.perform(get("/api/admin/market-data/refresh/latest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.runId").value(runId));

        mockMvc.perform(get("/api/admin/market-data/refresh-runs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].runId").value(runId))
            .andExpect(jsonPath("$[0].jobSummaries").isArray())
            .andExpect(jsonPath("$[0].errors").isArray());
    }

    @Test
    void unknownSymbolStillReturns404() throws Exception {
        mockMvc.perform(get("/api/instruments/FAKE"))
            .andExpect(status().isNotFound());
    }

    @TestConfiguration
    static class FakeProviderConfig {

        @Bean
        @Primary
        TwelveDataClient fakeTwelveDataClient() {
            TwelveDataProperties properties = new TwelveDataProperties(
                "http://localhost",
                "test",
                8,
                800,
                Duration.ofSeconds(1)
            );
            return new FakeTwelveDataClient(properties, new TwelveDataRateLimiter(properties));
        }

        @Bean
        @Primary
        FredClient fakeFredClient() {
            return new FakeFredClient(new FredProperties("http://localhost", "test", Duration.ofSeconds(1)));
        }
    }

    static class FakeTwelveDataClient extends TwelveDataClient {

        FakeTwelveDataClient(TwelveDataProperties properties, TwelveDataRateLimiter rateLimiter) {
            super(properties, rateLimiter);
        }

        @Override
        public TwelveDataQuote fetchQuote(String providerSymbol) {
            if (!"AAPL".equals(providerSymbol) && !"AMZN".equals(providerSymbol)) {
                throw new TwelveDataRateLimitException();
            }
            BigDecimal price = "AMZN".equals(providerSymbol) ? BigDecimal.valueOf(170.0) : BigDecimal.valueOf(190.0);
            return new TwelveDataQuote(
                providerSymbol,
                price,
                price.subtract(BigDecimal.valueOf(2.0)),
                price.add(BigDecimal.ONE),
                price.subtract(BigDecimal.valueOf(3.0)),
                price.subtract(BigDecimal.ONE),
                10_000_000L,
                "USD",
                Instant.now()
            );
        }

        @Override
        public List<TwelveDataDailyBar> fetchDailyBars(String providerSymbol, int outputSize) {
            if (!"AAPL".equals(providerSymbol) && !"AMZN".equals(providerSymbol)) {
                throw new TwelveDataRateLimitException();
            }
            List<TwelveDataDailyBar> bars = new ArrayList<>();
            LocalDate start = LocalDate.now().minusDays(70);
            for (int i = 0; i < 61; i++) {
                double base = "AMZN".equals(providerSymbol) ? 140.0 : 100.0;
                BigDecimal close = BigDecimal.valueOf(base + i + Math.sin(i));
                bars.add(new TwelveDataDailyBar(
                    start.plusDays(i),
                    close.subtract(BigDecimal.ONE),
                    close.add(BigDecimal.ONE),
                    close.subtract(BigDecimal.valueOf(2)),
                    close,
                    1_000_000L + i
                ));
            }
            return bars;
        }
    }

    static class FakeFredClient extends FredClient {

        FakeFredClient(FredProperties properties) {
            super(properties);
        }

        @Override
        public FredRateObservation fetchLatestTreasuryRate(FredTenor tenor) {
            if (tenor != FredTenor.ONE_YEAR) {
                throw new ProviderUnavailableException("Market data service unavailable");
            }
            return new FredRateObservation(
                tenor,
                BigDecimal.valueOf(0.045),
                LocalDate.now(),
                Instant.now()
            );
        }
    }
}
