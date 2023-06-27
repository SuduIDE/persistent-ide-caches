package com.github.SuduIDE.persistentidecaches.trigram;

import com.github.SuduIDE.persistentidecaches.records.Trigram;
import com.github.SuduIDE.persistentidecaches.records.TrigramFile;
import com.github.SuduIDE.persistentidecaches.utils.Counter;
import com.github.SuduIDE.persistentidecaches.utils.TriConsumer;
import java.nio.file.Path;
import java.util.Map;

public class TrigramFileCounter extends Counter<TrigramFile> {

    public static final TrigramFileCounter EMPTY_COUNTER = new TrigramFileCounter();

    public TrigramFileCounter() {
    }

    public TrigramFileCounter(final Map<TrigramFile, Integer> counter) {
        super(counter);
    }

    public void add(final Trigram trigram, final Path file, final int delta) {
        add(new TrigramFile(trigram, file), delta);
    }

    public void decrease(final Trigram trigram, final Path file, final int delta) {
        add(trigram, file, -delta);
    }

    public void add(final Path file, final TrigramCounter counter) {
        counter.forEach((trigram, integer) -> add(trigram, file, integer));
    }

    public void decrease(final Path file, final TrigramCounter counter) {
        counter.forEach((trigram, integer) -> decrease(trigram, file, integer));
    }

    public void forEach(final TriConsumer<Trigram, Path, Integer> consumer) {
        forEach(((trigramFile, integer) -> consumer.accept(trigramFile.trigram(), trigramFile.file(), integer)));
    }

    public int get(final Trigram trigram, final Path file) {
        return get(new TrigramFile(trigram, file));
    }
}