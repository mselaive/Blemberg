package com.blemberg.marketdata.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RiskFreeRateSelectorTest {

    private final RiskFreeRateSelector selector = new RiskFreeRateSelector();

    @Test
    void interpolatesBetweenAvailableTenors() {
        RiskFreeRateSelection selection = selector.select(18.0, List.of(
            rate("1Y", 12, "0.0400"),
            rate("2Y", 24, "0.0500")
        ));

        assertThat(selection.method()).isEqualTo(RateMethod.LINEAR_INTERPOLATION);
        assertThat(selection.rate().doubleValue()).isCloseTo(0.045, org.assertj.core.data.Offset.offset(0.000001));
    }

    @Test
    void fallsBackToNearestTenorOutsideCurve() {
        RiskFreeRateSelection selection = selector.select(72.0, List.of(
            rate("1Y", 12, "0.0400"),
            rate("2Y", 24, "0.0500")
        ));

        assertThat(selection.method()).isEqualTo(RateMethod.NEAREST_TENOR);
        assertThat(selection.rate()).isEqualByComparingTo("0.0500");
    }

    private RiskFreeRate rate(String code, int months, String value) {
        return new RiskFreeRate(code, months, "DGS" + code, new BigDecimal(value), LocalDate.now(), Instant.now());
    }
}
