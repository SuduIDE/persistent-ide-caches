package com.github.SuduIDE.persistentidecaches.trigram;

import com.github.SuduIDE.persistentidecaches.Index;
import com.github.SuduIDE.persistentidecaches.Revisions;
import com.github.SuduIDE.persistentidecaches.changes.AddChange;
import com.github.SuduIDE.persistentidecaches.changes.Change;
import com.github.SuduIDE.persistentidecaches.changes.CopyChange;
import com.github.SuduIDE.persistentidecaches.changes.DeleteChange;
import com.github.SuduIDE.persistentidecaches.changes.ModifyChange;
import com.github.SuduIDE.persistentidecaches.changes.RenameChange;
import com.github.SuduIDE.persistentidecaches.lmdb.CountingCacheImpl;
import com.github.SuduIDE.persistentidecaches.lmdb.maps.LmdbInt2Bytes;
import com.github.SuduIDE.persistentidecaches.records.Revision;
import com.github.SuduIDE.persistentidecaches.records.Trigram;
import com.github.SuduIDE.persistentidecaches.records.TrigramFile;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.lmdbjava.Env;

public class TrigramIndex implements Index<TrigramFile, Integer> {

    private final Env<ByteBuffer> env;
    private final TrigramCache cache;
    private final Revisions revisions;
    private final TrigramFileCounterLmdb counter;
    private final TrigramIndexUtils trigramIndexUtils;

    public TrigramIndex(final Env<ByteBuffer> env, final CountingCacheImpl<Path> pathCache, final Revisions revisions) {
        this.env = env;
        cache = new TrigramCache(revisions, new LmdbInt2Bytes(env, "trigram_deltas"), pathCache);
        this.revisions = revisions;
        counter = new TrigramFileCounterLmdb(this.env, pathCache);
        trigramIndexUtils = new TrigramIndexUtils(this);
    }

    private static TrigramCounter getTrigramsCount(final String str) {
        final byte[] bytes = str.getBytes();
        final TrigramCounter result = new TrigramCounter();
        for (int i = 2; i < bytes.length; i++) {
            final Trigram trigram = new Trigram(new byte[]{bytes[i - 2], bytes[i - 1], bytes[i]});
            result.add(trigram);
        }
        return result;
    }

    public TrigramIndexUtils getTrigramIndexUtils() {
        return trigramIndexUtils;
    }

    @Override
    public void prepare(final List<? extends Change> changes) {
        process(changes);
    }

    @Override
    public void processChanges(final List<? extends Change> changes) {
        process(changes);
    }

    private void pushActions(final TrigramFileCounter deltas, final long timestamp) {
        cache.pushCluster(timestamp, deltas);
    }

    @Override
    public Integer getValue(final TrigramFile trigramFile, final Revision revision) {
        final var currentRevision = revisions.getCurrentRevision();
        if (revision.equals(currentRevision)) {
            return counter.get(trigramFile.trigram(), trigramFile.file());
        } else {
            checkout(revision);
            final var ans = counter.get(trigramFile.trigram(), trigramFile.file());
            checkout(currentRevision);
            return ans;
        }
    }

    @Override
    public void checkout(Revision targetRevision) {
        var currentRevision = revisions.getCurrentRevision();
        try (final var txn = env.txnWrite()) {
//            final var deltasList = new ArrayList<ByteArrIntInt>();
            while (!currentRevision.equals(targetRevision)) {
                if (currentRevision.revision() > targetRevision.revision()) {
                    cache.processDataCluster(currentRevision,
                            (bytes, file, d) -> counter.addIt(txn, bytes, file, d));
                    currentRevision = revisions.getParent(currentRevision);
                } else {
                    cache.processDataCluster(targetRevision,
                            (bytes, file, d) -> counter.decreaseIt(txn, bytes, file, d));
                    targetRevision = revisions.getParent(targetRevision);
                }
//                counter.add(txn, deltasList);
//                deltasList.clear();
            }
            txn.commit();
        }

    }


    public void process(final List<? extends Change> changes) {
        final var delta = new TrigramFileCounter();
        changes.forEach(it -> countChange(it, delta));
        final var filteredDelta = new TrigramFileCounter(delta.getAsMap().entrySet().stream()
                .filter(it -> it.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        counter.add(filteredDelta);
        if (!changes.isEmpty()) {
            pushActions(delta, changes.get(0).getTimestamp());
        }
    }

    private boolean validateFilename(final String filename) {
        return Stream.of(".java"/*, ".txt", ".kt", ".py"*/).anyMatch(filename::endsWith);
    }

    private void countChange(final Change change, final TrigramFileCounter delta) {
        if (Objects.requireNonNull(change) instanceof final AddChange addChange) {
            delta.add(addChange.getPlace().file(), getTrigramsCount(addChange.getAddedString()));
        } else if (change instanceof final ModifyChange modifyChange) {
            delta.decrease(modifyChange.getOldFileName(), getTrigramsCount(modifyChange.getOldFileContent()));
            delta.add(modifyChange.getNewFileName(), getTrigramsCount(modifyChange.getNewFileContent()));
        } else if (change instanceof final CopyChange copyChange) {
            delta.add(copyChange.getNewFileName(), getTrigramsCount(copyChange.getNewFileContent()));
        } else if (change instanceof final RenameChange renameChange) {
            delta.decrease(renameChange.getOldFileName(), getTrigramsCount(renameChange.getOldFileContent()));
            delta.add(renameChange.getNewFileName(), getTrigramsCount(renameChange.getNewFileContent()));
        } else if (change instanceof final DeleteChange deleteChange) {
            delta.decrease(deleteChange.getPlace().file(), getTrigramsCount(deleteChange.getDeletedString()));
        } else {
            throw new AssertionError();
        }
    }

    public TrigramFileCounterLmdb getCounter() {
        return counter;
    }
}
