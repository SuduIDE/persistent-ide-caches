package caches.lmdb;

import org.lmdbjava.Dbi;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

import java.io.Closeable;
import java.nio.ByteBuffer;

public abstract class LmdbAbstractMap implements Closeable {

    protected final Env<ByteBuffer> env;
    protected final Dbi<ByteBuffer> db;

    public LmdbAbstractMap(Env<ByteBuffer> env, String dbName) {
        this.env = env;
        db = env.openDbi(dbName, DbiFlags.MDB_CREATE);
    }

    protected LmdbAbstractMap(Env<ByteBuffer> env, Dbi<ByteBuffer> db) {
        this.env = env;
        this.db = db;
    }

    public static ByteBuffer allocateInt(int it) {
        return ByteBuffer.allocateDirect(Integer.BYTES).putInt(it).flip();
    }

    public static ByteBuffer allocateLong(long it) {
        return ByteBuffer.allocateDirect(Long.BYTES).putLong(it).flip();
    }

    public static ByteBuffer allocateString(String it) {
        var bytes = it.getBytes();
        return ByteBuffer.allocateDirect(bytes.length).put(bytes).flip();
    }

    protected void putImpl(ByteBuffer key, ByteBuffer value) {
        db.put(key, value);
    }

    protected ByteBuffer getImpl(ByteBuffer key) {
        try (Txn<ByteBuffer> txn = env.txnRead()) {
            final ByteBuffer found = db.get(txn, key);
            if (found == null) {
                return null;
//                throw new RuntimeException(key + " key not found in DB " + new String(db.getName()));
            }
            return txn.val();
        }
    }

    @Override
    public void close() {
        db.close();
    }
}
