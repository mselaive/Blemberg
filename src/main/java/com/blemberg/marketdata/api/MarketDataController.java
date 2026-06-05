package com.blemberg.marketdata.api;

import com.blemberg.marketdata.application.MarketDataService;
import com.blemberg.marketdata.application.PricingInputsResponse;
import com.blemberg.marketdata.application.SnapshotResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/market-data")
public class MarketDataController {

    private final MarketDataService marketDataService;

    public MarketDataController(MarketDataService marketDataService) {
        this.marketDataService = marketDataService;
    }

    @GetMapping("/snapshots")
    public List<SnapshotResponse> snapshots(@RequestParam String symbols) {
        return marketDataService.snapshots(symbols);
    }

    @GetMapping("/pricing-inputs/european-option")
    public PricingInputsResponse europeanOptionPricingInputs(
        @RequestParam String symbol,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate maturityDate
    ) {
        return marketDataService.europeanOptionPricingInputs(symbol, maturityDate);
    }
}
