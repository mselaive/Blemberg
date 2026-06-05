package com.blemberg.marketdata.infrastructure;

import com.blemberg.marketdata.domain.MarketDailyBar;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MarketDailyBarRepository extends JpaRepository<MarketDailyBar, Long> {

    List<MarketDailyBar> findBySymbolOrderByBarDateDesc(String symbol, Pageable pageable);

    Optional<MarketDailyBar> findTopBySymbolOrderByBarDateDesc(String symbol);

    boolean existsBySymbolAndBarDate(String symbol, java.time.LocalDate barDate);
}
