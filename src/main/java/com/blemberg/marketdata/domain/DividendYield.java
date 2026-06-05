package com.blemberg.marketdata.domain;

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
@Table(name = "dividend_yields")
public class DividendYield {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol;

    @Column(name = "dividend_yield", nullable = false)
    private BigDecimal dividendYield;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DividendYieldMethod method;

    @Column(name = "as_of", nullable = false)
    private Instant asOf;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected DividendYield() {
    }

    public DividendYield(String symbol, BigDecimal dividendYield, DividendYieldMethod method, Instant asOf) {
        this.symbol = symbol;
        this.dividendYield = dividendYield;
        this.method = method;
        this.asOf = asOf;
        this.createdAt = Instant.now();
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getDividendYield() {
        return dividendYield;
    }

    public DividendYieldMethod getMethod() {
        return method;
    }

    public Instant getAsOf() {
        return asOf;
    }
}
