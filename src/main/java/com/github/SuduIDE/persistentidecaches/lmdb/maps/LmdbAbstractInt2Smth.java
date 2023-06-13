package com.github.SuduIDE.persistentidecaches.lmdb.maps;

import java.nio.ByteBuffer;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;

public abstract class LmdbAbstractInt2Smth extends LmdbAbstractMap  {
    private final ByteBuffer keyBuffer;

    public LmdbAbstractInt2Smth(final Env<ByteBuffer> env, final String dbName) {
        super(env, dbName);
        keyBuffer = ByteBuffer.allocateDirect(Integer.BYTES);
    }

    protected LmdbAbstractInt2Smth(final Env<ByteBuffer> env, final Dbi<ByteBuffer> db) {
        super(env, db);
        keyBuffer = ByteBuffer.allocateDirect(Integer.BYTES);
    }

    protected ByteBuffer getKey(final int key) {
        return keyBuffer.putInt(key).flip();
    }
}
