package caches.trigram;

import caches.GlobalVariables;
import caches.lmdb.LmdbLong2IntCounter;
import caches.records.LongInt;
import caches.records.Trigram;

import java.io.File;
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
        return ((long) trigram.toInt()) << Integer.SIZE + GlobalVariables.reverseFilesInProject.get(file);
    }

    public void add(TrigramFileCounter counter) {
        db.addAll(counterToList(counter));
    }


    public void decrease(TrigramFileCounter counter) {
        db.decreaseAll(counterToList(counter));
    }

    private List<LongInt> counterToList(TrigramFileCounter counter) {
        List<LongInt> list = new ArrayList<>();
        counter.forEach((file, trigram, integer) -> list.add(new LongInt(getKey(trigram, file), integer)));
        return list;
    }
}
