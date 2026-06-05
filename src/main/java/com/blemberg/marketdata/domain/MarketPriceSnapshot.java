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

@Entity
@Table(name = "market_price_snapshots")
public class MarketPriceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol;

    @Column(name = "last_price", nullable = false)
    private BigDecimal lastPrice;

    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;

    @Column(name = "previous_close")
    private BigDecimal previousClose;

    private Long volume;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(name = "as_of", nullable = false)
    private Instant asOf;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected MarketPriceSnapshot() {
    }

    public MarketPriceSnapshot(String symbol, BigDecimal lastPrice, BigDecimal open, BigDecimal high, BigDecimal low,
                               BigDecimal previousClose, Long volume, String currency, Provider provider, Instant asOf) {
        this.symbol = symbol;
        this.lastPrice = lastPrice;
        this.open = open;
        this.high = high;
        this.low = low;
        this.previousClose = previousClose;
        this.volume = volume;
        this.currency = currency;
        this.provider = provider;
        this.asOf = asOf;
        this.createdAt = Instant.now();
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
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

    public BigDecimal getPreviousClose() {
        return previousClose;
    }

    public Long getVolume() {
        return volume;
    }

    public String getCurrency() {
        return currency;
    }

    public Provider getProvider() {
        return provider;
    }

    public Instant getAsOf() {
        return asOf;
    }
}
