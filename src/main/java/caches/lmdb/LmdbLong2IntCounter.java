package caches.lmdb;

import caches.records.LongInt;
import org.lmdbjava.CursorIterable;
import org.lmdbjava.Env;
import org.lmdbjava.KeyRange;
import org.lmdbjava.Txn;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BiConsumer;

public class LmdbLong2IntCounter extends LmdbLong2Int {
    public LmdbLong2IntCounter(Env<ByteBuffer> env, String dbName) {
        super(env, dbName);
    }


    private ByteBuffer allocateReverseLong(long l) {
        // It's not writing
//        return allocateLong(Long.reverseBytes(l));
        return allocateLong(l);
    }

    public int countGet(long key) {
        ByteBuffer res = getImpl(allocateReverseLong(key));
        return res == null ? 0 : res.getInt();
    }

    public void addAll(List<LongInt> list) {
        try (var txn = env.txnWrite()) {
            list.forEach(it -> add(txn, it.l(), it.i()));
            txn.commit();
        }
    }

    public void decreaseAll(List<LongInt> list) {
        try (var txn = env.txnWrite()) {
            list.forEach(it -> add(txn, it.l(), -it.i()));
            txn.commit();
        }
    }

    public void add(Txn<ByteBuffer> txn, long key, int delta) {
        var keyBytes = allocateReverseLong(key);
        var found = db.get(txn, keyBytes);
        var val = found == null ? 0 : txn.val().getInt();
        db.put(txn, keyBytes, allocateInt(val + delta));
    }

    public void decrease(Txn<ByteBuffer> txn, long key, int delta) {
        add(txn, key, -delta);
    }

    public void forEachFromTo(BiConsumer<Long, Integer> consumer, long from, long to) {
        try (var txn = env.txnRead()) {
            try (CursorIterable<ByteBuffer> ci = db.iterate(txn,
                    KeyRange.closedOpen(allocateReverseLong(from), allocateReverseLong(to)))) {
                for (final CursorIterable.KeyVal<ByteBuffer> kv : ci) {
                    long key = kv.key().getLong();
                    consumer.accept(key, kv.val().getInt());
                }
            }
        }
    }

    public void forEach(BiConsumer<Long, Integer> consumer) {
        forEachFromTo(consumer, 0L, Long.MAX_VALUE);
    }
}