package com.blemberg.marketdata.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "blemberg.market-data")
public record MarketDataProperties(
    int historicalVolatilityWindowDays,
    Duration snapshotStaleAfter,
    Duration barsStaleAfter,
    Duration ratesStaleAfter,
    Duration dividendsStaleAfter
) {
}
