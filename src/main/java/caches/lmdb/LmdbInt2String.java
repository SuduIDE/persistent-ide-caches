package caches.lmdb;

import org.lmdbjava.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

public class LmdbInt2String extends LmdbAbstractMap {
    public LmdbInt2String(Env<ByteBuffer> env, String dbName) {
        super(env, env.openDbi(dbName, DbiFlags.MDB_CREATE, DbiFlags.MDB_INTEGERKEY));
    }

    public void put(int key, String value) {
        ByteBuffer valueBytes = ByteBuffer.wrap(value.getBytes());
        putImpl(allocateInt(key),
                allocateString(value));
    }

    public String get(int key) {
        ByteBuffer res = getImpl(allocateInt(key));
        return res == null ? null : String.valueOf(StandardCharsets.UTF_8.decode(res));
    }

    public void forEach(BiConsumer<Integer, String> consumer) {
        try (Txn<ByteBuffer> txn = env.txnRead()) {
            try (CursorIterable<ByteBuffer> ci = db.iterate(txn, KeyRange.all())) {
                for (final CursorIterable.KeyVal<ByteBuffer> kv : ci) {
                    consumer.accept(kv.key().getInt(), String.valueOf(StandardCharsets.UTF_8.decode(kv.val())));
                }
            }
        }
    }
}
