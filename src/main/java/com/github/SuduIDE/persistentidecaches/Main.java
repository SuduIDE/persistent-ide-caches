package com.github.SuduIDE.persistentidecaches;


import com.github.SuduIDE.persistentidecaches.records.Revision;
import com.github.SuduIDE.persistentidecaches.records.Trigram;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Main {


    public static final String SEPARATOR = "-----";

    // needs java options:
    /*
    --add-opens java.base/java.nio=ALL-UNNAMED
    --add-opens java.base/sun.nio.ch=ALL-UNNAMED
    */
    public static void main(final String[] args) {
        if (args.length < 1) {
            throw new RuntimeException("Needs path to repository as first arg");
        }
        try (final IndexesManager manager = new IndexesManager(true)) {
            final var trigramHistoryIndex = manager.addTrigramIndex();
            final var trigramIndexUtils = trigramHistoryIndex.getTrigramIndexUtils();
//            final var camelCaseIndex = manager.addCamelCaseIndex();
//            final var camelCaseIndexUtils = camelCaseIndex.getUtils();
//            final int LIMIT = 10;
            final var sizeCounterIndex = manager.addSizeCounterIndex();
            final int LIMIT = Integer.MAX_VALUE;
            benchmark(() -> manager.parseGitRepository(Path.of(args[0]), LIMIT));

            System.out.println("Sum size " + sizeCounterIndex.getSummarySize() + " bytes");
            final Map<Trigram, Integer> map = new HashMap<>();
            trigramHistoryIndex.getCounter().forEach(((trigram, path, integer) -> map.merge(trigram, 0, Integer::sum)));
            try {
                Files.writeString(Path.of("res.csv"), map.entrySet().stream()
                    .sorted(Entry.comparingByValue())
                    .map(it -> it.getKey().toPrettyString() + "," + it.getValue())
                    .collect(Collectors.joining("\n")));
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void benchmark(final Runnable runnable) {
        final long start = System.currentTimeMillis();
        runnable.run();
        System.out.println("Benchmarked: " + ((System.currentTimeMillis() - start) / 1000) + " second");
    }

    public static void benchmarkCheckout(final Revision targetRevision, final IndexesManager manager,
        final Revisions revisions) {
        benchmark(() -> {
            System.out.printf("checkout from %d to %d\n", revisions.getCurrentRevision().revision(),
                targetRevision.revision());
            manager.checkout(targetRevision);
            revisions.setCurrentRevision(targetRevision);
        });
    }
}
