package com.github.SuduIDE.persistentidecaches.lmdb.maps;

import java.nio.ByteBuffer;
import org.lmdbjava.Env;

public class LmdbString2Int extends LmdbAbstractMap {

    public LmdbString2Int(final Env<ByteBuffer> env, final String dbName) {
        super(env, dbName);
    }

    public void put(final String key, final int value) {
        putImpl(allocateString(key),
                allocateInt(value));
    }

    /**
     * @return value for key or -1
     */
    public int get(final String key) {
        final ByteBuffer res = getImpl(allocateString(key));
        return res == null ? -1 : res.getInt();
    }
}
