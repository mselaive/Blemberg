package com.blemberg.refresh.application;

import com.blemberg.instruments.domain.Instrument;
import com.blemberg.instruments.domain.Provider;
import com.blemberg.instruments.infrastructure.InstrumentRepository;
import com.blemberg.marketdata.domain.DividendYield;
import com.blemberg.marketdata.domain.DividendYieldMethod;
import com.blemberg.marketdata.domain.MarketDailyBar;
import com.blemberg.marketdata.domain.MarketPriceSnapshot;
import com.blemberg.marketdata.domain.RiskFreeRate;
import com.blemberg.marketdata.infrastructure.DividendYieldRepository;
import com.blemberg.marketdata.infrastructure.MarketDailyBarRepository;
import com.blemberg.marketdata.infrastructure.MarketPriceSnapshotRepository;
import com.blemberg.marketdata.infrastructure.RiskFreeRateRepository;
import com.blemberg.providers.fred.FredClient;
import com.blemberg.providers.fred.FredRateObservation;
import com.blemberg.providers.fred.FredTenor;
import com.blemberg.providers.twelvedata.TwelveDataClient;
import com.blemberg.providers.twelvedata.TwelveDataDailyBar;
import com.blemberg.providers.twelvedata.TwelveDataQuote;
import com.blemberg.providers.twelvedata.TwelveDataRateLimitException;
import com.blemberg.refresh.domain.RefreshItemStatus;
import com.blemberg.refresh.domain.RefreshJobName;
import com.blemberg.refresh.domain.RefreshRun;
import com.blemberg.refresh.domain.RefreshRunItem;
import com.blemberg.refresh.infrastructure.RefreshRunItemRepository;
import com.blemberg.refresh.infrastructure.RefreshRunRepository;
import com.blemberg.shared.error.NotFoundException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MarketDataRefreshService {

    private static final int DAILY_BAR_OUTPUT_SIZE = 90;
    private static final String TWELVE_DATA = "TWELVE_DATA";
    private static final String FRED = "FRED";
    private static final String BLEMBERG = "BLEMBERG";

    private final InstrumentRepository instrumentRepository;
    private final MarketPriceSnapshotRepository snapshotRepository;
    private final MarketDailyBarRepository barRepository;
    private final RiskFreeRateRepository riskFreeRateRepository;
    private final DividendYieldRepository dividendYieldRepository;
    private final RefreshRunRepository refreshRunRepository;
    private final RefreshRunItemRepository refreshRunItemRepository;
    private final TwelveDataClient twelveDataClient;
    private final FredClient fredClient;

    public MarketDataRefreshService(InstrumentRepository instrumentRepository,
                                    MarketPriceSnapshotRepository snapshotRepository,
                                    MarketDailyBarRepository barRepository,
                                    RiskFreeRateRepository riskFreeRateRepository,
                                    DividendYieldRepository dividendYieldRepository,
                                    RefreshRunRepository refreshRunRepository,
                                    RefreshRunItemRepository refreshRunItemRepository,
                                    TwelveDataClient twelveDataClient,
                                    FredClient fredClient) {
        this.instrumentRepository = instrumentRepository;
        this.snapshotRepository = snapshotRepository;
        this.barRepository = barRepository;
        this.riskFreeRateRepository = riskFreeRateRepository;
        this.dividendYieldRepository = dividendYieldRepository;
        this.refreshRunRepository = refreshRunRepository;
        this.refreshRunItemRepository = refreshRunItemRepository;
        this.twelveDataClient = twelveDataClient;
        this.fredClient = fredClient;
    }

    @Transactional
    public RefreshResponse refreshAll() {
        List<Instrument> instruments = activeInstrumentsAaplFirst();
        RefreshRun run = refreshRunRepository.save(new RefreshRun("manual-market-data-refresh", instruments.size()));

        refreshRates(run.getId());
        refreshDividendDefaults(run.getId(), instruments);
        refreshSymbolsAaplFirst(run.getId(), instruments);

        return finishAndRespond(run);
    }

    @Transactional(readOnly = true)
    public RefreshDetailResponse getRun(UUID runId) {
        RefreshRun run = refreshRunRepository.findById(runId)
            .orElseThrow(() -> new NotFoundException("Refresh run not found"));
        List<RefreshRunItem> items = refreshRunItemRepository.findByRunIdOrderByStartedAtAscIdAsc(run.getId());
        return RefreshDetailResponse.from(run, summarize(items), items);
    }

    @Transactional(readOnly = true)
    public RefreshDetailResponse latestRun() {
        RefreshRun run = refreshRunRepository.findTopByOrderByStartedAtDesc()
            .orElseThrow(() -> new NotFoundException("Refresh run not found"));
        List<RefreshRunItem> items = refreshRunItemRepository.findByRunIdOrderByStartedAtAscIdAsc(run.getId());
        return RefreshDetailResponse.from(run, summarize(items), items);
    }

    @Transactional(readOnly = true)
    public List<RefreshResponse> recentRuns(int limit) {
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100");
        }
        return refreshRunRepository.findAllByOrderByStartedAtDesc(PageRequest.of(0, limit)).stream()
            .map(run -> {
                List<RefreshRunItem> items = refreshRunItemRepository.findByRunIdOrderByStartedAtAscIdAsc(run.getId());
                List<RefreshRunItem> errors = errors(items).stream().limit(20).toList();
                return RefreshResponse.from(run, summarize(items), errors);
            })
            .toList();
    }

    @Scheduled(cron = "0 0 * * * *", zone = "UTC")
    @Transactional
    public void scheduledSnapshotRefresh() {
        List<Instrument> instruments = activeInstrumentsAaplFirst();
        RefreshRun run = refreshRunRepository.save(new RefreshRun("scheduled-snapshot-refresh", instruments.size()));
        for (Instrument instrument : instruments) {
            if (!refreshQuote(run.getId(), instrument)) {
                break;
            }
        }
        finishAndRespond(run);
    }

    @Scheduled(cron = "0 30 22 * * MON-FRI", zone = "UTC")
    @Transactional
    public void scheduledDailyRefresh() {
        List<Instrument> instruments = activeInstrumentsAaplFirst();
        RefreshRun run = refreshRunRepository.save(new RefreshRun("scheduled-daily-refresh", instruments.size()));
        refreshRates(run.getId());
        refreshDividendDefaults(run.getId(), instruments);
        for (Instrument instrument : instruments) {
            if (!refreshBars(run.getId(), instrument)) {
                break;
            }
        }
        finishAndRespond(run);
    }

    private void refreshSymbolsAaplFirst(UUID runId, List<Instrument> instruments) {
        for (int i = 0; i < instruments.size(); i++) {
            Instrument instrument = instruments.get(i);
            if (!refreshQuote(runId, instrument)) {
                saveRateLimit(runId, RefreshJobName.DAILY_BARS, instrument.getSymbol());
                markRemainingSymbolsSkipped(runId, instruments, i + 1);
                break;
            }
            if (!refreshBars(runId, instrument)) {
                markRemainingBarsSkipped(runId, instruments, i + 1);
                break;
            }
        }
    }

    private boolean refreshQuote(UUID runId, Instrument instrument) {
        Instant startedAt = Instant.now();
        try {
            TwelveDataQuote quote = twelveDataClient.fetchQuote(instrument.getProviderSymbol());
            snapshotRepository.save(new MarketPriceSnapshot(
                instrument.getSymbol(),
                quote.lastPrice(),
                quote.open(),
                quote.high(),
                quote.low(),
                quote.previousClose(),
                quote.volume(),
                quote.currency(),
                Provider.TWELVE_DATA,
                quote.asOf()
            ));
            saveSuccess(runId, RefreshJobName.QUOTES, TWELVE_DATA, instrument.getSymbol(), null, startedAt);
            return true;
        } catch (TwelveDataRateLimitException exception) {
            saveRateLimit(runId, RefreshJobName.QUOTES, instrument.getSymbol());
            return false;
        } catch (RuntimeException exception) {
            saveFailure(runId, RefreshJobName.QUOTES, TWELVE_DATA, instrument.getSymbol(), null, exception, startedAt);
            return true;
        }
    }

    private boolean refreshBars(UUID runId, Instrument instrument) {
        Instant startedAt = Instant.now();
        try {
            List<TwelveDataDailyBar> bars = twelveDataClient.fetchDailyBars(instrument.getProviderSymbol(), DAILY_BAR_OUTPUT_SIZE);
            for (TwelveDataDailyBar bar : bars) {
                if (!barRepository.existsBySymbolAndBarDate(instrument.getSymbol(), bar.date())) {
                    barRepository.save(new MarketDailyBar(
                        instrument.getSymbol(),
                        bar.date(),
                        bar.open(),
                        bar.high(),
                        bar.low(),
                        bar.close(),
                        bar.volume(),
                        Provider.TWELVE_DATA
                    ));
                }
            }
            saveSuccess(runId, RefreshJobName.DAILY_BARS, TWELVE_DATA, instrument.getSymbol(), null, startedAt);
            return true;
        } catch (TwelveDataRateLimitException exception) {
            saveRateLimit(runId, RefreshJobName.DAILY_BARS, instrument.getSymbol());
            return false;
        } catch (RuntimeException exception) {
            saveFailure(runId, RefreshJobName.DAILY_BARS, TWELVE_DATA, instrument.getSymbol(), null, exception, startedAt);
            return true;
        }
    }

    private void refreshRates(UUID runId) {
        for (FredTenor tenor : FredTenor.values()) {
            Instant startedAt = Instant.now();
            try {
                FredRateObservation observation = fredClient.fetchLatestTreasuryRate(tenor);
                riskFreeRateRepository.save(new RiskFreeRate(
                    observation.tenor().code(),
                    observation.tenor().months(),
                    observation.tenor().seriesId(),
                    observation.rateDecimal(),
                    observation.observationDate(),
                    observation.asOf()
                ));
                saveSuccess(runId, RefreshJobName.RISK_FREE_RATES, FRED, null, tenor.code(), startedAt);
            } catch (RuntimeException exception) {
                saveFailure(runId, RefreshJobName.RISK_FREE_RATES, FRED, null, tenor.code(), exception, startedAt);
            }
        }
    }

    private void refreshDividendDefaults(UUID runId, List<Instrument> instruments) {
        for (Instrument instrument : instruments) {
            Instant startedAt = Instant.now();
            try {
                if (!dividendYieldRepository.existsBySymbol(instrument.getSymbol())) {
                    dividendYieldRepository.save(new DividendYield(
                        instrument.getSymbol(),
                        BigDecimal.ZERO,
                        DividendYieldMethod.UNKNOWN_ZERO,
                        Instant.now()
                    ));
                }
                saveSuccess(runId, RefreshJobName.DIVIDEND_YIELDS, BLEMBERG, instrument.getSymbol(), null, startedAt);
            } catch (RuntimeException exception) {
                saveFailure(runId, RefreshJobName.DIVIDEND_YIELDS, BLEMBERG, instrument.getSymbol(), null, exception, startedAt);
            }
        }
    }

    private RefreshResponse finishAndRespond(RefreshRun run) {
        List<RefreshRunItem> items = refreshRunItemRepository.findByRunIdOrderByStartedAtAscIdAsc(run.getId());
        SymbolCounters symbolCounters = symbolCounters(items, run.getSymbolsRequested());
        List<RefreshRunItem> errors = errors(items);
        run.finish(symbolCounters.succeeded, symbolCounters.failed, sanitize(errors));
        refreshRunRepository.save(run);
        return RefreshResponse.from(run, summarize(items), errors.stream().limit(20).toList());
    }

    private List<Instrument> activeInstrumentsAaplFirst() {
        Map<String, Integer> priority = Map.of(
            "AAPL", 0,
            "AMZN", 1,
            "MSFT", 2,
            "SPY", 3
        );
        return instrumentRepository.findAll().stream()
            .filter(Instrument::isActive)
            .sorted(Comparator.comparing((Instrument instrument) ->
                    priority.getOrDefault(instrument.getSymbol(), Integer.MAX_VALUE))
                .thenComparing(Instrument::getSymbol))
            .toList();
    }

    private void markRemainingSymbolsSkipped(UUID runId, List<Instrument> instruments, int startIndex) {
        for (int i = startIndex; i < instruments.size(); i++) {
            Instrument instrument = instruments.get(i);
            saveRateLimit(runId, RefreshJobName.QUOTES, instrument.getSymbol());
            saveRateLimit(runId, RefreshJobName.DAILY_BARS, instrument.getSymbol());
        }
    }

    private void markRemainingBarsSkipped(UUID runId, List<Instrument> instruments, int startIndex) {
        for (int i = startIndex; i < instruments.size(); i++) {
            saveRateLimit(runId, RefreshJobName.DAILY_BARS, instruments.get(i).getSymbol());
        }
    }

    private void saveSuccess(UUID runId, RefreshJobName jobName, String provider, String symbol, String tenorCode,
                             Instant startedAt) {
        refreshRunItemRepository.save(RefreshRunItem.success(runId, jobName, provider, symbol, tenorCode, startedAt));
    }

    private void saveFailure(UUID runId, RefreshJobName jobName, String provider, String symbol, String tenorCode,
                             RuntimeException exception, Instant startedAt) {
        refreshRunItemRepository.save(RefreshRunItem.failure(
            runId,
            jobName,
            provider,
            symbol,
            tenorCode,
            errorCode(exception),
            sanitizeMessage(exception),
            startedAt
        ));
    }

    private void saveRateLimit(UUID runId, RefreshJobName jobName, String symbol) {
        refreshRunItemRepository.save(RefreshRunItem.skippedRateLimit(
            runId,
            jobName,
            TWELVE_DATA,
            symbol,
            "Twelve Data free-tier rate limit reached; item was not attempted."
        ));
    }

    private List<RefreshJobSummary> summarize(List<RefreshRunItem> items) {
        Map<RefreshJobName, JobCounters> counters = new EnumMap<>(RefreshJobName.class);
        for (RefreshJobName jobName : RefreshJobName.values()) {
            counters.put(jobName, new JobCounters());
        }
        for (RefreshRunItem item : items) {
            JobCounters counter = counters.get(item.getJobName());
            counter.requested++;
            if (item.getStatus() == RefreshItemStatus.SUCCESS) {
                counter.succeeded++;
            } else if (item.getStatus() == RefreshItemStatus.SKIPPED_RATE_LIMIT) {
                counter.skippedRateLimit++;
            } else {
                counter.failed++;
            }
        }
        List<RefreshJobSummary> summaries = new ArrayList<>();
        for (Map.Entry<RefreshJobName, JobCounters> entry : counters.entrySet()) {
            JobCounters counter = entry.getValue();
            summaries.add(new RefreshJobSummary(
                entry.getKey().name(),
                counter.requested,
                counter.succeeded,
                counter.failed,
                counter.skippedRateLimit
            ));
        }
        return summaries;
    }

    private SymbolCounters symbolCounters(List<RefreshRunItem> items, int requested) {
        Map<String, SymbolStatus> statuses = new LinkedHashMap<>();
        for (RefreshRunItem item : items) {
            if (item.getSymbol() == null) {
                continue;
            }
            SymbolStatus status = statuses.computeIfAbsent(item.getSymbol(), ignored -> new SymbolStatus());
            if (item.getJobName() == RefreshJobName.QUOTES) {
                status.quoteSuccess = item.getStatus() == RefreshItemStatus.SUCCESS;
            }
            if (item.getJobName() == RefreshJobName.DAILY_BARS) {
                status.barsSuccess = item.getStatus() == RefreshItemStatus.SUCCESS;
            }
        }
        int succeeded = 0;
        for (SymbolStatus status : statuses.values()) {
            if (status.quoteSuccess && status.barsSuccess) {
                succeeded++;
            }
        }
        return new SymbolCounters(succeeded, Math.max(0, requested - succeeded));
    }

    private List<RefreshRunItem> errors(List<RefreshRunItem> items) {
        return items.stream()
            .filter(item -> item.getStatus() != RefreshItemStatus.SUCCESS)
            .toList();
    }

    private String sanitize(List<RefreshRunItem> errors) {
        if (errors.isEmpty()) {
            return null;
        }
        return "Refresh had " + errors.size() + " failed or skipped item(s). See refresh detail endpoint.";
    }

    private String errorCode(RuntimeException exception) {
        String simpleName = exception.getClass().getSimpleName();
        if (simpleName.contains("ProviderUnavailable")) {
            return "PROVIDER_UNAVAILABLE";
        }
        return "REFRESH_ITEM_FAILED";
    }

    private String sanitizeMessage(RuntimeException exception) {
        if (exception instanceof TwelveDataRateLimitException) {
            return "Provider rate limit reached.";
        }
        return "Provider unavailable or returned unusable data.";
    }

    private static class JobCounters {
        int requested;
        int succeeded;
        int failed;
        int skippedRateLimit;
    }

    private static class SymbolStatus {
        boolean quoteSuccess;
        boolean barsSuccess;
    }

    private record SymbolCounters(int succeeded, int failed) {
    }
}
