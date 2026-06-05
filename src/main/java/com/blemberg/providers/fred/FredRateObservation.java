package com.blemberg.providers.fred;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record FredRateObservation(
    FredTenor tenor,
    BigDecimal rateDecimal,
    LocalDate observationDate,
    Instant asOf
) {
}
