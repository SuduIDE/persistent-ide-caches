package com.github.SuduIDE.persistentidecaches.trigram;

import com.github.SuduIDE.persistentidecaches.FileCache;
import com.github.SuduIDE.persistentidecaches.lmdb.LmdbLong2IntCounter;
import com.github.SuduIDE.persistentidecaches.records.LongInt;
import com.github.SuduIDE.persistentidecaches.records.Trigram;
import com.github.SuduIDE.persistentidecaches.trigram.TrigramIndex.ByteArrIntInt;
import com.github.SuduIDE.persistentidecaches.utils.TriConsumer;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

public class TrigramFileCounterLmdb {

    private final FileCache fileCache;
    private final LmdbLong2IntCounter db;

    public TrigramFileCounterLmdb(final Env<ByteBuffer> env, final FileCache fileCache) {
        db = new LmdbLong2IntCounter(env, "trigram_file_counter");
        this.fileCache = fileCache;
    }

    public int get(final Trigram trigram, final Path file) {
        return db.get(getKey(trigram, file));
    }

    private long getKey(final Trigram trigram, final Path file) {
        return getKey(trigram.trigram(), fileCache.getNumber(file));
    }

    private long getKey(final byte[] trigram, final int file) {
        return (Trigram.toLong(trigram) << Integer.SIZE) + file;
    }

    public void add(final TrigramFileCounter counter) {
        db.addAll(counterToList(counter));
    }

    public void add(final Txn<ByteBuffer> txn, final List<ByteArrIntInt> counter) {
        db.addAll(txn, counter.stream()
                .map(it -> new LongInt(getKey(it.trigram(), it.file()), it.delta()))
                .toList());
    }


    public void decrease(final TrigramFileCounter counter) {
        db.decreaseAll(counterToList(counter));
    }

    private List<LongInt> counterToList(final TrigramFileCounter counter) {
        final List<LongInt> list = new ArrayList<>();
        counter.forEach((trigram, file, integer) -> list.add(new LongInt(getKey(trigram, file), integer)));
        return list;
    }

    public void addIt(final Txn<ByteBuffer> txn, final byte[] bytes, final int file, final int delta) {
        db.add(txn, getKey(bytes, file), delta);
    }

    public void decreaseIt(final Txn<ByteBuffer> txn, final byte[] bytes, final int file, final int delta) {
        db.decrease(txn, getKey(bytes, file), delta);
    }

    public List<Path> getFilesForTrigram(final Trigram trigram) {
        final List<Path> list = new ArrayList<>();
        db.forEachFromTo((trigramFileLong, val) -> {
                    if (val > 0) {
                        list.add(fileCache.getFile(trigramFileLong.intValue()));
                    }
                },
                trigram.toLong() << Integer.SIZE,
                (trigram.toLong() + 1) << Integer.SIZE);
        return list;
    }

    public void forEach(final TriConsumer<Trigram, Path, Integer> consumer) {
        db.forEach((l, i) ->
                consumer.accept(new Trigram(l >> Integer.SIZE),
                        fileCache.getFile(l.intValue()),
                        i));
    }
}
