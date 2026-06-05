package com.blemberg.marketdata.api;

import com.blemberg.instruments.domain.Provider;
import com.blemberg.marketdata.domain.MarketDailyBar;
import com.blemberg.marketdata.domain.MarketPriceSnapshot;
import com.blemberg.marketdata.domain.RiskFreeRate;
import com.blemberg.marketdata.infrastructure.MarketDailyBarRepository;
import com.blemberg.marketdata.infrastructure.MarketPriceSnapshotRepository;
import com.blemberg.marketdata.infrastructure.RiskFreeRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class MarketDataApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MarketPriceSnapshotRepository snapshotRepository;

    @Autowired
    private MarketDailyBarRepository barRepository;

    @Autowired
    private RiskFreeRateRepository riskFreeRateRepository;

    @BeforeEach
    void seedMarketData() {
        snapshotRepository.save(new MarketPriceSnapshot(
            "AAPL",
            BigDecimal.valueOf(190.00),
            BigDecimal.valueOf(188.00),
            BigDecimal.valueOf(191.00),
            BigDecimal.valueOf(187.50),
            BigDecimal.valueOf(188.10),
            53_200_000L,
            "USD",
            Provider.TWELVE_DATA,
            Instant.now()
        ));

        LocalDate start = LocalDate.now().minusDays(70);
        for (int i = 0; i < 61; i++) {
            BigDecimal close = BigDecimal.valueOf(100.0 + i + Math.sin(i));
            barRepository.save(new MarketDailyBar(
                "AAPL",
                start.plusDays(i),
                close.subtract(BigDecimal.ONE),
                close.add(BigDecimal.ONE),
                close.subtract(BigDecimal.valueOf(2)),
                close,
                1_000_000L + i,
                Provider.TWELVE_DATA
            ));
        }

        Instant now = Instant.now();
        riskFreeRateRepository.save(new RiskFreeRate("1Y", 12, "DGS1", BigDecimal.valueOf(0.04), LocalDate.now(), now));
        riskFreeRateRepository.save(new RiskFreeRate("2Y", 24, "DGS2", BigDecimal.valueOf(0.05), LocalDate.now(), now));
    }

    @Test
    void returnsKnownInstrument() throws Exception {
        mockMvc.perform(get("/api/instruments/AAPL"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.symbol").value("AAPL"))
            .andExpect(jsonPath("$.active").value(true))
            .andExpect(jsonPath("$.assetClass").value("EQUITY"))
            .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    void unknownInstrumentReturns404() throws Exception {
        mockMvc.perform(get("/api/instruments/FAKE"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Instrument not found"));
    }

    @Test
    void returnsCachedSnapshot() throws Exception {
        mockMvc.perform(get("/api/market-data/snapshots").param("symbols", "aapl"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].symbol").value("AAPL"))
            .andExpect(jsonPath("$[0].lastPrice").value(190.00))
            .andExpect(jsonPath("$[0].source").value("TWELVE_DATA"))
            .andExpect(jsonPath("$[0].stale").value(false));
    }

    @Test
    void returnsEuropeanOptionPricingInputs() throws Exception {
        mockMvc.perform(get("/api/market-data/pricing-inputs/european-option")
                .param("symbol", "AAPL")
                .param("maturityDate", LocalDate.now().plusMonths(18).toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.symbol").value("AAPL"))
            .andExpect(jsonPath("$.spot").value(190.00))
            .andExpect(jsonPath("$.volatility").isNumber())
            .andExpect(jsonPath("$.volatilityMethod").value("HISTORICAL_REALIZED_60D"))
            .andExpect(jsonPath("$.riskFreeRate").isNumber())
            .andExpect(jsonPath("$.dividendYield").value(0.0))
            .andExpect(jsonPath("$.currency").value("USD"))
            .andExpect(jsonPath("$.source").value("BLEMBERG"));
    }
}
