package com.blemberg.marketdata.domain;

import java.math.BigDecimal;
import java.util.List;

public class HistoricalVolatilityCalculator {

    private static final double TRADING_DAYS_PER_YEAR = 252.0;

    public BigDecimal annualizedRealizedVolatility(List<BigDecimal> closesAscending) {
        if (closesAscending == null || closesAscending.size() < 2) {
            throw new IllegalArgumentException("At least two closes are required");
        }

        double[] returns = new double[closesAscending.size() - 1];
        for (int i = 1; i < closesAscending.size(); i++) {
            double previous = closesAscending.get(i - 1).doubleValue();
            double current = closesAscending.get(i).doubleValue();
            if (!Double.isFinite(previous) || !Double.isFinite(current) || previous <= 0.0 || current <= 0.0) {
                throw new IllegalArgumentException("Close prices must be positive finite values");
            }
            returns[i - 1] = Math.log(current / previous);
        }

        double mean = 0.0;
        for (double value : returns) {
            mean += value;
        }
        mean /= returns.length;

        double variance = 0.0;
        for (double value : returns) {
            double diff = value - mean;
            variance += diff * diff;
        }
        variance /= returns.length;

        double annualized = Math.sqrt(variance) * Math.sqrt(TRADING_DAYS_PER_YEAR);
        if (!Double.isFinite(annualized) || annualized <= 0.0) {
            throw new IllegalArgumentException("Volatility must be positive and finite");
        }
        return BigDecimal.valueOf(annualized);
    }
}
