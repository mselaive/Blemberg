package com.blemberg.instruments.infrastructure;

import com.blemberg.instruments.domain.AssetClass;
import com.blemberg.instruments.domain.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InstrumentRepository extends JpaRepository<Instrument, Long>, JpaSpecificationExecutor<Instrument> {

    Optional<Instrument> findBySymbol(String symbol);

    List<Instrument> findBySymbolInAndActiveTrue(Collection<String> symbols);

    long countByAssetClassAndActive(AssetClass assetClass, boolean active);
}
