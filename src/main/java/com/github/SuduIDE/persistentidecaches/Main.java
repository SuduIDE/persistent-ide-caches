package com.github.SuduIDE.persistentidecaches;


import com.github.SuduIDE.persistentidecaches.records.Revision;
import java.nio.file.Path;

public class Main {


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
//            final var trigramHistoryIndex = manager.addTrigramIndex();
//            final var trigramIndexUtils = trigramHistoryIndex.getTrigramIndexUtils();
            final var camelCaseIndex = manager.addCamelCaseIndex();
            final var camelCaseIndexUtils = camelCaseIndex.getUtils();
////            final int LIMIT = 50;
            final int LIMIT = Integer.MAX_VALUE;
            benchmark(() -> manager.parseGitRepository(Path.of(args[0]), LIMIT));
            camelCaseIndexUtils.getSymbolsFromClasses("ArrSe").forEach(System.out::println);
            camelCaseIndexUtils.getSymbolsFromClasses("Impl").forEach(System.out::println);
            camelCaseIndexUtils.getSymbolsFromClasses("HFVis").forEach(System.out::println);
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