package com.blemberg.instruments.domain;

import com.blemberg.shared.api.Symbols;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SymbolsTest {

    @Test
    void normalizesSymbolsToUppercase() {
        assertThat(Symbols.normalize(" aapl ")).isEqualTo("AAPL");
    }

    @Test
    void rejectsBlankSymbols() {
        assertThatThrownBy(() -> Symbols.normalize(" "))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
