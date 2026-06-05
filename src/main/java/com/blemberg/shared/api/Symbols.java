package com.blemberg.shared.api;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class Symbols {

    private Symbols() {
    }

    public static String normalize(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol is required");
        }
        return symbol.trim().toUpperCase(Locale.ROOT);
    }

    public static List<String> normalizeCsv(String symbols) {
        if (symbols == null || symbols.isBlank()) {
            throw new IllegalArgumentException("Symbols are required");
        }
        return Arrays.stream(symbols.split(","))
            .map(Symbols::normalize)
            .distinct()
            .toList();
    }
}
