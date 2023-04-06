package com.github.SuduIDE.persistentidecaches.lmdb;

import com.github.SuduIDE.persistentidecaches.records.LongInt;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BiConsumer;
import org.lmdbjava.CursorIterable;
import org.lmdbjava.Env;
import org.lmdbjava.KeyRange;
import org.lmdbjava.Txn;

public class LmdbLong2IntCounter extends LmdbLong2Int {

    public LmdbLong2IntCounter(final Env<ByteBuffer> env, final String dbName) {
        super(env, dbName);
    }


    public int countGet(final long key) {
        final ByteBuffer res = getImpl(allocateLong(key));
        return res == null ? 0 : res.getInt();
    }

    public void addAll(final List<LongInt> list) {
        try (final var txn = env.txnWrite()) {
            addAll(txn, list);
            txn.commit();
        }
    }

    public void addAll(final Txn<ByteBuffer> txn, final List<LongInt> list) {
        list.forEach(it -> add(txn, it.l(), it.i()));
    }

    public void decreaseAll(final List<LongInt> list) {
        try (final var txn = env.txnWrite()) {
            list.forEach(it -> add(txn, it.l(), -it.i()));
            txn.commit();
        }
    }

    public void add(final Txn<ByteBuffer> txn, final long key, final int delta) {
        final var keyBytes = allocateLong(key);
        final var found = db.get(txn, keyBytes);
        final var val = found == null ? 0 : txn.val().getInt();
        db.put(txn, keyBytes, allocateInt(val + delta));
    }

    public void decrease(final Txn<ByteBuffer> txn, final long key, final int delta) {
        add(txn, key, -delta);
    }

    public void forEachFromTo(final BiConsumer<Long, Integer> consumer, final long from, final long to) {
        try (final var txn = env.txnRead()) {
            try (final CursorIterable<ByteBuffer> ci = db.iterate(txn,
                    KeyRange.closedOpen(allocateLong(from), allocateLong(to)))) {
                for (final CursorIterable.KeyVal<ByteBuffer> kv : ci) {
                    final long key = kv.key().getLong();
                    consumer.accept(key, kv.val().getInt());
                }
            }
        }
    }

    public void forEach(final BiConsumer<Long, Integer> consumer) {
        forEachFromTo(consumer, 0L, Long.MAX_VALUE);
    }
}
