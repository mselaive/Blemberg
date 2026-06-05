package com.blemberg.providers.twelvedata;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TwelveDataDailyBar(
    LocalDate date,
    BigDecimal open,
    BigDecimal high,
    BigDecimal low,
    BigDecimal close,
    Long volume
) {
}
