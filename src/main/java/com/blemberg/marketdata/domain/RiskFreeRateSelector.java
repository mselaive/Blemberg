package com.blemberg.marketdata.domain;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public class RiskFreeRateSelector {

    public RiskFreeRateSelection select(double targetMonths, List<RiskFreeRate> rates) {
        if (targetMonths <= 0.0 || rates == null || rates.isEmpty()) {
            throw new IllegalArgumentException("Valid target maturity and rates are required");
        }

        List<RiskFreeRate> sorted = rates.stream()
            .sorted(Comparator.comparingInt(RiskFreeRate::getTenorMonths))
            .toList();

        RiskFreeRate lower = null;
        RiskFreeRate upper = null;
        for (RiskFreeRate rate : sorted) {
            if (rate.getTenorMonths() <= targetMonths) {
                lower = rate;
            }
            if (rate.getTenorMonths() >= targetMonths) {
                upper = rate;
                break;
            }
        }

        if (lower != null && upper != null && lower.getTenorMonths() != upper.getTenorMonths()) {
            double range = upper.getTenorMonths() - lower.getTenorMonths();
            double weight = (targetMonths - lower.getTenorMonths()) / range;
            double interpolated = lower.getRateDecimal().doubleValue()
                + weight * (upper.getRateDecimal().doubleValue() - lower.getRateDecimal().doubleValue());
            return new RiskFreeRateSelection(
                BigDecimal.valueOf(interpolated),
                RateMethod.LINEAR_INTERPOLATION,
                lower.getAsOf().isAfter(upper.getAsOf()) ? upper.getAsOf() : lower.getAsOf()
            );
        }

        RiskFreeRate nearest = sorted.stream()
            .min(Comparator.comparingDouble(rate -> Math.abs(rate.getTenorMonths() - targetMonths)))
            .orElseThrow();
        return new RiskFreeRateSelection(nearest.getRateDecimal(), RateMethod.NEAREST_TENOR, nearest.getAsOf());
    }
}
