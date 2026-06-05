package com.blemberg.marketdata.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record RiskFreeRateSelection(
    BigDecimal rate,
    RateMethod method,
    Instant asOf
) {
}
