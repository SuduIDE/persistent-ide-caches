package caches.trigram;

import caches.FileCache;
import caches.Revisions;
import caches.lmdb.LmdbInt2Long;
import caches.records.Revision;
import caches.utils.TriConsumer;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;


public class TrigramCache {
    private final Path dataFile;
    private final LmdbInt2Long pointers;
    private final Revisions revisions;
    private final FileCache fileCache;

    public TrigramCache(Revisions revisions, LmdbInt2Long pointers, FileCache fileCache, Path dataDirectory) {
        this.revisions = revisions;
        this.pointers = pointers;
        this.fileCache = fileCache;
        this.dataFile = dataDirectory.resolve(".data");
    }

    public void pushCluster(long timestamp, TrigramFileCounter deltas) {
        var revision = revisions.getCurrentRevision();
        try (OutputStream writer = Files.newOutputStream(dataFile)) {
            long size = Files.size(dataFile);
            pointers.put(revision.revision(), size);
            writer.write(new TrigramDataFileCluster(deltas, fileCache).toBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void processDataCluster(Revision revision, TriConsumer<byte[], Integer, Integer> consumer) {
        long pointer = pointers.get(revision.revision());
        if (pointer == -1) {
            return;
        }
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(dataFile.toFile(), "r")) {
            randomAccessFile.seek(pointer);
            var bufferedInputStream = new BufferedInputStream(new FileInputStream(randomAccessFile.getFD()));
            TrigramDataFileCluster.readTrigramDataFileCluster(bufferedInputStream, consumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
