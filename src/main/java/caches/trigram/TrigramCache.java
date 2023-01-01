package caches.trigram;

import caches.GlobalVariables;
import caches.records.Revision;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TrigramCache {
    public static final String DIRECTORY = ".trigrams/";
    public static final File DATA_FILE = new File(DIRECTORY + ".data");
    private final Map<Revision, Long> pointers = new HashMap<>();

    public void pushCluster(long timestamp, List<TrigramDataFileCluster.TrigramFileDelta> deltas) {
        var revision = GlobalVariables.revisions.getCurrentRevision();
        long size = DATA_FILE.length();
        pointers.put(revision, size);
        try (FileOutputStream writer = new FileOutputStream(DATA_FILE, true)) {
            writer.write(new TrigramDataFileCluster(deltas).toBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public TrigramFileCounter getDataCluster(Revision revision) {
        if (!pointers.containsKey(revision)) {
            return TrigramFileCounter.EMPTY_COUNTER;
        }
        long pointer = pointers.get(revision);
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(DATA_FILE, "r")) {
            randomAccessFile.seek(pointer);
            var bufferedInputStream = new BufferedInputStream(new FileInputStream(randomAccessFile.getFD()));
            TrigramDataFileCluster cluster = TrigramDataFileCluster.readTrigramDataFileCluster(bufferedInputStream);
            TrigramFileCounter result = new TrigramFileCounter();
            cluster.deltas().forEach(it -> result.add(it.trigram(), it.file(), it.delta()));
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
