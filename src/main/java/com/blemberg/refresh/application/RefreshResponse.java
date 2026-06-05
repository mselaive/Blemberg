package com.blemberg.refresh.application;

import com.blemberg.refresh.domain.RefreshRun;

import java.time.Instant;
import java.util.UUID;

public record RefreshResponse(
    UUID runId,
    String status,
    Instant startedAt,
    Instant finishedAt,
    int symbolsRequested,
    int symbolsSucceeded,
    int symbolsFailed
) {
    public static RefreshResponse from(RefreshRun run) {
        return new RefreshResponse(
            run.getId(),
            run.getStatus().name(),
            run.getStartedAt(),
            run.getFinishedAt(),
            run.getSymbolsRequested(),
            run.getSymbolsSucceeded(),
            run.getSymbolsFailed()
        );
    }
}
