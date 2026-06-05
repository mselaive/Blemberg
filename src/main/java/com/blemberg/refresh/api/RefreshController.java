package com.blemberg.refresh.api;

import com.blemberg.refresh.application.MarketDataRefreshService;
import com.blemberg.refresh.application.RefreshResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
