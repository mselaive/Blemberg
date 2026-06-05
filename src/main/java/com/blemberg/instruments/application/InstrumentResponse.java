package com.blemberg.instruments.application;

import com.blemberg.instruments.domain.Instrument;

public record InstrumentResponse(
    String symbol,
    boolean active,
    String name,
    String assetClass,
    String exchange,
    String currency,
    String provider,
    String providerSymbol
) {
    public static InstrumentResponse from(Instrument instrument) {
        return new InstrumentResponse(
            instrument.getSymbol(),
            instrument.isActive(),
            instrument.getName(),
            instrument.getAssetClass().name(),
            instrument.getExchange(),
            instrument.getCurrency(),
            instrument.getProvider().name(),
            instrument.getProviderSymbol()
        );
    }
}
