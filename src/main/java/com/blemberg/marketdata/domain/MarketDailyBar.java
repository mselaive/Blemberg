package com.blemberg.marketdata.domain;

import com.blemberg.instruments.domain.Provider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "market_daily_bars")
public class MarketDailyBar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol;

    @Column(name = "bar_date", nullable = false)
    private LocalDate barDate;

    @Column(nullable = false)
    private BigDecimal open;

    @Column(nullable = false)
    private BigDecimal high;

    @Column(nullable = false)
    private BigDecimal low;

    @Column(nullable = false)
    private BigDecimal close;

    private Long volume;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected MarketDailyBar() {
    }

    public MarketDailyBar(String symbol, LocalDate barDate, BigDecimal open, BigDecimal high, BigDecimal low,
                          BigDecimal close, Long volume, Provider provider) {
        this.symbol = symbol;
        this.barDate = barDate;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.provider = provider;
        this.createdAt = Instant.now();
    }

    public String getSymbol() {
        return symbol;
    }

    public LocalDate getBarDate() {
        return barDate;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public BigDecimal getClose() {
        return close;
    }

    public Long getVolume() {
        return volume;
    }

    public Provider getProvider() {
        return provider;
    }
}
