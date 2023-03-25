package caches.trigram;

import caches.FileCache;
import caches.lmdb.LmdbLong2IntCounter;
import caches.records.LongInt;
import caches.records.Trigram;
import caches.utils.TriConsumer;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

public class TrigramFileCounterLmdb {
    private final FileCache fileCache;
    private final LmdbLong2IntCounter db;

    public TrigramFileCounterLmdb(Env<ByteBuffer> env, FileCache fileCache) {
        db = new LmdbLong2IntCounter(env, "trigram_file_counter");
        this.fileCache = fileCache;
    }

    public int get(Trigram trigram, Path file) {
        return db.get(getKey(trigram, file));
    }

    private long getKey(Trigram trigram, Path file) {
        return getKey(trigram.trigram(), fileCache.getNumber(file));
    }

    private long getKey(byte[] trigram, int file) {
        return (Trigram.toLong(trigram) << Integer.SIZE) + file;
    }

    public void add(TrigramFileCounter counter) {
        db.addAll(counterToList(counter));
    }


    public void decrease(TrigramFileCounter counter) {
        db.decreaseAll(counterToList(counter));
    }

    private List<LongInt> counterToList(TrigramFileCounter counter) {
        List<LongInt> list = new ArrayList<>();
        counter.forEach((trigram, file, integer) -> list.add(new LongInt(getKey(trigram, file), integer)));
        return list;
    }

    public void addIt(Txn<ByteBuffer> txn, byte[] bytes, Integer file, int delta) {
        db.add(txn, getKey(bytes, file), delta);
    }

    public void decreaseIt(Txn<ByteBuffer> txn, byte[] bytes, Integer file, int delta) {
        db.decrease(txn, getKey(bytes, file), delta);
    }

    public List<Path> getFilesForTrigram(Trigram trigram) {
        List<Path> list = new ArrayList<>();
        db.forEachFromTo((trigramFileLong, val) -> {
                    if (val > 0)
                        list.add(fileCache.getFile(trigramFileLong.intValue()));
                },
                trigram.toLong() << Integer.SIZE,
                (trigram.toLong() + 1) << Integer.SIZE);
        return list;
    }

    public void forEach(TriConsumer<Trigram, Path, Integer> consumer) {
        db.forEach((l, i) ->
                consumer.accept(new Trigram(l >> Integer.SIZE),
                        fileCache.getFile(l.intValue()),
                        i));
    }
}
