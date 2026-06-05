package com.blemberg.refresh.application;

import com.blemberg.refresh.domain.RefreshRunItem;

import java.time.Instant;

public record RefreshItemResponse(
    String jobName,
    String provider,
    String symbol,
    String tenorCode,
    String status,
    String errorCode,
    String message,
    Instant startedAt,
    Instant finishedAt
) {
    public static RefreshItemResponse from(RefreshRunItem item) {
        return new RefreshItemResponse(
            item.getJobName().name(),
            item.getProvider(),
            item.getSymbol(),
            item.getTenorCode(),
            item.getStatus().name(),
            item.getErrorCode(),
            item.getMessage(),
            item.getStartedAt(),
            item.getFinishedAt()
        );
    }
}
