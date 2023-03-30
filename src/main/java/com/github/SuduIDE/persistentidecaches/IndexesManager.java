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

    private final Path trigramPath;
    private final Path lmdbGlobalPath;
    private final Path lmdbTrigramPath;
    private final Map<Class<?>, Index<?, ?>> indexes;
    private final Revisions revisions;
    private final FileCache fileCache;
    private final LmdbString2Int variables;
    private final Env<ByteBuffer> globalEnv;
    private final List<Env<ByteBuffer>> envs;


    public IndexesManager() {
        this(false);
    }

    public IndexesManager(boolean resetDBs) {
        this(resetDBs, Path.of(""));
    }

    public IndexesManager(boolean resetDBs, Path dataPath) {
        indexes = new HashMap<>();
        envs = new ArrayList<>();
        trigramPath = dataPath.resolve(".trigrams");
        lmdbGlobalPath = dataPath.resolve(".lmdb");
        lmdbTrigramPath = dataPath.resolve(".lmdb.trigrams");
        if (resetDBs) {
            try {
                if (Files.exists(trigramPath)) Files.walkFileTree(trigramPath, DELETE);
                if (Files.exists(lmdbGlobalPath)) Files.walkFileTree(lmdbGlobalPath, DELETE);
                if (Files.exists(lmdbTrigramPath)) Files.walkFileTree(lmdbTrigramPath, DELETE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        FileUtils.createParentDirectories(trigramPath, lmdbTrigramPath, lmdbGlobalPath);

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

    private LmdbString2Int initVariables(Env<ByteBuffer> env) {
        return new LmdbString2Int(env, "variables");
    }

    private Revisions initRevisions(Env<ByteBuffer> env, LmdbString2Int variables) {
        return new RevisionsImpl(variables, new LmdbInt2Int(globalEnv, "revisions"));
    }

    private FileCache initFileCache(Env<ByteBuffer> globalEnv, LmdbString2Int variables) {
        FileCache fileCache = new FileCache(new LmdbInt2Path(globalEnv, "files"), variables);
        fileCache.initFiles();
        fileCache.restoreFilesFromDB();
        return fileCache;
    }

    public EchoIndex addEchoIndex() {
        EchoIndex echoIndex = new EchoIndex();
        indexes.put(EchoIndex.class, echoIndex);
        return echoIndex;
    }

    public TrigramIndex addTrigramIndex() {
        var trigramEnv = Env.create()
                .setMapSize(10_485_760_00)
                .setMaxDbs(3)
                .setMaxReaders(2)
                .open(lmdbTrigramPath.toFile());
        envs.add(trigramEnv);
        TrigramIndex trigramHistoryIndex = new TrigramIndex(trigramEnv, fileCache, revisions, trigramPath);
        indexes.put(TrigramIndex.class, trigramHistoryIndex);
        return trigramHistoryIndex;
    }

    public void parseGitRepository(Path pathToRepository) {
        parseGitRepository(pathToRepository, Integer.MAX_VALUE);
    }

    public void parseGitRepository(Path pathToRepository, int LIMIT) {
        try (Git git = Git.open(pathToRepository.toFile())) {
            var parser = new GitParser(git, this,
                    new LmdbSha12Int(globalEnv, "git_commits_to_revision"),
                    LIMIT);
            parser.parseAll();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    public void checkout(Revision targetRevision) {
        indexes.values().forEach(index -> index.checkout(targetRevision));
        revisions.setCurrentRevision(targetRevision);
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

    public void applyChanges(List<Change> changes) {
        indexes.values().forEach(it -> it.processChanges(changes));
    }

    public <T, U> Index<?, ?> getIndex(Class<? extends Index<T, U>> indexClass) {
        return indexes.get(indexClass);
    }

    public void nextRevision() {
        revisions.setCurrentRevision(
                revisions.addRevision(
                        revisions.getCurrentRevision()
                ));
    }
}
