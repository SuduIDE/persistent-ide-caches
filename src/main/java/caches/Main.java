package caches;


import caches.changes.Change;
import caches.records.Revision;
import caches.trigram.TrigramCache;
import caches.trigram.TrigramIndex;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            throw new RuntimeException("Needs path to repository as first arg");
        }
        System.out.println(new File(TrigramCache.DIRECTORY).mkdir());
        Index<String, String> echoIndex = new Index<>() {
            @Override
            public String getValue(String s, Revision revision) {
                System.out.println("Echo: getValue " + s + " from" + revision.revision());
                return null;
            }

            @Override
            public void checkout(Revision revision) {
                System.out.println("Echo: checkout to " + revision.revision());
            }

            @Override
            public void prepare(List<Change> changes) {
                System.out.println("Echo: prepare");
                changes.forEach(System.out::println);
            }

            @Override
            public void processChanges(List<Change> changes) {
                System.out.println("Echo: process");
                changes.forEach(System.out::println);
            }
        };
        TrigramIndex trigramHistoryIndex = new TrigramIndex();
        final int LIMIT = 1000;
        benchmark(() -> {
            try (Git git = Git.open(new File(args[0]))) {
                var parser = new GitParser(git, List.of(/*echoIndex,*/ trigramHistoryIndex), LIMIT);
                parser.parse();
            } catch (IOException ioException) {
                throw new RuntimeException(ioException);
            }
//            System.out.println("Parsed 1000 commits from git");
        });
//        System.out.println("Current revision: " + GlobalVariables.revisions.getCurrentRevision());
//        trigramHistoryIndex.counter.forEach(System.out::println);
//        System.out.println(GlobalVariables.revisions.getCurrentRevision());
//        benchmarkCheckout(new Revision(3), trigramHistoryIndex);
//        trigramHistoryIndex.counter.forEach(System.out::println);
        benchmarkCheckout(new Revision(0), trigramHistoryIndex);
        benchmarkCheckout(new Revision(10), trigramHistoryIndex);
        benchmarkCheckout(new Revision(100), trigramHistoryIndex);
        benchmarkCheckout(new Revision(50), trigramHistoryIndex);
        benchmarkCheckout(new Revision(LIMIT), trigramHistoryIndex);
    }

    public static void benchmark(Runnable runnable) {
        long start = System.currentTimeMillis();
        runnable.run();
        System.out.println("Benchmarked: " + ((System.currentTimeMillis() - start) / 1000) + " second");
    }

    public static void benchmarkCheckout(Revision targetRevision, Index<?, ?> index) {
        benchmark(() -> {
            System.out.println("checkout to " + targetRevision.revision());
            index.checkout(targetRevision);
            GlobalVariables.revisions.setCurrentRevision(targetRevision);
        });
    }
}
