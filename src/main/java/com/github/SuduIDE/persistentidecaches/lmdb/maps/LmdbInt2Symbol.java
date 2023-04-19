package com.github.SuduIDE.persistentidecaches.lmdb.maps;

import com.github.SuduIDE.persistentidecaches.symbols.Symbol;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import org.lmdbjava.CursorIterable;
import org.lmdbjava.DbiFlags;
import org.lmdbjava.Env;
import org.lmdbjava.KeyRange;
import org.lmdbjava.Txn;

public class LmdbInt2Symbol extends LmdbAbstractMap implements LmdbInt2Obj<Symbol> {

    public LmdbInt2Symbol(final Env<ByteBuffer> env, final String dbName) {
        super(env, env.openDbi(dbName, DbiFlags.MDB_CREATE, DbiFlags.MDB_INTEGERKEY));
    }

    @Override
    public void put(final int key, final Symbol value) {
        final var nameBytes = value.name().getBytes();
        putImpl(allocateInt(key),
                ByteBuffer.allocateDirect(nameBytes.length + Integer.BYTES)
                        .putInt(value.pathNum())
                        .put(nameBytes)
                        .flip());
    }

    @Override
    public Symbol get(final int key) {
        final ByteBuffer res = getImpl(allocateInt(key));
        if (res == null) {
            return null;
        }
        final var pathNum = res.getInt();
        return new Symbol(String.valueOf(StandardCharsets.UTF_8.decode(res.slice())), pathNum);
    }

    private Symbol decodeSymbol(final ByteBuffer byteBuffer) {
        final var pathNum = byteBuffer.getInt();
        return new Symbol(String.valueOf(StandardCharsets.UTF_8.decode(byteBuffer.slice())), pathNum);

    }

    @Override
    public void forEach(final BiConsumer<Integer, Symbol> consumer) {
        try (final Txn<ByteBuffer> txn = env.txnRead()) {
            try (final CursorIterable<ByteBuffer> ci = db.iterate(txn, KeyRange.all())) {
                for (final CursorIterable.KeyVal<ByteBuffer> kv : ci) {
                    consumer.accept(kv.key().getInt(), decodeSymbol(kv.val()));
                }
            }
        }
    }
}
