package com.github.SuduIDE.persistentidecaches.lmdb;

import com.github.SuduIDE.persistentidecaches.records.Trigram;
import com.github.SuduIDE.persistentidecaches.utils.TriConsumer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

public abstract class TrigramObjCounterLmdb<U> {

    protected final CountingCacheImpl<U> cache;
    protected final LmdbLong2IntCounter db;

    public TrigramObjCounterLmdb(final CountingCacheImpl<U> cache, final Env<ByteBuffer> env, final String dbName) {
        this.cache = cache;
        db = new LmdbLong2IntCounter(env, dbName);
    }

    public int get(final Trigram trigram, final U obj) {
        return db.get(getKey(trigram, obj));
    }

    protected long getKey(final Trigram trigram, final U num) {
        return getKey(trigram.trigram(), cache.getNumber(num));
    }

    protected long getKey(final byte[] trigram, final int num) {
        return (Trigram.toLong(trigram) << Integer.SIZE) + num;
    }

    public void addIt(final Txn<ByteBuffer> txn, final byte[] bytes, final int num, final int delta) {
        db.add(txn, getKey(bytes, num), delta);
    }

    public void decreaseIt(final Txn<ByteBuffer> txn, final byte[] bytes, final int num, final int delta) {
        db.decrease(txn, getKey(bytes, num), delta);
    }

    public List<U> getObjForTrigram(final Trigram trigram) {
        final List<U> list = new ArrayList<>();
        db.forEachFromTo((trigramFileLong, val) -> {
                    if (val > 0) {
                        list.add(cache.getObject(trigramFileLong.intValue()));
                    }
                },
                trigram.toLong() << Integer.SIZE,
                (trigram.toLong() + 1) << Integer.SIZE);
        return list;
    }

    public void forEach(final TriConsumer<Trigram, U, Integer> consumer) {
        db.forEach((l, i) ->
                consumer.accept(new Trigram(l >> Integer.SIZE),
                        cache.getObject(l.intValue()),
                        i));
    }
}
