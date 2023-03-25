package caches.trigram;

import caches.records.Trigram;
import caches.records.TrigramFile;
import caches.utils.Counter;
import caches.utils.TriConsumer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TrigramFileCounter extends Counter<TrigramFile> {
    public static final TrigramFileCounter EMPTY_COUNTER = new TrigramFileCounter();

    public TrigramFileCounter() {
    }

    public TrigramFileCounter(final Map<TrigramFile, Integer> counter) {
        super(counter);
    }

    public void add(Trigram trigram, File file, int delta) {
        add(new TrigramFile(trigram, file), delta);
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
        forEach(((trigramFile, integer) -> consumer.accept(trigramFile.trigram(), trigramFile.file(), integer)));
    }

    public int get(Trigram trigram, File file) {
        return get(new TrigramFile(trigram, file));
    }
}