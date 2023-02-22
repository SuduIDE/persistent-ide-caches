package caches.lmdb;

import org.lmdbjava.Env;

import java.nio.ByteBuffer;

public class LmdbString2Int extends LmdbAbstractMap {
    public LmdbString2Int(Env<ByteBuffer> env, String dbName) {
        super(env, dbName);
    }

    public void put(String key, int value) {
        putImpl(allocateString(key),
                allocateInt(value));
    }

    public int get(String key) {
        ByteBuffer res = getImpl(allocateString(key));
        return res == null ? -1 : res.getInt();
    }
}
