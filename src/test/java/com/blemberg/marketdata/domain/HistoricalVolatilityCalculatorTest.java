package com.blemberg.marketdata.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HistoricalVolatilityCalculatorTest {

    private final HistoricalVolatilityCalculator calculator = new HistoricalVolatilityCalculator();

    @Test
    void calculatesAnnualizedRealizedVolatilityFromLogReturns() {
        BigDecimal volatility = calculator.annualizedRealizedVolatility(List.of(
            BigDecimal.valueOf(100.00),
            BigDecimal.valueOf(101.00),
            BigDecimal.valueOf(99.50),
            BigDecimal.valueOf(102.00)
        ));

        assertThat(volatility.doubleValue()).isCloseTo(0.260518, withinTolerance());
    }

    @Test
    void rejectsNonPositiveCloses() {
        assertThatThrownBy(() -> calculator.annualizedRealizedVolatility(List.of(
            BigDecimal.valueOf(100.00),
            BigDecimal.ZERO
        ))).isInstanceOf(IllegalArgumentException.class);
    }

    private org.assertj.core.data.Offset<Double> withinTolerance() {
        return org.assertj.core.data.Offset.offset(0.00001);
    }
}
