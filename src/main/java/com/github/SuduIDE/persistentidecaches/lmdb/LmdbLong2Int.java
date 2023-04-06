package com.github.SuduIDE.persistentidecaches.lmdb;

import java.nio.ByteBuffer;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;

public class LmdbLong2Int extends LmdbAbstractMap {

    public LmdbLong2Int(final Env<ByteBuffer> env, final String dbName) {
        super(env, env.openDbi(dbName, DbiFlags.MDB_CREATE));
    }

    public void put(final long key, final int value) {
        putImpl(allocateLong(key),
                allocateInt(value));
    }

    /**
     * @return value for key or -1
     */
    public int get(final long key) {
        final ByteBuffer res = getImpl(allocateLong(key));
        return res == null ? -1 : res.getInt();
    }
}
