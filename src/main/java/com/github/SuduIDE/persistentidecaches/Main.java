package com.github.SuduIDE.persistentidecaches;


import com.github.SuduIDE.persistentidecaches.records.Revision;
import java.nio.file.Path;

public class Main {


    // needs java options:
    /*
    --add-opens java.base/java.nio=ALL-UNNAMED
    --add-opens java.base/sun.nio.ch=ALL-UNNAMED
    */
    public static void main(String[] args) {
        if (args.length < 1) {
            throw new RuntimeException("Needs path to repository as first arg");
        }
        try (IndexesManager manager = new IndexesManager(true)) {
            var trigramHistoryIndex = manager.addTrigramIndex();
            var trigramIndexUtils = trigramHistoryIndex.getTrigramIndexUtils();
            final int LIMIT = 10;
            benchmark(() -> manager.parseGitRepository(Path.of(args[0]), LIMIT));
            System.out.println("Current revision: " + manager.getRevisions().getCurrentRevision());
//            trigramHistoryIndex.getCounter().forEach((tri, file, i) -> System.out.println(tri + " " + file + " " + i));
            benchmark(() -> System.out.println(trigramIndexUtils.filesForString("text")));
            benchmark(() -> System.out.println(trigramIndexUtils.filesForString("another text")));
//        benchmarkCheckout(new Revision(0), trigramHistoryIndex);
//        benchmarkCheckout(new Revision(10), trigramHistoryIndex);
//        benchmarkCheckout(new Revision(100), trigramHistoryIndex);
//        benchmarkCheckout(new Revision(50), trigramHistoryIndex);
//        benchmarkCheckout(new Revision(LIMIT - 1), trigramHistoryIndex);
        }
    }

    public static void benchmark(Runnable runnable) {
        long start = System.currentTimeMillis();
        runnable.run();
        System.out.println("Benchmarked: " + ((System.currentTimeMillis() - start) / 1000) + " second");
    }

    public static void benchmarkCheckout(Revision targetRevision, Index<?, ?> index, Revisions revisions) {
        benchmark(() -> {
            System.out.printf("checkout from %d to %d\n", revisions.getCurrentRevision().revision(),
                    targetRevision.revision());
            index.checkout(targetRevision);
            revisions.setCurrentRevision(targetRevision);
        });
    }
}
