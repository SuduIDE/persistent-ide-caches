package caches.lmdb;

import caches.records.LongInt;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

import java.nio.ByteBuffer;
import java.util.List;

public class LmdbLong2IntCounter extends LmdbLong2Int {
    public LmdbLong2IntCounter(Env<ByteBuffer> env, String dbName) {
        super(env, dbName);
    }

    public int countGet(long key) {
        ByteBuffer res = getImpl(allocateLong(key));
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
        var keyBytes = allocateLong(key);
        var found = db.get(txn, keyBytes);
        var val = found == null ? 0 : txn.val().getInt();
        db.put(txn, keyBytes, allocateInt(val + delta));
    }

    public void decrease(Txn<ByteBuffer> txn, long key, int delta) {
        add(txn, key, -delta);
    }
}
