package com.blemberg.marketdata.application;

import java.math.BigDecimal;
import java.time.Instant;

public record PricingInputsResponse(
    String symbol,
    BigDecimal spot,
    BigDecimal volatility,
    String volatilityMethod,
    BigDecimal riskFreeRate,
    String rateMethod,
    BigDecimal dividendYield,
    String dividendYieldMethod,
    String currency,
    Instant asOf,
    String source,
    boolean stale
) {
}
