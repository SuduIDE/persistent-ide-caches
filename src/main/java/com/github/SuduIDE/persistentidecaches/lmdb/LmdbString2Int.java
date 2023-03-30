package com.github.SuduIDE.persistentidecaches.lmdb;

import java.nio.ByteBuffer;
import org.lmdbjava.Env;

public class LmdbString2Int extends LmdbAbstractMap {
    public LmdbString2Int(Env<ByteBuffer> env, String dbName) {
        super(env, dbName);
    }

    public void put(String key, int value) {
        putImpl(allocateString(key),
                allocateInt(value));
    }
    /**
     * @return value for key or -1
     */
    public int get(String key) {
        ByteBuffer res = getImpl(allocateString(key));
        return res == null ? -1 : res.getInt();
    }
}
