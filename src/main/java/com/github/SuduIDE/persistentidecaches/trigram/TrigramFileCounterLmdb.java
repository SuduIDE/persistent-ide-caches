package com.github.SuduIDE.persistentidecaches.trigram;

import com.github.SuduIDE.persistentidecaches.lmdb.CountingCacheImpl;
import com.github.SuduIDE.persistentidecaches.lmdb.TrigramObjCounterLmdb;
import com.github.SuduIDE.persistentidecaches.records.ByteArrIntInt;
import com.github.SuduIDE.persistentidecaches.records.LongInt;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.lmdbjava.Env;
import org.lmdbjava.Txn;

public class TrigramFileCounterLmdb extends TrigramObjCounterLmdb<Path> {

    public TrigramFileCounterLmdb(final Env<ByteBuffer> env, final CountingCacheImpl<Path> pathCache) {
        super(pathCache, env, "trigram_file_counter");
    }
    public void add(final TrigramFileCounter counter) {
        db.addAll(counterToList(counter));
    }

    public void add(final Txn<ByteBuffer> txn, final List<ByteArrIntInt> counter) {
        db.addAll(txn, counter.stream()
                .map(it -> new LongInt(getKey(it.trigram(), it.num()), it.delta()))
                .toList());
    }

    public void decrease(final TrigramFileCounter counter) {
        db.decreaseAll(counterToList(counter));
    }

    private List<LongInt> counterToList(final TrigramFileCounter counter) {
        final List<LongInt> list = new ArrayList<>();
        counter.forEach((trigram, file, integer) -> list.add(new LongInt(getKey(trigram, file), integer)));
        return list;
    }
}
