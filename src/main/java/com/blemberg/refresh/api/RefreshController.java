package com.blemberg.refresh.api;

import com.blemberg.refresh.application.RefreshDetailResponse;
import com.blemberg.refresh.application.MarketDataRefreshService;
import com.blemberg.refresh.application.RefreshResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/market-data")
public class RefreshController {

    private final MarketDataRefreshService refreshService;

    public RefreshController(MarketDataRefreshService refreshService) {
        this.refreshService = refreshService;
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh() {
        return refreshService.refreshAll();
    }

    @GetMapping("/refresh/latest")
    public RefreshDetailResponse latestRefresh() {
        return refreshService.latestRun();
    }

    @GetMapping("/refresh-runs")
    public List<RefreshResponse> refreshRuns(@RequestParam(defaultValue = "20") int limit) {
        return refreshService.recentRuns(limit);
    }

    @GetMapping("/refresh/{runId}")
    public RefreshDetailResponse refreshDetail(@PathVariable UUID runId) {
        return refreshService.getRun(runId);
    }
}
