package com.github.SuduIDE.persistentidecaches.ccsearch;

import com.github.SuduIDE.persistentidecaches.records.Trigram;
import com.github.SuduIDE.persistentidecaches.symbols.Symbol;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

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

    private List<Symbol> symbolsForTrigramInCounters(final Trigram trigram,
            final List<TrigramSymbolCounterLmdb> counters) {
        return counters.stream()
                .map(counter -> counter.getObjForTrigram(trigram))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<Symbol> getSymbols(final String request,
            final List<TrigramSymbolCounterLmdb> counters) {
        final var trigramSet = getTrigramsSet(request.toLowerCase());
        if (trigramSet.isEmpty()) {
            return List.of();
        }
        final var fileSet = new TreeSet<>(symbolsForTrigramInCounters(trigramSet.first(), counters));
        trigramSet.pollFirst();
        trigramSet.forEach(it -> fileSet.retainAll(symbolsForTrigramInCounters(it, counters)));
        return fileSet.stream()
                .map(it -> Pair.of(it, Matcher.match(request, it.name())))
                .sorted(Comparator.comparing((Pair<Symbol, Integer> pair) -> pair.getRight()).reversed())
                .map(Pair::getLeft)
                .toList();
    }

    public List<Symbol> getSymbolsFromClasses(
            final String request
    ) {
        return getSymbols(request, List.of(camelCaseIndex.getClassCounter()));
    }

    public List<Symbol> getSymbolsFromAny(final String request) {

        return getSymbols(request, List.of(
                camelCaseIndex.getClassCounter(),
                camelCaseIndex.getMethodCounter(),
                camelCaseIndex.getFieldCounter()
        ));
    }

}
