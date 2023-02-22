package caches.lmdb;

import caches.records.LongInt;
import org.lmdbjava.Env;

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
            list.forEach(it -> {
                var key = allocateLong(it.l());
                var found = db.get(txn, key);
                var val = found == null ? 0 : txn.val().getInt();
                db.put(txn, key, allocateInt(val + it.i()));
            });
            txn.commit();
        }
    }

    public void decreaseAll(List<LongInt> list) {
        try (var txn = env.txnWrite()) {
            list.forEach(it -> {
                var key = allocateLong(it.l());
                db.put(txn, key, allocateInt(db.get(txn, key).getInt() - it.i()));
            });
            txn.commit();
        }
    }
}
