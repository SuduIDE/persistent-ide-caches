package caches;


import caches.lmdb.LmdbInt2File;
import caches.lmdb.LmdbSha12Int;
import caches.lmdb.LmdbString2Int;
import caches.records.Revision;
import caches.trigram.TrigramCache;
import caches.trigram.TrigramIndex;
import caches.trigram.TrigramIndexManager;
import caches.utils.EchoIndex;
import org.eclipse.jgit.api.Git;
import org.lmdbjava.Env;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static org.lmdbjava.EnvFlags.MDB_NOTLS;

public class Main {
    private static final SimpleFileVisitor<Path> DELETE = new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    };

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            throw new RuntimeException("Needs path to repository as first arg");
        }
        Path trigramPath = Path.of(TrigramCache.DIRECTORY);
        Path lmdbPath = Path.of(GlobalVariables.LMDB_DIRECTORY);
//        Files.walkFileTree(trigramPath, DELETE);
//        Files.walkFileTree(lmdbPath, DELETE);
        Files.createDirectories(trigramPath);
        Files.createDirectories(lmdbPath);

        GlobalVariables.env = Env.create()
                .setMapSize(10_485_760_00)
                .setMaxDbs(7)
                .setMaxReaders(3)
                .open(new File(GlobalVariables.LMDB_DIRECTORY), MDB_NOTLS);
        GlobalVariables.filesInProject = new LmdbInt2File(GlobalVariables.env, "files");
        GlobalVariables.gitCommits2Revisions = new LmdbSha12Int(GlobalVariables.env, "git_commits_to_revision");
        GlobalVariables.variables = new LmdbString2Int(GlobalVariables.env, "variables");
        GlobalVariables.revisions = new Revisions();
        GlobalVariables.initFiles();
        GlobalVariables.restoreFilesFromDB();
        Index<String, String> echoIndex = new EchoIndex();
        TrigramIndex trigramHistoryIndex = new TrigramIndex();
        TrigramIndexManager trigramIndexManager = new TrigramIndexManager(trigramHistoryIndex);
        final int LIMIT = 300;
        benchmark(() -> {
            try (Git git = Git.open(new File(args[0]))) {
                var parser = new GitParser(git, List.of(/*echoIndex,*/ trigramHistoryIndex), LIMIT);
                parser.parse();
            } catch (IOException ioException) {
                throw new RuntimeException(ioException);
            }
        });
        GlobalVariables.env.sync(true);
        System.out.println("Current revision: " + GlobalVariables.revisions.getCurrentRevision());
        System.out.println(GlobalVariables.revisions.getCurrentRevision());
//        trigramHistoryIndex.counter.forEach((tri, file, i) -> System.out.println(tri + " " + file + " " + i));
        benchmark(() -> System.out.println(trigramIndexManager.filesForString("text")));
        benchmark(() -> System.out.println(trigramIndexManager.filesForString("another text")));
//        benchmarkCheckout(new Revision(0), trigramHistoryIndex);
//        benchmarkCheckout(new Revision(10), trigramHistoryIndex);
//        benchmarkCheckout(new Revision(100), trigramHistoryIndex);
//        benchmarkCheckout(new Revision(50), trigramHistoryIndex);
//        benchmarkCheckout(new Revision(LIMIT - 1), trigramHistoryIndex);
    }

    public static void benchmark(Runnable runnable) {
        long start = System.currentTimeMillis();
        runnable.run();
        System.out.println("Benchmarked: " + ((System.currentTimeMillis() - start) / 1000) + " second");
    }

    public static void benchmarkCheckout(Revision targetRevision, Index<?, ?> index) {
        benchmark(() -> {
            System.out.printf("checkout from %d to %d\n", GlobalVariables.revisions.getCurrentRevision().revision(),
                    targetRevision.revision());
            index.checkout(targetRevision);
            GlobalVariables.revisions.setCurrentRevision(targetRevision);
        });
    }
}
