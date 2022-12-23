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
    private final Map<Revision, TrigramNode> tree = new HashMap<>();
    private Revision cachedRevision = new Revision(0);

    public void pushCluster(long timestamp, List<TrigramDataFileCluster.TrigramFileDelta> deltas) {
        var revision = new Revision(GlobalVariables.revisions.get());
        var parent = cachedRevision;
        long size = DATA_FILE.length();
        tree.put(revision, new TrigramNode(revision, parent, size));
        try (FileOutputStream writer = new FileOutputStream(DATA_FILE, true)) {
            writer.write(new TrigramDataFileCluster(revision, parent, deltas).toBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        cacheRevision();
    }

    public TrigramFileCounter getDataCluster(Revision currentRevision) {
        long pointer = tree.get(currentRevision).pointer();
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(DATA_FILE, "r")) {
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

    public Revision getParent(Revision revision) {
        return tree.get(revision).parent();
    }

    private void cacheRevision() {
        cachedRevision = new Revision(GlobalVariables.revisions.get());
    }

    public void cacheRevision(Revision revision) {
        cachedRevision = revision;
    }
}
