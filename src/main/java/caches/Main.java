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
                return null;
            }

            @Override
            public void checkout(Revision revision) {
            }

            @Override
            public void prepare(List<Change> changes) {
                changes.forEach(System.out::println);
            }

            @Override
            public void processChanges(List<Change> changes) {

            }
        };
        TrigramIndex trigramHistoryIndex = new TrigramIndex();
        benchmark(() -> {
            try (Git git = Git.open(new File(args[0]))) {
                var parser = new GitParser(git, List.of(/*echoIndex,*/ trigramHistoryIndex));
                parser.parse();
            } catch (IOException ioException) {
                throw new RuntimeException(ioException);
            }
            System.out.println("Parsed 1000 commits from git");
        });
        System.out.println("Current revision: " + GlobalVariables.currentRevision.get());
        benchmark(() -> {
            System.out.println("checkout to 1");
            trigramHistoryIndex.checkout(new Revision(1));
            GlobalVariables.currentRevision.set(1);
        });
        benchmark(() -> {
            System.out.println("checkout to 10");
            trigramHistoryIndex.checkout(new Revision(10));
            GlobalVariables.currentRevision.set(10);
        });
        benchmark(() -> {
            System.out.println("checkout to 100");
            trigramHistoryIndex.checkout(new Revision(100));
            GlobalVariables.currentRevision.set(100);
        });
        benchmark(() -> {
            System.out.println("checkout to 50");
            trigramHistoryIndex.checkout(new Revision(50));
            GlobalVariables.currentRevision.set(50);
        });
        benchmark(() -> {
            System.out.println("checkout to 1000");
            trigramHistoryIndex.checkout(new Revision(1000));
            GlobalVariables.currentRevision.set(1000);
        });
    }

    public static void benchmark(Runnable runnable) {
        long start = System.currentTimeMillis();
        runnable.run();
        System.out.println("Benchmarked: " + ((System.currentTimeMillis() - start) / 1000) + " second");
    }
}
