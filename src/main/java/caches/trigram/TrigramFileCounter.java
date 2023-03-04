package caches.trigram;

import caches.records.Trigram;
import caches.records.TrigramFile;
import caches.utils.TriConsumer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TrigramFileCounter {
    public static final TrigramFileCounter EMPTY_COUNTER = new TrigramFileCounter();
    private final Map<TrigramFile, Integer> map;

    public TrigramFileCounter() {
        map = new HashMap<>();
    }

    public TrigramFileCounter(Map<TrigramFile, Integer> map) {
        this.map = map;
    }

    public void add(Trigram trigram, File file, int delta) {
        map.compute(new TrigramFile(trigram, file), (ignore, val) -> val == null ? delta : val + delta);
    }

    public void decrease(Trigram trigram, File file, int delta) {
        add(trigram, file, -delta);
    }

    public void add(File file, TrigramCounter counter) {
        counter.forEach((trigram, integer) -> add(trigram, file, integer));
    }

    public void decrease(File file, TrigramCounter counter) {
        counter.forEach((trigram, integer) -> decrease(trigram, file, integer));
    }

    public void forEach(TriConsumer<Trigram, File, Integer> consumer) {
        map.forEach(((trigramFile, integer) -> consumer.accept(trigramFile.trigram(), trigramFile.file(), integer)));
    }

    public Map<TrigramFile, Integer> getAsMap() {
        return map;
    }
}