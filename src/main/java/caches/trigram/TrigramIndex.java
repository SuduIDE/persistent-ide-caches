package caches.trigram;

import caches.FileCache;
import caches.Index;
import caches.Revisions;
import caches.changes.AddChange;
import caches.changes.Change;
import caches.changes.CopyChange;
import caches.changes.DeleteChange;
import caches.changes.ModifyChange;
import caches.changes.RenameChange;
import caches.lmdb.LmdbInt2Long;
import caches.records.Revision;
import caches.records.Trigram;
import caches.records.TrigramFile;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.lmdbjava.Env;

public class TrigramIndex implements Index<TrigramFile, Integer> {

    private final Env<ByteBuffer> env;
    private final TrigramCache cache;
    private final Revisions revisions;
    private final TrigramFileCounterLmdb counter;
    private final TrigramIndexUtils trigramIndexUtils;

    public TrigramIndex(Env<ByteBuffer> env, FileCache fileCache, Revisions revisions, Path dataDirectory) {
        this.env = env;
        cache = new TrigramCache(revisions, new LmdbInt2Long(env, "trigram_pointers"), fileCache, dataDirectory);
        this.revisions = revisions;
        counter = new TrigramFileCounterLmdb(this.env, fileCache);
        trigramIndexUtils = new TrigramIndexUtils(this);
    }

    private static TrigramCounter getTrigramsCount(String str) {
        byte[] bytes = str.getBytes();
        TrigramCounter result = new TrigramCounter();
        for (int i = 2; i < bytes.length; i++) {
            Trigram trigram = new Trigram(new byte[]{bytes[i - 2], bytes[i - 1], bytes[i]});
            result.add(trigram);
        }
        return result;
    }

    public TrigramIndexUtils getTrigramIndexUtils() {
        return trigramIndexUtils;
    }

    @Override
    public void prepare(List<? extends Change> changes) {
        process(changes);
    }

    @Override
    public void processChanges(List<? extends Change> changes) {
        process(changes);
    }

    private void pushActions(TrigramFileCounter deltas, long timestamp) {
        cache.pushCluster(timestamp, deltas);
    }

    @Override
    public Integer getValue(TrigramFile trigramFile, Revision revision) {
        var currentRevision = revisions.getCurrentRevision();
        if (revision.equals(currentRevision)) {
            return counter.get(trigramFile.trigram(), trigramFile.file());
        } else {
            checkout(revision);
            var ans = counter.get(trigramFile.trigram(), trigramFile.file());
            checkout(currentRevision);
            return ans;
        }
    }

    @Override
    public void checkout(Revision targetRevision) {
        try (var txn = env.txnWrite()) {
            var currentRevision = revisions.getCurrentRevision();
            while (!currentRevision.equals(targetRevision)) {
                if (currentRevision.revision() > targetRevision.revision()) {
                    cache.processDataCluster(currentRevision,
                            (bytes, file, d) -> counter.decreaseIt(txn, bytes, file, d));
                    currentRevision = revisions.getParent(currentRevision);
                } else {
                    cache.processDataCluster(targetRevision,
                            (bytes, file, d) -> counter.addIt(txn, bytes, file, d));
                    targetRevision = revisions.getParent(targetRevision);
                }
            }
            txn.commit();
        }
    }


    public void process(List<? extends Change> changes) {
        var delta = new TrigramFileCounter();
        changes.forEach(it -> countChange(it, delta));
        var filteredDelta = new TrigramFileCounter(delta.getAsMap().entrySet().stream()
                .filter(it -> it.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        counter.add(filteredDelta);
        if (!changes.isEmpty()) pushActions(delta, changes.get(0).getTimestamp());
    }

    private boolean validateFilename(String filename) {
        return Stream.of(".java"/*, ".txt", ".kt", ".py"*/).anyMatch(filename::endsWith);
    }

    private void countChange(Change change, TrigramFileCounter delta) {
        switch (change) {
            case AddChange addChange ->
                    delta.add(addChange.getPlace().file(), getTrigramsCount(addChange.getAddedString()));
            case ModifyChange modifyChange -> {
                delta.decrease(modifyChange.getOldFileName(), getTrigramsCount(modifyChange.getOldFileContent()));
                delta.add(modifyChange.getNewFileName(), getTrigramsCount(modifyChange.getNewFileContent()));
            }
            case CopyChange copyChange ->
                    delta.add(copyChange.getNewFileName(), getTrigramsCount(copyChange.getNewFileContent()));
            case RenameChange renameChange -> {
                delta.decrease(renameChange.getOldFileName(), getTrigramsCount(renameChange.getOldFileContent()));
                delta.add(renameChange.getNewFileName(), getTrigramsCount(renameChange.getNewFileContent()));
            }
            case DeleteChange deleteChange ->
                    delta.add(deleteChange.getPlace().file(), getTrigramsCount(deleteChange.getDeletedString()));
        }
    }

    public TrigramFileCounterLmdb getCounter() {
        return counter;
    }
}
