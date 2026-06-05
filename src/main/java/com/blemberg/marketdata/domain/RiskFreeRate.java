package com.blemberg.marketdata.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "risk_free_rates")
public class RiskFreeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenor_code", nullable = false)
    private String tenorCode;

    @Column(name = "tenor_months", nullable = false)
    private int tenorMonths;

    @Column(name = "fred_series_id", nullable = false)
    private String fredSeriesId;

    @Column(name = "rate_decimal", nullable = false)
    private BigDecimal rateDecimal;

    @Column(name = "observation_date", nullable = false)
    private LocalDate observationDate;

    @Column(name = "as_of", nullable = false)
    private Instant asOf;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected RiskFreeRate() {
    }

    public RiskFreeRate(String tenorCode, int tenorMonths, String fredSeriesId, BigDecimal rateDecimal,
                        LocalDate observationDate, Instant asOf) {
        this.tenorCode = tenorCode;
        this.tenorMonths = tenorMonths;
        this.fredSeriesId = fredSeriesId;
        this.rateDecimal = rateDecimal;
        this.observationDate = observationDate;
        this.asOf = asOf;
        this.createdAt = Instant.now();
    }

    public String getTenorCode() {
        return tenorCode;
    }

    public int getTenorMonths() {
        return tenorMonths;
    }

    public String getFredSeriesId() {
        return fredSeriesId;
    }

    public BigDecimal getRateDecimal() {
        return rateDecimal;
    }

    public LocalDate getObservationDate() {
        return observationDate;
    }

    public Instant getAsOf() {
        return asOf;
    }
}
