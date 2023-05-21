package com.github.SuduIDE.persistentidecaches.lmdb.maps;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import org.lmdbjava.CursorIterable;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;
import org.lmdbjava.KeyRange;
import org.lmdbjava.Txn;

public class LmdbLong2String extends LmdbAbstractMap {

    public LmdbLong2String(final Env<ByteBuffer> env, final String dbName) {
        super(env, env.openDbi(dbName, DbiFlags.MDB_CREATE));
    }

    public void put(final long key, final String value) {
        final ByteBuffer valueBytes = ByteBuffer.wrap(value.getBytes());
        putImpl(allocateLong(key),
                allocateString(value));
    }

    /**
     * @return value for key or null
     */
    public String get(final long key) {
        final ByteBuffer res = getImpl(allocateLong(key));
        return res == null ? null : String.valueOf(StandardCharsets.UTF_8.decode(res));
    }

    public void forEach(final BiConsumer<Long, String> consumer) {
        try (final Txn<ByteBuffer> txn = env.txnRead()) {
            try (final CursorIterable<ByteBuffer> ci = db.iterate(txn, KeyRange.all())) {
                for (final CursorIterable.KeyVal<ByteBuffer> kv : ci) {
                    consumer.accept(kv.key().getLong(), String.valueOf(StandardCharsets.UTF_8.decode(kv.val())));
                }
            }
        }
    }
}
