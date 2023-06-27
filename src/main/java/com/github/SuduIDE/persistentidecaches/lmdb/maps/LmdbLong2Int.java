package com.github.SuduIDE.persistentidecaches.lmdb.maps;

import java.nio.ByteBuffer;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;

public class LmdbLong2Int extends LmdbAbstractMap {

    protected final ByteBuffer keyBuffer;
    protected final ByteBuffer valueBuffer;

    public LmdbLong2Int(final Env<ByteBuffer> env, final String dbName) {
        super(env, env.openDbi(dbName, DbiFlags.MDB_CREATE));
        keyBuffer = ByteBuffer.allocateDirect(Long.BYTES);
        valueBuffer = ByteBuffer.allocateDirect(Integer.BYTES);
    }

    protected ByteBuffer getKey(final long key) {
        return keyBuffer.putLong(key).flip();
    }

    protected ByteBuffer getValue(final int key) {
        return valueBuffer.putInt(key).flip();
    }

    public void put(final long key, final int value) {
        putImpl(getKey(key),
            getValue(value));
    }

    /**
     * @return value for key or -1
     */
    public int get(final long key) {
        final ByteBuffer res = getImpl(getKey(key));
        return res == null ? -1 : res.getInt();
    }
}
