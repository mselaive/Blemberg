package com.blemberg.providers.fred;

public enum FredTenor {
    ONE_MONTH("1M", 1, "DGS1MO"),
    THREE_MONTH("3M", 3, "DGS3MO"),
    SIX_MONTH("6M", 6, "DGS6MO"),
    ONE_YEAR("1Y", 12, "DGS1"),
    TWO_YEAR("2Y", 24, "DGS2"),
    FIVE_YEAR("5Y", 60, "DGS5"),
    TEN_YEAR("10Y", 120, "DGS10");

    private final String code;
    private final int months;
    private final String seriesId;

    FredTenor(String code, int months, String seriesId) {
        this.code = code;
        this.months = months;
        this.seriesId = seriesId;
    }

    public String code() {
        return code;
    }

    public int months() {
        return months;
    }

    public String seriesId() {
        return seriesId;
    }
}
