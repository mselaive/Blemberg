package com.blemberg.marketdata.application;

import com.blemberg.instruments.application.InstrumentService;
import com.blemberg.instruments.domain.Instrument;
import com.blemberg.marketdata.domain.DividendYield;
import com.blemberg.marketdata.domain.HistoricalVolatilityCalculator;
import com.blemberg.marketdata.domain.MarketDailyBar;
import com.blemberg.marketdata.domain.MarketPriceSnapshot;
import com.blemberg.marketdata.domain.RateMethod;
import com.blemberg.marketdata.domain.RiskFreeRate;
import com.blemberg.marketdata.domain.RiskFreeRateSelection;
import com.blemberg.marketdata.domain.RiskFreeRateSelector;
import com.blemberg.marketdata.domain.VolatilityMethod;
import com.blemberg.marketdata.infrastructure.DividendYieldRepository;
import com.blemberg.marketdata.infrastructure.MarketDailyBarRepository;
import com.blemberg.marketdata.infrastructure.MarketPriceSnapshotRepository;
import com.blemberg.marketdata.infrastructure.RiskFreeRateRepository;
import com.blemberg.shared.api.Symbols;
import com.blemberg.shared.error.NotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MarketDataService {

    private final InstrumentService instrumentService;
    private final MarketPriceSnapshotRepository snapshotRepository;
    private final MarketDailyBarRepository barRepository;
    private final RiskFreeRateRepository riskFreeRateRepository;
    private final DividendYieldRepository dividendYieldRepository;
    private final MarketDataProperties properties;
    private final HistoricalVolatilityCalculator volatilityCalculator = new HistoricalVolatilityCalculator();
    private final RiskFreeRateSelector riskFreeRateSelector = new RiskFreeRateSelector();

    public MarketDataService(InstrumentService instrumentService,
                             MarketPriceSnapshotRepository snapshotRepository,
                             MarketDailyBarRepository barRepository,
                             RiskFreeRateRepository riskFreeRateRepository,
                             DividendYieldRepository dividendYieldRepository,
                             MarketDataProperties properties) {
        this.instrumentService = instrumentService;
        this.snapshotRepository = snapshotRepository;
        this.barRepository = barRepository;
        this.riskFreeRateRepository = riskFreeRateRepository;
        this.dividendYieldRepository = dividendYieldRepository;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public List<SnapshotResponse> snapshots(String symbolsCsv) {
        List<String> symbols = Symbols.normalizeCsv(symbolsCsv);
        Map<String, SnapshotResponse> latestBySymbol = new LinkedHashMap<>();
        for (String symbol : symbols) {
            MarketPriceSnapshot snapshot = snapshotRepository.findTopBySymbolOrderByAsOfDesc(symbol)
                .orElseThrow(() -> new NotFoundException("Snapshot not found"));
            latestBySymbol.put(symbol, SnapshotResponse.from(snapshot, isStale(snapshot.getAsOf(), properties.snapshotStaleAfter())));
        }
        return latestBySymbol.values().stream().toList();
    }

    @Transactional(readOnly = true)
    public PricingInputsResponse europeanOptionPricingInputs(String symbol, LocalDate maturityDate) {
        String normalized = Symbols.normalize(symbol);
        if (maturityDate == null || !maturityDate.isAfter(LocalDate.now(ZoneOffset.UTC))) {
            throw new IllegalArgumentException("Maturity date must be in the future");
        }

        Instrument instrument = instrumentService.findRequired(normalized);
        if (!instrument.isActive()) {
            throw new NotFoundException("Pricing inputs not found");
        }

        MarketPriceSnapshot snapshot = snapshotRepository.findTopBySymbolOrderByAsOfDesc(normalized)
            .orElseThrow(() -> new NotFoundException("Pricing inputs not found"));
        requirePositive(snapshot.getLastPrice(), "spot");

        List<MarketDailyBar> barsDescending = barRepository.findBySymbolOrderByBarDateDesc(
            normalized,
            PageRequest.of(0, properties.historicalVolatilityWindowDays() + 1)
        );
        if (barsDescending.size() < 2) {
            throw new NotFoundException("Pricing inputs not found");
        }
        List<MarketDailyBar> barsAscending = barsDescending.stream()
            .sorted(Comparator.comparing(MarketDailyBar::getBarDate))
            .toList();
        BigDecimal volatility = volatilityCalculator.annualizedRealizedVolatility(
            barsAscending.stream().map(MarketDailyBar::getClose).toList()
        );
        requirePositive(volatility, "volatility");

        RiskFreeRateSelection riskFreeRate = riskFreeRateSelector.select(
            maturityMonths(maturityDate),
            latestRiskFreeRates()
        );

        DividendYield dividendYield = dividendYieldRepository.findTopBySymbolOrderByAsOfDesc(normalized)
            .orElseGet(() -> new DividendYield(normalized, BigDecimal.ZERO,
                com.blemberg.marketdata.domain.DividendYieldMethod.UNKNOWN_ZERO, Instant.now()));
        if (dividendYield.getDividendYield().signum() < 0) {
            throw new NotFoundException("Pricing inputs not found");
        }

        Instant barsAsOf = barsAscending.get(barsAscending.size() - 1).getBarDate()
            .atStartOfDay().toInstant(ZoneOffset.UTC);
        boolean stale = isStale(snapshot.getAsOf(), properties.snapshotStaleAfter())
            || isStale(barsAsOf, properties.barsStaleAfter())
            || isStale(riskFreeRate.asOf(), properties.ratesStaleAfter())
            || isStale(dividendYield.getAsOf(), properties.dividendsStaleAfter());

        Instant asOf = min(snapshot.getAsOf(), barsAsOf, riskFreeRate.asOf(), dividendYield.getAsOf());

        return new PricingInputsResponse(
            normalized,
            snapshot.getLastPrice(),
            volatility,
            VolatilityMethod.HISTORICAL_REALIZED_60D.name(),
            riskFreeRate.rate(),
            riskFreeRate.method().name(),
            dividendYield.getDividendYield(),
            dividendYield.getMethod().name(),
            instrument.getCurrency(),
            asOf,
            "BLEMBERG",
            stale
        );
    }

    private List<RiskFreeRate> latestRiskFreeRates() {
        Map<Integer, RiskFreeRate> latestByTenor = new LinkedHashMap<>();
        for (RiskFreeRate rate : riskFreeRateRepository.findAllByOrderByTenorMonthsAscAsOfDesc()) {
            latestByTenor.putIfAbsent(rate.getTenorMonths(), rate);
        }
        if (latestByTenor.isEmpty()) {
            throw new NotFoundException("Pricing inputs not found");
        }
        return latestByTenor.values().stream().toList();
    }

    private double maturityMonths(LocalDate maturityDate) {
        long days = ChronoUnit.DAYS.between(LocalDate.now(ZoneOffset.UTC), maturityDate);
        return days / 365.0 * 12.0;
    }

    private boolean isStale(Instant asOf, java.time.Duration maxAge) {
        return asOf.isBefore(Instant.now().minus(maxAge));
    }

    private void requirePositive(BigDecimal value, String field) {
        if (value == null || value.signum() <= 0) {
            throw new NotFoundException("Pricing inputs not found");
        }
    }

    private Instant min(Instant first, Instant second, Instant third, Instant fourth) {
        return List.of(first, second, third, fourth).stream()
            .min(Comparator.naturalOrder())
            .orElse(first);
    }
}
