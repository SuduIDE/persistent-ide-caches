package com.github.SuduIDE.persistentidecaches.ccsearch;

import com.github.SuduIDE.persistentidecaches.lmdb.CountingCacheImpl;
import com.github.SuduIDE.persistentidecaches.lmdb.TrigramObjCounterLmdb;
import com.github.SuduIDE.persistentidecaches.records.ByteArrIntInt;
import com.github.SuduIDE.persistentidecaches.records.LongInt;
import com.github.SuduIDE.persistentidecaches.symbols.Symbol;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

public class TrigramSymbolCounterLmdb extends TrigramObjCounterLmdb<Symbol> {

    public TrigramSymbolCounterLmdb(final Env<ByteBuffer> env, final CountingCacheImpl<Symbol> symbolCache,
            final String dbNameSuffix) {
        super(symbolCache, env, "trigram_symbol_counter_" + dbNameSuffix);
    }

    public void add(final Map<TrigramSymbol, Integer> counter) {
        db.addAll(counterToList(counter));
    }

    public void add(final Txn<ByteBuffer> txn, final List<ByteArrIntInt> counter) {
        db.addAll(txn, counter.stream()
                .map(it -> new LongInt(getKey(it.trigram(), it.num()), it.delta()))
                .toList());
    }

    public void decrease(final Map<TrigramSymbol, Integer> counter) {
        db.decreaseAll(counterToList(counter));
    }

    private List<LongInt> counterToList(final Map<TrigramSymbol, Integer> counter) {
        final List<LongInt> list = new ArrayList<>();
        counter.forEach((trigramSymbol, integer) ->
                list.add(new LongInt(getKey(trigramSymbol.trigram(), trigramSymbol.word()), integer)));
        return list;
    }
}
