package caches.trigram;

import caches.GlobalVariables;
import caches.lmdb.LmdbLong2IntCounter;
import caches.records.LongInt;
import caches.records.Trigram;
import org.lmdbjava.Txn;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class TrigramFileCounterLmdb {
    private final LmdbLong2IntCounter db;

    public TrigramFileCounterLmdb() {
        db = new LmdbLong2IntCounter(GlobalVariables.env, "trigram_file_counter");
    }

    public int get(Trigram trigram, File file) {
        return db.get(getKey(trigram, file));
    }

    private long getKey(Trigram trigram, File file) {
        return getKey(trigram.trigram(), GlobalVariables.reverseFilesInProject.get(file));
    }
    private long getKey(byte[] trigram, int file) {
        return Trigram.toLong(trigram) << Integer.SIZE + file;
    }

    public void add(TrigramFileCounter counter) {
        db.addAll(counterToList(counter));
    }


    public void decrease(TrigramFileCounter counter) {
        db.decreaseAll(counterToList(counter));
    }

    private List<LongInt> counterToList(TrigramFileCounter counter) {
        List<LongInt> list = new ArrayList<>();
        counter.forEach((trigram, file, integer) -> list.add(new LongInt(getKey(trigram, file), integer)));
        return list;
    }

    public void addIt(Txn<ByteBuffer> txn, byte[] bytes, Integer file, int delta) {
        db.add(txn, getKey(bytes, file), delta);
    }

    public void decreaseIt(Txn<ByteBuffer> txn, byte[] bytes, Integer file, int delta) {
        db.decrease(txn, getKey(bytes, file), delta);
    }
}
