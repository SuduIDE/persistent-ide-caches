package com.github.SuduIDE.persistentidecaches.lmdb.maps;

import java.nio.ByteBuffer;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;

public class LmdbInt2Int extends LmdbAbstractMap {

    public LmdbInt2Int(final Env<ByteBuffer> env, final String dbName) {
        super(env, env.openDbi(dbName, DbiFlags.MDB_CREATE, DbiFlags.MDB_INTEGERKEY));
    }

    public void put(final int key, final int value) {
        putImpl(allocateInt(key),
                allocateInt(value));
    }


    /**
     * @return value for key or -1
     */
    public int get(final int key) {
        final ByteBuffer res = getImpl(allocateInt(key));
        return res == null ? -1 : res.getInt();
    }
}
