package com.blemberg.instruments.api;

import com.blemberg.instruments.application.InstrumentResponse;
import com.blemberg.instruments.application.InstrumentService;
import com.blemberg.instruments.domain.AssetClass;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {

    private final InstrumentService instrumentService;

    public InstrumentController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @GetMapping
    public List<InstrumentResponse> list(
        @RequestParam(required = false) String symbol,
        @RequestParam(required = false) AssetClass assetClass,
        @RequestParam(required = false) Boolean active
    ) {
        return instrumentService.list(symbol, assetClass, active);
    }

    @GetMapping("/{symbol}")
    public InstrumentResponse get(@PathVariable String symbol) {
        return instrumentService.get(symbol);
    }
}
