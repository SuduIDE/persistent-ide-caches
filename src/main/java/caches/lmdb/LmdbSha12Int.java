package caches.lmdb;

import org.eclipse.jgit.util.Hex;
import org.lmdbjava.CursorIterable;
import org.lmdbjava.Env;
import org.lmdbjava.KeyRange;

import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.function.BiConsumer;

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

    /**
     * @return value for key or -1
     */
    public int get(String hash) {
        byte[] bytes = HexFormat.of().parseHex(hash);
        ByteBuffer res = getImpl(allocateDirect(bytes.length).put(bytes).flip());
        return res == null ? -1 : res.getInt();
    }

    public void forEach(BiConsumer<String, Integer> consumer) {
        try (var txn = env.txnRead()) {
            try (CursorIterable<ByteBuffer> ci = db.iterate(txn, KeyRange.all())) {
                for (final CursorIterable.KeyVal<ByteBuffer> kv : ci) {
                    var bytes = new byte[kv.key().capacity()];
                    kv.key().get(bytes);
                    consumer.accept(Hex.toHexString(bytes), kv.val().getInt());
                }
            }
        }
    }
}
