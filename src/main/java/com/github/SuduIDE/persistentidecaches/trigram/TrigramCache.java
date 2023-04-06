package com.github.SuduIDE.persistentidecaches.trigram;

import com.github.SuduIDE.persistentidecaches.FileCache;
import com.github.SuduIDE.persistentidecaches.Revisions;
import com.github.SuduIDE.persistentidecaches.lmdb.LmdbInt2Bytes;
import com.github.SuduIDE.persistentidecaches.records.Revision;
import com.github.SuduIDE.persistentidecaches.utils.ByteArrIntIntConsumer;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;


public class TrigramCache {

    private final LmdbInt2Bytes pointers;
    private final Revisions revisions;
    private final FileCache fileCache;

    public TrigramCache(final Revisions revisions, final LmdbInt2Bytes pointers, final FileCache fileCache) {
        this.revisions = revisions;
        this.pointers = pointers;
        this.fileCache = fileCache;
    }

    public void pushCluster(final long timestamp, final TrigramFileCounter deltas) {
        final var revision = revisions.getCurrentRevision();
        pointers.put(revision.revision(), new TrigramDataFileCluster(deltas, fileCache).toBytes());
    }

    public void processDataCluster(final Revision revision, final ByteArrIntIntConsumer consumer) {
        final byte[] data = pointers.get(revision.revision());
        if (data == null) {
            return;
        }
        final var bufferedInputStream =
                new BufferedInputStream(new ByteArrayInputStream(pointers.get(revision.revision())));
        TrigramDataFileCluster.readTrigramDataFileCluster(bufferedInputStream, consumer);
    }
}
