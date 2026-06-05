package com.blemberg.refresh.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_run_items")
public class RefreshRunItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_name", nullable = false)
    private RefreshJobName jobName;

    @Column(nullable = false)
    private String provider;

    private String symbol;

    @Column(name = "tenor_code")
    private String tenorCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefreshItemStatus status;

    @Column(name = "error_code")
    private String errorCode;

    private String message;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at", nullable = false)
    private Instant finishedAt;

    protected RefreshRunItem() {
    }

    private RefreshRunItem(UUID runId, RefreshJobName jobName, String provider, String symbol, String tenorCode,
                           RefreshItemStatus status, String errorCode, String message, Instant startedAt,
                           Instant finishedAt) {
        this.runId = runId;
        this.jobName = jobName;
        this.provider = provider;
        this.symbol = symbol;
        this.tenorCode = tenorCode;
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public static RefreshRunItem success(UUID runId, RefreshJobName jobName, String provider, String symbol,
                                         String tenorCode, Instant startedAt) {
        return new RefreshRunItem(runId, jobName, provider, symbol, tenorCode, RefreshItemStatus.SUCCESS,
            null, null, startedAt, Instant.now());
    }

    public static RefreshRunItem failure(UUID runId, RefreshJobName jobName, String provider, String symbol,
                                         String tenorCode, String errorCode, String message, Instant startedAt) {
        return new RefreshRunItem(runId, jobName, provider, symbol, tenorCode, RefreshItemStatus.FAILED,
            errorCode, message, startedAt, Instant.now());
    }

    public static RefreshRunItem skippedRateLimit(UUID runId, RefreshJobName jobName, String provider, String symbol,
                                                  String message) {
        Instant now = Instant.now();
        return new RefreshRunItem(runId, jobName, provider, symbol, null, RefreshItemStatus.SKIPPED_RATE_LIMIT,
            "RATE_LIMIT", message, now, now);
    }

    public Long getId() {
        return id;
    }

    public UUID getRunId() {
        return runId;
    }

    public RefreshJobName getJobName() {
        return jobName;
    }

    public String getProvider() {
        return provider;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getTenorCode() {
        return tenorCode;
    }

    public RefreshItemStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }
}
