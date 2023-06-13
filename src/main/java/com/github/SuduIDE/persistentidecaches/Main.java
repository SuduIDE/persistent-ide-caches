package com.github.SuduIDE.persistentidecaches;


import com.github.SuduIDE.persistentidecaches.records.Revision;
import java.nio.file.Path;

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
        try (final IndexesManager manager = new IndexesManager(false)) {
            final var trigramHistoryIndex = manager.addTrigramIndex();
            final var trigramIndexUtils = trigramHistoryIndex.getTrigramIndexUtils();
//            final var camelCaseIndex = manager.addCamelCaseIndex();
//            final var camelCaseIndexUtils = camelCaseIndex.getUtils();
//            final int LIMIT = 10;
            final var sizeCounterIndex = manager.addSizeCounterIndex();
            final int LIMIT = Integer.MAX_VALUE;
            benchmark(() -> manager.parseGitRepository(Path.of(args[0]), LIMIT));

            System.out.println("Sum size " + sizeCounterIndex.getSummarySize() + " bytes");
//            manager.getFileCache().forEach(((path, number) -> System.out.println(path + " " + number)));
//            Map<Symbol, Integer> symbols = new TreeMap<>();
//            camelCaseIndex.getClassCounter().forEach((trigram, symbol, integer) -> System.out.println(trigram.toPrettyString() + " " + symbol + " " + integer));
//            symbols.forEach((symbol, integer) -> System.out.println(symbol + " " + integer));
//            trigramHistoryIndex.getCounter()
//                    .forEach((tri, file, i) -> {
////                        if (file.toString().contains("ArraySet")) {
//                            System.out.println(tri.toPrettyString().replace("\n", "\\n") + " " + file + " " + i);
////                        }
//                    });
//            System.out.println(SEPARATOR);
//            trigramIndexUtils.filesForString("ArraySet").forEach(System.out::println);
//          спецэффект camelCaseIndexUtils.getSymbolsFromAny("create") -> LocalRemoteThreadsTest
//          спецэффект camelCaseIndexUtils.getSymbolsFromAny("crea") -> streamMapReduceReduce
//            System.out.println(SEPARATOR);
//            camelCaseIndexUtils.getSymbolsFromAny("ArrSe").forEach(System.out::println);
//            System.out.println(SEPARATOR);
//            camelCaseIndexUtils.getSymbolsFromAny("Impl").forEach(System.out::println);
//            System.out.println(SEPARATOR);
//            camelCaseIndexUtils.getSymbolsFromAny("HFVis").forEach(System.out::println);
//            System.out.println("Current revision: " + manager.getRevisions().getCurrentRevision());
////            trigramHistoryIndex.getCounter().forEach((tri, file, i) -> System.out.println(tri + " " + file + " " + i));
//            benchmark(() -> manager.checkoutToGitRevision("a12b6970620c5b83df8d786630e9372c8f56daba"));
////            benchmark(() -> System.out.println(trigramIndexUtils.filesForString("text")));
////            benchmark(() -> System.out.println(trigramIndexUtils.filesForString("another text")));
//            benchmarkCheckout(new Revision(2600), manager, manager.getRevisions());
//            benchmark(() -> manager.checkoutToGitRevision("a12b6970620c5b83df8d786630e9372c8f56daba"));
////        benchmarkCheckout(new Revision(100), trigramHistoryIndex);
////        benchmarkCheckout(new Revision(50), trigramHistoryIndex);
////        benchmarkCheckout(new Revision(LIMIT - 1), trigramHistoryIndex);
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
