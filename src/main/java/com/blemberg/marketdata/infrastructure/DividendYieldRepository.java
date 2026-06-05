package com.blemberg.marketdata.infrastructure;

import com.blemberg.marketdata.domain.DividendYield;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DividendYieldRepository extends JpaRepository<DividendYield, Long> {

    Optional<DividendYield> findTopBySymbolOrderByAsOfDesc(String symbol);

    boolean existsBySymbol(String symbol);
}
