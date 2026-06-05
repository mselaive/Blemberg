package com.blemberg.refresh.application;

import com.blemberg.refresh.domain.RefreshRun;
import com.blemberg.refresh.domain.RefreshRunItem;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RefreshDetailResponse(
    UUID runId,
    String status,
    String jobName,
    Instant startedAt,
    Instant finishedAt,
    int symbolsRequested,
    int symbolsSucceeded,
    int symbolsFailed,
    List<RefreshJobSummary> jobSummaries,
    List<RefreshItemResponse> items
) {
    public static RefreshDetailResponse from(RefreshRun run, List<RefreshJobSummary> jobSummaries,
                                             List<RefreshRunItem> items) {
        return new RefreshDetailResponse(
            run.getId(),
            run.getStatus().name(),
            run.getJobName(),
            run.getStartedAt(),
            run.getFinishedAt(),
            run.getSymbolsRequested(),
            run.getSymbolsSucceeded(),
            run.getSymbolsFailed(),
            jobSummaries,
            items.stream().map(RefreshItemResponse::from).toList()
        );
    }
}
