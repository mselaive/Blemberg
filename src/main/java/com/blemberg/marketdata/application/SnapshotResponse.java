package com.blemberg.marketdata.application;

import com.blemberg.marketdata.domain.MarketPriceSnapshot;

import java.math.BigDecimal;
import java.time.Instant;

public record SnapshotResponse(
    String symbol,
    BigDecimal lastPrice,
    BigDecimal open,
    BigDecimal high,
    BigDecimal low,
    BigDecimal previousClose,
    Long volume,
    String currency,
    Instant asOf,
    String source,
    boolean stale
) {
    public static SnapshotResponse from(MarketPriceSnapshot snapshot, boolean stale) {
        return new SnapshotResponse(
            snapshot.getSymbol(),
            snapshot.getLastPrice(),
            snapshot.getOpen(),
            snapshot.getHigh(),
            snapshot.getLow(),
            snapshot.getPreviousClose(),
            snapshot.getVolume(),
            snapshot.getCurrency(),
            snapshot.getAsOf(),
            snapshot.getProvider().name(),
            stale
        );
    }
}
