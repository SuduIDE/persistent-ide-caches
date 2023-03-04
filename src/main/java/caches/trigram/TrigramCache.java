package caches.trigram;

import caches.FileCache;
import caches.Revisions;
import caches.lmdb.LmdbInt2Long;
import caches.records.Revision;
import caches.utils.TriConsumer;

import java.io.*;


public class TrigramCache {
    public static final String DIRECTORY = ".trigrams/";
    public static final File DATA_FILE = new File(DIRECTORY + ".data");
    private final LmdbInt2Long pointers;
    private final Revisions revisions;
    private final FileCache fileCache;

    public TrigramCache(Revisions revisions, LmdbInt2Long pointers, FileCache fileCache) {
        this.revisions = revisions;
        this.pointers = pointers;
        this.fileCache = fileCache;
    }

    public void pushCluster(long timestamp, TrigramFileCounter deltas) {
        var revision = revisions.getCurrentRevision();
        long size = DATA_FILE.length();
        pointers.put(revision.revision(), size);
        try (FileOutputStream writer = new FileOutputStream(DATA_FILE, true)) {
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
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(DATA_FILE, "r")) {
            randomAccessFile.seek(pointer);
            var bufferedInputStream = new BufferedInputStream(new FileInputStream(randomAccessFile.getFD()));
            TrigramDataFileCluster.readTrigramDataFileCluster(bufferedInputStream, consumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
