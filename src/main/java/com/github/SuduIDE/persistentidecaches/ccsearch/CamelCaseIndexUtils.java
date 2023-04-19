package com.github.SuduIDE.persistentidecaches.ccsearch;

import com.github.SuduIDE.persistentidecaches.records.Trigram;
import com.github.SuduIDE.persistentidecaches.symbols.Symbol;
import java.util.List;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class CamelCaseIndexUtils {

    private final CamelCaseIndex camelCaseIndex;

    public CamelCaseIndexUtils(final CamelCaseIndex camelCaseIndex) {
        this.camelCaseIndex = camelCaseIndex;
    }

    public static NavigableSet<Trigram> getTrigramsSet(final String request) {
        final NavigableSet<Trigram> trigramSet = new TreeSet<>();
        final byte[] bytes = request.getBytes();
        for (int i = 2; i < bytes.length; i++) {
            final Trigram trigram = new Trigram(new byte[]{bytes[i - 2], bytes[i - 1], bytes[i]});
            trigramSet.add(trigram);
        }
        return trigramSet;
    }

    private Set<Symbol> symbolsForTrigramInCounter(final Trigram trigram,
            final NavigableMap<TrigramSymbol, Long> counter) {
        return counter.subMap(new TrigramSymbol(trigram, Symbol.MIN), new TrigramSymbol(trigram, Symbol.MAX)).keySet()
                .stream().map(TrigramSymbol::word).collect(Collectors.toSet());
    }

    public List<Symbol> getSymbols(final String request,
            final NavigableMap<TrigramSymbol, Long> counter) {
        final var trigramSet = getTrigramsSet(request);
        final var fileSet = new TreeSet<>(symbolsForTrigramInCounter(trigramSet.first(), counter));
        trigramSet.pollFirst();
        trigramSet.forEach(it -> fileSet.retainAll(symbolsForTrigramInCounter(trigramSet.first(), counter)));
        return fileSet.stream().toList();
    }

    public List<Symbol> getSymbolsFromClasses(
            final String request
    ) {
        return getSymbols(request, camelCaseIndex.getClassCounter());
    }

}
