package com.blemberg.marketdata.infrastructure;

import com.blemberg.marketdata.domain.RiskFreeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiskFreeRateRepository extends JpaRepository<RiskFreeRate, Long> {

    List<RiskFreeRate> findAllByOrderByTenorMonthsAscAsOfDesc();
}
