package com.blemberg.refresh.application;

public record RefreshJobSummary(
    String jobName,
    int requested,
    int succeeded,
    int failed,
    int skippedRateLimit
) {
}
