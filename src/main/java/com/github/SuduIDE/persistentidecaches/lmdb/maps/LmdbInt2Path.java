package com.github.SuduIDE.persistentidecaches.lmdb.maps;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import org.lmdbjava.Env;

public class LmdbInt2Path implements LmdbMap, LmdbInt2Obj<Path> {

    private final LmdbInt2String db;

    public LmdbInt2Path(final Env<ByteBuffer> env, final String dbName) {
        db = new LmdbInt2String(env, dbName);
    }

    public void put(final int key, final Path value) {
        db.put(key, value.toString());
    }

    public Path get(final int key) {
        final var value = db.get(key);
        return value == null ? null : Path.of(value);
    }

    public void forEach(final BiConsumer<Integer, Path> consumer) {
        db.forEach((integer, s) -> consumer.accept(integer, Path.of(s)));
    }
}
