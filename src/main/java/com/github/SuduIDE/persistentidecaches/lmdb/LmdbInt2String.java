package com.github.SuduIDE.persistentidecaches.lmdb;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import org.lmdbjava.CursorIterable;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;
import org.lmdbjava.KeyRange;
import org.lmdbjava.Txn;

public class LmdbInt2String extends LmdbAbstractMap {

    public LmdbInt2String(final Env<ByteBuffer> env, final String dbName) {
        super(env, env.openDbi(dbName, DbiFlags.MDB_CREATE, DbiFlags.MDB_INTEGERKEY));
    }

    public void put(final int key, final String value) {
        final ByteBuffer valueBytes = ByteBuffer.wrap(value.getBytes());
        putImpl(allocateInt(key),
                allocateString(value));
    }

    /**
     * @return value for key or null
     */
    public String get(final int key) {
        final ByteBuffer res = getImpl(allocateInt(key));
        return res == null ? null : String.valueOf(StandardCharsets.UTF_8.decode(res));
    }

    public void forEach(final BiConsumer<Integer, String> consumer) {
        try (final Txn<ByteBuffer> txn = env.txnRead()) {
            try (final CursorIterable<ByteBuffer> ci = db.iterate(txn, KeyRange.all())) {
                for (final CursorIterable.KeyVal<ByteBuffer> kv : ci) {
                    consumer.accept(kv.key().getInt(), String.valueOf(StandardCharsets.UTF_8.decode(kv.val())));
                }
            }
        }
    }
}
