package com.github.SuduIDE.persistentidecaches.ccsearch;

import com.github.SuduIDE.persistentidecaches.records.Trigram;
import com.github.SuduIDE.persistentidecaches.symbols.Symbol;

public record TrigramSymbol(Trigram trigram, Symbol word) implements Comparable<TrigramSymbol> {

    @Override
    public int compareTo(final TrigramSymbol o) {
        final int res = trigram.compareTo(o.trigram);
        return res == 0 ? word.compareTo(o.word) : res;
    }
}
