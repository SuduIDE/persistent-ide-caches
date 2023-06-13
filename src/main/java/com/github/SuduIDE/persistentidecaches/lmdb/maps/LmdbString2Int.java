package com.github.SuduIDE.persistentidecaches.lmdb.maps;

import java.nio.ByteBuffer;
import org.lmdbjava.Env;

public class LmdbString2Int extends LmdbAbstractMap {

    private final ByteBuffer valueBuffer;

    public LmdbString2Int(final Env<ByteBuffer> env, final String dbName) {
        super(env, dbName);
        valueBuffer = ByteBuffer.allocateDirect(Integer.BYTES);
    }

    protected ByteBuffer getValue(final int key) {
        return valueBuffer.putInt(key).flip();
    }

    public void put(final String key, final int value) {
        putImpl(allocateString(key),
            getValue(value));
    }

    /**
     * @return value for key or -1
     */
    public int get(final String key) {
        final ByteBuffer res = getImpl(allocateString(key));
        return res == null ? -1 : res.getInt();
    }
}
