package com.github.SuduIDE.persistentidecaches.lmdb.maps;

import static java.nio.ByteBuffer.allocateDirect;

import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.function.BiConsumer;
import org.eclipse.jgit.util.Hex;
import org.lmdbjava.CursorIterable;
import org.lmdbjava.Env;
import org.lmdbjava.KeyRange;

public class LmdbSha12Int extends LmdbAbstractMap {

    public LmdbSha12Int(final Env<ByteBuffer> env, final String dbName) {
        super(env, dbName);
    }

    public void put(final String hash, final int value) {
        final byte[] bytes = HexFormat.of().parseHex(hash);
        putImpl(allocateDirect(bytes.length).put(bytes).flip(),
                allocateInt(value));
    }

    /**
     * @return value for key or -1
     */
    public int get(final String hash) {
        final byte[] bytes = HexFormat.of().parseHex(hash);
        final ByteBuffer res = getImpl(allocateDirect(bytes.length).put(bytes).flip());
        return res == null ? -1 : res.getInt();
    }

    public void forEach(final BiConsumer<String, Integer> consumer) {
        try (final var txn = env.txnRead()) {
            try (final CursorIterable<ByteBuffer> ci = db.iterate(txn, KeyRange.all())) {
                for (final CursorIterable.KeyVal<ByteBuffer> kv : ci) {
                    final var bytes = new byte[kv.key().capacity()];
                    kv.key().get(bytes);
                    consumer.accept(Hex.toHexString(bytes), kv.val().getInt());
                }
            }
        }
    }
}
