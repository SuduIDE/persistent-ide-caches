package caches.lmdb;

import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;

import java.nio.ByteBuffer;

public class LmdbInt2Long extends LmdbAbstractMap {
    public LmdbInt2Long(Env<ByteBuffer> env, String dbName) {
        super(env, env.openDbi(dbName, DbiFlags.MDB_CREATE, DbiFlags.MDB_INTEGERKEY));
    }

    public void put(int key, long value) {
        putImpl(allocateInt(key),
                allocateLong(value));
    }

    /**
     * @return value for key or -1
     */
    public long get(int key) {
        ByteBuffer res = getImpl(allocateInt(key));
        return res == null ? -1 : res.getLong();
    }
}
