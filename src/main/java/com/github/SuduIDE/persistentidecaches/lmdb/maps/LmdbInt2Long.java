package com.github.SuduIDE.persistentidecaches.lmdb.maps;

import java.nio.ByteBuffer;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;

public class LmdbInt2Long extends LmdbAbstractInt2Smth {
    private final ByteBuffer valueBuffer;

    public LmdbInt2Long(final Env<ByteBuffer> env, final String dbName) {
        super(env, env.openDbi(dbName, DbiFlags.MDB_CREATE, DbiFlags.MDB_INTEGERKEY));
        valueBuffer = ByteBuffer.allocateDirect(Long.BYTES);
    }

    public void put(final int key, final long value) {
        putImpl(getKey(key),
                getValue(value));
    }

    protected ByteBuffer getValue(final long value) {
        return valueBuffer.putLong(value).flip();
    }

    /**
     * @return value for key or -1
     */
    public long get(final int key) {
        final ByteBuffer res = getImpl(getKey(key));
        return res == null ? -1 : res.getLong();
    }
}
