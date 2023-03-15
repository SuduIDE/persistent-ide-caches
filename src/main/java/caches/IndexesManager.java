package caches;

import caches.lmdb.LmdbInt2File;
import caches.lmdb.LmdbInt2Int;
import caches.lmdb.LmdbSha12Int;
import caches.lmdb.LmdbString2Int;
import caches.records.Revision;
import caches.trigram.TrigramCache;
import caches.trigram.TrigramIndex;
import caches.utils.EchoIndex;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
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

    private final static Path trigramPath = Path.of(TrigramCache.DIRECTORY);
    private final static Path lmdbGlobalPath = Path.of(".lmdb");
    private final static Path lmdbTrigramPath = Path.of(".lmdb.trigrams");
    private final List<Index<?, ?>> indexes;
    private final Revisions revisions;
    private final FileCache fileCache;
    private final LmdbString2Int variables;
    private final Env<ByteBuffer> globalEnv;
    private final List<Env<ByteBuffer>> envs;


    public IndexesManager() {
        indexes = new ArrayList<>();
        envs = new ArrayList<>();

        globalEnv = initGlobalEnv();
        variables = initVariables(globalEnv);
        revisions = initRevisions(globalEnv, variables);
        fileCache = initFileCache(globalEnv, variables);
    }

    public static void resetAllDataBases() {
        try {
            Files.walkFileTree(trigramPath, DELETE);
            Files.walkFileTree(lmdbGlobalPath, DELETE);
            Files.walkFileTree(lmdbTrigramPath, DELETE);
            Files.createDirectories(trigramPath);
            Files.createDirectories(lmdbGlobalPath);
            Files.createDirectories(lmdbTrigramPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        return new Revisions(variables, new LmdbInt2Int(globalEnv, "revisions"));
    }

    private FileCache initFileCache(Env<ByteBuffer> globalEnv, LmdbString2Int variables) {
        FileCache fileCache = new FileCache(new LmdbInt2File(globalEnv, "files"), variables);
        fileCache.initFiles();
        fileCache.restoreFilesFromDB();
        return fileCache;
    }

    public EchoIndex addEchoIndex() {
        EchoIndex echoIndex = new EchoIndex();
        indexes.add(echoIndex);
        return echoIndex;
    }

    public TrigramIndex addTrigramIndex() {
        var trigramEnv = Env.create()
                .setMapSize(10_485_760_00)
                .setMaxDbs(3)
                .setMaxReaders(2)
                .open(lmdbTrigramPath.toFile());
        envs.add(trigramEnv);
        TrigramIndex trigramHistoryIndex = new TrigramIndex(trigramEnv, fileCache, revisions);
        indexes.add(trigramHistoryIndex);
        return trigramHistoryIndex;
    }

    public void parseGitRepository(Path pathToRepository) {
        parseGitRepository(pathToRepository, Integer.MAX_VALUE);
    }

    public void parseGitRepository(Path pathToRepository, int LIMIT) {
        try (Git git = Git.open(pathToRepository.toFile())) {
            var parser = new GitParser(git, indexes,
                    new LmdbSha12Int(globalEnv, "git_commits_to_revision"),
                    revisions, fileCache, LIMIT);
            parser.parseAll();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    public void checkout(Revision targetRevision) {
        indexes.forEach(it -> it.checkout(targetRevision));
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
}
