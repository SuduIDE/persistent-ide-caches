package caches.trigram;

import caches.records.Trigram;
import caches.records.TrigramFileInteger;
import caches.utils.Counter;
import caches.utils.DoubleCounter;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public class TrigramFileCounter extends DoubleCounter<File, Trigram> {
    public static final TrigramFileCounter EMPTY_COUNTER = new TrigramFileCounter(Collections.emptyMap());

    public TrigramFileCounter(Map<File, Counter<Trigram>> counter) {
        super(counter);
    }

    public TrigramFileCounter() {
    }

    public void add(TrigramFileInteger it) {
        add(it.file(), it.trigram(), it.value());
    }

    public void decrease(TrigramFileInteger it) {
        decrease(it.file(), it.trigram(), it.value());
    }

    public void forEach(Consumer<TrigramFileInteger> function) {
        super.forEach((file, trigram, integer) -> function.accept(new TrigramFileInteger(trigram, file, integer)));
    }

    public TrigramFileCounter copy() {
        return new TrigramFileCounter(super.copy().getAsMap());
    }
}
