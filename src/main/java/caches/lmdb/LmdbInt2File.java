package caches.lmdb;

import org.lmdbjava.Env;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.function.BiConsumer;

public class LmdbInt2File {
    private final LmdbInt2String db;

    public LmdbInt2File(Env<ByteBuffer> env, String dbName) {
        db = new LmdbInt2String(env, dbName);
    }

    public void put(int key, File value) {
        db.put(key, value.getPath());
    }

    public File get(int key) {
        return new File(db.get(key));
    }

    public void forEach(BiConsumer<Integer, File> consumer) {
        db.forEach((integer, s) -> consumer.accept(integer, new File(s)));
    }
}