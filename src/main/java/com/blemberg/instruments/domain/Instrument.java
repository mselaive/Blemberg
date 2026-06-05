package com.blemberg.instruments.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "instruments")
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String symbol;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_class", nullable = false)
    private AssetClass assetClass;

    @Column(nullable = false)
    private String exchange;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(name = "provider_symbol", nullable = false)
    private String providerSymbol;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Instrument() {
    }

    public Instrument(String symbol, String name, AssetClass assetClass, String exchange, String currency,
                      Provider provider, String providerSymbol, boolean active) {
        Instant now = Instant.now();
        this.symbol = symbol;
        this.name = name;
        this.assetClass = assetClass;
        this.exchange = exchange;
        this.currency = currency;
        this.provider = provider;
        this.providerSymbol = providerSymbol;
        this.active = active;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public AssetClass getAssetClass() {
        return assetClass;
    }

    public String getExchange() {
        return exchange;
    }

    public String getCurrency() {
        return currency;
    }

    public Provider getProvider() {
        return provider;
    }

    public String getProviderSymbol() {
        return providerSymbol;
    }

    public boolean isActive() {
        return active;
    }
}
