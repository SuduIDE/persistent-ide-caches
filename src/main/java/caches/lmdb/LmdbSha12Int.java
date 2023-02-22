package caches.lmdb;

import org.lmdbjava.Env;

import java.nio.ByteBuffer;
import java.util.HexFormat;

import static java.nio.ByteBuffer.allocateDirect;

public class LmdbSha12Int extends LmdbAbstractMap {
    public LmdbSha12Int(Env<ByteBuffer> env, String dbName) {
        super(env, dbName);
    }

    public void put(String hash, int value) {
        byte[] bytes = HexFormat.of().parseHex(hash);
        putImpl(allocateDirect(bytes.length).put(bytes).flip(),
                allocateInt(value));
    }

    public int get(String hash) {
        byte[] bytes = HexFormat.of().parseHex(hash);
        ByteBuffer res = getImpl(allocateDirect(bytes.length).put(bytes).flip());
        return res == null ? -1 : res.getInt();
    }
}
