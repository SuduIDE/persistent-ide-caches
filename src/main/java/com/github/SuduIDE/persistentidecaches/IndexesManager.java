package com.github.SuduIDE.persistentidecaches;

import com.github.SuduIDE.persistentidecaches.changes.Change;
import com.github.SuduIDE.persistentidecaches.lmdb.LmdbInt2Int;
import com.github.SuduIDE.persistentidecaches.lmdb.LmdbInt2Path;
import com.github.SuduIDE.persistentidecaches.lmdb.LmdbSha12Int;
import com.github.SuduIDE.persistentidecaches.lmdb.LmdbString2Int;
import com.github.SuduIDE.persistentidecaches.records.Revision;
import com.github.SuduIDE.persistentidecaches.trigram.TrigramIndex;
import com.github.SuduIDE.persistentidecaches.utils.EchoIndex;
import com.github.SuduIDE.persistentidecaches.utils.FileUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.api.Git;
import org.lmdbjava.Env;

public class IndexesManager implements AutoCloseable {

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

    private final Path lmdbGlobalPath;
    private final Path lmdbTrigramPath;
    private final Map<Class<?>, Index<?, ?>> indexes;
    private final Revisions revisions;
    private final FileCache fileCache;
    private final LmdbString2Int variables;
    private final Env<ByteBuffer> globalEnv;
    private final List<Env<ByteBuffer>> envs;

    private LmdbSha12Int lmdbSha12Int;


    public IndexesManager() {
        this(false);
    }

    public IndexesManager(final boolean resetDBs) {
        this(resetDBs, Path.of(""));
    }

    public IndexesManager(final boolean resetDBs, final Path dataPath) {
        indexes = new HashMap<>();
        envs = new ArrayList<>();
        lmdbGlobalPath = dataPath.resolve(".lmdb");
        lmdbTrigramPath = dataPath.resolve(".lmdb.trigrams");
        if (resetDBs) {
            try {
                if (Files.exists(lmdbGlobalPath)) {
                    Files.walkFileTree(lmdbGlobalPath, DELETE);
                }
                if (Files.exists(lmdbTrigramPath)) {
                    Files.walkFileTree(lmdbTrigramPath, DELETE);
                }
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
        FileUtils.createParentDirectories(lmdbTrigramPath, lmdbGlobalPath);

        globalEnv = initGlobalEnv();
        variables = initVariables(globalEnv);
        revisions = initRevisions(globalEnv, variables);
        fileCache = initFileCache(globalEnv, variables);

    }

    private Env<ByteBuffer> initGlobalEnv() {
        return Env.create()
                .setMapSize(10_485_760)
                .setMaxDbs(7)
                .setMaxReaders(2)
                .open(lmdbGlobalPath.toFile());
    }

    private LmdbString2Int initVariables(final Env<ByteBuffer> env) {
        return new LmdbString2Int(env, "variables");
    }

    private Revisions initRevisions(final Env<ByteBuffer> env, final LmdbString2Int variables) {
        return new RevisionsImpl(variables, new LmdbInt2Int(globalEnv, "revisions"));
    }

    private FileCache initFileCache(final Env<ByteBuffer> globalEnv, final LmdbString2Int variables) {
        final FileCache fileCache = new FileCache(new LmdbInt2Path(globalEnv, "files"), variables);
        fileCache.initFiles();
        fileCache.restoreFilesFromDB();
        return fileCache;
    }

    public EchoIndex addEchoIndex() {
        final EchoIndex echoIndex = new EchoIndex();
        indexes.put(EchoIndex.class, echoIndex);
        return echoIndex;
    }

    public TrigramIndex addTrigramIndex() {
        final var trigramEnv = Env.create()
                .setMapSize(10_485_760_00)
                .setMaxDbs(3)
                .setMaxReaders(2)
                .open(lmdbTrigramPath.toFile());
        envs.add(trigramEnv);
        final TrigramIndex trigramHistoryIndex = new TrigramIndex(trigramEnv, fileCache, revisions);
        indexes.put(TrigramIndex.class, trigramHistoryIndex);
        return trigramHistoryIndex;
    }

    public void parseGitRepository(final Path pathToRepository) {
        parseGitRepository(pathToRepository, Integer.MAX_VALUE);
    }

    public void parseGitRepository(final Path pathToRepository, final int LIMIT) {
        try (final Git git = Git.open(pathToRepository.toFile())) {
            lmdbSha12Int = new LmdbSha12Int(globalEnv, "git_commits_to_revision");
            final var parser = new GitParser(git, this,
                    lmdbSha12Int,
                    LIMIT);
            parser.parseAll();
        } catch (final IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    public void checkout(final Revision targetRevision) {
        indexes.values().forEach(index -> index.checkout(targetRevision));
        revisions.setCurrentRevision(targetRevision);
    }

    public void checkoutToGitRevision(final String commitHashName) {
        final int revision = lmdbSha12Int.get(commitHashName);
        if (revision == -1) {
            throw new IllegalArgumentException();
        }
        checkout(new Revision(revision));
    }

    @Override
    public void close() {
        envs.forEach(Env::close);
        globalEnv.close();
    }

    public Revisions getRevisions() {
        return revisions;
    }

    public FileCache getFileCache() {
        return fileCache;
    }

    public LmdbString2Int getVariables() {
        return variables;
    }

    public void applyChanges(final List<Change> changes) {
        indexes.values().forEach(it -> it.processChanges(changes));
    }

    public <T, U> Index<?, ?> getIndex(final Class<? extends Index<T, U>> indexClass) {
        return indexes.get(indexClass);
    }

    public void nextRevision() {
        revisions.setCurrentRevision(
                revisions.addRevision(
                        revisions.getCurrentRevision()
                ));
    }
}
