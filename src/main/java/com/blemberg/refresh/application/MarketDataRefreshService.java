package com.blemberg.refresh.application;

import com.blemberg.instruments.domain.Instrument;
import com.blemberg.instruments.domain.Provider;
import com.blemberg.instruments.infrastructure.InstrumentRepository;
import com.blemberg.marketdata.domain.MarketDailyBar;
import com.blemberg.marketdata.domain.MarketPriceSnapshot;
import com.blemberg.marketdata.domain.RiskFreeRate;
import com.blemberg.marketdata.infrastructure.MarketDailyBarRepository;
import com.blemberg.marketdata.infrastructure.MarketPriceSnapshotRepository;
import com.blemberg.marketdata.infrastructure.RiskFreeRateRepository;
import com.blemberg.providers.fred.FredClient;
import com.blemberg.providers.fred.FredRateObservation;
import com.blemberg.providers.twelvedata.TwelveDataClient;
import com.blemberg.providers.twelvedata.TwelveDataDailyBar;
import com.blemberg.providers.twelvedata.TwelveDataQuote;
import com.blemberg.refresh.domain.RefreshRun;
import com.blemberg.refresh.infrastructure.RefreshRunRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MarketDataRefreshService {

    private static final int DAILY_BAR_OUTPUT_SIZE = 90;

    private final InstrumentRepository instrumentRepository;
    private final MarketPriceSnapshotRepository snapshotRepository;
    private final MarketDailyBarRepository barRepository;
    private final RiskFreeRateRepository riskFreeRateRepository;
    private final RefreshRunRepository refreshRunRepository;
    private final TwelveDataClient twelveDataClient;
    private final FredClient fredClient;

    public MarketDataRefreshService(InstrumentRepository instrumentRepository,
                                    MarketPriceSnapshotRepository snapshotRepository,
                                    MarketDailyBarRepository barRepository,
                                    RiskFreeRateRepository riskFreeRateRepository,
                                    RefreshRunRepository refreshRunRepository,
                                    TwelveDataClient twelveDataClient,
                                    FredClient fredClient) {
        this.instrumentRepository = instrumentRepository;
        this.snapshotRepository = snapshotRepository;
        this.barRepository = barRepository;
        this.riskFreeRateRepository = riskFreeRateRepository;
        this.refreshRunRepository = refreshRunRepository;
        this.twelveDataClient = twelveDataClient;
        this.fredClient = fredClient;
    }

    @Transactional
    public RefreshResponse refreshAll() {
        List<Instrument> instruments = activeInstruments();
        RefreshRun run = new RefreshRun("manual-market-data-refresh", instruments.size());
        Counters counters = new Counters();
        List<String> errors = new ArrayList<>();

        refreshSnapshots(instruments, counters, errors);
        refreshDailyBars(instruments, counters, errors);
        refreshRates(counters, errors);

        run.finish(counters.succeeded, counters.failed, sanitize(errors));
        refreshRunRepository.save(run);
        return RefreshResponse.from(run);
    }

    @Scheduled(cron = "0 0 * * * *", zone = "UTC")
    @Transactional
    public void scheduledSnapshotRefresh() {
        List<Instrument> instruments = activeInstruments();
        RefreshRun run = new RefreshRun("scheduled-snapshot-refresh", instruments.size());
        Counters counters = new Counters();
        List<String> errors = new ArrayList<>();
        refreshSnapshots(instruments, counters, errors);
        run.finish(counters.succeeded, counters.failed, sanitize(errors));
        refreshRunRepository.save(run);
    }

    @Scheduled(cron = "0 30 22 * * MON-FRI", zone = "UTC")
    @Transactional
    public void scheduledDailyRefresh() {
        List<Instrument> instruments = activeInstruments();
        RefreshRun run = new RefreshRun("scheduled-daily-refresh", instruments.size());
        Counters counters = new Counters();
        List<String> errors = new ArrayList<>();
        refreshDailyBars(instruments, counters, errors);
        refreshRates(counters, errors);
        run.finish(counters.succeeded, counters.failed, sanitize(errors));
        refreshRunRepository.save(run);
    }

    private List<Instrument> activeInstruments() {
        return instrumentRepository.findAll().stream()
            .filter(Instrument::isActive)
            .toList();
    }

    private void refreshSnapshots(List<Instrument> instruments, Counters counters, List<String> errors) {
        for (Instrument instrument : instruments) {
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
                counters.succeeded++;
            } catch (RuntimeException exception) {
                counters.failed++;
                errors.add(instrument.getSymbol());
            }
        }
    }

    private void refreshDailyBars(List<Instrument> instruments, Counters counters, List<String> errors) {
        for (Instrument instrument : instruments) {
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
                counters.succeeded++;
            } catch (RuntimeException exception) {
                counters.failed++;
                errors.add(instrument.getSymbol() + ":bars");
            }
        }
    }

    private void refreshRates(Counters counters, List<String> errors) {
        try {
            for (FredRateObservation observation : fredClient.fetchLatestTreasuryRates()) {
                riskFreeRateRepository.save(new RiskFreeRate(
                    observation.tenor().code(),
                    observation.tenor().months(),
                    observation.tenor().seriesId(),
                    observation.rateDecimal(),
                    observation.observationDate(),
                    observation.asOf()
                ));
            }
            counters.succeeded++;
        } catch (RuntimeException exception) {
            counters.failed++;
            errors.add("FRED");
        }
    }

    private String sanitize(List<String> errors) {
        if (errors.isEmpty()) {
            return null;
        }
        return "Refresh failed for: " + String.join(", ", errors.stream().limit(20).toList());
    }

    private static class Counters {
        int succeeded;
        int failed;
    }
}
