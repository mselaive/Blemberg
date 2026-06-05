package com.blemberg.providers.twelvedata;

import com.blemberg.shared.error.ProviderUnavailableException;

public class TwelveDataRateLimitException extends ProviderUnavailableException {

    public TwelveDataRateLimitException() {
        super("Market data service unavailable");
    }
}
