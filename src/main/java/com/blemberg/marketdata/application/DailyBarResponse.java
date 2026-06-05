package com.blemberg.marketdata.application;

import com.blemberg.marketdata.domain.MarketDailyBar;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyBarResponse(
    String symbol,
    LocalDate barDate,
    BigDecimal open,
    BigDecimal high,
    BigDecimal low,
    BigDecimal close,
    Long volume,
    String source
) {
    public static DailyBarResponse from(MarketDailyBar bar) {
        return new DailyBarResponse(
            bar.getSymbol(),
            bar.getBarDate(),
            bar.getOpen(),
            bar.getHigh(),
            bar.getLow(),
            bar.getClose(),
            bar.getVolume(),
            bar.getProvider().name()
        );
    }
}
