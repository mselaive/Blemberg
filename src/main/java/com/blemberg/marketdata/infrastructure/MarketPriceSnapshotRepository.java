package com.blemberg.marketdata.infrastructure;

import com.blemberg.marketdata.domain.MarketPriceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MarketPriceSnapshotRepository extends JpaRepository<MarketPriceSnapshot, Long> {

    Optional<MarketPriceSnapshot> findTopBySymbolOrderByAsOfDesc(String symbol);

    List<MarketPriceSnapshot> findBySymbolInOrderBySymbolAscAsOfDesc(Collection<String> symbols);
}
