package caches.lmdb;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import org.lmdbjava.Env;

public class LmdbInt2Path implements LmdbMap {

    private final LmdbInt2String db;

    public LmdbInt2Path(Env<ByteBuffer> env, String dbName) {
        db = new LmdbInt2String(env, dbName);
    }

    public void put(int key, Path value) {
        db.put(key, value.toString());
    }

    public Path get(int key) {
        var value = db.get(key);
        return value == null ? null : Path.of(db.get(key));
    }

    public void forEach(BiConsumer<Integer, Path> consumer) {
        db.forEach((integer, s) -> consumer.accept(integer, Path.of(s)));
    }
}
