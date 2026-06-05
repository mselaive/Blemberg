package com.blemberg.marketdata.application;

import java.util.List;

public record SnapshotsResponse(
    List<SnapshotResponse> snapshots,
    List<String> missingSymbols
) {
}
