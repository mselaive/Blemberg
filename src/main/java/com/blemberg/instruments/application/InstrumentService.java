package com.blemberg.instruments.application;

import com.blemberg.instruments.domain.AssetClass;
import com.blemberg.instruments.domain.Instrument;
import com.blemberg.instruments.infrastructure.InstrumentRepository;
import com.blemberg.shared.api.Symbols;
import com.blemberg.shared.error.NotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;

    public InstrumentService(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    @Transactional(readOnly = true)
    public List<InstrumentResponse> list(String symbol, AssetClass assetClass, Boolean active) {
        Specification<Instrument> specification = Specification.where(null);
        if (symbol != null && !symbol.isBlank()) {
            String normalized = Symbols.normalize(symbol);
            specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("symbol"), normalized));
        }
        if (assetClass != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("assetClass"), assetClass));
        }
        if (active != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("active"), active));
        }
        return instrumentRepository.findAll(specification).stream()
            .map(InstrumentResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public InstrumentResponse get(String symbol) {
        return InstrumentResponse.from(findRequired(symbol));
    }

    @Transactional(readOnly = true)
    public Instrument findRequired(String symbol) {
        return instrumentRepository.findBySymbol(Symbols.normalize(symbol))
            .orElseThrow(() -> new NotFoundException("Instrument not found"));
    }
}
