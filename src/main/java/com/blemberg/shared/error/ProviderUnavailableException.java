package com.blemberg.shared.error;

import org.springframework.http.HttpStatus;

public class ProviderUnavailableException extends ApiException {

    public ProviderUnavailableException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }
}
