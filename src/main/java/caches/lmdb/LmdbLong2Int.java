package caches.lmdb;

import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;

import java.nio.ByteBuffer;

public class LmdbLong2Int extends LmdbAbstractMap {
    public LmdbLong2Int(Env<ByteBuffer> env, String dbName) {
        super(env, env.openDbi(dbName, DbiFlags.MDB_CREATE));
    }

    public void put(long key, int value) {
        putImpl(allocateLong(key),
                allocateInt(value));
    }

    public int get(long key) {
        ByteBuffer res = getImpl(allocateLong(key));
        return res == null ? -1 : res.getInt();
    }
}
