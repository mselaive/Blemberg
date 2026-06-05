package com.blemberg.providers.twelvedata;

import com.blemberg.shared.error.ProviderUnavailableException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TwelveDataRateLimiterTest {

    @Test
    void enforcesMinuteBudget() {
        TwelveDataRateLimiter limiter = new TwelveDataRateLimiter(
            new TwelveDataProperties("http://localhost", "key", 2, 800, Duration.ofSeconds(1))
        );

        limiter.acquire(1);
        limiter.acquire(1);

        assertThatThrownBy(() -> limiter.acquire(1))
            .isInstanceOf(ProviderUnavailableException.class);
    }
}
