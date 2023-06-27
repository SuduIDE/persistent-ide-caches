package com.github.SuduIDE.persistentidecaches.lmdb.maps;

import java.nio.ByteBuffer;
import org.lmdbjava.Dbi;
import org.lmdbjava.Env;

public class LmdbInt2Bytes extends LmdbAbstractInt2Smth implements LmdbMap {

    public LmdbInt2Bytes(final Env<ByteBuffer> env, final String dbName) {
        super(env, dbName);
    }

    protected LmdbInt2Bytes(final Env<ByteBuffer> env, final Dbi<ByteBuffer> db) {
        super(env, db);
    }

    public void put(final int key, final byte[] value) {
        putImpl(getKey(key), ByteBuffer.allocateDirect(value.length).put(value).flip());
    }

    public byte[] get(final int key) {
        final var value = getImpl(getKey(key));
        if (value != null) {
            final var data = new byte[value.remaining()];
            value.get(data);
            return data;
        }
        return null;
    }
}
