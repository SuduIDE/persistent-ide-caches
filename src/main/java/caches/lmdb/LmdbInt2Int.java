package caches.lmdb;

import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;

import java.nio.ByteBuffer;

public class LmdbInt2Int extends LmdbAbstractMap {
    public LmdbInt2Int(Env<ByteBuffer> env, String dbName) {
        super(env, env.openDbi(dbName, DbiFlags.MDB_CREATE, DbiFlags.MDB_INTEGERKEY));
    }

    public void put(int key, int value) {
        putImpl(allocateInt(key),
                allocateInt(value));
    }


    /**
     * @return value for key or -1
     */
    public int get(int key) {
        ByteBuffer res = getImpl(allocateInt(key));
        return res == null ? -1 : res.getInt();
    }
}
