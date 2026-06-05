package com.blemberg.refresh.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_runs")
public class RefreshRun {

    @Id
    private UUID id;

    @Column(name = "job_name", nullable = false)
    private String jobName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefreshStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "symbols_requested", nullable = false)
    private int symbolsRequested;

    @Column(name = "symbols_succeeded", nullable = false)
    private int symbolsSucceeded;

    @Column(name = "symbols_failed", nullable = false)
    private int symbolsFailed;

    @Column(name = "error_message")
    private String errorMessage;

    protected RefreshRun() {
    }

    public RefreshRun(String jobName, int symbolsRequested) {
        this.id = UUID.randomUUID();
        this.jobName = jobName;
        this.status = RefreshStatus.FAILED;
        this.startedAt = Instant.now();
        this.symbolsRequested = symbolsRequested;
    }

    public void finish(int succeeded, int failed, String errorMessage) {
        this.symbolsSucceeded = succeeded;
        this.symbolsFailed = failed;
        this.errorMessage = errorMessage;
        this.finishedAt = Instant.now();
        if (failed == 0) {
            this.status = RefreshStatus.SUCCESS;
        } else if (succeeded > 0) {
            this.status = RefreshStatus.PARTIAL_SUCCESS;
        } else {
            this.status = RefreshStatus.FAILED;
        }
    }

    public UUID getId() {
        return id;
    }

    public String getJobName() {
        return jobName;
    }

    public RefreshStatus getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public int getSymbolsRequested() {
        return symbolsRequested;
    }

    public int getSymbolsSucceeded() {
        return symbolsSucceeded;
    }

    public int getSymbolsFailed() {
        return symbolsFailed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
