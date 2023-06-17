package com.github.SuduIDE.persistentidecaches;


import com.github.SuduIDE.persistentidecaches.ccsearch.CamelCaseIndex;
import com.github.SuduIDE.persistentidecaches.changes.Change;
import com.github.SuduIDE.persistentidecaches.lmdb.CountingCacheImpl;
import com.github.SuduIDE.persistentidecaches.lmdb.maps.LmdbInt2Int;
import com.github.SuduIDE.persistentidecaches.lmdb.maps.LmdbInt2Path;
import com.github.SuduIDE.persistentidecaches.lmdb.maps.LmdbInt2Symbol;
import com.github.SuduIDE.persistentidecaches.lmdb.maps.LmdbSha12Int;
import com.github.SuduIDE.persistentidecaches.lmdb.maps.LmdbString2Int;
import com.github.SuduIDE.persistentidecaches.records.Revision;
import com.github.SuduIDE.persistentidecaches.symbols.Symbol;
import com.github.SuduIDE.persistentidecaches.trigram.TrigramIndex;
import com.github.SuduIDE.persistentidecaches.utils.FileUtils;
import com.github.SuduIDE.persistentidecaches.utils.indexes.EchoIndex;
import com.github.SuduIDE.persistentidecaches.utils.indexes.SizeCounterIndex;
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
    private final Path lmdbCamelCaseSearchPath;
    private final Map<Class<?>, Index<?, ?>> indexes;
    private final Revisions revisions;
    private final CountingCacheImpl<Path> pathCache;
    private final LmdbString2Int variables;
    private final Env<ByteBuffer> globalEnv;
    private final List<Env<ByteBuffer>> envs;
    private final CountingCacheImpl<Symbol> symbolCache;
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
        lmdbCamelCaseSearchPath = dataPath.resolve(".lmdb.camelCaseSearch");
        if (resetDBs) {
            try {
                if (Files.exists(lmdbGlobalPath)) {
                    Files.walkFileTree(lmdbGlobalPath, DELETE);
                }
                if (Files.exists(lmdbTrigramPath)) {
                    Files.walkFileTree(lmdbTrigramPath, DELETE);
                }
                if (Files.exists(lmdbCamelCaseSearchPath)) {
                    Files.walkFileTree(lmdbCamelCaseSearchPath, DELETE);
                }
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
        FileUtils.createParentDirectories(lmdbTrigramPath, lmdbGlobalPath, lmdbCamelCaseSearchPath);

        globalEnv = initGlobalEnv();
        variables = initVariables(globalEnv);
        revisions = initRevisions(globalEnv, variables);
        pathCache = initFileCache(globalEnv, variables);
        symbolCache = initSymbolCache(globalEnv, variables);

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

    private CountingCacheImpl<Path> initFileCache(final Env<ByteBuffer> globalEnv, final LmdbString2Int variables) {
        final CountingCacheImpl<Path>
                pathCache = new CountingCacheImpl<>("files",
                new LmdbInt2Path(globalEnv, "files"),
                variables);
        pathCache.init();
        pathCache.restoreObjectsFromDB();
        return pathCache;
    }

    private CountingCacheImpl<Symbol> initSymbolCache(
            final Env<ByteBuffer> globalEnv, final LmdbString2Int variables) {
        final CountingCacheImpl<Symbol>
                symbolCache =
                new CountingCacheImpl<>("symbols",
                        new LmdbInt2Symbol(globalEnv, "symbols"),
                        variables);
        symbolCache.init();
        symbolCache.restoreObjectsFromDB();
        return symbolCache;
    }

    public EchoIndex addEchoIndex() {
        final EchoIndex echoIndex = new EchoIndex();
        indexes.put(EchoIndex.class, echoIndex);
        return echoIndex;
    }

    public SizeCounterIndex addSizeCounterIndex() {
        final var sizeCounterIndex = new SizeCounterIndex();
        indexes.put(SizeCounterIndex.class, sizeCounterIndex);
        return sizeCounterIndex;
    }

    public TrigramIndex addTrigramIndex() {
        final var trigramEnv = Env.create()
                .setMapSize(10_485_760_000L)
                .setMaxDbs(3)
                .setMaxReaders(2)
                .open(lmdbTrigramPath.toFile());
        envs.add(trigramEnv);
        final TrigramIndex trigramHistoryIndex = new TrigramIndex(trigramEnv, pathCache, revisions);
        indexes.put(TrigramIndex.class, trigramHistoryIndex);
        return trigramHistoryIndex;
    }

    public CamelCaseIndex addCamelCaseIndex() {
        final var camelCaseEnv = Env.create()
                .setMapSize(10_485_760_00)
                .setMaxDbs(3)
                .setMaxReaders(2)
                .open(lmdbCamelCaseSearchPath.toFile());
        envs.add(camelCaseEnv);
        final var camelCaseIndex = new CamelCaseIndex(camelCaseEnv, symbolCache, pathCache);
        indexes.put(CamelCaseIndex.class, camelCaseIndex);
        return camelCaseIndex;
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
            checkoutToGitRevision(parser.getHead());
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

    public CountingCache<Path> getFileCache() {
        return pathCache;
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
