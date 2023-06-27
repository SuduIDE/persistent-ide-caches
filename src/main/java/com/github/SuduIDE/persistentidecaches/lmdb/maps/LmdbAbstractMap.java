package com.github.SuduIDE.persistentidecaches.lmdb.maps;

import java.io.Closeable;
import java.nio.ByteBuffer;
import org.lmdbjava.Dbi;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

public abstract class LmdbAbstractMap implements Closeable, LmdbMap {

    protected final Env<ByteBuffer> env;
    protected final Dbi<ByteBuffer> db;

    public LmdbAbstractMap(final Env<ByteBuffer> env, final String dbName) {
        this.env = env;
        db = env.openDbi(dbName, DbiFlags.MDB_CREATE);
    }

    protected LmdbAbstractMap(final Env<ByteBuffer> env, final Dbi<ByteBuffer> db) {
        this.env = env;
        this.db = db;
    }

    public static ByteBuffer allocateInt(final int it) {
        return ByteBuffer.allocateDirect(Integer.BYTES).putInt(it).flip();
    }

    public static ByteBuffer allocateLong(final long it) {
        return ByteBuffer.allocateDirect(Long.BYTES).putLong(it).flip();
    }

    public static ByteBuffer allocateString(final String it) {
        final var bytes = it.getBytes();
        return ByteBuffer.allocateDirect(bytes.length).put(bytes).flip();
    }

    protected void putImpl(final ByteBuffer key, final ByteBuffer value) {
        db.put(key, value);
    }

    protected ByteBuffer getImpl(final ByteBuffer key) {
        try (final Txn<ByteBuffer> txn = env.txnRead()) {
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
