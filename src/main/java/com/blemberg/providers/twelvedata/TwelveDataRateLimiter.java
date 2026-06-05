package com.blemberg.providers.twelvedata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Deque;

@Component
public class TwelveDataRateLimiter {

    private final TwelveDataProperties properties;
    private final Clock clock;
    private final Deque<Instant> minuteCredits = new ArrayDeque<>();
    private LocalDate day;
    private int dayCredits;

    @Autowired
    public TwelveDataRateLimiter(TwelveDataProperties properties) {
        this(properties, Clock.systemUTC());
    }

    TwelveDataRateLimiter(TwelveDataProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.day = LocalDate.now(clock);
    }

    public synchronized void acquire(int credits) {
        Instant now = Instant.now(clock);
        while (!minuteCredits.isEmpty() && minuteCredits.peekFirst().isBefore(now.minusSeconds(60))) {
            minuteCredits.removeFirst();
        }
        LocalDate today = LocalDate.now(clock);
        if (!today.equals(day)) {
            day = today;
            dayCredits = 0;
        }

        if (minuteCredits.size() + credits > properties.creditsPerMinute()
            || dayCredits + credits > properties.creditsPerDay()) {
            throw new TwelveDataRateLimitException();
        }

        for (int i = 0; i < credits; i++) {
            minuteCredits.addLast(now);
        }
        dayCredits += credits;
    }
}
