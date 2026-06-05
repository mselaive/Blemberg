package com.blemberg.marketdata.application;

import com.blemberg.marketdata.domain.RiskFreeRate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record RiskFreeRateResponse(
    String tenorCode,
    int tenorMonths,
    String fredSeriesId,
    BigDecimal rateDecimal,
    LocalDate observationDate,
    Instant asOf
) {
    public static RiskFreeRateResponse from(RiskFreeRate rate) {
        return new RiskFreeRateResponse(
            rate.getTenorCode(),
            rate.getTenorMonths(),
            rate.getFredSeriesId(),
            rate.getRateDecimal(),
            rate.getObservationDate(),
            rate.getAsOf()
        );
    }
}
