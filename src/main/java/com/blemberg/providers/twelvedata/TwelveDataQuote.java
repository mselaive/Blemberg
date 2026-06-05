package com.blemberg.providers.twelvedata;

import java.math.BigDecimal;
import java.time.Instant;

public record TwelveDataQuote(
    String symbol,
    BigDecimal lastPrice,
    BigDecimal open,
    BigDecimal high,
    BigDecimal low,
    BigDecimal previousClose,
    Long volume,
    String currency,
    Instant asOf
) {
}
